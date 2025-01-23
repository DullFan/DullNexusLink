package com.dullfan.nexuslink.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private const val dataStoreName = "DullNexusLink"
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = dataStoreName)

/**
 * 是否第一次进入App
 */
val firstEnterAppKey = booleanPreferencesKey("firstEnterApp")

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
