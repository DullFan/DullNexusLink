package com.example.communication.room.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "call_log", indices = [Index(value = ["phoneNumber"])])
data class CallLogEntity(
    @PrimaryKey val callLogId: Long,
    var contactId: Long,
    val phoneNumber: String,
    val name: String?,
    val timestamp: Long,
    val date: String,
    val time: String,
    val belongPlace: String,
    val netName: String,
    val duration: Int,
    /**
     * Android CallLog.Calls 中的通话类型常量
     * CallLog.Calls.INCOMING_TYPE     值为1，表示呼入电话
     * CallLog.Calls.OUTGOING_TYPE     值为2，表示呼出电话
     * CallLog.Calls.MISSED_TYPE       值为3，表示未接电话
     * CallLog.Calls.VOICEMAIL_TYPE    值为4，表示语音邮件
     * CallLog.Calls.REJECTED_TYPE     值为5，表示拒接电话
     * CallLog.Calls.BLOCKED_TYPE      值为6，表示被屏蔽的电话
     * CallLog.Calls.ANSWERED_EXTERNALLY_TYPE   值为7，表示在其他设备上接听的电话
     */
    val type: Int,
    val simInfo: String
)