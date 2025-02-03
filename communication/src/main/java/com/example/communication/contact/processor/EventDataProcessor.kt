package com.example.communication.contact.processor

import android.database.Cursor
import android.provider.ContactsContract
import com.example.communication.utils.ExtendUtils.getIntValue
import com.example.communication.utils.ExtendUtils.getStringValue
import com.example.communication.contact.BaseContactDataProcessor
import com.example.communication.room.entity.ContactPersonEntity
import com.example.communication.room.entity.EventEntity

/**
 * 事件处理器
 */
class EventDataProcessor : BaseContactDataProcessor() {
    override fun process(cursor: Cursor, contactPerson: ContactPersonEntity) {
        val eventEntity = EventEntity().apply {
            contactId = getContactId(cursor)
            date = cursor.getStringValue(ContactsContract.CommonDataKinds.Event.START_DATE)
            event = when (cursor.getIntValue(ContactsContract.CommonDataKinds.Event.TYPE)) {
                ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY -> "生日"
                ContactsContract.CommonDataKinds.Event.TYPE_ANNIVERSARY -> "纪念日"
                else -> cursor.getStringValue(ContactsContract.Data.DATA3, "其他")
            }
        }
        contactPerson.events.add(eventEntity)
    }
}