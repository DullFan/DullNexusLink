package com.example.communication.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_log")
data class CallLogEntity(
    @PrimaryKey val callLogId: Long,
    val contactId: Long,
    val phoneNumber: String,
    val name: String?,
    val timestamp: Long,
    val date: String,
    val time: String,
    val belongPlace: String,
    val netName: String,
    val duration: Int,
    val type: Int
)