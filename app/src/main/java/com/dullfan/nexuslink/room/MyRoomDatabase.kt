package com.dullfan.nexuslink.room

import android.app.Application
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dullfan.nexuslink.room.dao.AddressDao
import com.dullfan.nexuslink.room.dao.CallLogDao
import com.dullfan.nexuslink.room.dao.ContactPersonDao
import com.dullfan.nexuslink.room.dao.EmailDao
import com.dullfan.nexuslink.room.dao.EventDao
import com.dullfan.nexuslink.room.dao.IMDao
import com.dullfan.nexuslink.room.dao.OrganizationsDao
import com.dullfan.nexuslink.room.dao.PhoneDao
import com.dullfan.nexuslink.room.dao.WebsiteDao
import com.dullfan.nexuslink.room.entity.AddressEntity
import com.dullfan.nexuslink.room.entity.CallLogEntity
import com.dullfan.nexuslink.room.entity.ContactPersonRoomEntity
import com.dullfan.nexuslink.room.entity.EmailEntity
import com.dullfan.nexuslink.room.entity.EventEntity
import com.dullfan.nexuslink.room.entity.IMEntity
import com.dullfan.nexuslink.room.entity.OrganizationsEntity
import com.dullfan.nexuslink.room.entity.PhoneEntity
import com.dullfan.nexuslink.room.entity.WebsiteEntity
import com.dullfan.nexuslink.utils.BitmapConverter

@Database(
    entities = [
        CallLogEntity::class,
        ContactPersonRoomEntity::class,
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
abstract class MyRoomDatabase() : RoomDatabase() {
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
            return instance!!
        }
    }
}