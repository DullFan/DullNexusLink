package com.dullfan.nexuslink.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.dullfan.nexuslink.room.entity.ContactPersonRoomEntity
import com.dullfan.nexuslink.room.entity.OrganizationsEntity
import com.dullfan.nexuslink.room.entity.PhoneEntity

@Dao
interface PhoneDao {
    @Upsert
    suspend fun upsert(vararg phoneEntity: PhoneEntity)

    @Delete
    fun delete(vararg phoneEntity: PhoneEntity)

    @Query("select * from contact_phone where contactId = :contactId")
    fun findByContactId(contactId: String?): MutableList<PhoneEntity>?

    @Query("DELETE FROM contact_phone WHERE contactId = :contactId")
    suspend fun deleteContactById(contactId: String)
}