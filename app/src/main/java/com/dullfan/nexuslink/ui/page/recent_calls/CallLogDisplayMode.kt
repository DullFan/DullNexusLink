package com.dullfan.nexuslink.ui.page.recent_calls

import com.example.communication.room.entity.CallLogEntity

/**
 * 通话记录显示模式
 */
enum class CallLogDisplayMode {
    /**
     * 时间线模式：按timestamp排序显示所有记录
     * 例： [通话1] 10:00
     *     [通话2] 09:30
     *     [通话3] 09:00
     */
    TIMELINE,

    /**
     * 号码合并模式：按phoneNumber合并所有记录，按最新timestamp排序
     * 例：[138xxxx1234] 3个通话 最近通话时间10:00
     *     [139xxxx5678] 1个通话 最近通话时间09:30
     */
    MERGED,

    /**
     * 连续合并模式：合并连续的相同phoneNumber记录
     * 例：[通话1] 10:00
     *     [138xxxx1234] 2个连续通话 09:30-09:00
     *     [通话4] 08:30
     */
    CONTINUOUS_MERGE
}

/**
 * 通话记录显示项
 */
sealed class CallLogItem {
    /**
     * 单条通话记录
     */
    data class Single(
        val entity: CallLogEntity
    ) : CallLogItem()

    /**
     * 合并的通话记录
     */
    data class Merged(
        val entities: List<CallLogEntity>, val summary: CallLogSummary
    ) : CallLogItem()
}

data class CallLogSummary(
    val phoneNumber: String,
    val name: String?,
    val lastCallTime: Long,
    val belongPlace: String,
    val netName: String,
    val callCount: Int,
    val totalDuration: Int
)

/**
 * 通话记录处理器
 */
object CallLogProcessor {
    fun processCallLogs(
        logs: List<CallLogEntity>, mode: CallLogDisplayMode
    ): List<CallLogItem> = when (mode) {
        CallLogDisplayMode.TIMELINE -> {
//            logs.sortedByDescending { it.timestamp }.map { CallLogItem.Single(it) }
            logs.map { CallLogItem.Single(it) }
        }

        CallLogDisplayMode.MERGED -> {
            mergeCallLogs(logs)
        }

        CallLogDisplayMode.CONTINUOUS_MERGE -> {
            mergeContinuousCallLogs(logs.sortedByDescending { it.timestamp })
        }
    }

    fun mergeCallLogs(callLogs: List<CallLogEntity>): List<CallLogItem> {
        // 首先按照自定义规则分组
        val groupedLogs = callLogs.groupBy { entity ->
            if (entity.contactId == 0L) {
                // contactId 为 0 时使用 phoneNumber 作为 key
                "phone_${entity.phoneNumber}"
            } else {
                // 否则使用 contactId 作为 key
                "contact_${entity.contactId}"
            }
        }

        // 将分组后的数据转换为 CallLogItem
        return groupedLogs.map { (_, logs) ->
            if (logs.size == 1) {
                // 单条记录
                CallLogItem.Single(logs.first())
            } else {
                // 多条记录需要合并
                val firstLog = logs.maxBy { it.timestamp }  // 获取最新的记录
                val summary = CallLogSummary(phoneNumber = firstLog.phoneNumber,
                    name = firstLog.name,
                    lastCallTime = firstLog.timestamp,
                    belongPlace = firstLog.belongPlace,
                    netName = firstLog.netName,
                    callCount = logs.size,
                    totalDuration = logs.sumOf { it.duration })
                CallLogItem.Merged(
                    entities = logs, summary = summary
                )
            }
        }.sortedByDescending { item ->
            when (item) {
                is CallLogItem.Single -> item.entity.timestamp
                is CallLogItem.Merged -> item.summary.lastCallTime
            }
        }
    }
    fun mergeContinuousCallLogs(callLogs: List<CallLogEntity>): List<CallLogItem> {
        // 首先按时间戳降序排序
        val sortedLogs = callLogs.sortedByDescending { it.timestamp }
        val result = mutableListOf<CallLogItem>()

        val currentGroup = mutableListOf<CallLogEntity>()

        fun isSameContact(log1: CallLogEntity, log2: CallLogEntity): Boolean {
            return when {
                // 如果两条记录都有 contactId，必须完全相同
                log1.contactId != 0L && log2.contactId != 0L ->
                    log1.contactId == log2.contactId
                // 否则比较电话号码
                else ->
                    log1.phoneNumber == log2.phoneNumber
            }
        }

        fun addGroupToResult() {
            if (currentGroup.isEmpty()) return

            if (currentGroup.size == 1) {
                result.add(CallLogItem.Single(currentGroup.first()))
            } else {
                val firstLog = currentGroup.first() // 最新的记录
                val summary = CallLogSummary(
                    phoneNumber = firstLog.phoneNumber,
                    name = firstLog.name,
                    lastCallTime = firstLog.timestamp,
                    belongPlace = firstLog.belongPlace,
                    netName = firstLog.netName,
                    callCount = currentGroup.size,
                    totalDuration = currentGroup.sumOf { it.duration }
                )
                result.add(CallLogItem.Merged(
                    entities = currentGroup.toList(),
                    summary = summary
                ))
            }
            currentGroup.clear()
        }

        for (i in sortedLogs.indices) {
            val currentLog = sortedLogs[i]

            if (currentGroup.isEmpty()) {
                currentGroup.add(currentLog)
                continue
            }

            val lastLog = currentGroup.last()
            if (isSameContact(lastLog, currentLog)) {
                currentGroup.add(currentLog)
            } else {
                addGroupToResult()
                currentGroup.add(currentLog)
            }
        }

        // 处理最后一组
        if (currentGroup.isNotEmpty()) {
            addGroupToResult()
        }

        return result
    }
}