package com.example.communication.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.communication.room.entity.AddressEntity
import com.example.communication.room.entity.EmailEntity
import com.example.communication.room.entity.EventEntity

@Dao
interface EventDao {

    @Upsert
    suspend fun upsert(vararg eventEntity: EventEntity)

    @Delete
    fun delete(vararg eventEntity: EventEntity)

    @Query("select * from contact_event")
    fun findAll(): MutableList<EventEntity>

    @Query("SELECT * FROM contact_event ORDER BY contactId DESC LIMIT :limit")
    fun findInitialLoadData(limit: Int): MutableList<EventEntity>

    @Query("SELECT * FROM contact_event WHERE contactId < :contactId")
    fun findByContactIdLessThan(contactId: Long): MutableList<EventEntity>

    @Query("select * from contact_event where contactId = :contactId")
    fun findByContactId(contactId: Long?): MutableList<EventEntity>?

    @Query("DELETE FROM contact_event WHERE contactId = :contactId")
    suspend fun deleteContactById( contactId: Long)

    @Query("DELETE FROM contact_event WHERE contactId IN (:contactIds)")
    suspend fun deleteByContactIds(contactIds: List<Long>)

}