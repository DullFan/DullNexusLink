package com.dullfan.nexuslink.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.dullfan.nexuslink.room.entity.ContactPersonRoomEntity
import com.dullfan.nexuslink.room.entity.PhoneEntity
import com.dullfan.nexuslink.room.entity.WebsiteEntity

@Dao
interface WebsiteDao {

    @Upsert
    suspend fun upsert(vararg websiteEntity: WebsiteEntity)

    @Delete
    fun delete(vararg websiteEntity: WebsiteEntity)

    @Query("select * from contact_website where contactId = :contactId")
    fun findByContactId(contactId: String?): MutableList<WebsiteEntity>?

    @Query("DELETE FROM contact_website WHERE contactId = :contactId")
    suspend fun deleteContactById(contactId: String)
}