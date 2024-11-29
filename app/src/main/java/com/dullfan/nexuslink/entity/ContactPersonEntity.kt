package com.dullfan.nexuslink.entity

import android.graphics.Bitmap
import com.dullfan.nexuslink.room.entity.AddressEntity
import com.dullfan.nexuslink.room.entity.EmailEntity
import com.dullfan.nexuslink.room.entity.EventEntity
import com.dullfan.nexuslink.room.entity.IMEntity
import com.dullfan.nexuslink.room.entity.OrganizationsEntity
import com.dullfan.nexuslink.room.entity.PhoneEntity
import com.dullfan.nexuslink.room.entity.WebsiteEntity

data class ContactPersonEntity(
    var contactId: String = "",
    var disPlayName: String? = "",
    var noteInfo: String? = "",
    var nickName: String? = "",
    var updateTime: Long? = 0L,
    var avatar: Bitmap? = null,
    var phoneEntityList: MutableList<PhoneEntity> = mutableListOf(),
    var imEntityList: MutableList<IMEntity> = mutableListOf(),
    var emailEntityList: MutableList<EmailEntity> = mutableListOf(),
    var addressEntityList: MutableList<AddressEntity> = mutableListOf(),
    var organizationsEntityList: MutableList<OrganizationsEntity> = mutableListOf(),
    var eventEntityList: MutableList<EventEntity> = mutableListOf(),
    var websiteEntityList: MutableList<WebsiteEntity> = mutableListOf()
)