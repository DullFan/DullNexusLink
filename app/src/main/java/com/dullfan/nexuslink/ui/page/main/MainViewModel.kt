package com.dullfan.nexuslink.ui.page.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dullfan.nexuslink.datastore.getLastReadTimeFirst
import com.dullfan.nexuslink.datastore.isFirstEnterApp
import com.dullfan.nexuslink.datastore.updateFirstEnterApp
import com.dullfan.nexuslink.datastore.updateLastReadTime
import com.dullfan.nexuslink.entity.ContactPersonEntity
import com.dullfan.nexuslink.room.MyRoomDatabase
import com.dullfan.nexuslink.room.entity.CallLogEntity
import com.dullfan.nexuslink.room.findAllContactPersonRoom
import com.dullfan.nexuslink.room.upsertContactPersonRoom
import com.dullfan.nexuslink.utils.core.CallLogUtil.queryCallLog
import com.dullfan.nexuslink.utils.core.CallLogUtil.queryCallLogAfterTimestamp
import com.dullfan.nexuslink.utils.core.EntityDataProcessing.contactPersonEntityAddOrUpdate
import com.dullfan.nexuslink.utils.core.EntityDataProcessing.nameSort
import com.dullfan.nexuslink.utils.core.observeContacts
import com.dullfan.nexuslink.utils.core.queryAllContactId
import com.dullfan.nexuslink.utils.core.queryContacts
import com.dullfan.nexuslink.utils.core.queryUpdatedContacts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(
    private val application: Application
) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> get() = _state

    /**
     * 上次更新时间
     */
    private val lastUpdateTime = MutableStateFlow(0L)

    /**
     * 是否第一次进入app
     */
    private var isFirstEnterApp = true

    private val myRoomDatabase: MyRoomDatabase = MyRoomDatabase.getMyRoomDatabase()

    // 处理 Intent
    fun onIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.LoadContent -> {
                loadContactList()
                observeContactsChanges()
            }

            is MainIntent.RequestPermissions -> {
                noPermissions()
            }
        }
    }

    private fun launchContactJob() = viewModelScope.launch(Dispatchers.IO) {
        val details: MutableList<ContactPersonEntity> = if (isFirstEnterApp) {
            handleContactFirstEnterApp()
        } else {
            handleContactSubsequentEnterApp()
        }
        nameSort(details)
        _state.value = _state.value.copy(
            isContactPersonLoading = false, contactPersonEntityList = details
        )
    }

    private suspend fun handleContactSubsequentEnterApp(): MutableList<ContactPersonEntity> {
        val startTime = System.currentTimeMillis()
        // 1、获取Room中的所有数据
        val details = findAllContactPersonRoom(myRoomDatabase)
        // 2、获取上次登录时间后的联系人数据（新增 or 修改）
        val queryUpdatedContacts =
            application.contentResolver.queryUpdatedContacts(lastUpdateTime.value)
        // 3、判断Room中是否有联系人ID，存在则修改不存在则新增
        if (queryUpdatedContacts.isNotEmpty()) {
            queryUpdatedContacts.forEach { contactPersonEntity ->
                if (myRoomDatabase.contactPersonDao()
                        .findByContactId(contactPersonEntity.contactId) == null
                ) {
                    details.add(contactPersonEntity)
                } else {
                    val index =
                        details.indexOfFirst { it.contactId == contactPersonEntity.contactId }
                    details[index] = contactPersonEntity
                }
            }
            myRoomDatabase.contactPersonDao()
                .upsert(*upsertContactPersonRoom(myRoomDatabase, queryUpdatedContacts))
        }

        // 防止用户删除联系人后无法感知到
        // 4、获取系统联系人ID
        val queryAllContactIds = application.contentResolver.queryAllContactId()

        val currentRoomContactIds = details.map { it.contactId }

        // 5、与Room中的ID进行比对，不存在则删除Room中的数据
        val idsToDelete =
            currentRoomContactIds.filterNot { queryAllContactIds.contains(it) }

        // 在Room中删除这些联系人
        idsToDelete.forEach { contactId ->
            myRoomDatabase.contactPersonDao().deleteByContactId(contactId)
            details.removeIf { it.contactId == contactId }
        }

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        Log.d("TAG", "联系人从Room中获取：$duration 毫秒")
        return details
    }

    private suspend fun handleContactFirstEnterApp(): MutableList<ContactPersonEntity> {
        val startTime = System.currentTimeMillis()
        val details = application.contentResolver.queryContacts()
        myRoomDatabase.contactPersonDao()
            .upsert(*upsertContactPersonRoom(myRoomDatabase, details))
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        Log.d("TAG", "。联系人存储Room耗时：$duration 毫秒")
        return details
    }

    private fun launchCallLogJob() = viewModelScope.launch(Dispatchers.IO) {
        val callLogMap: LinkedHashMap<String, MutableList<CallLogEntity>> = LinkedHashMap()
        val callLogList: MutableList<CallLogEntity> = mutableListOf()
        if (isFirstEnterApp) {
            handleCallLogFirstEnterApp(callLogMap, callLogList)
        } else {
            handleCallLogSubsequentEnterApp(callLogMap, callLogList)
        }

        withContext(Dispatchers.Main) {
            _state.value = _state.value.copy(
                isCallLogLoading = false,
                callLogEntityMap = callLogMap,
                callLogEntityList = callLogList
            )
        }
    }

    private suspend fun handleCallLogSubsequentEnterApp(
        callLogMap: LinkedHashMap<String, MutableList<CallLogEntity>>,
        callLogList: MutableList<CallLogEntity>
    ) {
        val startTime = System.currentTimeMillis()

        application.contentResolver.queryCallLogAfterTimestamp(lastUpdateTime.value) { map, list ->
            callLogList.addAll(list)
            callLogMap.putAll(map)
        }
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        Log.d("TAG", "通话记录从Room取出耗时：$duration 毫秒")
    }

    private suspend fun handleCallLogFirstEnterApp(
        callLogMap: LinkedHashMap<String, MutableList<CallLogEntity>>,
        callLogList: MutableList<CallLogEntity>
    ) {
        val startTime = System.currentTimeMillis()
        application.contentResolver.queryCallLog { map, list ->
            callLogList.addAll(list)
            callLogMap.putAll(map)
        }
        myRoomDatabase.callLogDao()
            .upsert(*callLogList.toTypedArray())
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        Log.d("TAG", "通话记录存储Room中：$duration 毫秒")
    }

    private fun loadContactList() {
        _state.value = _state.value.copy(
            isContactPersonLoading = true,
            isCallLogLoading = true,
            hasPermissions = true
        )

        viewModelScope.launch {
            isFirstEnterApp = application.isFirstEnterApp()
            // 更新进入App时间
            application.getLastReadTimeFirst {
                lastUpdateTime.value = it
            }
            val contactJob = launchContactJob()
            val callLogJob = launchCallLogJob()
            // 等待所有任务完成
            contactJob.join()
            callLogJob.join()

            // 执行更新最后读取时间
//            application.updateLastReadTime()
            if(isFirstEnterApp){
                application.updateFirstEnterApp()
            }
        }
    }

    private fun noPermissions() {
        _state.value = _state.value.copy(hasPermissions = false)
    }

    /**
     * 监听联系人变化
     */
    private fun observeContactsChanges() {
        viewModelScope.launch {
            application.contentResolver.observeContacts(lastUpdateTimeFlow = lastUpdateTime,
                updateTime = {
                    viewModelScope.launch {
                        application.updateLastReadTime()
                    }
                }).collect {
                val contactPersonEntityList: MutableList<ContactPersonEntity>
                if (it.isEmpty()) {
                    val queryAllContactId: MutableList<String> =
                        application.contentResolver.queryAllContactId()
                    val updatedContactPersonList =
                        _state.value.contactPersonEntityList.filter { contact ->
                            queryAllContactId.contains(contact.contactId)
                        }.toMutableList()
                    contactPersonEntityList = updatedContactPersonList
                } else {
                    contactPersonEntityList =
                        contactPersonEntityAddOrUpdate(_state.value.contactPersonEntityList, it)
                }
                _state.value = _state.value.copy(contactPersonEntityList = contactPersonEntityList)
            }
        }
    }
}