package com.dullfan.nexuslink.ui.page.main

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.dullfan.nexuslink.datastore.isFirstEnterApp
import com.dullfan.nexuslink.datastore.updateFirstEnterApp
import com.dullfan.nexuslink.utils.extensions.launchIO
import com.dullfan.nexuslink.utils.extensions.launchMain
import com.example.communication.room.entity.CallLogEntity
import com.example.communication.service.CallLogService.initializeCallLogs
import com.example.communication.service.CallLogService.loadCallLogsFromRoom
import com.example.communication.service.CallLogService.observeCallLogs
import com.example.communication.service.ContactService.loadAllContactPersonFromRoom
import com.example.communication.service.ContactService.loadRemainingContacts
import com.example.communication.service.ContactService.observeContacts
import com.example.communication.service.ContactService.storeInitialContacts
import com.example.communication.service.ContactService.updateContactPersonListByTimestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel(
    private val application: Application
) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(MainState())
    val state: StateFlow<MainState> get() = _state

    /**
     * 是否第一次进入app
     */
    private var isFirstEnterApp = true

    // 处理 Intent
    fun onIntent(intent: MainIntent) {
        when (intent) {
            is MainIntent.LoadContent -> {
                loadContactList()
                observeContactsChanges()
                observeCallLogChanges()
            }

            is MainIntent.RequestPermissions -> {
                noPermissions()
            }
        }
    }

    private fun launchContactJob() = launchIO {
        val initialContacts = if (isFirstEnterApp) {
            application.storeInitialContacts()
        } else {
            loadAllContactPersonFromRoom()
        }

        launchMain {
            _state.value = _state.value.copy(
                isContactPersonLoading = false,
                contactPersonEntityList = initialContacts
            )
        }

        val updatedContacts = if (isFirstEnterApp) {
            application.loadRemainingContacts()
        } else {
            application.updateContactPersonListByTimestamp()
        }
        launchMain {
            _state.value = _state.value.copy(
                contactPersonEntityList = updatedContacts
            )
        }

    }

    private fun launchCallLogJob() = launchIO {
        val callLogMap: LinkedHashMap<String, MutableList<CallLogEntity>> = LinkedHashMap()
        if(isFirstEnterApp){
            application.initializeCallLogs {
                Log.e("TAG", "launchCallLogJob:${it.size}", )
                _state.value = _state.value.copy(
                    isCallLogLoading = false,
                    callLogEntityMap = callLogMap,
                    callLogEntityList = it
                )
            }
        } else {
            application.loadCallLogsFromRoom {
                Log.e("TAG", "launchCallLogJob:${it.size}", )
                _state.value = _state.value.copy(
                    isCallLogLoading = false,
                    callLogEntityMap = callLogMap,
                    callLogEntityList = it
                )
            }
        }
    }

    private fun List<CallLogEntity>.toSortedDateMap(): LinkedHashMap<String, List<CallLogEntity>> {
        return this
            .groupBy { it.date }
            .toSortedMap(compareByDescending { it })
            .toMap(LinkedHashMap())
    }

    private fun loadContactList() {
        _state.value = _state.value.copy(
            isContactPersonLoading = true, isCallLogLoading = true, hasPermissions = true
        )

        launchIO {
            isFirstEnterApp = application.isFirstEnterApp()
            launchCallLogJob()
            launchContactJob()

            if (isFirstEnterApp) {
                application.updateFirstEnterApp()
            }
        }
    }

    private fun noPermissions() {
        _state.value = _state.value.copy(hasPermissions = false)
    }

    /**
     * 监听通话记录变化
     */
    private fun observeCallLogChanges() {
        launchIO {
            application.observeCallLogs {
                _state.emit(_state.value.copy(callLogEntityList = it))
            }
        }
    }

    /**
     * 监听联系人变化
     */
    private fun observeContactsChanges() {
        launchIO {
            application.observeContacts {
                _state.emit(_state.value.copy(contactPersonEntityList = it))
            }
        }
    }
}
