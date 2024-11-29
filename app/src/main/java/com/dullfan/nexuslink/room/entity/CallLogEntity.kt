package com.dullfan.nexuslink.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_log")
data class CallLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    // 手机号
    val phoneNumber: String,
    // 名称
    val name: String?,
    // 时间戳
    val timestamp: Long,
    // 日期
    val date: String,
    // 时间
    val time: String,
    // 归属地
    val belongPlace: String,
    // 运营商名称
    val netName: String,
    // 通话时长
    val duration: Int,
    // 类型
    val type: Int,
)