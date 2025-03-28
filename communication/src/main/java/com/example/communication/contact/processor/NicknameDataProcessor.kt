package com.example.communication.contact.processor

import android.database.Cursor
import android.provider.ContactsContract
import com.example.communication.utils.ExtendUtils.getStringValue
import com.example.communication.contact.BaseContactDataProcessor
import com.example.communication.room.entity.ContactPersonEntity

/**
 * 昵称处理器
 */
class NicknameDataProcessor : BaseContactDataProcessor() {
    override fun process(cursor: Cursor, contactPerson: ContactPersonEntity) {
        contactPerson.baseInfo.nickName =
            cursor.getStringValue(ContactsContract.CommonDataKinds.Nickname.NAME)
    }
}