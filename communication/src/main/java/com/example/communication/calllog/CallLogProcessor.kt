package com.example.communication.calllog

import android.content.Context
import android.database.Cursor
import android.provider.CallLog
import com.example.communication.calllog.CallLogUtil.getSimInfo
import com.example.communication.contact.ContactManager
import com.example.communication.utils.ExtendUtils.getIntValue
import com.example.communication.utils.ExtendUtils.getLongValue
import com.example.communication.utils.ExtendUtils.getStringValue
import com.example.communication.room.entity.CallLogEntity
import com.example.communication.utils.SDKConstants.INITIAL_LOAD_SIZE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

interface CallLogProcessor {
    suspend fun process(cursor: Cursor?, context: Context): MutableList<CallLogEntity>
}

object CallLogProcessorFactory {
    fun create(
        type: QueryType,
        onResult: suspend (MutableList<CallLogEntity>) -> Unit = {}
    ): CallLogProcessor = when (type) {
        QueryType.DEFAULT -> DefaultCallLogProcessor()
        QueryType.BY_PHONE -> SingleCallLogProcessor()
        QueryType.ALL_IDS -> IdOnlyCallLogProcessor()
        QueryType.AFTER_TIME -> TimeBasedCallLogProcessor()
        QueryType.INIT -> InitCallLogProcessor(onResult)
    }
}

abstract class BaseCallLogProcessor : CallLogProcessor {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    protected fun createCallLogEntity(
        cursor: Cursor, context: Context, contactId: Long = 0L
    ): CallLogEntity {
        val dateLong = cursor.getLongValue(CallLog.Calls.DATE)
        val date = Date(dateLong)

        return CallLogEntity(
            contactId = contactId,
            callLogId = cursor.getLongValue(CallLog.Calls._ID),
            phoneNumber = cursor.getStringValue(CallLog.Calls.NUMBER),
            name = cursor.getStringValue(CallLog.Calls.CACHED_NAME),
            timestamp = dateLong,
            date = dateFormat.format(date),
            time = timeFormat.format(date),
            belongPlace = "",
            netName = "",
            duration = cursor.getIntValue(CallLog.Calls.DURATION),
            type = cursor.getIntValue(CallLog.Calls.TYPE),
            simInfo = getSimInfo(context, cursor.getStringValue(CallLog.Calls.PHONE_ACCOUNT_ID))
        )
    }
}


/**
 * 默认处理器
 */
class DefaultCallLogProcessor : BaseCallLogProcessor() {
    override suspend fun process(
        cursor: Cursor?,
        context: Context
    ): MutableList<CallLogEntity> = withContext(Dispatchers.IO) {
        val list = mutableListOf<CallLogEntity>()
        if (cursor == null) return@withContext list

        val contactManager = ContactManager.getInstance(context)
        val contactIdMap = contactManager.getContactIdsWithPhoneNumbers()

        cursor.use {
            while (cursor.moveToNext()) {
                val number = cursor.getStringValue(CallLog.Calls.NUMBER)
                val entity = createCallLogEntity(
                    cursor = cursor,
                    context = context,
                    contactId = contactIdMap[number] ?: 0L
                )
                list.add(entity)
            }
        }

        list
    }
}

class InitCallLogProcessor(private val onResult: suspend (MutableList<CallLogEntity>) -> Unit) :
    BaseCallLogProcessor() {
    override suspend fun process(
        cursor: Cursor?,
        context: Context
    ): MutableList<CallLogEntity> = withContext(Dispatchers.IO) {
        var list = mutableListOf<CallLogEntity>()
        if (cursor == null) return@withContext list

        val contactManager = ContactManager.getInstance(context)
        val contactIdMap = contactManager.getContactIdsWithPhoneNumbers()

        cursor.use {
            while (cursor.moveToNext()) {
                val number = cursor.getStringValue(CallLog.Calls.NUMBER)
                val entity = createCallLogEntity(
                    cursor = cursor,
                    context = context,
                    contactId = contactIdMap[number] ?: 0L
                )
                list.add(entity)
                if (list.size == INITIAL_LOAD_SIZE) {
                    onResult(list)
                    list = mutableListOf()
                }
            }
        }
        onResult(list)
        list
    }
}

/**
 * 单条记录处理器
 */
class SingleCallLogProcessor : BaseCallLogProcessor() {
    override suspend fun process(
        cursor: Cursor?, context: Context
    ): MutableList<CallLogEntity> = withContext(Dispatchers.IO) {
        val list = mutableListOf<CallLogEntity>()
        if (cursor == null) return@withContext list
        val contactManager = ContactManager.getInstance(context)
        cursor.use {
            while (cursor.moveToNext()) {
                val number = cursor.getStringValue(CallLog.Calls.NUMBER)
                val contactId = contactManager.getContactIdByPhone(number)
                list.add(createCallLogEntity(cursor, context, contactId))
            }
        }

        list
    }
}

/**
 * ID处理器
 */
class IdOnlyCallLogProcessor : CallLogProcessor {
    override suspend fun process(
        cursor: Cursor?, context: Context
    ): MutableList<CallLogEntity> = withContext(Dispatchers.IO) {
        val list = mutableListOf<CallLogEntity>()
        cursor?.use {
            while (cursor.moveToNext()) {
                list.add(
                    CallLogEntity(
                        callLogId = cursor.getLongValue(CallLog.Calls._ID),
                        contactId = 0L,
                        phoneNumber = "",
                        name = "",
                        timestamp = 0L,
                        date = "",
                        time = "",
                        belongPlace = "",
                        netName = "",
                        duration = 0,
                        type = 0,
                        simInfo = ""
                    )
                )
            }
        }
        list
    }
}

/**
 * 时间查询处理器
 */
class TimeBasedCallLogProcessor : BaseCallLogProcessor() {
    override suspend fun process(
        cursor: Cursor?, context: Context
    ): MutableList<CallLogEntity> = withContext(Dispatchers.IO) {
        val list = mutableListOf<CallLogEntity>()
        if (cursor == null) return@withContext list
        val contactManager = ContactManager.getInstance(context)
        cursor.use {
            while (cursor.moveToNext()) {
                list.add(createCallLogEntity(cursor, context))
            }

            // 处理联系人ID
            val contactIdMap = if (list.size > INITIAL_LOAD_SIZE) {
                contactManager.getContactIdsWithPhoneNumbers()
            } else {
                list.map { it.phoneNumber }.distinct().associateWith { phoneNumber ->
                    contactManager.getContactIdByPhone(phoneNumber)
                }
            }

            // 更新联系人ID
            list.forEach { entity ->
                entity.contactId = contactIdMap[entity.phoneNumber] ?: 0L
            }
        }

        list
    }
}


