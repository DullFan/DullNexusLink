package com.example.communication.contact

import android.database.Cursor
import android.provider.ContactsContract
import com.example.communication.utils.ExtendUtils.getLongValue
import com.example.communication.room.entity.ContactPersonEntity

/**
 * 联系人数据处理器接口
 */
interface ContactDataProcessor {
    fun process(cursor: Cursor, contactPerson: ContactPersonEntity)
}

abstract class BaseContactDataProcessor : ContactDataProcessor {
    protected fun getContactId(cursor: Cursor): Long =
        cursor.getLongValue(ContactsContract.Data.CONTACT_ID)
}
