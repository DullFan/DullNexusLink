package com.example.communication.contact.processor

import android.database.Cursor
import android.provider.ContactsContract
import com.example.communication.utils.ExtendUtils.getIntValue
import com.example.communication.utils.ExtendUtils.getStringValue
import com.example.communication.contact.BaseContactDataProcessor
import com.example.communication.room.entity.ContactPersonEntity
import com.example.communication.room.entity.EmailEntity

/**
 * 邮箱处理器
 */
class EmailDataProcessor : BaseContactDataProcessor() {
    override fun process(cursor: Cursor, contactPerson: ContactPersonEntity) {
        val emailEntity = EmailEntity().apply {
            contactId = getContactId(cursor)
            emailData = cursor.getStringValue(ContactsContract.CommonDataKinds.Email.ADDRESS)
            emailType = when (cursor.getIntValue(ContactsContract.CommonDataKinds.Email.TYPE)) {
                ContactsContract.CommonDataKinds.Email.TYPE_HOME -> "个人"
                ContactsContract.CommonDataKinds.Email.TYPE_WORK -> "单位"
                ContactsContract.CommonDataKinds.Email.TYPE_OTHER -> "其他"
                ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM ->
                    cursor.getStringValue(ContactsContract.CommonDataKinds.Email.LABEL, "自定义")
                else -> "未知"
            }
        }
        contactPerson.emails.add(emailEntity)
    }
}
