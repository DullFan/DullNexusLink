package com.dullfan.nexuslink.ui.page.recent_calls

import com.dullfan.nexuslink.entity.ContactPersonEntity
import com.dullfan.nexuslink.room.entity.CallLogEntity

data class RecentCallState(
    val contactPersonEntityList: MutableList<ContactPersonEntity> = mutableListOf(),
    val callLogEntityList: MutableList<CallLogEntity> = mutableListOf(),
    val callLogEntityMap: LinkedHashMap<String, MutableList<CallLogEntity>> = linkedMapOf(),
    val isLoading: Boolean = false,
)