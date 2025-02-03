package com.example.communication.contact.processor

import android.database.Cursor
import android.provider.ContactsContract
import com.example.communication.utils.ExtendUtils.getStringValue
import com.example.communication.contact.BaseContactDataProcessor
import com.example.communication.room.entity.ContactPersonEntity

/**
 * 备注处理器
 */
class NoteDataProcessor : BaseContactDataProcessor() {
    override fun process(cursor: Cursor, contactPerson: ContactPersonEntity) {
        contactPerson.baseInfo.noteInfo =
            cursor.getStringValue(ContactsContract.CommonDataKinds.Note.NOTE)
    }
}
