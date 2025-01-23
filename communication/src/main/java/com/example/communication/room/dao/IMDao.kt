package com.example.communication.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.communication.room.entity.IMEntity

@Dao
interface IMDao {

    @Upsert
    suspend fun upsert(vararg imEntity: IMEntity)

    @Delete
    fun delete(vararg imEntity: IMEntity)

    @Query("select * from contact_im")
    fun findAll(): MutableList<IMEntity>

    @Query("select * from contact_im where contactId = :contactId")
    fun findByContactId(contactId: Long?): MutableList<IMEntity>?

    @Query("DELETE FROM contact_im WHERE contactId = :contactId")
    suspend fun deleteContactById( contactId: Long)

    @Query("DELETE FROM contact_im WHERE contactId IN (:contactIds)")
    suspend fun deleteByContactIds(contactIds: List<Long>)

}