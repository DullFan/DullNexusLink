package com.example.communication.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.communication.room.entity.CallLogEntity

@Dao
interface CallLogDao {
    @Upsert
    suspend fun upsert(vararg callLogEntity: CallLogEntity)

    @Delete
    fun delete(vararg callLogEntity: CallLogEntity)

    @Query("select * from call_log")
    fun findAll(): MutableList<CallLogEntity>

    @Query("SELECT * FROM call_log WHERE callLogId = :callLogId")
    fun findById(callLogId: Long): CallLogEntity?

    @Query("DELETE FROM call_log WHERE callLogId = :callLogId")
    fun deleteByContactId(callLogId: Long)

    @Query("DELETE FROM call_log WHERE callLogId IN (:callLogIds)")
    suspend fun deleteByCallLogs(callLogIds: List<Long>)
}