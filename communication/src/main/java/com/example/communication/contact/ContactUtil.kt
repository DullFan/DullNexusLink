package com.example.communication.contact

import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Event
import android.provider.ContactsContract.CommonDataKinds.Im
import android.provider.ContactsContract.CommonDataKinds.Nickname
import android.provider.ContactsContract.CommonDataKinds.Note
import android.provider.ContactsContract.CommonDataKinds.Organization
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.Data
import com.example.communication.calllog.CallLogHelper.getCarrier
import com.example.communication.calllog.CallLogHelper.getGen
import com.example.communication.contact.ContactUtilHelper.getIntValue
import com.example.communication.contact.ContactUtilHelper.getLongValue
import com.example.communication.contact.ContactUtilHelper.getStringValue
import com.example.communication.datastore.LastUpdateTimeManager.getContactTime
import com.example.communication.room.entity.AddressEntity
import com.example.communication.room.entity.BaseInfoEntity
import com.example.communication.room.entity.CallLogEntity
import com.example.communication.room.entity.ContactPersonEntity
import com.example.communication.room.entity.EmailEntity
import com.example.communication.room.entity.EventEntity
import com.example.communication.room.entity.IMEntity
import com.example.communication.room.entity.OrganizationsEntity
import com.example.communication.room.entity.PhoneEntity
import com.example.communication.room.entity.WebsiteEntity
import com.example.communication.utils.logTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 实时监听联系人变化
 */
internal fun ContentResolver.observeContacts(
    context: Context,
): Flow<MutableList<ContactPersonEntity>> = callbackFlow {
    val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            CoroutineScope(Dispatchers.IO).launch {
                val currentLastUpdateTime = context.getContactTime()
                val contacts = queryUpdatedContacts(currentLastUpdateTime)
                withContext(Dispatchers.Main) {
                    trySend(contacts)
                }
            }
        }
    }
    registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, observer)
    awaitClose { unregisterContentObserver(observer) }
}.conflate()


/**
 * 查询所有联系人ID
 */
internal fun ContentResolver.queryAllContactId(): MutableList<Long> {
    val contactIdList = mutableListOf<Long>()

    val cursor = query(
        ContactsContract.Contacts.CONTENT_URI,
        arrayOf(ContactsContract.Contacts._ID),
        null,
        null,
        null
    )

    if (cursor != null && cursor.count > 0) {
        while (cursor.moveToNext()) {
            contactIdList.add(cursor.getLongValue(ContactsContract.Contacts._ID))
        }
        cursor.close()
    }

    return contactIdList
}

/**
 * 获取最后更新时间后的联系人
 */
internal fun ContentResolver.queryUpdatedContacts(lastUpdateTime: Long): MutableList<ContactPersonEntity> {
    // 查询条件：仅获取最近更新的联系人
    val selection = "${ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP} > ?"
    val selectionArgs = arrayOf(lastUpdateTime.toString())

    val queryContacts = queryContacts(
        selection,
        selectionArgs,
        ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC"
    )
    return queryContacts
}


/**
 * 查询联系人
 */
internal fun ContentResolver.queryContacts(
    selection: String? = null,
    selectionArgs: Array<String>? = null,
    sortOrder: String? = "${Data.CONTACT_ID} ASC",
    maxCount: Int? = null
): MutableList<ContactPersonEntity> {
    // 这里使用Map替换List是因为一个联系人可能有多个电话
    val contactDetailsList = mutableListOf<ContactPersonEntity>()
    val contactsMap = mutableMapOf<Long, ContactPersonEntity>()

    // 构建查询
    val projection = arrayOf(
        Data.CONTACT_ID,                   // 联系人ID
        Data.MIMETYPE,                     // 数据类型
        ContactsContract.Contacts.DISPLAY_NAME,    // 显示名称
        ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP,  // 更新时间
        Data.DATA1,                        // 通用数据字段
        Data.DATA2,                        // 类型字段
        Data.DATA3,                        // 标签字段
        Organization.COMPANY,              // 公司
        Organization.TITLE,                // 职位
        Phone.NUMBER,                      // 电话号码
        Phone.TYPE,                        // 电话类型
        Phone.LABEL,                       // 电话标签
        ContactsContract.CommonDataKinds.Email.ADDRESS,  // 邮箱地址
        ContactsContract.CommonDataKinds.Email.TYPE,     // 邮箱类型
        ContactsContract.CommonDataKinds.Email.LABEL,    // 邮箱标签
        Im.PROTOCOL,                       // IM协议
        Im.CUSTOM_PROTOCOL,                // 自定义IM协议
        Im.DATA,                          // IM账号
        Note.NOTE,                         // 备注
        Nickname.NAME,                     // 昵称
        Event.START_DATE,                  // 事件日期
        Event.TYPE,                        // 事件类型
        ContactsContract.CommonDataKinds.Website.URL,    // 网站URL
        ContactsContract.CommonDataKinds.Website.TYPE,   // 网站类型
        ContactsContract.CommonDataKinds.StructuredPostal.STREET,    // 街道
        ContactsContract.CommonDataKinds.StructuredPostal.CITY,      // 城市
        ContactsContract.CommonDataKinds.StructuredPostal.REGION,    // 地区
        ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE,  // 邮编
        ContactsContract.CommonDataKinds.StructuredPostal.TYPE,      // 地址类型
        ContactsContract.CommonDataKinds.StructuredPostal.LABEL,     // 地址标签
        ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS  // 完整地址
    )


    val cursor = query(
        Data.CONTENT_URI, projection, selection, selectionArgs, sortOrder
    )

    cursor?.use {
        while (it.moveToNext()) {
            // 安全获取列索引和值
            val contactId = it.getLongValue(Data.CONTACT_ID)

            // 一个联系人有多条数据比如电话、地址、邮箱。
            // !contactsMap.containsKey(contactId)作用：确保已存在联系人的数据能够完整处理，防止查询联系人其余信息时被break
            if (maxCount != null &&
                contactsMap.size >= maxCount &&
                !contactsMap.containsKey(contactId)
            ) {
                break
            }
            // 获取或创建联系人实体
            val contactPerson = contactsMap.getOrPut(contactId) {
                ContactPersonEntity().apply {
                    baseInfo = BaseInfoEntity().apply {
                        this.contactId = contactId
                        this.disPlayName =
                            it.getStringValue(ContactsContract.Contacts.DISPLAY_NAME)
                        this.updateTime =
                            it.getLongValue(ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP)
                    }
                }
            }

            // 根据MIMETYPE处理不同类型的数据
            when (it.getStringValue(Data.MIMETYPE)) {
                Phone.CONTENT_ITEM_TYPE -> {
                    val phoneEntity = PhoneEntity().apply {
                        this.contactId = contactId
                        phoneNumber = it.getStringValue(Phone.NUMBER)
                        val type = it.getIntValue(Phone.TYPE)
                        phoneType = when (type) {
                            Phone.TYPE_HOME -> "住宅"
                            Phone.TYPE_MOBILE -> "手机"
                            Phone.TYPE_WORK -> "单位"
                            Phone.TYPE_FAX_WORK -> "单位传真"
                            Phone.TYPE_FAX_HOME -> "住宅传真"
                            Phone.TYPE_PAGER -> "寻呼机"
                            Phone.TYPE_CALLBACK -> "回拨号码"
                            Phone.TYPE_CAR -> "车载电话"
                            Phone.TYPE_COMPANY_MAIN -> "公司总机"
                            Phone.TYPE_ISDN -> "ISDN"
                            Phone.TYPE_MAIN -> "总机"
                            Phone.TYPE_OTHER -> "其他"
                            Phone.TYPE_RADIO -> "无线装置"
                            Phone.TYPE_TELEX -> "电报"
                            Phone.TYPE_TTY_TDD -> "TTY_TDD"
                            Phone.TYPE_WORK_MOBILE -> "单位手机"
                            Phone.TYPE_WORK_PAGER -> "单位寻呼机"
                            Phone.TYPE_ASSISTANT -> "助理"
                            Phone.TYPE_MMS -> "彩信"
                            Phone.TYPE_CUSTOM -> it.getStringValue(Phone.LABEL, "自定义")

                            else -> "未知"
                        }
                    }
                    contactPerson.phones.add(phoneEntity)
                }

                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE -> {
                    val emailEntity = EmailEntity().apply {
                        this.contactId = contactId
                        emailData =
                            it.getStringValue(ContactsContract.CommonDataKinds.Email.ADDRESS)
                        val type = it.getIntValue(ContactsContract.CommonDataKinds.Email.TYPE)
                        emailType = when (type) {
                            ContactsContract.CommonDataKinds.Email.TYPE_HOME -> "个人"
                            ContactsContract.CommonDataKinds.Email.TYPE_WORK -> "单位"
                            ContactsContract.CommonDataKinds.Email.TYPE_OTHER -> "其他"
                            ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM -> {
                                it.getStringValue(
                                    ContactsContract.CommonDataKinds.Email.LABEL,
                                    "自定义"
                                )
                            }

                            else -> "未知"
                        }
                    }
                    contactPerson.emails.add(emailEntity)
                }

                Im.CONTENT_ITEM_TYPE -> {
                    val imEntity = IMEntity().apply {
                        this.contactId = contactId
                        imNumber = it.getStringValue(Im.DATA)
                        val protocol = it.getIntValue(Im.PROTOCOL)
                        imType = when (protocol) {
                            Im.PROTOCOL_AIM -> "AIM"
                            Im.PROTOCOL_MSN -> "MSN"
                            Im.PROTOCOL_YAHOO -> "Yahoo"
                            Im.PROTOCOL_SKYPE -> "Skype"
                            Im.PROTOCOL_QQ -> "QQ"
                            Im.PROTOCOL_GOOGLE_TALK -> "Google Talk"
                            Im.PROTOCOL_ICQ -> "ICQ"
                            Im.PROTOCOL_JABBER -> "Jabber"
                            Im.PROTOCOL_NETMEETING -> "NetMeeting"
                            Im.PROTOCOL_CUSTOM -> it.getStringValue(Im.CUSTOM_PROTOCOL, "Custom")

                            else -> "Unknown"
                        }
                    }
                    contactPerson.ims.add(imEntity)
                }

                ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE -> {
                    val addressEntity = AddressEntity().apply {
                        this.contactId = contactId
                        street =
                            it.getStringValue(ContactsContract.CommonDataKinds.StructuredPostal.STREET)
                        city =
                            it.getStringValue(ContactsContract.CommonDataKinds.StructuredPostal.CITY)
                        region =
                            it.getStringValue(ContactsContract.CommonDataKinds.StructuredPostal.REGION)
                        postcode =
                            it.getStringValue(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE)
                        formatted =
                            it.getStringValue(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS)

                        val addressType =
                            it.getIntValue(ContactsContract.CommonDataKinds.StructuredPostal.TYPE)
                        type = when (addressType) {
                            ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME -> "住宅"
                            ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK -> "单位"
                            ContactsContract.CommonDataKinds.StructuredPostal.TYPE_OTHER -> "其他"
                            ContactsContract.CommonDataKinds.StructuredPostal.TYPE_CUSTOM -> it.getStringValue(
                                ContactsContract.CommonDataKinds.StructuredPostal.LABEL, "自定义"
                            )

                            else -> "未知"
                        }
                    }

                    contactPerson.addresses.add(addressEntity)
                }

                Organization.CONTENT_ITEM_TYPE -> {
                    val orgEntity = OrganizationsEntity().apply {
                        this.contactId = contactId
                        company = it.getStringValue(Organization.COMPANY)
                        title = it.getStringValue(Organization.TITLE)
                    }
                    contactPerson.organizations.add(orgEntity)
                }

                Note.CONTENT_ITEM_TYPE -> {
                    contactPerson.baseInfo.noteInfo = it.getStringValue(Note.NOTE)
                }

                Nickname.CONTENT_ITEM_TYPE -> {
                    contactPerson.baseInfo.nickName = it.getStringValue(Nickname.NAME)
                }

                Event.CONTENT_ITEM_TYPE -> {
                    val eventEntity = EventEntity().apply {
                        this.contactId = contactId
                        date = it.getStringValue(Event.START_DATE)
                        val type = it.getIntValue(Event.TYPE)
                        event = when (type) {
                            Event.TYPE_BIRTHDAY -> "生日"
                            Event.TYPE_ANNIVERSARY -> "纪念日"
                            else -> it.getStringValue(Data.DATA3, "其他")
                        }
                    }
                    contactPerson.events.add(eventEntity)
                }

                ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE -> {
                    val websiteEntity = WebsiteEntity().apply {
                        this.contactId = contactId
                        url = it.getStringValue(ContactsContract.CommonDataKinds.Website.URL)
                        val type = it.getIntValue(ContactsContract.CommonDataKinds.Website.TYPE)
                        this.type = when (type) {
                            ContactsContract.CommonDataKinds.Website.TYPE_HOMEPAGE -> "个人主页"
                            ContactsContract.CommonDataKinds.Website.TYPE_BLOG -> "博客"
                            ContactsContract.CommonDataKinds.Website.TYPE_PROFILE -> "个人资料"
                            ContactsContract.CommonDataKinds.Website.TYPE_HOME -> "主页"
                            ContactsContract.CommonDataKinds.Website.TYPE_WORK -> "工作"
                            ContactsContract.CommonDataKinds.Website.TYPE_FTP -> "FTP"
                            ContactsContract.CommonDataKinds.Website.TYPE_OTHER -> "其他"
                            else -> "未知"
                        }
                    }
                    contactPerson.websites.add(websiteEntity)
                }
            }
        }
    }

    // 处理头像
    contactsMap.values.forEach { contact ->
        val contactUri = Uri.withAppendedPath(
            ContactsContract.Contacts.CONTENT_URI, contact.baseInfo.contactId.toString()
        )
        val iconIs = ContactsContract.Contacts.openContactPhotoInputStream(this, contactUri)
        iconIs?.use { inputStream ->
            contact.baseInfo.avatar = BitmapFactory.decodeStream(inputStream)
        }
    }

    // 将Map转换为List
    contactDetailsList.addAll(contactsMap.values)
    return contactDetailsList
}

/**
 * 根据手机号码获取联系人ID
 */
fun ContentResolver.getContactIdByPhone(phoneNumber: String): Long {
    return query(
        Phone.CONTENT_URI,
        arrayOf(Phone.CONTACT_ID),
        "${Phone.NUMBER} = ?",
        arrayOf(phoneNumber),
        null
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            cursor.getLong(0)
        } else 0L
    } ?: 0L
}

/**
 * 获取所有联系人手机号和ID
 */
suspend fun ContentResolver.getContactIdsWithPhoneNumbers(): Map<String, Long> = logTime("获取所有联系人手机号和ID") {
    val contactIdMap = mutableMapOf<String, Long>()
    query(
        Phone.CONTENT_URI,
        arrayOf(Phone.NUMBER, Phone.CONTACT_ID),
        null,
        null,
        null
    )?.use { cursor ->
        val numberIndex = cursor.getColumnIndex(Phone.NUMBER)
        val contactIdIndex = cursor.getColumnIndex(Phone.CONTACT_ID)

        while (cursor.moveToNext()) {
            val number = cursor.getString(numberIndex)
            val contactId = cursor.getLong(contactIdIndex)
            if (!number.isNullOrEmpty()) {
                contactIdMap[number] = contactId
            }
        }
    }
    contactIdMap
}

