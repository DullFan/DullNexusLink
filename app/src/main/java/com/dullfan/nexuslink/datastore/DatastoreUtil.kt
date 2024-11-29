package com.dullfan.nexuslink.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val dataStoreName = "DullNexusLink"
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = dataStoreName)

/**
 * 最后读取时间
 */
val lastReadTimeKey = longPreferencesKey("lastReadTime")

/**
 * 是否第一次进入App
 */
val firstEnterAppKey = booleanPreferencesKey("firstEnterApp")


suspend fun Context.updateLastReadTime(lastReadTime: Long? = null) {
    dataStore.edit {
        it[lastReadTimeKey] = lastReadTime ?: System.currentTimeMillis()
    }
}

suspend fun Context.getLastReadTime(updateLastTime: (lastTime: Long) -> Unit) {
    dataStore.data.map { it[lastReadTimeKey] }.collect {
        updateLastTime(it ?: 0L)
    }
}

suspend fun Context.getLastReadTimeFirst(updateLastTime: (lastTime: Long) -> Unit) {
    val first = dataStore.data.map { it[lastReadTimeKey] }.first()
    updateLastTime(first ?: 0L)
}

/**
 * 调用这个后就一直为false
 */
suspend fun Context.updateFirstEnterApp() {
    dataStore.edit {
        it[firstEnterAppKey] = false
    }
}

suspend fun Context.isFirstEnterApp(): Boolean {
    return dataStore.data.first()[firstEnterAppKey] ?: true
}
