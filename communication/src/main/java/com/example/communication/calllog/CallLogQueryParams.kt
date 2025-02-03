package com.example.communication.calllog

import android.provider.CallLog

data class CallLogQueryParams(
    val projection: Array<String> = CallLogColumns.BASE_PROJECTION,
    val selection: String? = null,
    val selectionArgs: Array<String>? = null,
    val sortOrder: String = "${CallLog.Calls.DATE} DESC",
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CallLogQueryParams

        if (!projection.contentEquals(other.projection)) return false
        if (selection != other.selection) return false
        if (selectionArgs != null) {
            if (other.selectionArgs == null) return false
            if (!selectionArgs.contentEquals(other.selectionArgs)) return false
        } else if (other.selectionArgs != null) return false
        if (sortOrder != other.sortOrder) return false

        return true
    }

    override fun hashCode(): Int {
        var result = projection.contentHashCode()
        result = 31 * result + (selection?.hashCode() ?: 0)
        result = 31 * result + (selectionArgs?.contentHashCode() ?: 0)
        result = 31 * result + sortOrder.hashCode()
        return result
    }
}
