package com.dullfan.nexuslink.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.dullfan.nexuslink.room.entity.ContactPersonRoomEntity
import com.dullfan.nexuslink.room.entity.EmailEntity
import com.dullfan.nexuslink.room.entity.EventEntity

@Dao
interface EventDao {

    @Upsert
    suspend fun upsert(vararg eventEntity: EventEntity)

    @Delete
    fun delete(vararg eventEntity: EventEntity)

    @Query("select * from contact_event where contactId = :contactId")
    fun findByContactId(contactId: String?): MutableList<EventEntity>?

    @Query("DELETE FROM contact_event WHERE contactId = :contactId")
    suspend fun deleteContactById(contactId: String)
}