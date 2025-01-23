package com.dullfan.nexuslink

import android.app.Application
import com.example.communication.CommunicationSDK
import com.example.communication.room.MyRoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        CommunicationSDK.init(this@MyApplication)
    }

}