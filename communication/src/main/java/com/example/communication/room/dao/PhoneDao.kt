package com.example.communication.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.communication.room.entity.OrganizationsEntity
import com.example.communication.room.entity.PhoneEntity

@Dao
interface PhoneDao {
    @Upsert
    suspend fun upsert(vararg phoneEntity: PhoneEntity)

    @Delete
    fun delete(vararg phoneEntity: PhoneEntity)

    @Query("select * from contact_phone")
    fun findAll(): MutableList<PhoneEntity>

    @Query("SELECT * FROM contact_phone ORDER BY contactId DESC LIMIT :limit")
    fun findInitialLoadData(limit: Int): MutableList<PhoneEntity>

    @Query("SELECT * FROM contact_phone WHERE contactId < :contactId")
    fun findByContactIdLessThan(contactId: Long): MutableList<PhoneEntity>

    @Query("select * from contact_phone where contactId = :contactId")
    fun findByContactId(contactId: Long?): MutableList<PhoneEntity>?

    @Query("DELETE FROM contact_phone WHERE contactId = :contactId")
    suspend fun deleteContactById( contactId: Long)

    @Query("DELETE FROM contact_phone WHERE contactId IN (:contactIds)")
    suspend fun deleteByContactIds(contactIds: List<Long>)

}