package com.example.communication.datastore

import android.content.Context
import android.util.Log

internal object LastUpdateTimeManager {

    internal suspend fun Context.updateCallLogTime(time: Long? = null) {
        val updateTime = time ?: System.currentTimeMillis()
        updateCallLogLastReadTime(updateTime)
    }

    internal suspend fun Context.getCallLogTime(): Long =
        getLastCallLogReadTimeOnce()

    internal suspend fun Context.updateContactTime(time: Long? = null) {
        val updateTime = time ?: System.currentTimeMillis()
        updateContactLastReadTime(updateTime)
    }

    internal suspend fun Context.getContactTime(): Long =
        getContactLastReadTimeOnce()
}