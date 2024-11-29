package com.dullfan.nexuslink.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.dullfan.nexuslink.room.entity.ContactPersonRoomEntity
import com.dullfan.nexuslink.room.entity.EmailEntity

@Dao
interface EmailDao {
    @Upsert
    suspend fun upsert(vararg emailEntity: EmailEntity)

    @Delete
    fun delete(vararg emailEntity:EmailEntity)

    @Query("select * from contact_email where contactId = :contactId")
    fun findByContactId(contactId: String?): MutableList<EmailEntity>?

    @Query("DELETE FROM contact_email WHERE contactId = :contactId")
    suspend fun deleteContactById(contactId: String)
}