package com.dullfan.nexuslink.room

import android.content.Context
import com.dullfan.nexuslink.entity.ContactPersonEntity
import com.dullfan.nexuslink.room.entity.ContactPersonRoomEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

suspend fun upsertContactPersonRoom(
    myRoomDatabase: MyRoomDatabase, list: MutableList<ContactPersonEntity>
): Array<ContactPersonRoomEntity> {
    return list.map { detail ->
        myRoomDatabase.addressDao().upsert(*detail.addressEntityList.toTypedArray())
        myRoomDatabase.emailDao().upsert(*detail.emailEntityList.toTypedArray())
        myRoomDatabase.eventDao().upsert(*detail.eventEntityList.toTypedArray())
        myRoomDatabase.iMDao().upsert(*detail.imEntityList.toTypedArray())
        myRoomDatabase.organizationsDao().upsert(*detail.organizationsEntityList.toTypedArray())
        myRoomDatabase.phoneDao().upsert(*detail.phoneEntityList.toTypedArray())
        myRoomDatabase.websiteDao().upsert(*detail.websiteEntityList.toTypedArray())
        ContactPersonRoomEntity(
            contactId = detail.contactId,
            disPlayName = detail.disPlayName,
            noteInfo = detail.noteInfo,
            nickName = detail.nickName,
            updateTime = System.currentTimeMillis(),
            avatar = detail.avatar
        )
    }.toTypedArray()
}


suspend fun findAllContactPersonRoom(
    myRoomDatabase: MyRoomDatabase
): MutableList<ContactPersonEntity> {
    val all = myRoomDatabase.contactPersonDao().findAll()
    return all.map {
        ContactPersonEntity(
            contactId = it.contactId,
            disPlayName = it.disPlayName,
            noteInfo = it.noteInfo,
            nickName = it.nickName,
            updateTime = it.updateTime,
            avatar = it.avatar,
            addressEntityList = myRoomDatabase.addressDao().findByContactId(it.contactId)
                ?: mutableListOf(),
            emailEntityList = myRoomDatabase.emailDao().findByContactId(it.contactId)
                ?: mutableListOf(),
            eventEntityList = myRoomDatabase.eventDao().findByContactId(it.contactId)
                ?: mutableListOf(),
            imEntityList = myRoomDatabase.iMDao().findByContactId(it.contactId) ?: mutableListOf(),
            organizationsEntityList = myRoomDatabase.organizationsDao().findByContactId(it.contactId)
                ?: mutableListOf(),
            phoneEntityList = myRoomDatabase.phoneDao().findByContactId(it.contactId)
                ?: mutableListOf(),
            websiteEntityList = myRoomDatabase.websiteDao().findByContactId(it.contactId)
                ?: mutableListOf(),
        )
    }.toMutableList()
}