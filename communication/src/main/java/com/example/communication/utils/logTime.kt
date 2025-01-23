package com.example.communication.utils

import android.util.Log

inline fun <T> logTime(tag: String, block: () -> T): T {
    val start = System.currentTimeMillis()
    return block().also {
        val time = System.currentTimeMillis() - start
        Log.e("TAG", "$tag: $time ms")
    }
}