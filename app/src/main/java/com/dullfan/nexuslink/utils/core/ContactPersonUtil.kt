package com.dullfan.nexuslink.utils.core

import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Event
import android.provider.ContactsContract.CommonDataKinds.Im
import android.provider.ContactsContract.CommonDataKinds.Nickname
import android.provider.ContactsContract.CommonDataKinds.Note
import android.provider.ContactsContract.CommonDataKinds.Organization
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.Data
import android.util.Log
import com.dullfan.nexuslink.entity.ContactPersonEntity
import com.dullfan.nexuslink.room.entity.AddressEntity
import com.dullfan.nexuslink.room.entity.EmailEntity
import com.dullfan.nexuslink.room.entity.EventEntity
import com.dullfan.nexuslink.room.entity.IMEntity
import com.dullfan.nexuslink.room.entity.OrganizationsEntity
import com.dullfan.nexuslink.room.entity.PhoneEntity
import com.dullfan.nexuslink.room.entity.WebsiteEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 实时监听联系人变化
 */
fun ContentResolver.observeContacts(
    lastUpdateTimeFlow: StateFlow<Long>,
    updateTime: () -> Unit
): Flow<MutableList<ContactPersonEntity>> =
    callbackFlow {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                CoroutineScope(Dispatchers.IO).launch {
                    val currentLastUpdateTime = lastUpdateTimeFlow.value
                    val contacts = queryUpdatedContacts(currentLastUpdateTime)
                    withContext(Dispatchers.Main) {
                        trySend(contacts)
                        updateTime()
                    }
                }
            }
        }
        registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true, observer)
        awaitClose { unregisterContentObserver(observer) }
    }.conflate()

/**
 * 查询所有联系人
 */
fun ContentResolver.queryContacts(): MutableList<ContactPersonEntity> {
    val startTime = System.currentTimeMillis()
    val contactDetailsList = mutableListOf<ContactPersonEntity>()
    val cursor = query(
        ContactsContract.Contacts.CONTENT_URI,
        arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME),
        null,
        null,
        ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC"
    )

    contactsLoadData(cursor, contactDetailsList)

    // 记录结束时间并计算耗时
    val endTime = System.currentTimeMillis()
    val duration = endTime - startTime
    Log.d("TAG", "查询所有联系人耗时：$duration 毫秒")
    return contactDetailsList
}

/**
 * 获取最后更新时间后的联系人
 */
fun ContentResolver.queryUpdatedContacts(lastUpdateTime: Long): MutableList<ContactPersonEntity> {
    val startTime = System.currentTimeMillis()
    val contactDetailsList = mutableListOf<ContactPersonEntity>()

    // 查询条件：仅获取最近更新的联系人
    val selection = "${ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP} > ?"
    val selectionArgs = arrayOf(lastUpdateTime.toString())

    val cursor = query(
        ContactsContract.Contacts.CONTENT_URI,
        arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME),
        selection,
        selectionArgs,
        ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC"
    )

    contactsLoadData(cursor, contactDetailsList)

    // 记录结束时间并计算耗时
    val endTime = System.currentTimeMillis()
    val duration = endTime - startTime
    Log.d("TAG", "查询已更新联系人耗时：$duration 毫秒")
    return contactDetailsList
}


/**
 * 查询所有联系人ID
 */
fun ContentResolver.queryAllContactId(): MutableList<String> {
    val startTime = System.currentTimeMillis()
    val contactIdList = mutableListOf<String>()

    val cursor = query(
        ContactsContract.Contacts.CONTENT_URI,
        arrayOf(ContactsContract.Contacts._ID),
        null,
        null,
        null
    )

    if (cursor != null && cursor.count > 0) {
        while (cursor.moveToNext()) {
            val columnIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
            contactIdList.add(cursor.getString(columnIndex))
        }
        cursor.close()
    }

    val endTime = System.currentTimeMillis()
    val duration = endTime - startTime
    Log.d("TAG", "查询所有联系人ID：$duration 毫秒")
    return contactIdList
}




/**
 * 加载联系人数据
 */
fun ContentResolver.contactsLoadData(
    cursor: Cursor?,
    contactDetailsList: MutableList<ContactPersonEntity>
) {
    if (cursor != null && cursor.count > 0) {
        while (cursor.moveToNext()) {
            val contactPersonEntity = ContactPersonEntity()

            val columnIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
            contactPersonEntity.contactId = cursor.getString(columnIndex)

            val displayNameColumn =
                cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            contactPersonEntity.disPlayName = cursor.getString(displayNameColumn)
            // 获取联系人更新数据
            getLastUpdatedTime(contactPersonEntity)
            // 获取手机号
            getPhone(contactPersonEntity)
            // 获取邮箱
            getEmail(contactPersonEntity)
            // 获取IM
            getIM(contactPersonEntity)
            // 获取地址
            getAddress(contactPersonEntity)
            // 获取组织
            getOrganizations(contactPersonEntity)
            // 获取备注
            getNote(contactPersonEntity)
            // 获取昵称
            getNickName(contactPersonEntity)
            // 获取事件
            getBirth(contactPersonEntity)
            // 获取网址
            getWebsites(contactPersonEntity)
            // 获取头像
            getAvatar(contactPersonEntity)
            contactDetailsList.add(contactPersonEntity)
        }
        cursor.close()
    }
}

/**
 * 获取返回联系人修改时间
 */
private fun ContentResolver.getLastUpdatedTime(contactPersonEntity: ContactPersonEntity) {
    val projection = arrayOf(ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP)
    val selection = "${ContactsContract.Contacts._ID} = ?"
    val selectionArgs = arrayOf(contactPersonEntity.contactId)

    val cursor = query(
        ContactsContract.Contacts.CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        null
    )

    cursor?.use {
        if (it.moveToNext()) {
            val columnIndex =
                it.getColumnIndex(ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP)
            if (columnIndex >= 0) {
                contactPersonEntity.updateTime =
                    it.getLong(columnIndex)
            }
        }
    }
}

private fun ContentResolver.getAvatar(
    contactPersonEntity: ContactPersonEntity,
) {
    val contactUri = Uri.withAppendedPath(
        ContactsContract.Contacts.CONTENT_URI, contactPersonEntity.contactId
    )
    val iconIs = ContactsContract.Contacts.openContactPhotoInputStream(this, contactUri)
    if (iconIs != null) {
        // 解码输入流为 Bitmap
        val bitmap = BitmapFactory.decodeStream(iconIs)
        contactPersonEntity.avatar = bitmap
        iconIs.close()
    } else {
        // 处理头像不存在的情况，比如设置默认头像
        contactPersonEntity.avatar = null
    }
}


private fun ContentResolver.getWebsites(
    contactPersonEntity: ContactPersonEntity,
) {
    val websiteList = mutableListOf<WebsiteEntity>()
    val websites = query(
        Data.CONTENT_URI, arrayOf(
            // 存储网址的列
            Data.DATA1,
            ContactsContract.CommonDataKinds.Website.TYPE,
            ContactsContract.CommonDataKinds.Website.LABEL
        ), "${Data.CONTACT_ID} = ? AND ${Data.MIMETYPE} = ?", arrayOf(
            contactPersonEntity.contactId,
            ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE
        ), null
    )

    if (websites != null && websites.moveToFirst()) {
        do {
            val websiteEntity = WebsiteEntity()
            websiteEntity.contactId = contactPersonEntity.contactId
            // 获取网址URL
            val urlColumnIndex = websites.getColumnIndex(Data.DATA1)
            websiteEntity.url = websites.getString(urlColumnIndex)

            // 获取类型
            val typeIndex =
                websites.getColumnIndex(ContactsContract.CommonDataKinds.Website.TYPE)
            val type = websites.getInt(typeIndex)

            // 获取自定义标签
            val labelIndex =
                websites.getColumnIndex(ContactsContract.CommonDataKinds.Website.LABEL)
            val label = websites.getString(labelIndex)

            // 根据类型设置标签
            websiteEntity.type = when (type) {
                ContactsContract.CommonDataKinds.Website.TYPE_HOMEPAGE -> "个人主页"
                ContactsContract.CommonDataKinds.Website.TYPE_WORK -> "工作"
                ContactsContract.CommonDataKinds.Website.TYPE_BLOG -> "博客"
                ContactsContract.CommonDataKinds.Website.TYPE_PROFILE -> "个人资料"
                ContactsContract.CommonDataKinds.Website.TYPE_FTP -> "FTP"
                ContactsContract.CommonDataKinds.Website.TYPE_CUSTOM -> label ?: "自定义"
                else -> "网址"
            }

            websiteList.add(websiteEntity)
        } while (websites.moveToNext())
    }

    contactPersonEntity.websiteEntityList = websiteList
    websites?.close()
}

private fun ContentResolver.getBirth(
    contactPersonEntity: ContactPersonEntity,
) {
    val list = mutableListOf<EventEntity>()
    // 查询条件，包括 contactId 的匹配
    val selection = query(
        Data.CONTENT_URI,
        // DATA3 用于存储自定义事件名称
        arrayOf(
            Data.RAW_CONTACT_ID, Event.TYPE, Event.START_DATE, Data.DATA3
        ),
        "${Event.MIMETYPE} = ? AND ${Data.RAW_CONTACT_ID} = ?",
        arrayOf(Event.CONTENT_ITEM_TYPE, contactPersonEntity.contactId),
        null
    )

    if (selection != null && selection.moveToFirst()) {
        do {
            val eventEntity = EventEntity()
            eventEntity.contactId = contactPersonEntity.contactId
            // 获取列索引
            val dateIndex = selection.getColumnIndex(Event.START_DATE)
            val eventTypeIndex = selection.getColumnIndex(Event.TYPE)
            val customLabelIndex = selection.getColumnIndex(Data.DATA3)

            // 获取事件日期和类型
            val date = selection.getString(dateIndex) ?: "无日期"
            val eventType = selection.getInt(eventTypeIndex)
            val customLabel = selection.getString(customLabelIndex)

            // 根据类型和自定义名称确定事件描述
            val eventDescription = when (eventType) {
                Event.TYPE_BIRTHDAY -> "生日"
                Event.TYPE_ANNIVERSARY -> "纪念日"
                else -> customLabel ?: "其他"
            }
            eventEntity.event = eventDescription
            eventEntity.date = date
            list.add(eventEntity)
        } while (selection.moveToNext())
    }
    contactPersonEntity.eventEntityList = list
    selection?.close()
}

private fun ContentResolver.getNickName(
    contactPersonEntity: ContactPersonEntity,
) {
    val nickNameCursor = query(
        Data.CONTENT_URI,
        arrayOf(Data._ID, Nickname.NAME),
        "${Data.CONTACT_ID} =? AND ${Data.MIMETYPE} = '${Nickname.CONTENT_ITEM_TYPE}'",
        arrayOf(contactPersonEntity.contactId),
        null
    )

    if (nickNameCursor != null && nickNameCursor.moveToFirst()) {
        val columnIndex = nickNameCursor.getColumnIndex(Nickname.NAME)
        contactPersonEntity.nickName = nickNameCursor.getString(columnIndex)
    }

    nickNameCursor?.close()
}

private fun ContentResolver.getNote(
    contactPersonEntity: ContactPersonEntity,
) {
    val notes = query(
        Data.CONTENT_URI,
        arrayOf(Data._ID, Note.NOTE),
        "${Data.CONTACT_ID} =? AND ${Data.MIMETYPE} = '${Note.CONTENT_ITEM_TYPE}'",
        arrayOf(contactPersonEntity.contactId),
        null
    )
    if (notes != null && notes.moveToFirst()) {
        val columnIndex = notes.getColumnIndex(Note.NOTE)
        contactPersonEntity.noteInfo = notes.getString(columnIndex)
    }
    notes?.close()
}

private fun ContentResolver.getOrganizations(
    contactPersonEntity: ContactPersonEntity,
) {
    val organizationsList = mutableListOf<OrganizationsEntity>()
    val organizations = query(
        Data.CONTENT_URI,
        arrayOf(Data._ID, Organization.COMPANY, Organization.TITLE),
        "${Data.CONTACT_ID} =? AND ${Data.MIMETYPE} = '${Organization.CONTENT_ITEM_TYPE}'",
        arrayOf(contactPersonEntity.contactId),
        null
    )
    if (organizations != null && organizations.moveToFirst()) {
        do {
            val organizationsEntity = OrganizationsEntity()
            organizationsEntity.contactId = contactPersonEntity.contactId
            val companyColumnIndex = organizations.getColumnIndex(Organization.COMPANY)
            val titleColumnIndex = organizations.getColumnIndex(Organization.TITLE)
            organizationsEntity.company = organizations.getString(companyColumnIndex)
            organizationsEntity.title = organizations.getString(titleColumnIndex)
            organizationsList.add(organizationsEntity)
        } while (organizations.moveToNext())
    }
    contactPersonEntity.organizationsEntityList = organizationsList
    organizations?.close()
}

/**
 * 获取联系人地址
 */
private fun ContentResolver.getAddress(
    contactPersonEntity: ContactPersonEntity,
) {
    val addressList = mutableListOf<AddressEntity>()
    val address = query(
        ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,
        null,
        "${ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID} = ?",
        arrayOf(contactPersonEntity.contactId),
        null
    )

    if (address != null && address.moveToFirst()) {
        do {
            val streetColumnIndex =
                address.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET)
            val cityColumnIndex =
                address.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY)
            val regionColumnIndex =
                address.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION)
            val postcodeColumnIndex =
                address.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE)
            val formattedAddColumnIndex =
                address.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS)
            val typeIndex =
                address.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE)
            val labelIndex =
                address.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.LABEL)

            val addressEntity = AddressEntity()
            addressEntity.street = address.getString(streetColumnIndex)
            addressEntity.city = address.getString(cityColumnIndex)
            addressEntity.region = address.getString(regionColumnIndex)
            addressEntity.postcode = address.getString(postcodeColumnIndex)
            addressEntity.formatted = address.getString(formattedAddColumnIndex)

            // 获取类型索引
            val type = address.getInt(typeIndex)

            // 根据类型设置标签
            addressEntity.type = when (type) {
                ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME -> "住宅"
                ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK -> "单位"
                ContactsContract.CommonDataKinds.StructuredPostal.TYPE_OTHER -> "其他"
                ContactsContract.CommonDataKinds.StructuredPostal.TYPE_CUSTOM -> {
                    // 获取自定义标签
                    address.getString(labelIndex) ?: "自定义"
                }

                else -> "未知"
            }
            addressEntity.contactId = contactPersonEntity.contactId
            addressList.add(addressEntity)
        } while (address.moveToNext())
    }

    contactPersonEntity.addressEntityList = addressList
    address?.close()
}

/**
 * 获取IM
 * TODO 修改过时代码
 */
private fun ContentResolver.getIM(contactPersonEntity: ContactPersonEntity) {
    val imList = mutableListOf<IMEntity>()
    val ims = query(
        Data.CONTENT_URI,
        arrayOf(Data._ID, Im.PROTOCOL, Im.CUSTOM_PROTOCOL, Im.DATA),
        "${Data.CONTACT_ID} = ? AND ${Data.MIMETYPE} = '${Im.CONTENT_ITEM_TYPE}'",
        arrayOf(contactPersonEntity.contactId),
        null
    )

    if (ims != null && ims.moveToFirst()) {
        do {
            val protocolColumnIndex = ims.getColumnIndexOrThrow(Im.PROTOCOL)
            val customProtocolColumnIndex = ims.getColumnIndexOrThrow(Im.CUSTOM_PROTOCOL)
            val dataColumnIndex = ims.getColumnIndexOrThrow(Im.DATA)

            val protocol = ims.getInt(protocolColumnIndex)

            val protocolLabel = when (protocol) {
                Im.PROTOCOL_AIM -> "AIM"
                Im.PROTOCOL_MSN -> "MSN"
                Im.PROTOCOL_YAHOO -> "Yahoo"
                Im.PROTOCOL_SKYPE -> "Skype"
                Im.PROTOCOL_QQ -> "QQ"
                Im.PROTOCOL_GOOGLE_TALK -> "Google Talk"
                Im.PROTOCOL_ICQ -> "ICQ"
                Im.PROTOCOL_JABBER -> "Jabber"
                Im.PROTOCOL_NETMEETING -> "NetMeeting"
                Im.PROTOCOL_CUSTOM -> ims.getString(customProtocolColumnIndex) ?: "Custom"
                else -> "Unknown"
            }

            val imEntity = IMEntity().apply {
                imType = protocolLabel
                imNumber = ims.getString(dataColumnIndex) ?: ""
            }
            imEntity.contactId = contactPersonEntity.contactId
            imList.add(imEntity)
        } while (ims.moveToNext())
    }
    contactPersonEntity.imEntityList = imList
    ims?.close()
}


/**
 * 获取邮箱
 */
private fun ContentResolver.getEmail(
    contactPersonEntity: ContactPersonEntity,
) {
    val emailList = mutableListOf<EmailEntity>()
    val emails = query(
        ContactsContract.CommonDataKinds.Email.CONTENT_URI,
        null,
        "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?",
        arrayOf(contactPersonEntity.contactId),
        null
    )

    if (emails != null && emails.moveToFirst()) {
        do {
            val emailEntity = EmailEntity()
            val emailTypeIndex =
                emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE)
            val emailDataIndex =
                emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)
            val labelIndex = emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.LABEL)

            // 获取邮箱类型
            val type = emails.getInt(emailTypeIndex)

            // 根据类型设置标签
            emailEntity.emailType = when (type) {
                ContactsContract.CommonDataKinds.Email.TYPE_HOME -> "个人"
                ContactsContract.CommonDataKinds.Email.TYPE_WORK -> "单位"
                ContactsContract.CommonDataKinds.Email.TYPE_OTHER -> "其他"
                ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM -> {
                    // 获取自定义标签
                    emails.getString(labelIndex) ?: "自定义"
                }

                else -> "未知"
            }

            // 获取邮箱地址
            emailEntity.emailData = emails.getString(emailDataIndex)
            emailEntity.contactId = contactPersonEntity.contactId
            emailList.add(emailEntity)
        } while (emails.moveToNext())
    }

    contactPersonEntity.emailEntityList = emailList
    emails?.close()
}


/**
 * 获取联系人手机号
 */
private fun ContentResolver.getPhone(
    contactPersonEntity: ContactPersonEntity,
) {
    val phones = query(
        Phone.CONTENT_URI,
        null,
        "${Phone.CONTACT_ID} = ?",
        arrayOf(contactPersonEntity.contactId),
        null
    )
    val phoneTypeList = mutableListOf<PhoneEntity>()
    if (phones != null && phones.moveToFirst()) {
        do {
            val phoneTypeEntity = PhoneEntity()

            // 获取电话类型
            val phoneTypeColumnIndex = phones.getColumnIndex(Phone.TYPE)
            val phoneType = if (phoneTypeColumnIndex != -1) {
                phones.getInt(phoneTypeColumnIndex)
            } else {
                // 如果找不到类型，则默认为移动设备
                Phone.TYPE_MOBILE
            }

            // 获取电话号码
            val phoneNumberColumnIndex = phones.getColumnIndex(Phone.NUMBER)
            if (phoneNumberColumnIndex != -1) {
                val phoneNumber = phones.getString(phoneNumberColumnIndex)
                phoneTypeEntity.phoneNumber = phoneNumber
            }

            // 根据类型设置标签
            phoneTypeEntity.phoneType = when (phoneType) {
                Phone.TYPE_HOME -> "住宅"
                Phone.TYPE_WORK -> "单位"
                Phone.TYPE_FAX_WORK -> "单位传真"
                Phone.TYPE_FAX_HOME -> "住宅传真"
                Phone.TYPE_PAGER -> "寻呼机"
                Phone.TYPE_CALLBACK -> "回拨号码"
                Phone.TYPE_COMPANY_MAIN -> "公司总机"
                Phone.TYPE_CAR -> "车载电话"
                Phone.TYPE_ISDN -> "ISDN"
                Phone.TYPE_MAIN -> "总机"
                Phone.TYPE_RADIO -> "无线装置"
                Phone.TYPE_TELEX -> "电报"
                Phone.TYPE_TTY_TDD -> "TTY_TDD"
                Phone.TYPE_WORK_MOBILE -> "单位手机"
                Phone.TYPE_WORK_PAGER -> "单位寻呼机"
                Phone.TYPE_ASSISTANT -> "助理"
                Phone.TYPE_MMS -> "彩信"
                else -> "手机"
            }

            phoneTypeEntity.contactId = contactPersonEntity.contactId

            phoneTypeList.add(phoneTypeEntity)
        } while (phones.moveToNext())
    }
    contactPersonEntity.phoneEntityList = phoneTypeList
    phones?.close()
}
