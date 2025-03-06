package com.dullfan.nexuslink.ui.page.main

import com.dullfan.nexuslink.ui.page.recent_calls.CallLogDisplayMode
import com.dullfan.nexuslink.ui.page.recent_calls.CallLogItem
import com.example.communication.room.entity.ContactPersonEntity


data class MainState(
    val hasPermissions: Boolean? = null,
    /**
     * 联系人数据
     */
    val contactInitialsMap: MutableMap<Char,MutableList<ContactPersonEntity>> = mutableMapOf(),
    /**
     * 通话记录显示模式
     */
    val displayMode: CallLogDisplayMode = CallLogDisplayMode.TIMELINE,
    /**
     * 通话记录集合
     */
    val callLogItems: List<CallLogItem> = mutableListOf(),
    /**
     * 联系人是否在加载
     */
    val isContactPersonLoading: Boolean = false,
    /**
     * 最近通话是否在加载
     */
    val isCallLogLoading: Boolean = false,
)