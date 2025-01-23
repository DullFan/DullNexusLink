package com.example.communication.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.communication.room.entity.PhoneEntity
import com.example.communication.room.entity.WebsiteEntity

@Dao
interface WebsiteDao {

    @Upsert
    suspend fun upsert(vararg websiteEntity: WebsiteEntity)

    @Delete
    fun delete(vararg websiteEntity: WebsiteEntity)

    @Query("select * from contact_website")
    fun findAll(): MutableList<WebsiteEntity>

    @Query("select * from contact_website where contactId = :contactId")
    fun findByContactId(contactId: Long?): MutableList<WebsiteEntity>?

    @Query("DELETE FROM contact_website WHERE contactId = :contactId")
    suspend fun deleteContactById(contactId: Long)

    @Query("DELETE FROM contact_website WHERE contactId IN (:contactIds)")
    suspend fun deleteByContactIds(contactIds: List<Long>)

}