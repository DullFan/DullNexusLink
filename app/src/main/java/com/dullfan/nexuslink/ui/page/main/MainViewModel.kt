package com.dullfan.nexuslink.ui.page.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dullfan.nexuslink.datastore.isFirstEnterApp
import com.dullfan.nexuslink.datastore.updateFirstEnterApp
import com.dullfan.nexuslink.ui.page.recent_calls.CallLogProcessor.processCallLogs
import com.dullfan.nexuslink.utils.extensions.launchIO
import com.dullfan.nexuslink.utils.extensions.launchMain
import com.example.communication.calllog.CallLogManager
import com.example.communication.contact.ContactManager
import com.example.communication.room.entity.CallLogEntity
import com.example.communication.room.entity.ContactPersonEntity
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val application: Application
) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> get() = _state
    private val callLogManager = CallLogManager.getInstance(application)
    private val contactManager = ContactManager.getInstance(application)

    /**
     * 是否第一次进入app
     */
    private var isFirstEnterApp = true

    init {
        viewModelScope.launch {
            callLogManager.currentCallLog.collect { logs ->
                processAndUpdateCallLogs(logs)
            }
        }
        viewModelScope.launch {
            contactManager.contacts.collect { contacts ->
                Log.e("TAG", "${contacts.size}: ")
                _state.value = _state.value.copy(
                    isContactPersonLoading = false, contactPersonEntityList = contacts
                )
            }
        }
    }

    // 处理 Intent
    fun onIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.LoadContent -> {
                loadData()
            }

            is MainIntent.RequestPermissions -> {
                noPermissions()
            }
        }
    }

    private fun processAndUpdateCallLogs(rawLogs: List<CallLogEntity>) {
        val mergeCallLogs = processCallLogs(rawLogs.toMutableList(), state.value.displayMode)
        _state.value = _state.value.copy(
            isCallLogLoading = false, callLogItems = mergeCallLogs
        )
    }

    private fun loadData() {
        _state.value = _state.value.copy(
            isContactPersonLoading = true, isCallLogLoading = true, hasPermissions = true
        )
        launchIO {
            isFirstEnterApp = application.isFirstEnterApp()
            if (isFirstEnterApp) {
                launch { callLogManager.initialize() }
                launch { contactManager.storeInitialContacts() }
                application.updateFirstEnterApp()
            } else {
                launch { callLogManager.loadFromRoom() }
                launch { contactManager.loadAllContactPersonFromRoom() }
            }
            launch {
                callLogManager.startObserveCallLogs()
            }
            launch {
                contactManager.startObserveContacts()
            }
        }

    }

    private fun noPermissions() {
        _state.value = _state.value.copy(hasPermissions = false)
    }

    /**
     * 删除通话记录
     */
    fun deleteCallLog(callLogEntity: CallLogEntity) {
        launchIO {
            callLogManager.deleteCallLog(callLogEntity)
        }
    }
}