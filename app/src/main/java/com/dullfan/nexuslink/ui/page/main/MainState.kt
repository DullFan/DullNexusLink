package com.dullfan.nexuslink.ui.page.main

import com.example.communication.room.entity.CallLogEntity
import com.example.communication.room.entity.ContactPersonEntity


data class MainState(
    val hasPermissions: Boolean? = null,
    /**
     * 联系人数据
     */
    val contactPersonEntityList: MutableList<ContactPersonEntity> = mutableListOf(),
    /**
     * 最近通话List
     */
    val callLogEntityList: MutableList<CallLogEntity> = mutableListOf(),
    /**
     * 最近通话Map
     */
    val callLogEntityMap: LinkedHashMap<String, MutableList<CallLogEntity>> = linkedMapOf(),
    /**
     * 联系人是否在加载
     */
    val isContactPersonLoading: Boolean = false,
    /**
     * 最近通话是否在加载
     */
    val isCallLogLoading: Boolean = false,
)