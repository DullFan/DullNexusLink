package com.example.communication.contact

import android.content.Context
import android.provider.ContactsContract
import android.util.Log
import androidx.room.Dao
import com.example.communication.datastore.LastUpdateTimeManager.getContactTime
import com.example.communication.datastore.LastUpdateTimeManager.updateContactTime
import com.example.communication.room.MyRoomDatabase
import com.example.communication.room.entity.AddressEntity
import com.example.communication.room.entity.BaseInfoEntity
import com.example.communication.room.entity.ContactPersonEntity
import com.example.communication.room.entity.EmailEntity
import com.example.communication.room.entity.EventEntity
import com.example.communication.room.entity.IMEntity
import com.example.communication.room.entity.OrganizationsEntity
import com.example.communication.room.entity.PhoneEntity
import com.example.communication.room.entity.WebsiteEntity
import com.example.communication.utils.SDKConstants.INITIAL_LOAD_SIZE
import com.example.communication.utils.logTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.withContext

/**
 * 联系人管理器
 */
class ContactManager(private val context: Context) {
    private val contentResolver = context.contentResolver
    private val queryManager = ContactQueryManager(contentResolver)
    private val myRoomDatabase: MyRoomDatabase = MyRoomDatabase.getMyRoomDatabase()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _contacts = MutableStateFlow<MutableList<ContactPersonEntity>>(mutableListOf())
    val contacts: StateFlow<MutableList<ContactPersonEntity>> = _contacts.asStateFlow()

    /**
     * 启动监听
     */
    suspend fun startObserveContacts() {
        observeContacts().collect { logs ->
            updateData(logs)
        }
    }


    /**
     * 根据手机号码获取联系人ID
     */
    suspend fun getContactIdByPhone(phoneNumber: String): Long =
        queryManager.getContactIdByPhone(phoneNumber)

    /**
     * 获取所有联系人手机号和ID的映射
     */
    suspend fun getContactIdsWithPhoneNumbers(): Map<String, Long> =
        queryManager.getContactIdsWithPhoneNumbers()

    /**
     * 获取所有联系人ID
     */
    suspend fun queryAllContactIds(): List<Long> = queryManager.queryAllContactIds()

    /**
     * 存储初始联系人数据
     */
    suspend fun storeInitialContacts() =
        withContext(Dispatchers.IO) {
            logTime("初始化联系人数据-》") {
                val contacts = queryManager.queryContacts(maxCount = INITIAL_LOAD_SIZE)
                updateData(contacts.toMutableList())
                upsertContactPersonRoom(contacts.toMutableList())
                context.updateContactTime()
                loadRemainingContacts()
            }
        }


    /**
     * 使用时间戳更新联系人数据
     */
    private suspend fun updateContactPersonListByTimestamp() =
        withContext(Dispatchers.IO) {
            logTime("使用时间戳更新联系人数据-》") {
                val lastUpdateTime = context.getContactTime()
                val newContacts = queryManager.queryContacts(
                    selection = "${ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP} > ?",
                    selectionArgs = arrayOf(lastUpdateTime.toString())
                ).toMutableList()
                val mergeAndUpdateContacts = mergeAndUpdateContacts(newContacts)
                updateData(mergeAndUpdateContacts)
            }
        }

    /**
     * 加载剩余联系人数据
     */
    private suspend fun loadRemainingContacts() =
        withContext(Dispatchers.IO) {
            val currentContacts = getData()
            if (currentContacts.isEmpty()) {
                updateData(currentContacts)
                return@withContext
            }

            val maxContactId = currentContacts.maxOf { it.baseInfo.contactId }
            val remainingContacts = queryManager.queryContacts(
                selection = "${ContactsContract.Data.CONTACT_ID} > ?",
                selectionArgs = arrayOf(maxContactId.toString())
            )

            if (remainingContacts.isNotEmpty()) {
                upsertContactPersonRoom(remainingContacts.toMutableList())
                currentContacts.addAll(remainingContacts)
                updateData(currentContacts)
            }
        }

    /**
     * 监听联系人变化
     */
    private suspend fun observeContacts(): Flow<MutableList<ContactPersonEntity>> =
        withContext(Dispatchers.IO) {
            callbackFlow {
                val observer = ContactObserver(context, queryManager) { newContacts ->
                    val updatedContacts = mergeAndUpdateContacts(newContacts.toMutableList())
                    withContext(Dispatchers.Main) {
                        trySend(updatedContacts)
                    }
                }

                contentResolver.registerContentObserver(
                    ContactsContract.Contacts.CONTENT_URI, true, observer
                )

                awaitClose {
                    observer.release()
                    contentResolver.unregisterContentObserver(observer)
                }
            }.conflate()
        }

    /**
     * 合并和更新联系人数据
     */
    private suspend fun mergeAndUpdateContacts(
        newContacts: MutableList<ContactPersonEntity>
    ): MutableList<ContactPersonEntity> =
        withContext(Dispatchers.IO) {
            val currentContacts = getData()
            val currentContactsMap = currentContacts.associateBy { it.baseInfo.contactId }

            if (newContacts.isNotEmpty()) {
                val (updateContacts, addContacts) = newContacts.partition {
                    currentContactsMap.containsKey(it.baseInfo.contactId)
                }

                if (addContacts.isNotEmpty()) {
                    currentContacts.addAll(addContacts)
                }

                updateContacts.forEach { newContact ->
                    currentContactsMap[newContact.baseInfo.contactId]?.let { oldContact ->
                        val index = currentContacts.indexOf(oldContact)
                        if (index != -1) {
                            currentContacts[index] = newContact
                        }
                    }
                }

                upsertContactPersonRoom(newContacts)
            }

            val systemIds = queryManager.queryAllContactIds().toSet()
            val idsToDelete =
                currentContacts.map { it.baseInfo.contactId }.filter { !systemIds.contains(it) }

            if (idsToDelete.isNotEmpty()) {
                deleteContactPersonByContactIds(idsToDelete)
                currentContacts.removeAll { it.baseInfo.contactId in idsToDelete }
            }

            context.updateContactTime()
            currentContacts
        }


    /**
     * Room 数据库操作
     */
    private suspend fun upsertContactPersonRoom(
        list: MutableList<ContactPersonEntity>
    ) =
        withContext(Dispatchers.IO) {
            list.forEach {
                myRoomDatabase.addressDao().upsert(*it.addresses.toTypedArray())
                myRoomDatabase.emailDao().upsert(*it.emails.toTypedArray())
                myRoomDatabase.eventDao().upsert(*it.events.toTypedArray())
                myRoomDatabase.iMDao().upsert(*it.ims.toTypedArray())
                myRoomDatabase.organizationsDao().upsert(*it.organizations.toTypedArray())
                myRoomDatabase.phoneDao().upsert(*it.phones.toTypedArray())
                myRoomDatabase.websiteDao().upsert(*it.websites.toTypedArray())
                myRoomDatabase.contactPersonDao().upsert(it.baseInfo)
            }
        }

    suspend fun deleteContactPersonByContactIds(
        list: List<Long>
    ) =
        withContext(Dispatchers.IO) {
            myRoomDatabase.addressDao().deleteByContactIds(list)
            myRoomDatabase.emailDao().deleteByContactIds(list)
            myRoomDatabase.eventDao().deleteByContactIds(list)
            myRoomDatabase.iMDao().deleteByContactIds(list)
            myRoomDatabase.organizationsDao().deleteByContactIds(list)
            myRoomDatabase.phoneDao().deleteByContactIds(list)
            myRoomDatabase.websiteDao().deleteByContactIds(list)
            myRoomDatabase.contactPersonDao().deleteByContactIds(list)
        }

    suspend fun deleteContactPersonByContactId(contactId: Long) =
        withContext(Dispatchers.IO) {
            myRoomDatabase.addressDao().deleteContactById(contactId)
            myRoomDatabase.emailDao().deleteContactById(contactId)
            myRoomDatabase.eventDao().deleteContactById(contactId)
            myRoomDatabase.iMDao().deleteContactById(contactId)
            myRoomDatabase.organizationsDao().deleteContactById(contactId)
            myRoomDatabase.phoneDao().deleteContactById(contactId)
            myRoomDatabase.websiteDao().deleteContactById(contactId)
            myRoomDatabase.contactPersonDao().deleteContactById(contactId)
        }

    /**
     * 添加async，看DS的代码
     * TODO 待优化
     */
    suspend fun loadAllContactPersonFromRoom() = withContext(Dispatchers.IO) {
        logTime("从Room中获取联系人-》") {
            // 使用辅助函数加载所有数据
            val contacts =
                myRoomDatabase.contactPersonDao().findInitialLoadData(INITIAL_LOAD_SIZE)
            val addresses = myRoomDatabase.addressDao().findInitialLoadData(INITIAL_LOAD_SIZE)
            val emails = myRoomDatabase.emailDao().findInitialLoadData(INITIAL_LOAD_SIZE)
            val events = myRoomDatabase.eventDao().findInitialLoadData(INITIAL_LOAD_SIZE)
            val ims = myRoomDatabase.iMDao().findInitialLoadData(INITIAL_LOAD_SIZE)
            val organizations =
                myRoomDatabase.organizationsDao().findInitialLoadData(INITIAL_LOAD_SIZE)
            val phones = myRoomDatabase.phoneDao().findInitialLoadData(INITIAL_LOAD_SIZE)
            val websites = myRoomDatabase.websiteDao().findInitialLoadData(INITIAL_LOAD_SIZE)

            // 构建联系人数据
            val list = buildContactPersonList(
                contacts, addresses, emails, events, ims, organizations, phones, websites
            )

            updateData(list)

            // 获取剩余的数据
            val contactsAll = myRoomDatabase.contactPersonDao()
                .findByContactIdLessThan(contacts.last().contactId)
            val addressesAll =
                myRoomDatabase.addressDao().findByContactIdLessThan(contacts.last().contactId)
            val emailsAll =
                myRoomDatabase.emailDao().findByContactIdLessThan(contacts.last().contactId)
            val eventsAll =
                myRoomDatabase.eventDao().findByContactIdLessThan(contacts.last().contactId)
            val imsAll =
                myRoomDatabase.iMDao().findByContactIdLessThan(contacts.last().contactId)
            val organizationsAll = myRoomDatabase.organizationsDao()
                .findByContactIdLessThan(contacts.last().contactId)
            val phonesAll =
                myRoomDatabase.phoneDao().findByContactIdLessThan(contacts.last().contactId)
            val websitesAll =
                myRoomDatabase.websiteDao().findByContactIdLessThan(contacts.last().contactId)

            val listAll = buildContactPersonList(
                contactsAll,
                addressesAll,
                emailsAll,
                eventsAll,
                imsAll,
                organizationsAll,
                phonesAll,
                websitesAll
            )

            listAll.addAll(list)
            updateData(listAll)

            updateContactPersonListByTimestamp()
        }
    }


    /**
     * 构建联系人信息列表
     */
    private fun buildContactPersonList(
        contacts: List<BaseInfoEntity>,
        addresses: List<AddressEntity>,
        emails: List<EmailEntity>,
        events: List<EventEntity>,
        ims: List<IMEntity>,
        organizations: List<OrganizationsEntity>,
        phones: List<PhoneEntity>,
        websites: List<WebsiteEntity>
    ): MutableList<ContactPersonEntity> {
        return contacts.map { contact ->
            ContactPersonEntity(
                baseInfo = contact,
                addresses = addresses.filter { it.contactId == contact.contactId }.toMutableList(),
                emails = emails.filter { it.contactId == contact.contactId }.toMutableList(),
                events = events.filter { it.contactId == contact.contactId }.toMutableList(),
                ims = ims.filter { it.contactId == contact.contactId }.toMutableList(),
                organizations = organizations.filter { it.contactId == contact.contactId }
                    .toMutableList(),
                phones = phones.filter { it.contactId == contact.contactId }.toMutableList(),
                websites = websites.filter { it.contactId == contact.contactId }.toMutableList()
            )
        }.toMutableList()
    }

    /**
     * 工具方法
     */
    private fun nameSort(list: MutableList<ContactPersonEntity>) {
        list.sortBy { it.baseInfo.disPlayName }
    }

    fun release() {
        scope.cancel()
    }

    private fun updateData(list: MutableList<ContactPersonEntity>) {
        nameSort(list)
        _contacts.value = list
    }

    private fun getData() = _contacts.value.toMutableList()

    companion object {
        @Volatile
        private var instance: ContactManager? = null

        fun getInstance(context: Context): ContactManager {
            return instance ?: synchronized(this) {
                instance ?: ContactManager(context.applicationContext).also { instance = it }
            }
        }
    }
}