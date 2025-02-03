package com.example.communication.contact.processor

import android.database.Cursor
import android.provider.ContactsContract
import com.example.communication.utils.ExtendUtils.getIntValue
import com.example.communication.utils.ExtendUtils.getStringValue
import com.example.communication.contact.BaseContactDataProcessor
import com.example.communication.room.entity.ContactPersonEntity
import com.example.communication.room.entity.IMEntity

/**
 * 即时通讯处理器
 */
class IMDataProcessor : BaseContactDataProcessor() {
    override fun process(cursor: Cursor, contactPerson: ContactPersonEntity) {
        val imEntity = IMEntity().apply {
            contactId = getContactId(cursor)
            imNumber = cursor.getStringValue(ContactsContract.CommonDataKinds.Im.DATA)
            imType = when (cursor.getIntValue(ContactsContract.CommonDataKinds.Im.PROTOCOL)) {
                ContactsContract.CommonDataKinds.Im.PROTOCOL_AIM -> "AIM"
                ContactsContract.CommonDataKinds.Im.PROTOCOL_MSN -> "MSN"
                ContactsContract.CommonDataKinds.Im.PROTOCOL_YAHOO -> "Yahoo"
                ContactsContract.CommonDataKinds.Im.PROTOCOL_SKYPE -> "Skype"
                ContactsContract.CommonDataKinds.Im.PROTOCOL_QQ -> "QQ"
                ContactsContract.CommonDataKinds.Im.PROTOCOL_GOOGLE_TALK -> "Google Talk"
                ContactsContract.CommonDataKinds.Im.PROTOCOL_ICQ -> "ICQ"
                ContactsContract.CommonDataKinds.Im.PROTOCOL_JABBER -> "Jabber"
                ContactsContract.CommonDataKinds.Im.PROTOCOL_NETMEETING -> "NetMeeting"
                ContactsContract.CommonDataKinds.Im.PROTOCOL_CUSTOM -> cursor.getStringValue(
                    ContactsContract.CommonDataKinds.Im.CUSTOM_PROTOCOL, "Custom"
                )

                else -> "Unknown"
            }
        }
        contactPerson.ims.add(imEntity)
    }
}
