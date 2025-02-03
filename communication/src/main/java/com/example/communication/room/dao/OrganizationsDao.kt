package com.example.communication.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.communication.room.entity.IMEntity
import com.example.communication.room.entity.OrganizationsEntity

@Dao
interface OrganizationsDao {

    @Upsert
    suspend fun upsert(vararg organizationsEntity: OrganizationsEntity)

    @Delete
    fun delete(vararg organizationsEntity: OrganizationsEntity)

    @Query("select * from contact_organizations")
    fun findAll(): MutableList<OrganizationsEntity>

    @Query("SELECT * FROM contact_organizations ORDER BY contactId DESC LIMIT :limit")
    fun findInitialLoadData(limit: Int): MutableList<OrganizationsEntity>

    @Query("SELECT * FROM contact_organizations WHERE contactId < :contactId")
    fun findByContactIdLessThan(contactId: Long): MutableList<OrganizationsEntity>

    @Query("select * from contact_organizations where contactId = :contactId")
    fun findByContactId(contactId: Long?): MutableList<OrganizationsEntity>?

    @Query("DELETE FROM contact_organizations WHERE contactId = :contactId")
    suspend fun deleteContactById( contactId: Long)

    @Query("DELETE FROM contact_organizations WHERE contactId IN (:contactIds)")
    suspend fun deleteByContactIds(contactIds: List<Long>)

}