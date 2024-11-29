package com.dullfan.nexuslink.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.dullfan.nexuslink.room.entity.ContactPersonRoomEntity
import com.dullfan.nexuslink.room.entity.EventEntity
import com.dullfan.nexuslink.room.entity.IMEntity

@Dao
interface IMDao {

    @Upsert
    suspend fun upsert(vararg imEntity: IMEntity)

    @Delete
    fun delete(vararg imEntity:IMEntity)

    @Query("select * from contact_im where contactId = :contactId")
    fun findByContactId(contactId: String?): MutableList<IMEntity>?

    @Query("DELETE FROM contact_im WHERE contactId = :contactId")
    suspend fun deleteContactById(contactId: String)
}