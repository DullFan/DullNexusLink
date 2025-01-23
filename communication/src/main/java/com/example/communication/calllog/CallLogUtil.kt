package com.example.communication.calllog

import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.CallLog
import android.util.Log
import com.example.communication.calllog.CallLogHelper.getCarrier
import com.example.communication.calllog.CallLogHelper.getGen
import com.example.communication.contact.getContactIdsWithPhoneNumbers
import com.example.communication.datastore.LastUpdateTimeManager.getCallLogTime
import com.example.communication.room.entity.CallLogEntity
import com.example.communication.utils.SDKConstants.FIRST_COUNT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 通话记录工具类
 */
object CallLogUtil {

    /**
     * 根据号码查询通话记录
     */
    fun getCallLogByNumber(contentResolver: ContentResolver, number: String): Cursor? {
        val projection = arrayOf(
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.NUMBER,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
            CallLog.Calls.TYPE
        )

        val selection = "${CallLog.Calls.NUMBER} = ?"
        val selectionArgs = arrayOf(number)

        return contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            CallLog.Calls.DEFAULT_SORT_ORDER
        )
    }

    /**
     * 获取所有通话记录
     */
    suspend fun ContentResolver.newTest(
        updateData: suspend (MutableList<CallLogEntity>) -> Unit
    ) {
        val cursor = queryCallLogs()
        val list = mutableListOf<CallLogEntity>()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

        // 获取联系人ID映射
        val contactIdMap = withContext(Dispatchers.IO) {
            getContactIdsWithPhoneNumbers()
        }

        cursor?.use {
            val idIndex = cursor.getColumnIndex(CallLog.Calls._ID)
            val nameIndex = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
            val numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER)
            val dateIndex = cursor.getColumnIndex(CallLog.Calls.DATE)
            val durationIndex = cursor.getColumnIndex(CallLog.Calls.DURATION)
            val typeIndex = cursor.getColumnIndex(CallLog.Calls.TYPE)

            val uniqueNumbers = mutableSetOf<String>()
            while (cursor.moveToNext()) {
                cursor.getString(numberIndex)?.let { uniqueNumbers.add(it) }
            }

            cursor.moveToPosition(-1)

            var firstBatchSent = false
            while (cursor.moveToNext()) {
                val callLogId = cursor.getLong(idIndex)
                val name = cursor.getString(nameIndex)
                val number = cursor.getString(numberIndex)
                val dateLong = cursor.getLong(dateIndex)
                val date = Date(dateLong)

                list.add(
                    CallLogEntity(
                        contactId = contactIdMap[number] ?: 0L,
                        callLogId = callLogId,
                        phoneNumber = number,
                        name = name,
                        timestamp = dateLong,
                        date = dateFormat.format(date),
                        time = timeFormat.format(date),
                        belongPlace =  "",
                        netName =  "",
                        duration = cursor.getInt(durationIndex),
                        type = cursor.getInt(typeIndex),
                    )
                )

                // 当达到FIRST_COUNT时，发送第一批数据
                if (list.size == FIRST_COUNT && !firstBatchSent) {
                    firstBatchSent = true
                    withContext(Dispatchers.Main) {
                        updateData(list.toMutableList())
                    }
                }
            }

            if (list.size > FIRST_COUNT) {
                updateData(list)
            } else if (!firstBatchSent) {
                // 如果数据总量小于FIRST_COUNT且还没发送过，则发送所有数据
                updateData(list)
            }
        }
    }


    /**
     * 后续
     */
    suspend fun ContentResolver.newSecondTest(
        context: Context,
        updateData: suspend (MutableList<CallLogEntity>) -> Unit
    ) {
        val cursor = queryCallLogs()
        val list = mutableListOf<CallLogEntity>()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())



        // 获取联系人ID映射
        val contactIdMap = withContext(Dispatchers.IO) {
            getContactIdsWithPhoneNumbers()
        }

        cursor?.use {
            val idIndex = cursor.getColumnIndex(CallLog.Calls._ID)
            val nameIndex = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
            val numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER)
            val dateIndex = cursor.getColumnIndex(CallLog.Calls.DATE)
            val durationIndex = cursor.getColumnIndex(CallLog.Calls.DURATION)
            val typeIndex = cursor.getColumnIndex(CallLog.Calls.TYPE)

            val uniqueNumbers = mutableSetOf<String>()
            while (cursor.moveToNext()) {
                cursor.getString(numberIndex)?.let { uniqueNumbers.add(it) }
            }

            cursor.moveToPosition(-1)

            var firstBatchSent = false
            while (cursor.moveToNext()) {
                val callLogId = cursor.getLong(idIndex)
                val name = cursor.getString(nameIndex)
                val number = cursor.getString(numberIndex)
                val dateLong = cursor.getLong(dateIndex)
                val date = Date(dateLong)

                list.add(
                    CallLogEntity(
                        contactId = contactIdMap[number] ?: 0L,
                        callLogId = callLogId,
                        phoneNumber = number,
                        name = name,
                        timestamp = dateLong,
                        date = dateFormat.format(date),
                        time = timeFormat.format(date),
                        belongPlace =  "",
                        netName =  "",
                        duration = cursor.getInt(durationIndex),
                        type = cursor.getInt(typeIndex),
                    )
                )

                // 当达到FIRST_COUNT时，发送第一批数据
                if (list.size == FIRST_COUNT && !firstBatchSent) {
                    firstBatchSent = true
                    withContext(Dispatchers.Main) {
                        updateData(list.toMutableList())
                    }
                }
            }

            if (list.size > FIRST_COUNT) {
                updateData(list)
            } else if (!firstBatchSent) {
                // 如果数据总量小于FIRST_COUNT且还没发送过，则发送所有数据
                updateData(list)
            }
        }
    }


    /**
     * 获取所有通话记录
     */
    suspend fun ContentResolver.queryCallLog(
    ): MutableList<CallLogEntity> {
        val cursor = queryCallLogs()
        return queryData(cursor, FIRST_COUNT)
    }

    /**
     * 查询所有通话记录ID
     */
    suspend fun ContentResolver.queryCallLogId(): MutableList<Long> {
        val callLogIdList = mutableListOf<Long>()
        val cursor = queryCallLogs()
        cursor?.let {
            queryDataId(cursor, callLogIdList)
        }
        return callLogIdList
    }

    /**
     * 获取指定手机号的通话记录
     */
    suspend fun ContentResolver.queryCallLogByPhoneNumber(
        phoneNumber: String, onResult: suspend (list: MutableList<CallLogEntity>) -> Unit
    ): MutableList<CallLogEntity> {
        val selection = "${CallLog.Calls.NUMBER} = ?"
        val cursor = queryCallLogs(selection, arrayOf(phoneNumber))
        return queryData(cursor)
    }

    private fun ContentResolver.queryCallLogs(
        selection: String? = null, selectionArgs: Array<String>? = null
    ): Cursor? {
        val columns = arrayOf(
            CallLog.Calls.CACHED_NAME,  // 通话记录的联系人
            CallLog.Calls._ID,  // 通话记录的ID
            CallLog.Calls.NUMBER,  // 通话记录的电话号码
            CallLog.Calls.DATE,  // 通话记录的日期
            CallLog.Calls.DURATION,  // 通话时长
            CallLog.Calls.TYPE // 通话类型
        )

        return query(
            CallLog.Calls.CONTENT_URI,
            columns,
            selection,
            selectionArgs,
            "${CallLog.Calls.DATE} DESC"
        )
    }

    private suspend fun ContentResolver.queryData(
        cursor: Cursor?,
        maxCount: Int? = null
    ): MutableList<CallLogEntity> {
        val list = mutableListOf<CallLogEntity>()
        if (cursor == null) return list

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

        // 获取联系人ID映射
        val contactIdMap = withContext(Dispatchers.IO) {
            getContactIdsWithPhoneNumbers()
        }

        cursor.use {
            val idIndex = cursor.getColumnIndex(CallLog.Calls._ID)
            val nameIndex = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
            val numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER)
            val dateIndex = cursor.getColumnIndex(CallLog.Calls.DATE)
            val durationIndex = cursor.getColumnIndex(CallLog.Calls.DURATION)
            val typeIndex = cursor.getColumnIndex(CallLog.Calls.TYPE)

            val uniqueNumbers = mutableSetOf<String>()
            while (cursor.moveToNext()) {
                cursor.getString(numberIndex)?.let { uniqueNumbers.add(it) }
            }

            val carrierCache = uniqueNumbers.associateWith { getCarrier(it) }
            val genCache = uniqueNumbers.associateWith { getGen(it) }

            cursor.moveToPosition(-1)

            val condition = if (maxCount != null) {
                { cursor.moveToNext() && list.size < maxCount }
            } else {
                { cursor.moveToNext() }
            }

            while (condition.invoke()) {
                val callLogId = cursor.getLong(idIndex)
                val name = cursor.getString(nameIndex)
                val number = cursor.getString(numberIndex)
                val dateLong = cursor.getLong(dateIndex)
                val date = Date(dateLong)

                list.add(
                    CallLogEntity(
                        contactId = contactIdMap[number] ?: 0L,
                        callLogId = callLogId,
                        phoneNumber = number,
                        name = name,
                        timestamp = dateLong,
                        date = dateFormat.format(date),
                        time = timeFormat.format(date),
                        belongPlace = carrierCache[number] ?: "",
                        netName = genCache[number] ?: "",
                        duration = cursor.getInt(durationIndex),
                        type = cursor.getInt(typeIndex),
                    )
                )
            }
        }
        return list
    }

    private fun queryDataId(
        cursor: Cursor, ids: MutableList<Long>
    ) {
        val idIndex = cursor.getColumnIndex(CallLog.Calls._ID)  // 获取通话记录ID

        while (cursor.moveToNext()) {
            // 获取通话记录的ID
            val id = cursor.getLong(idIndex)
            ids.add(id)
        }

        cursor.close()
    }

    /**
     * 获取指定时间戳以后的通话记录
     */
    suspend fun ContentResolver.queryCallLogAfterTimestamp(
        timestamp: Long,
    ): MutableList<CallLogEntity> {
        val selection = "${CallLog.Calls.DATE} > ?"
        val selectionArgs = arrayOf(timestamp.toString())
        val cursor = queryCallLogs(selection, selectionArgs)
        val queryData = queryData(cursor)
        return queryData
    }

    /**
     * 获取指定时间戳之前的通话记录
     */
    suspend fun ContentResolver.queryCallLogBeforeTimestamp(
        timestamp: Long,
    ): MutableList<CallLogEntity> {
        val selection = "${CallLog.Calls.DATE} < ?"
        val selectionArgs = arrayOf(timestamp.toString())
        val cursor = queryCallLogs(selection, selectionArgs)
        return queryData(cursor)
    }

    /**
     * 删除通话记录
     */
    suspend fun ContentResolver.deleteCallLog(callLogEntity: CallLogEntity) {
        val uri: Uri = CallLog.Calls.CONTENT_URI
        val selection = "${CallLog.Calls._ID} = ?"
        val selectionArgs = arrayOf(callLogEntity.callLogId.toString())

        val rowsDeleted = delete(uri, selection, selectionArgs)
        if (rowsDeleted > 0) {
            Log.e("TAG", "删除成功: ${callLogEntity.callLogId}")
        } else {
            Log.e("TAG", "删除失败或未找到: ${callLogEntity.callLogId}")
        }
    }

    /**
     * 实时监听通话记录
     */
    fun ContentResolver.observeCallLog(context: Context): Flow<MutableList<CallLogEntity>> =
        callbackFlow {
            val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    super.onChange(selfChange)
                    CoroutineScope(Dispatchers.IO).launch {
                        val queryCallLogAfterTimestamp =
                            queryCallLogAfterTimestamp(context.getCallLogTime())
                        withContext(Dispatchers.Main) {
                            trySend(queryCallLogAfterTimestamp)
                        }
                    }
                }
            }
            // 注册 ContentObserver 来监听通话记录的变化
            registerContentObserver(CallLog.Calls.CONTENT_URI, true, observer)
            // 在 Flow 结束时注销 ContentObserver
            awaitClose { unregisterContentObserver(observer) }
        }.conflate()
}