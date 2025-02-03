package com.example.communication.contact.processor

import android.database.Cursor
import android.provider.ContactsContract
import com.example.communication.utils.ExtendUtils.getIntValue
import com.example.communication.utils.ExtendUtils.getStringValue
import com.example.communication.contact.BaseContactDataProcessor
import com.example.communication.room.entity.ContactPersonEntity
import com.example.communication.room.entity.WebsiteEntity

/**
 * 网站处理器
 */
class WebsiteDataProcessor : BaseContactDataProcessor() {
    override fun process(cursor: Cursor, contactPerson: ContactPersonEntity) {
        val websiteEntity = WebsiteEntity().apply {
            contactId = getContactId(cursor)
            url = cursor.getStringValue(ContactsContract.CommonDataKinds.Website.URL)
            type = when (cursor.getIntValue(ContactsContract.CommonDataKinds.Website.TYPE)) {
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