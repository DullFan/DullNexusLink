package com.dullfan.nexuslink.ui.page.recent_calls

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RecentCallPageViewModel(
    private val application: Application
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(RecentCallState())
    val state: StateFlow<RecentCallState> get() = _state



    init {
        onIntent(RecentCallIntent.LoadContactList)
    }

    // 处理 Intent
    fun onIntent(intent: RecentCallIntent) {
        when (intent) {
            is RecentCallIntent.LoadContactList -> {
                loadContactList()
            }
        }
    }

    private fun loadContactList() {


    }

    fun setLoading(isLoading: Boolean) {
        _state.value = _state.value.copy(isLoading = isLoading)
    }

}


