package com.example.communication.contact

import android.provider.ContactsContract
import com.example.communication.contact.processor.AddressDataProcessor
import com.example.communication.contact.processor.EmailDataProcessor
import com.example.communication.contact.processor.EventDataProcessor
import com.example.communication.contact.processor.IMDataProcessor
import com.example.communication.contact.processor.NicknameDataProcessor
import com.example.communication.contact.processor.NoteDataProcessor
import com.example.communication.contact.processor.OrganizationDataProcessor
import com.example.communication.contact.processor.PhoneDataProcessor
import com.example.communication.contact.processor.WebsiteDataProcessor

/**
 * 联系人数据处理器工厂
 */
object ContactDataProcessorFactory {
    fun createProcessor(mimeType: String): ContactDataProcessor? = when (mimeType) {
        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE -> PhoneDataProcessor()
        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE -> EmailDataProcessor()
        ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE -> IMDataProcessor()
        ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE -> AddressDataProcessor()
        ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE -> OrganizationDataProcessor()
        ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE -> NoteDataProcessor()
        ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE -> NicknameDataProcessor()
        ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE -> EventDataProcessor()
        ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE -> WebsiteDataProcessor()
        else -> null
    }
}