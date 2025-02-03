package com.example.communication.calllog

import android.annotation.SuppressLint
import android.content.Context
import android.telephony.SubscriptionManager

/**
 * 通话记录工具类
 */
object CallLogUtil {

    @SuppressLint("MissingPermission")
    fun getSimInfo(context: Context, phoneAccountId: String?): String {
        if (phoneAccountId == null) return "未知SIM卡"

        try {
            val subscriptionManager = context.getSystemService(SubscriptionManager::class.java)
            val activeSubscriptionInfoList =
                subscriptionManager.activeSubscriptionInfoList ?: return "未知SIM卡"

            // 解析 phoneAccountId 中的 subId
            val subId = phoneAccountId.filter { it.isDigit() }.toIntOrNull()

            for (subscriptionInfo in activeSubscriptionInfoList) {
                if (subId == subscriptionInfo.subscriptionId) {
                    return when {
                        !subscriptionInfo.displayName.isNullOrEmpty() -> subscriptionInfo.displayName.toString()

                        !subscriptionInfo.carrierName.isNullOrEmpty() -> subscriptionInfo.carrierName.toString()

                        else -> "SIM${subscriptionInfo.simSlotIndex + 1}"
                    }
                }
            }
            return "未知SIM卡"
        } catch (e: Exception) {
            e.printStackTrace()
            return "未知SIM卡"
        }
    }

    // 初始化通话时长
    fun formatDuration(time: Long): String {
        val s = time % 60
        val m = time / 60
        val h = time / 60 / 60
        val sb = StringBuilder()
        if (h > 0) {
            sb.append(h).append("小时")
        }
        if (m > 0) {
            sb.append(m).append("分")
        }
        sb.append(s).append("秒")
        return sb.toString()
    }

}