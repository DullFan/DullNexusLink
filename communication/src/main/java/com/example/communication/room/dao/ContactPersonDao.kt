package com.example.communication.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.communication.room.entity.BaseInfoEntity
import com.example.communication.room.entity.CallLogEntity

@Dao
interface ContactPersonDao {
    @Upsert
    suspend fun upsert(vararg contactPersonRoomEntity: BaseInfoEntity)

    @Delete
    fun delete(vararg contactPersonRoomEntity: BaseInfoEntity)

    @Query("select * from base_info")
    fun findAll(): MutableList<BaseInfoEntity>

    @Query("SELECT * FROM base_info ORDER BY contactId DESC LIMIT :limit")
    fun findInitialLoadData(limit: Int): MutableList<BaseInfoEntity>

    @Query("SELECT * FROM base_info WHERE contactId < :contactId")
    fun findByContactIdLessThan(contactId: Long): MutableList<BaseInfoEntity>


    @Query("SELECT * FROM base_info WHERE contactId = :contactId")
    fun findByContactId(contactId:Long): BaseInfoEntity?

    @Query("DELETE FROM base_info WHERE contactId = :contactId")
    fun deleteContactById( contactId: Long)

    @Query("DELETE FROM base_info WHERE contactId IN (:contactIds)")
    suspend fun deleteByContactIds(contactIds: List<Long>)
}