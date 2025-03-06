package com.dullfan.nexuslink.utils

import android.content.Context
import android.util.Log
import com.dullfan.nexuslink.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun String.log() {
    Log.e(Constant.TAG, this)
}

fun formatTimestamp(context: Context, timestamp: Long): String {
    val dateTime = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault())
    val date = dateTime.toLocalDate()
    val today = LocalDate.now()
    val startOfWeek = today.with(java.time.DayOfWeek.MONDAY)
    val startOfYear = today.withDayOfYear(1)

    return when {
        date.isEqual(today) -> {
            val timeFormatter = DateTimeFormatter.ofPattern(context.getString(R.string.time_today))
            dateTime.toLocalTime().format(timeFormatter)
        }

        date.isAfter(startOfWeek) -> {
            val dayOfWeekFormatter = DateTimeFormatter.ofPattern(
                context.getString(R.string.time_this_week),
                Locale.getDefault()
            )
            dateTime.format(dayOfWeekFormatter)
        }

        date.isAfter(startOfYear) -> {
            val dateFormatter = DateTimeFormatter.ofPattern(
                context.getString(R.string.date_this_year),
                Locale.getDefault()
            )
            dateTime.format(dateFormatter)
        }

        else -> {
            val fullDateFormatter = DateTimeFormatter.ofPattern(
                context.getString(R.string.date_other_years),
                Locale.getDefault()
            )
            dateTime.format(fullDateFormatter)
        }
    }
}