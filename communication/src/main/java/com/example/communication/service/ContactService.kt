package com.example.communication.service

import android.app.Application
import android.provider.ContactsContract
import android.util.Log
import com.example.communication.contact.observeContacts
import com.example.communication.contact.queryAllContactId
import com.example.communication.contact.queryContacts
import com.example.communication.contact.queryUpdatedContacts
import com.example.communication.datastore.LastUpdateTimeManager.getContactTime
import com.example.communication.datastore.LastUpdateTimeManager.updateContactTime
import com.example.communication.room.MyRoomDatabase
import com.example.communication.room.entity.ContactPersonEntity
import com.example.communication.utils.EntityDataProcessing.nameSort
import com.example.communication.utils.SDKConstants.FIRST_COUNT
import com.example.communication.utils.logTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext

object ContactService {
    private val myRoomDatabase: MyRoomDatabase = MyRoomDatabase.getMyRoomDatabase()
    private val _currentContacts =
        MutableStateFlow<MutableList<ContactPersonEntity>>(mutableListOf())

    /**
     * 存储初始联系人数据（限制数量）
     */
    suspend fun Application.storeInitialContacts(): MutableList<ContactPersonEntity> =
        logTime("第一次进入App获取${FIRST_COUNT}条联系人") {
            val details = contentResolver.queryContacts(maxCount = FIRST_COUNT)
            upsertContactPersonRoom(details)
            updateContactTime()
            _currentContacts.value = details
            nameSort(details)
            details
        }

    /**
     * 加载剩余联系人数据
     */
    suspend fun Application.loadRemainingContacts(): MutableList<ContactPersonEntity> =
        logTime("加载剩余联系人数据") {
            val currentContacts = _currentContacts.value
            if (currentContacts.isEmpty()) return@logTime currentContacts

            // 使用当前已加载联系人的最大ID作为起始点
            val maxContactId = currentContacts.maxOf { it.baseInfo.contactId }
            val selection = "${ContactsContract.Data.CONTACT_ID} > ?"
            val selectionArgs = arrayOf(maxContactId.toString())

            val remainingContacts = contentResolver.queryContacts(
                selection = selection,
                selectionArgs = selectionArgs
            )
            if (remainingContacts.isNotEmpty()) {
                upsertContactPersonRoom(remainingContacts)
                currentContacts.addAll(remainingContacts)
                nameSort(currentContacts)
                _currentContacts.value = currentContacts
            }

            currentContacts
        }

    /**
     * 监听联系人变化
     */
    suspend fun Application.observeContacts(
        onContactsUpdated: suspend (MutableList<ContactPersonEntity>) -> Unit
    ) {
        contentResolver.observeContacts(this).collect { newContacts ->
            logTime("处理联系人变化") {
                mergeAndUpdateContacts(newContacts)
                onContactsUpdated(_currentContacts.value)
            }
        }
    }

    private suspend fun Application.mergeAndUpdateContacts(
        newContacts: MutableList<ContactPersonEntity>
    ): MutableList<ContactPersonEntity> = withContext(Dispatchers.IO) {
        logTime("合并联系人数据") {
            val currentContacts = _currentContacts.value.toMutableList()
            val currentContactsMap = currentContacts.associateBy {
                it.baseInfo.contactId
            }

            if (newContacts.isNotEmpty()) {
                val (updateContacts, addContacts) = newContacts.partition {
                    currentContactsMap.containsKey(it.baseInfo.contactId)
                }

                // 添加新数据
                if (addContacts.isNotEmpty()) {
                    currentContacts.addAll(addContacts)
                }

                // 修改数据
                updateContacts.forEach { newContact ->
                    currentContactsMap[newContact.baseInfo.contactId]?.let { oldContact ->
                        val index = currentContacts.indexOf(oldContact)
                        if (index != -1) {
                            currentContacts[index] = newContact
                        }
                    }
                }

                upsertContactPersonRoom(newContacts)
                nameSort(currentContacts)
            }

            val queryAllContactIds = contentResolver.queryAllContactId()

            // 找出需要删除的联系人
            val idsToDelete = currentContacts
                .map { it.baseInfo.contactId }
                .filter { !queryAllContactIds.contains(it) }

            if (idsToDelete.isNotEmpty()) {
                deleteContactPersonByContactIds(idsToDelete)
                currentContacts.removeAll { it.baseInfo.contactId in idsToDelete }
            }

            updateContactTime()
            withContext(Dispatchers.Main) {
                _currentContacts.value = currentContacts
            }

            currentContacts
        }
    }

    /**
     * 使用时间戳更新联系人数据
     */
    suspend fun Application.updateContactPersonListByTimestamp(): MutableList<ContactPersonEntity> =
        logTime("根据时间戳更新联系人") {
            val newContacts =
                contentResolver.queryUpdatedContacts(getContactTime())
            mergeAndUpdateContacts(newContacts)
            _currentContacts.value
        }

    private suspend fun upsertContactPersonRoom(
        list: MutableList<ContactPersonEntity>
    ) = logTime("更新联系人到Room") {
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
    ) = logTime("根据ContactId批量删除") {
        myRoomDatabase.addressDao().deleteByContactIds(list)
        myRoomDatabase.emailDao().deleteByContactIds(list)
        myRoomDatabase.eventDao().deleteByContactIds(list)
        myRoomDatabase.iMDao().deleteByContactIds(list)
        myRoomDatabase.organizationsDao().deleteByContactIds(list)
        myRoomDatabase.phoneDao().deleteByContactIds(list)
        myRoomDatabase.websiteDao().deleteByContactIds(list)
        myRoomDatabase.contactPersonDao().deleteByContactIds(list)
    }

    suspend fun deleteContactPersonByContactId(
        contactId: Long
    ) = logTime("根据ContactId从Room删除联系人") {
        myRoomDatabase.addressDao().deleteContactById(contactId)
        myRoomDatabase.emailDao().deleteContactById(contactId)
        myRoomDatabase.eventDao().deleteContactById(contactId)
        myRoomDatabase.iMDao().deleteContactById(contactId)
        myRoomDatabase.organizationsDao().deleteContactById(contactId)
        myRoomDatabase.phoneDao().deleteContactById(contactId)
        myRoomDatabase.websiteDao().deleteContactById(contactId)
        myRoomDatabase.contactPersonDao().deleteContactById(contactId)
    }

    fun loadAllContactPersonFromRoom(): MutableList<ContactPersonEntity> =
        logTime("从Room加载所有联系人") {
            val allContacts = myRoomDatabase.contactPersonDao().findAll()
            val allAddresses = myRoomDatabase.addressDao().findAll()
            val allEmails = myRoomDatabase.emailDao().findAll()
            val allEvents = myRoomDatabase.eventDao().findAll()
            val allIMs = myRoomDatabase.iMDao().findAll()
            val allOrganizations = myRoomDatabase.organizationsDao().findAll()
            val allPhones = myRoomDatabase.phoneDao().findAll()
            val allWebsites = myRoomDatabase.websiteDao().findAll()

            val list = allContacts.map { contact ->
                ContactPersonEntity(
                    baseInfo = contact,
                    addresses = allAddresses.filter { it.contactId == contact.contactId }
                        .toMutableList(),
                    emails = allEmails.filter { it.contactId == contact.contactId }.toMutableList(),
                    events = allEvents.filter { it.contactId == contact.contactId }.toMutableList(),
                    ims = allIMs.filter { it.contactId == contact.contactId }.toMutableList(),
                    organizations = allOrganizations.filter { it.contactId == contact.contactId }
                        .toMutableList(),
                    phones = allPhones.filter { it.contactId == contact.contactId }.toMutableList(),
                    websites = allWebsites.filter { it.contactId == contact.contactId }
                        .toMutableList()
                )
            }.toMutableList()
            _currentContacts.value = list
            nameSort(list)
            list
        }
}