package com.example.communication.contact.processor

import android.database.Cursor
import android.provider.ContactsContract
import com.example.communication.utils.ExtendUtils.getIntValue
import com.example.communication.utils.ExtendUtils.getStringValue
import com.example.communication.contact.BaseContactDataProcessor
import com.example.communication.room.entity.AddressEntity
import com.example.communication.room.entity.ContactPersonEntity

/**
 * 地址处理器
 */
class AddressDataProcessor : BaseContactDataProcessor() {
    override fun process(cursor: Cursor, contactPerson: ContactPersonEntity) {
        val addressEntity = AddressEntity().apply {
            contactId = getContactId(cursor)
            street = cursor.getStringValue(ContactsContract.CommonDataKinds.StructuredPostal.STREET)
            city = cursor.getStringValue(ContactsContract.CommonDataKinds.StructuredPostal.CITY)
            region = cursor.getStringValue(ContactsContract.CommonDataKinds.StructuredPostal.REGION)
            postcode =
                cursor.getStringValue(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE)
            formatted =
                cursor.getStringValue(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS)
            type =
                when (cursor.getIntValue(ContactsContract.CommonDataKinds.StructuredPostal.TYPE)) {
                    ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME -> "住宅"
                    ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK -> "单位"
                    ContactsContract.CommonDataKinds.StructuredPostal.TYPE_OTHER -> "其他"
                    ContactsContract.CommonDataKinds.StructuredPostal.TYPE_CUSTOM ->
                        cursor.getStringValue(
                            ContactsContract.CommonDataKinds.StructuredPostal.LABEL,
                            "自定义"
                        )

                    else -> "未知"
                }
        }
        contactPerson.addresses.add(addressEntity)
    }
}