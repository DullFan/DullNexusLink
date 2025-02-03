package com.example.communication.contact.processor

import android.database.Cursor
import android.provider.ContactsContract
import com.example.communication.utils.ExtendUtils.getStringValue
import com.example.communication.contact.BaseContactDataProcessor
import com.example.communication.room.entity.ContactPersonEntity
import com.example.communication.room.entity.OrganizationsEntity

/**
 * 组织处理器
 */
class OrganizationDataProcessor : BaseContactDataProcessor() {
    override fun process(cursor: Cursor, contactPerson: ContactPersonEntity) {
        val orgEntity = OrganizationsEntity().apply {
            contactId = getContactId(cursor)
            company = cursor.getStringValue(ContactsContract.CommonDataKinds.Organization.COMPANY)
            title = cursor.getStringValue(ContactsContract.CommonDataKinds.Organization.TITLE)
        }
        contactPerson.organizations.add(orgEntity)
    }
}
