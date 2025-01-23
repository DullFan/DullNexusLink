package com.example.communication.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.communication.room.dao.AddressDao
import com.example.communication.room.dao.CallLogDao
import com.example.communication.room.dao.ContactPersonDao
import com.example.communication.room.dao.EmailDao
import com.example.communication.room.dao.EventDao
import com.example.communication.room.dao.IMDao
import com.example.communication.room.dao.OrganizationsDao
import com.example.communication.room.dao.PhoneDao
import com.example.communication.room.dao.WebsiteDao
import com.example.communication.room.converter.BitmapConverter
import com.example.communication.room.entity.AddressEntity
import com.example.communication.room.entity.BaseInfoEntity
import com.example.communication.room.entity.CallLogEntity
import com.example.communication.room.entity.EmailEntity
import com.example.communication.room.entity.EventEntity
import com.example.communication.room.entity.IMEntity
import com.example.communication.room.entity.OrganizationsEntity
import com.example.communication.room.entity.PhoneEntity
import com.example.communication.room.entity.WebsiteEntity

@Database(
    entities = [
        CallLogEntity::class,
        BaseInfoEntity::class,
        WebsiteEntity::class,
        EventEntity::class,
        OrganizationsEntity::class,
        AddressEntity::class,
        EmailEntity::class,
        IMEntity::class,
        PhoneEntity::class,
    ], version = 1
)
@TypeConverters(BitmapConverter::class)
abstract class MyRoomDatabase : RoomDatabase() {
    abstract fun addressDao(): AddressDao
    abstract fun contactPersonDao(): ContactPersonDao
    abstract fun emailDao(): EmailDao
    abstract fun eventDao(): EventDao
    abstract fun iMDao(): IMDao
    abstract fun organizationsDao(): OrganizationsDao
    abstract fun phoneDao(): PhoneDao
    abstract fun websiteDao(): WebsiteDao
    abstract fun callLogDao(): CallLogDao

    //创建单例
    companion object {
        private var instance: MyRoomDatabase? = null

        @Synchronized
        fun init(context: Context): MyRoomDatabase {
            return instance ?: Room.databaseBuilder(
                context.applicationContext, MyRoomDatabase::class.java, "dull_fan"
            ).build().apply { instance = this }
        }

        @Synchronized
        fun getMyRoomDatabase(): MyRoomDatabase {
            if(instance == null){
                throw IllegalStateException("Instance cannot be null")
            }
            return instance!!
        }
    }
}