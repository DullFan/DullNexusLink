package com.example.communication.utils

import android.database.Cursor

object ExtendUtils {

    fun Cursor.getStringValue(columnName: String, defString: String = ""): String {
        return try {
            val index = getColumnIndex(columnName)
            if (index != -1 && !isNull(index)) getString(index) ?: defString else defString
        } catch (e: Exception) {
            defString
        }
    }

    fun Cursor.getIntValue(columnName: String): Int {
        return try {
            val index = getColumnIndex(columnName)
            if (index != -1 && !isNull(index)) getInt(index) else 0
        } catch (e: Exception) {
            0
        }
    }

    fun Cursor.getLongValue(columnName: String): Long {
        return try {
            val index = getColumnIndex(columnName)
            if (index != -1 && !isNull(index)) getLong(index) else 0L
        } catch (e: Exception) {
            0L
        }
    }
}