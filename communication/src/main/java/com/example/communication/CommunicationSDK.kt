package com.example.communication

import android.content.Context
import com.example.communication.room.MyRoomDatabase


object CommunicationSDK {

    fun init(context: Context) {
        MyRoomDatabase.init(context)

    }
}