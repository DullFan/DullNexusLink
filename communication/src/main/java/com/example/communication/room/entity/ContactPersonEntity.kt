package com.example.communication.room.entity

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.communication.room.converter.BitmapConverter

data class ContactPersonEntity(
    var baseInfo: BaseInfoEntity = BaseInfoEntity(),
    var phones: MutableList<PhoneEntity> = mutableListOf(),
    var ims: MutableList<IMEntity> = mutableListOf(),
    var emails: MutableList<EmailEntity> = mutableListOf(),
    var addresses: MutableList<AddressEntity> = mutableListOf(),
    var organizations: MutableList<OrganizationsEntity> = mutableListOf(),
    var events: MutableList<EventEntity> = mutableListOf(),
    var websites: MutableList<WebsiteEntity> = mutableListOf(),
)

@Entity(tableName = "contact_phone")
data class PhoneEntity(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var contactId: Long = 0L,
    var phoneType: String = "",
    var phoneNumber: String = ""
)

@Entity(tableName = "contact_im")
data class IMEntity(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var contactId: Long = 0L,
    var imType: String = "",
    var imNumber: String = ""
)

@Entity(tableName = "contact_email")
data class EmailEntity(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var contactId: Long = 0L,
    var emailType: String = "",
    var emailData: String = ""

)

@Entity(tableName = "contact_address")
data class AddressEntity(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var contactId: Long = 0L,
    var street: String = "",
    var city: String = "",
    var region: String = "",
    var postcode: String = "",
    var formatted: String = "",
    var type: String = ""

)

@Entity(tableName = "contact_organizations")
data class OrganizationsEntity(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var contactId: Long = 0L,
    var company: String = "",
    var title: String = ""
)

@Entity(tableName = "contact_event")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) var id: Long = 0L,
    var contactId: Long = 0L,
    var event: String = "",
    var date: String = ""
)

@Entity(tableName = "contact_website")
data class WebsiteEntity(
    @PrimaryKey(autoGenerate = true) var id: Long = 0L,
    var contactId: Long = 0L,
    var url: String = "",
    var type: String = ""
)


@Entity(tableName = "base_info")
data class BaseInfoEntity(
    @PrimaryKey var contactId: Long = 0L,
    var disPlayName: String = "",
    var noteInfo: String = "",
    var nickName: String = "",
    var updateTime: Long = 0L,
    @TypeConverters(BitmapConverter::class) var avatar: Bitmap? = null
)