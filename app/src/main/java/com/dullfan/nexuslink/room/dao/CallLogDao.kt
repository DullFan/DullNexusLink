package com.dullfan.nexuslink.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.dullfan.nexuslink.room.entity.CallLogEntity
import com.dullfan.nexuslink.room.entity.ContactPersonRoomEntity

@Dao
interface CallLogDao {
    @Upsert
    suspend fun upsert(vararg callLogEntity: CallLogEntity)

    @Delete
    fun delete(vararg callLogEntity: CallLogEntity)

//    @Query("select * from Student where name like :name")
//    fun findByContactId(name: String?): ContactPersonRoomEntity?

    @Query("select * from call_log")
    fun findAll(): MutableList<CallLogEntity>

    @Query("SELECT * FROM call_log WHERE id = :id")
    fun findById(id:Int): CallLogEntity?
}