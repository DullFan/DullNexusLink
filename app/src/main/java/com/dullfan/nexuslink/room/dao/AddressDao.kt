package com.dullfan.nexuslink.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.dullfan.nexuslink.room.entity.AddressEntity
import com.dullfan.nexuslink.room.entity.ContactPersonRoomEntity

@Dao
interface AddressDao {

    @Upsert
    suspend fun upsert(vararg addressEntity: AddressEntity)

    @Delete
    fun delete(vararg addressEntity: AddressEntity)

    @Query("select * from contact_address where contactId = :contactId")
    fun findByContactId(contactId: String?): MutableList<AddressEntity>?

    @Query("DELETE FROM contact_address WHERE contactId = :contactId")
    suspend fun deleteContactById(contactId: String)
}