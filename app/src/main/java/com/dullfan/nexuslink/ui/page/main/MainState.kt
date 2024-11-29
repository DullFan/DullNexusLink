package com.dullfan.nexuslink.ui.page.main

import com.dullfan.nexuslink.entity.ContactPersonEntity
import com.dullfan.nexuslink.room.entity.CallLogEntity

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