package com.example.communication.calllog

import android.content.ContentResolver
import android.database.Cursor
import android.provider.CallLog


interface CallLogQuery {
    fun buildQueryParams(): CallLogQueryParams

    suspend fun execute(contentResolver: ContentResolver): Cursor? {
        val params = buildQueryParams()
        return contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            params.projection,
            params.selection,
            params.selectionArgs,
            params.sortOrder
        )
    }
}

object CallLogColumns {
    val BASE_PROJECTION = arrayOf(
        CallLog.Calls.CACHED_NAME,  // 通话记录的联系人
        CallLog.Calls._ID,  // 通话记录的ID
        CallLog.Calls.NUMBER,  // 通话记录的电话号码
        CallLog.Calls.DATE,  // 通话记录的日期
        CallLog.Calls.DURATION,  // 通话时长
        CallLog.Calls.TYPE, // 通话类型
        CallLog.Calls.PHONE_ACCOUNT_ID // 手机卡
    )

    val ID_PROJECTION = arrayOf(CallLog.Calls._ID)
}


class DefaultCallLogQuery() : CallLogQuery {
    override fun buildQueryParams() = CallLogQueryParams()
}

class PhoneNumberCallLogQuery(private val phoneNumber: String) : CallLogQuery {
    override fun buildQueryParams() = CallLogQueryParams(
        selection = "${CallLog.Calls.NUMBER} = ?",
        selectionArgs = arrayOf(phoneNumber)
    )
}

class AllIdCallLogQuery : CallLogQuery {
    override fun buildQueryParams() = CallLogQueryParams(
        projection = CallLogColumns.ID_PROJECTION
    )
}

class AfterTimestampCallLogQuery(private val timestamp: Long) : CallLogQuery {
    override fun buildQueryParams() = CallLogQueryParams(
        selection = "${CallLog.Calls.DATE} > ?",
        selectionArgs = arrayOf(timestamp.toString())
    )
}
