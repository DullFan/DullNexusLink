package com.dullfan.nexuslink.ui.page.recent_calls

import com.example.communication.room.entity.CallLogEntity
import com.example.communication.room.entity.ContactPersonEntity


data class RecentCallState(
    val contactPersonEntityList: MutableList<ContactPersonEntity> = mutableListOf(),
    val callLogEntityList: MutableList<CallLogEntity> = mutableListOf(),
    val callLogEntityMap: LinkedHashMap<String, MutableList<CallLogEntity>> = linkedMapOf(),
    val isLoading: Boolean = false,
)