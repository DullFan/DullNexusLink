package com.dullfan.nexuslink.room.entity

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.dullfan.nexuslink.utils.BitmapConverter

@Entity(tableName = "contact_phone")
data class PhoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    var contactId: String = "",
    var phoneType: String? = "",
    var phoneNumber: String? = ""
)

@Entity(tableName = "contact_im")
data class IMEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    var contactId: String = "",
    var imType: String? = "",
    var imNumber: String? = ""
)

@Entity(tableName = "contact_email")
data class EmailEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    var contactId: String = "",
    var emailType: String? = "",
    var emailData: String? = ""
)

@Entity(tableName = "contact_address")
data class AddressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    var contactId: String = "",
    var street: String? = "",
    var city: String? = "",
    var region: String? = "",
    var postcode: String? = "",
    var formatted: String? = "",
    var type: String? = ""
)

@Entity(tableName = "contact_organizations")
data class OrganizationsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    var contactId: String = "",
    var company: String? = "",
    var title: String? = ""
)

@Entity(tableName = "contact_event")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    var contactId: String = "",
    var event: String? = "",
    var date: String? = ""
)

@Entity(tableName = "contact_website")
data class WebsiteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    var contactId: String = "",
    var url: String? = "",
    var type: String? = ""
)


@Entity(tableName = "contact_person")
data class ContactPersonRoomEntity(
    @PrimaryKey
    var contactId: String = "",
    var disPlayName: String? = "",
    var noteInfo: String? = "",
    var nickName: String? = "",
    var updateTime: Long? = 0L,
    @TypeConverters(BitmapConverter::class) var avatar: Bitmap? = null
)