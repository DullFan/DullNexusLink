package com.example.communication.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit

private const val dataStoreName = "Communication"
private val Context.datastore: DataStore<Preferences> by preferencesDataStore(name = dataStoreName)

/**
 * 通话记录最后读取时间
 */
private val callLogLastReadTimeKey = longPreferencesKey("callLogLastReadTime")

/**
 * 联系人最后读取时间
 */
private val contactLastReadTimeKey = longPreferencesKey("contactLastReadTime")

/**
 * 更新通话记录最后读取时间
 */
internal suspend fun Context.updateCallLogLastReadTime(lastReadTime: Long? = null) {
    datastore.edit {
        it[callLogLastReadTimeKey] = lastReadTime ?: System.currentTimeMillis()
    }
}

/**
 * 监听通话记录最后读取时间
 */
internal suspend fun Context.observeCallLogLastReadTime(updateLastTime: (lastTime: Long) -> Unit) {
    datastore.data.map { it[callLogLastReadTimeKey] }.collect {
        updateLastTime(it ?: 0L)
    }
}

/**
 * 获取通话记录最后读取时间
 */
internal suspend fun Context.getLastCallLogReadTimeOnce(): Long {
    val first = datastore.data.map { it[callLogLastReadTimeKey] }.first() ?: 0L
    return first
}

/**
 * 更新联系人最后读取时间
 */
internal suspend fun Context.updateContactLastReadTime(lastReadTime: Long? = null) {
    datastore.edit {
        it[contactLastReadTimeKey] = lastReadTime ?: System.currentTimeMillis()
    }
}

/**
 * 监听联系人最后读取时间
 */
internal suspend fun Context.observeContactLastReadTime(updateLastTime: (lastTime: Long) -> Unit) {
    datastore.data.map { it[contactLastReadTimeKey] }.collect {
        updateLastTime(it ?: 0L)
    }
}

/**
 * 获取联系人最后读取时间
 */
internal suspend fun Context.getContactLastReadTimeOnce(): Long {
    val first = datastore.data.map { it[contactLastReadTimeKey] }.first() ?: 0
    return first
}
