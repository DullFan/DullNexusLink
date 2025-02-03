package com.example.communication.contact

import android.content.ContentResolver
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.ContactsContract
import com.example.communication.utils.ExtendUtils.getLongValue
import com.example.communication.utils.ExtendUtils.getStringValue
import com.example.communication.room.entity.BaseInfoEntity
import com.example.communication.room.entity.ContactPersonEntity

/**
 * 联系人构建器
 */
class ContactPersonBuilder {
    private val contactPerson = ContactPersonEntity()

    fun setBaseInfo(cursor: Cursor): ContactPersonBuilder {
        contactPerson.baseInfo = BaseInfoEntity().apply {
            contactId = cursor.getLongValue(ContactsContract.Data.CONTACT_ID)
            disPlayName = cursor.getStringValue(ContactsContract.Contacts.DISPLAY_NAME)
            updateTime = cursor.getLongValue(ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP)
        }
        return this
    }

    fun processData(cursor: Cursor) {
        val mimeType = cursor.getStringValue(ContactsContract.Data.MIMETYPE)
        ContactDataProcessorFactory.createProcessor(mimeType)?.process(cursor, contactPerson)
    }

    fun setAvatar(contentResolver: ContentResolver) {
        val contactUri = Uri.withAppendedPath(
            ContactsContract.Contacts.CONTENT_URI,
            contactPerson.baseInfo.contactId.toString()
        )
        ContactsContract.Contacts.openContactPhotoInputStream(contentResolver, contactUri)?.use {
            contactPerson.baseInfo.avatar = BitmapFactory.decodeStream(it)
        }
    }

    fun build() = contactPerson
}
