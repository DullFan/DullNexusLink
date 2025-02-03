package com.example.communication.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.communication.room.entity.AddressEntity
import com.example.communication.room.entity.EmailEntity

@Dao
interface EmailDao {
    @Upsert
    suspend fun upsert(vararg emailEntity: EmailEntity)

    @Delete
    fun delete(vararg emailEntity: EmailEntity)

    @Query("select * from contact_email")
    fun findAll(): MutableList<EmailEntity>

    @Query("SELECT * FROM contact_email ORDER BY contactId DESC LIMIT :limit")
    fun findInitialLoadData(limit: Int): MutableList<EmailEntity>

    @Query("SELECT * FROM contact_email WHERE contactId < :contactId")
    fun findByContactIdLessThan(contactId: Long): MutableList<EmailEntity>


    @Query("select * from contact_email where contactId = :contactId")
    fun findByContactId(contactId: Long?): MutableList<EmailEntity>?

    @Query("DELETE FROM contact_email WHERE contactId = :contactId")
    suspend fun deleteContactById( contactId: Long)

    @Query("DELETE FROM contact_email WHERE contactId IN (:contactIds)")
    suspend fun deleteByContactIds(contactIds: List<Long>)
}