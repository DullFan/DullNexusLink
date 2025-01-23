package com.example.communication.service

import android.app.Application
import com.example.communication.calllog.CallLogUtil.newTest
import com.example.communication.calllog.CallLogUtil.observeCallLog
import com.example.communication.calllog.CallLogUtil.queryCallLogAfterTimestamp
import com.example.communication.calllog.CallLogUtil.queryCallLogId
import com.example.communication.datastore.LastUpdateTimeManager.getCallLogTime
import com.example.communication.datastore.LastUpdateTimeManager.updateCallLogTime
import com.example.communication.room.MyRoomDatabase
import com.example.communication.room.entity.CallLogEntity
import com.example.communication.utils.EntityDataProcessing.recentSort
import com.example.communication.utils.logTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object CallLogService {
    private val myRoomDatabase: MyRoomDatabase = MyRoomDatabase.getMyRoomDatabase()
    private val _currentCallLog = MutableStateFlow<MutableList<CallLogEntity>>(mutableListOf())


    suspend fun Application.initializeCallLogs(updateData: suspend (MutableList<CallLogEntity>) -> Unit) {
        logTime("初始化通话记录: ") {
            contentResolver.newTest { list ->
                coroutineScope {
                    recentSort(list)
                    launch(Dispatchers.Main) {
                        _currentCallLog.value = list
                        updateData(list)
                    }
                    launch(Dispatchers.IO) {
                        myRoomDatabase.callLogDao().upsert(*list.toTypedArray())
                    }
                }
            }
        }
    }

    suspend fun Application.loadCallLogsFromRoom(updateData: suspend (MutableList<CallLogEntity>) -> Unit) {
        logTime("从Room中获取通话记录: ") {
            val loadAllCallLogsFromRoom = loadSortedCallLogsFromRoom()
            withContext(Dispatchers.Main) {
                updateData.invoke(loadAllCallLogsFromRoom)
            }
            val loadLatestCallLogs = fetchLatestSystemCallLogs()
            withContext(Dispatchers.Main) {
                updateData.invoke(loadLatestCallLogs)
            }
        }
    }

    private suspend fun loadSortedCallLogsFromRoom(): MutableList<CallLogEntity> =
        withContext(Dispatchers.IO) {
            val callLogList: MutableList<CallLogEntity> = mutableListOf()
            val findAll = myRoomDatabase.callLogDao().findAll()
            callLogList.addAll(findAll)
            recentSort(callLogList)
            _currentCallLog.value = callLogList
            callLogList
        }

    private suspend fun Application.fetchLatestSystemCallLogs() = withContext(Dispatchers.IO) {
        val updateCallLogsWithNewRecords = mutableListOf<CallLogEntity>()
        val queryCallLogAfterTimestamp =
            contentResolver.queryCallLogAfterTimestamp(getCallLogTime())
        updateCallLogsWithNewRecords.addAll(updateCallLogsWithNewRecords(queryCallLogAfterTimestamp))
        updateCallLogsWithNewRecords
    }

    /**
     * 监听通话记录变化
     */
    suspend fun Application.observeCallLogs(
        onCallLogUpdated: suspend (MutableList<CallLogEntity>) -> Unit
    ) {
        contentResolver.observeCallLog(this).collect {
            logTime("处理通话记录变化") {
                updateCallLogsWithNewRecords(it)
                onCallLogUpdated(_currentCallLog.value)
            }
        }
    }

    private suspend fun Application.updateCallLogsWithNewRecords(
        newCallLogs: MutableList<CallLogEntity>
    ): MutableList<CallLogEntity> = withContext(Dispatchers.IO) {
        logTime("合并通话数据") {
            val currentCallLogs = _currentCallLog.value.toMutableList()
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

            val queryCallLogIds = contentResolver.queryCallLogId().toSet()

            val idsToDelete = currentCallLogs.mapNotNull {
                if (!queryCallLogIds.contains(it.callLogId)) it.callLogId else null
            }

            if (idsToDelete.isNotEmpty()) {
                myRoomDatabase.callLogDao().deleteByCallLogs(idsToDelete)
                currentCallLogs.removeAll { it.callLogId in idsToDelete }
            }

            recentSort(currentCallLogs)

            withContext(Dispatchers.Main) {
                _currentCallLog.value = currentCallLogs
            }
            updateCallLogTime()
            currentCallLogs
        }
    }
}