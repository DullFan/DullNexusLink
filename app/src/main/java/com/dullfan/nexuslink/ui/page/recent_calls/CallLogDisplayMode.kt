package com.dullfan.nexuslink.ui.page.recent_calls

import com.example.communication.room.entity.CallLogEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

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

enum class TimeLabel {
    NONE, TODAY, YESTERDAY, EARLIER
}

data class CallLogItem(
    val entities: List<CallLogEntity>,
    val summary: CallLogSummary,
    var timeLabel: TimeLabel = TimeLabel.NONE
)

data class CallLogSummary(
    val entity: CallLogEntity,
    val callCount: Int,
    val totalDuration: Int,
)

/**
 * 通话记录处理器
 */
object CallLogProcessor {
    fun processCallLogs(
        logs: List<CallLogEntity>, mode: CallLogDisplayMode
    ): List<CallLogItem> {
        val callLogItems = when (mode) {
            CallLogDisplayMode.TIMELINE -> {
                logs.map { createUnifiedItem(listOf(it)) }
            }

            CallLogDisplayMode.MERGED -> {
                mergeCallLogs(logs)
            }

            CallLogDisplayMode.CONTINUOUS_MERGE -> {
                mergeContinuousCallLogs(logs.sortedByDescending { it.timestamp })
            }
        }
        processCallLogsWithTimeLabels(callLogItems)
        return callLogItems
    }

    private fun createUnifiedItem(logs: List<CallLogEntity>): CallLogItem {
        val firstLog = logs.maxBy { it.timestamp }
        val summary = CallLogSummary(
            entity = firstLog,
            callCount = logs.size,
            totalDuration = logs.sumOf { it.duration },
        )
        return CallLogItem(entities = logs, summary = summary)
    }

    private fun mergeCallLogs(callLogs: List<CallLogEntity>): List<CallLogItem> {
        val groupedLogs = callLogs.groupBy { entity ->
            if (entity.contactId == 0L) {
                "phone_${entity.phoneNumber}"
            } else {
                "contact_${entity.contactId}"
            }
        }

        return groupedLogs.map { (_, logs) ->
            createUnifiedItem(logs)
        }.sortedByDescending { it.summary.entity.timestamp }
    }

    private fun mergeContinuousCallLogs(callLogs: List<CallLogEntity>): List<CallLogItem> {
        val sortedLogs = callLogs.sortedByDescending { it.timestamp }
        val result = mutableListOf<CallLogItem>()
        val currentGroup = mutableListOf<CallLogEntity>()

        fun isSameContact(log1: CallLogEntity, log2: CallLogEntity): Boolean {
            return when {
                log1.contactId != 0L && log2.contactId != 0L -> log1.contactId == log2.contactId
                else -> log1.phoneNumber == log2.phoneNumber
            }
        }

        fun addGroupToResult() {
            if (currentGroup.isNotEmpty()) {
                result.add(createUnifiedItem(currentGroup.toList()))
                currentGroup.clear()
            }
        }

        for (currentLog in sortedLogs) {
            if (currentGroup.isEmpty() || isSameContact(currentGroup.last(), currentLog)) {
                currentGroup.add(currentLog)
            } else {
                addGroupToResult()
                currentGroup.add(currentLog)
            }
        }

        addGroupToResult()
        return result
    }

    private fun processCallLogsWithTimeLabels(sortedLogs: List<CallLogItem>) {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        var currentLabel: TimeLabel? = null
        var isEarlierProcessed = false

        for (log in sortedLogs) {
            val logDate = Instant.ofEpochMilli(log.summary.entity.timestamp)
                .atZone(ZoneId.systemDefault()).toLocalDate()

            val timeLabel = when {
                logDate.isEqual(today) -> TimeLabel.TODAY
                logDate.isEqual(yesterday) -> TimeLabel.YESTERDAY
                else -> {
                    if (!isEarlierProcessed) {
                        isEarlierProcessed = true
                        TimeLabel.EARLIER
                    } else {
                        break
                    }
                }
            }

            // 仅在时间段的第一个记录上设置时间标识
            val labelToSet = if (timeLabel != currentLabel) {
                currentLabel = timeLabel
                timeLabel
            } else {
                TimeLabel.NONE
            }
            log.timeLabel = labelToSet
        }
    }
}