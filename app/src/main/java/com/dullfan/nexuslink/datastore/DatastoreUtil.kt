package com.dullfan.nexuslink.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dullfan.nexuslink.ui.page.recent_calls.CallLogDisplayMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val dataStoreName = "DullNexusLink"
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = dataStoreName)

/**
 * 是否第一次进入App
 */
val firstEnterAppKey = booleanPreferencesKey("firstEnterApp")

val recentCallDisplayModeKey = stringPreferencesKey("recentCallDisplayMode")

suspend fun Context.updateFirstEnterApp() {
    dataStore.edit {
        it[firstEnterAppKey] = false
    }
}

suspend fun Context.isFirstEnterApp(): Boolean {
    return dataStore.data.first()[firstEnterAppKey] ?: true
}

suspend fun Context.saveCallLogDisplayModeAsString(
    mode: CallLogDisplayMode
) {
    dataStore.edit { preferences ->
        preferences[recentCallDisplayModeKey] = mode.name
    }
}

fun Context.getCallLogDisplayModeAsString(): Flow<CallLogDisplayMode> {
    return dataStore.data.map { preferences ->
        val modeName = preferences[recentCallDisplayModeKey] ?: CallLogDisplayMode.TIMELINE.name
        CallLogDisplayMode.valueOf(modeName)
    }
}

