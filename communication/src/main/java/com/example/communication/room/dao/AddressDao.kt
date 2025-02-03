package com.example.communication.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.communication.room.entity.AddressEntity
import com.example.communication.room.entity.BaseInfoEntity

@Dao
interface AddressDao {

    @Upsert
    suspend fun upsert(vararg addressEntity: AddressEntity)

    @Delete
    suspend fun delete(vararg addressEntity: AddressEntity)

    @Query("select * from contact_address")
    fun findAll(): MutableList<AddressEntity>

    @Query("SELECT * FROM contact_address ORDER BY contactId DESC LIMIT :limit")
    fun findInitialLoadData(limit: Int): MutableList<AddressEntity>

    @Query("SELECT * FROM contact_address WHERE contactId < :contactId")
    fun findByContactIdLessThan(contactId: Long): MutableList<AddressEntity>

    @Query("select * from contact_address where contactId = :contactId")
    fun findByContactId(contactId: String?): MutableList<AddressEntity>?

    @Query("DELETE FROM contact_address WHERE contactId = :contactId")
    suspend fun deleteContactById(contactId: Long)

    @Query("DELETE FROM contact_address WHERE contactId IN (:contactIds)")
    suspend fun deleteByContactIds(contactIds: List<Long>)
}