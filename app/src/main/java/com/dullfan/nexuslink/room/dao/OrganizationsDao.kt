package com.dullfan.nexuslink.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.dullfan.nexuslink.room.entity.ContactPersonRoomEntity
import com.dullfan.nexuslink.room.entity.IMEntity
import com.dullfan.nexuslink.room.entity.OrganizationsEntity

@Dao
interface OrganizationsDao {

    @Upsert
    suspend fun upsert(vararg organizationsEntity: OrganizationsEntity)

    @Delete
    fun delete(vararg organizationsEntity: OrganizationsEntity)

    @Query("select * from contact_organizations where contactId = :contactId")
    fun findByContactId(contactId: String?): MutableList<OrganizationsEntity>?

    @Query("DELETE FROM contact_organizations WHERE contactId = :contactId")
    suspend fun deleteContactById(contactId: String)
}