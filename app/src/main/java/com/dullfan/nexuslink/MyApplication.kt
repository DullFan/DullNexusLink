package com.dullfan.nexuslink

import android.app.Application
import android.util.Log
import androidx.lifecycle.LifecycleObserver
import com.dullfan.nexuslink.room.MyRoomDatabase
import com.dullfan.nexuslink.ui.page.main.MainViewModel

class MyApplication() : Application() {

    override fun onCreate() {
        super.onCreate()
        MyRoomDatabase.init(this)

    }
}
