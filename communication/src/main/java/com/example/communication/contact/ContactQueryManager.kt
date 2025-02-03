package com.example.communication.contact

import android.content.ContentResolver
import android.provider.ContactsContract
import com.example.communication.utils.ExtendUtils.getLongValue
import com.example.communication.room.entity.ContactPersonEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 联系人查询管理器
 */
class ContactQueryManager(private val contentResolver: ContentResolver) {
    private val projection = arrayOf(
        ContactsContract.Data.CONTACT_ID,                   // 联系人ID
        ContactsContract.Data.MIMETYPE,                     // 数据类型
        ContactsContract.Contacts.DISPLAY_NAME,    // 显示名称
        ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP,  // 更新时间
        ContactsContract.Data.DATA1,                        // 通用数据字段
        ContactsContract.Data.DATA2,                        // 类型字段
        ContactsContract.Data.DATA3,                        // 标签字段
        ContactsContract.CommonDataKinds.Organization.COMPANY,              // 公司
        ContactsContract.CommonDataKinds.Organization.TITLE,                // 职位
        ContactsContract.CommonDataKinds.Phone.NUMBER,                      // 电话号码
        ContactsContract.CommonDataKinds.Phone.TYPE,                        // 电话类型
        ContactsContract.CommonDataKinds.Phone.LABEL,                       // 电话标签
        ContactsContract.CommonDataKinds.Email.ADDRESS,  // 邮箱地址
        ContactsContract.CommonDataKinds.Email.TYPE,     // 邮箱类型
        ContactsContract.CommonDataKinds.Email.LABEL,    // 邮箱标签
        ContactsContract.CommonDataKinds.Im.PROTOCOL,                       // IM协议
        ContactsContract.CommonDataKinds.Im.CUSTOM_PROTOCOL,                // 自定义IM协议
        ContactsContract.CommonDataKinds.Im.DATA,                          // IM账号
        ContactsContract.CommonDataKinds.Note.NOTE,                         // 备注
        ContactsContract.CommonDataKinds.Nickname.NAME,                     // 昵称
        ContactsContract.CommonDataKinds.Event.START_DATE,                  // 事件日期
        ContactsContract.CommonDataKinds.Event.TYPE,                        // 事件类型
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

    suspend fun queryContacts(
        selection: String? = null,
        selectionArgs: Array<String>? = null,
        sortOrder: String? = "${ContactsContract.Data.CONTACT_ID} ASC",
        maxCount: Int? = null
    ): List<ContactPersonEntity> = withContext(Dispatchers.IO) {
        val contactsMap = mutableMapOf<Long, ContactPersonBuilder>()

        contentResolver.query(
            ContactsContract.Data.CONTENT_URI, projection, selection, selectionArgs, sortOrder
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val contactId = cursor.getLongValue(ContactsContract.Data.CONTACT_ID)

                if (maxCount != null && contactsMap.size >= maxCount && !contactsMap.containsKey(
                        contactId
                    )
                ) {
                    break
                }

                val builder = contactsMap.getOrPut(contactId) {
                    ContactPersonBuilder().apply { setBaseInfo(cursor) }
                }
                builder.processData(cursor)
            }
        }

        contactsMap.values.forEach { builder ->
            builder.setAvatar(contentResolver)
        }

        contactsMap.values.map { it.build() }
    }

    suspend fun queryAllContactIds(): List<Long> = withContext(Dispatchers.IO) {
        val ids = mutableListOf<Long>()
        contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            arrayOf(ContactsContract.Contacts._ID),
            null,
            null,
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                ids.add(cursor.getLongValue(ContactsContract.Contacts._ID))
            }
        }
        ids
    }

    suspend fun getContactIdByPhone(phoneNumber: String): Long = withContext(Dispatchers.IO) {
        contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.CONTACT_ID),
            "${ContactsContract.CommonDataKinds.Phone.NUMBER} = ?",
            arrayOf(phoneNumber),
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getLongValue(ContactsContract.CommonDataKinds.Phone.CONTACT_ID) else 0L
        } ?: 0L
    }


    /**
     * 获取所有联系人手机号和ID的映射
     */
    suspend fun getContactIdsWithPhoneNumbers(): Map<String, Long> = withContext(Dispatchers.IO) {
        val contactIdMap = mutableMapOf<String, Long>()

        contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID
            ),
            null,
            null,
            null
        )?.use { cursor ->
            val numberIndex =
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val contactIdIndex =
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)

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
}