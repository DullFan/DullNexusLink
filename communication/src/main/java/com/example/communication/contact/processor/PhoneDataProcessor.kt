package com.example.communication.contact.processor

import android.database.Cursor
import android.provider.ContactsContract
import com.example.communication.utils.ExtendUtils.getIntValue
import com.example.communication.utils.ExtendUtils.getStringValue
import com.example.communication.contact.BaseContactDataProcessor
import com.example.communication.room.entity.ContactPersonEntity
import com.example.communication.room.entity.PhoneEntity

/**
 * 电话处理器
 */
class PhoneDataProcessor : BaseContactDataProcessor() {
    override fun process(cursor: Cursor, contactPerson: ContactPersonEntity) {
        val phoneEntity = PhoneEntity().apply {
            contactId = getContactId(cursor)
            phoneNumber = cursor.getStringValue(ContactsContract.CommonDataKinds.Phone.NUMBER)
            phoneType = when (cursor.getIntValue(ContactsContract.CommonDataKinds.Phone.TYPE)) {
                ContactsContract.CommonDataKinds.Phone.TYPE_HOME -> "住宅"
                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE -> "手机"
                ContactsContract.CommonDataKinds.Phone.TYPE_WORK -> "单位"
                ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK -> "单位传真"
                ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME -> "住宅传真"
                ContactsContract.CommonDataKinds.Phone.TYPE_PAGER -> "寻呼机"
                ContactsContract.CommonDataKinds.Phone.TYPE_CALLBACK -> "回拨号码"
                ContactsContract.CommonDataKinds.Phone.TYPE_CAR -> "车载电话"
                ContactsContract.CommonDataKinds.Phone.TYPE_COMPANY_MAIN -> "公司总机"
                ContactsContract.CommonDataKinds.Phone.TYPE_ISDN -> "ISDN"
                ContactsContract.CommonDataKinds.Phone.TYPE_MAIN -> "总机"
                ContactsContract.CommonDataKinds.Phone.TYPE_OTHER -> "其他"
                ContactsContract.CommonDataKinds.Phone.TYPE_RADIO -> "无线装置"
                ContactsContract.CommonDataKinds.Phone.TYPE_TELEX -> "电报"
                ContactsContract.CommonDataKinds.Phone.TYPE_TTY_TDD -> "TTY_TDD"
                ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE -> "单位手机"
                ContactsContract.CommonDataKinds.Phone.TYPE_WORK_PAGER -> "单位寻呼机"
                ContactsContract.CommonDataKinds.Phone.TYPE_ASSISTANT -> "助理"
                ContactsContract.CommonDataKinds.Phone.TYPE_MMS -> "彩信"
                ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM -> cursor.getStringValue(
                    ContactsContract.CommonDataKinds.Phone.LABEL, "自定义"
                )

                else -> "未知"
            }
        }
        contactPerson.phones.add(phoneEntity)
    }
}