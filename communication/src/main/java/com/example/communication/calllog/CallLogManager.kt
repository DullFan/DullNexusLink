package com.example.communication.calllog

import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.os.Looper
import android.provider.CallLog
import android.util.Log
import com.example.communication.datastore.LastUpdateTimeManager.getCallLogTime
import com.example.communication.datastore.LastUpdateTimeManager.updateCallLogTime
import com.example.communication.room.MyRoomDatabase
import com.example.communication.room.entity.CallLogEntity
import com.example.communication.room.entity.ContactPersonEntity
import com.example.communication.utils.SDKConstants.INITIAL_LOAD_SIZE
import com.example.communication.utils.logTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.log

/**
 * 通话记录管理器
 */
class CallLogManager(private val context: Context) {
    private val contentResolver: ContentResolver = context.contentResolver
    private val myRoomDatabase: MyRoomDatabase = MyRoomDatabase.getMyRoomDatabase()
    private val _currentCallLog = MutableStateFlow<MutableList<CallLogEntity>>(mutableListOf())
    val currentCallLog: StateFlow<List<CallLogEntity>> = _currentCallLog.asStateFlow()

    /**
     * 启动监听
     */
    suspend fun startObserveCallLogs() {
        observeCallLogs().collect { logs ->
            updateData(logs)
        }
    }

    /**
     * 查询通话记录
     */
    suspend fun queryCallLogs(
        type: QueryType,
        param: Any? = null,
        onResult: suspend (MutableList<CallLogEntity>) -> Unit = {},
    ): MutableList<CallLogEntity> {
        val query = CallLogQueryFactory.create(type, param)
        val processor = CallLogProcessorFactory.create(type, onResult)

        val cursor = query.execute(contentResolver)
        return processor.process(cursor, context)
    }

    /**
     * 初始化通话记录
     */
    suspend fun initialize() = logTime("初始化通话记录: ") {
        queryCallLogs(type = QueryType.INIT) {
            val data = getData()
            data.addAll(it)
            updateData(data)
        }
        myRoomDatabase.callLogDao().upsert(*getData().toTypedArray())
        context.updateCallLogTime()
    }

    /**
     * 从Room加载数据
     */
    suspend fun loadFromRoom() = logTime("从Room中获取通话记录: ") {
        val roomData =
            myRoomDatabase.callLogDao().findInitialLoadData(INITIAL_LOAD_SIZE).toMutableList()
        withContext(Dispatchers.Main) {
            updateData(roomData)
        }

        val roomDataAll =
            myRoomDatabase.callLogDao().findByCallLogIdLessThan(roomData.last().callLogId)
                .toMutableList()
        withContext(Dispatchers.Main) {
            roomDataAll.addAll(0,roomData)
            updateData(roomDataAll)
        }

        // 获取最新系统记录
        val latestLogs = queryCallLogs(
            type = QueryType.AFTER_TIME,
            param = context.getCallLogTime()
        )

        val updateCallLogsWithNewRecords = updateCallLogsWithNewRecords(latestLogs.toMutableList())
        withContext(Dispatchers.Main) {
            updateData(updateCallLogsWithNewRecords)
        }
    }

    /**
     * 监听通话记录变化
     */
    private fun observeCallLogs(): Flow<MutableList<CallLogEntity>> = callbackFlow {
        val observer = object : ContentObserver(android.os.Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                CoroutineScope(Dispatchers.IO).launch {
                    val newLogs = queryCallLogs(
                        type = QueryType.AFTER_TIME,
                        param = context.getCallLogTime()
                    )
                    val updatedLogs = updateCallLogsWithNewRecords(newLogs.toMutableList())
                    trySend(updatedLogs)
                }
            }
        }

        contentResolver.registerContentObserver(
            CallLog.Calls.CONTENT_URI,
            true,
            observer
        )

        awaitClose {
            contentResolver.unregisterContentObserver(observer)
        }
    }.conflate()

    private suspend fun updateCallLogsWithNewRecords(
        newCallLogs: MutableList<CallLogEntity>
    ): MutableList<CallLogEntity> = withContext(Dispatchers.IO) {
        logTime("合并通话记录数据") {
            val currentCallLogs = getData()
            val currentLogsMap = currentCallLogs.associateBy { it.callLogId }
            if (newCallLogs.isNotEmpty()) {
                myRoomDatabase.callLogDao().upsert(*newCallLogs.toTypedArray())

                val (updateLogs, addLogs) = newCallLogs.partition {
                    currentLogsMap.containsKey(it.callLogId)
                }

                if (addLogs.isNotEmpty()) {
                    currentCallLogs.addAll(0, addLogs)
                }

                updateLogs.forEach { newLog ->
                    currentLogsMap[newLog.callLogId]?.let { oldLog ->
                        val index = currentCallLogs.indexOf(oldLog)
                        if (index != -1) {
                            currentCallLogs[index] = newLog
                        }
                    }
                }
            }

            // 检查删除的记录
            val systemIds = queryCallLogs(QueryType.ALL_IDS)
                .map { it.callLogId }
                .toSet()

            val idsToDelete = currentCallLogs.mapNotNull { entity ->
                if (!systemIds.contains(entity.callLogId)) entity.callLogId else null
            }

            if (idsToDelete.isNotEmpty()) {
                myRoomDatabase.callLogDao().deleteByCallLogs(idsToDelete)
                currentCallLogs.removeAll { it.callLogId in idsToDelete }
            }

            context.updateCallLogTime()
            currentCallLogs
        }
    }

    /**
     * 删除通话记录
     */
    suspend fun deleteCallLog(callLogEntity: CallLogEntity) = withContext(Dispatchers.IO) {
        contentResolver.delete(
            CallLog.Calls.CONTENT_URI,
            "${CallLog.Calls._ID} = ?",
            arrayOf(callLogEntity.callLogId.toString())
        )
        myRoomDatabase.callLogDao().delete(callLogEntity)

        val currentLogs = getData()
        currentLogs.remove(callLogEntity)
        updateData(currentLogs)
    }

    private fun recentSort(list: MutableList<CallLogEntity>) {
        list.sortByDescending { it.timestamp }
    }

    private fun updateData(list: MutableList<CallLogEntity>) {
        recentSort(list)
        _currentCallLog.value = list
    }

    private fun getData() = _currentCallLog.value.toMutableList()

    companion object {
        @Volatile
        private var instance: CallLogManager? = null

        fun getInstance(context: Context): CallLogManager {
            return instance ?: synchronized(this) {
                instance ?: CallLogManager(context.applicationContext).also { instance = it }
            }
        }
    }
}