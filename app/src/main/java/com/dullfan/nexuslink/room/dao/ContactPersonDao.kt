package com.dullfan.nexuslink.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.dullfan.nexuslink.room.entity.ContactPersonRoomEntity

@Dao
interface ContactPersonDao {
    @Upsert
    suspend fun upsert(vararg contactPersonRoomEntity: ContactPersonRoomEntity)

    @Delete
    fun delete(vararg contactPersonRoomEntity: ContactPersonRoomEntity)

//    @Query("select * from Student where name like :name")
//    fun findByContactId(name: String?): ContactPersonRoomEntity?

    @Query("select * from contact_person")
    fun findAll(): MutableList<ContactPersonRoomEntity>

    @Query("SELECT * FROM contact_person WHERE contactId = :contactId")
    fun findByContactId(contactId:String): ContactPersonRoomEntity?

    @Query("DELETE FROM contact_person WHERE contactId = :contactId")
    fun deleteByContactId(contactId: String)
}