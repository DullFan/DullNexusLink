package com.dullfan.nexuslink.ui.page.recent_calls

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dullfan.nexuslink.ui.components.load.LoadComponent
import com.dullfan.nexuslink.ui.page.main.MainViewModel
import com.example.communication.room.entity.CallLogEntity
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.lazy.items
import com.example.communication.calllog.CallLogUtil

@Composable
fun RecentCallPage(viewModel: MainViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    when {
        state.isCallLogLoading -> LoadComponent()
        state.callLogItems.isEmpty() -> EmptyView()
        else -> CallLogList(items = state.callLogItems)
    }
}

@Composable
private fun CallLogList(
    items: List<CallLogItem>
) {
    LazyColumn(modifier = Modifier.fillMaxHeight()) {
        items(items = items, key = { item ->
            when (item) {
                is CallLogItem.Single -> "single_${item.entity.callLogId}"
                is CallLogItem.Merged -> "merged_${item.summary.phoneNumber}_${item.summary.lastCallTime}"
            }
        }) { item ->
            when (item) {
                is CallLogItem.Single -> SingleCallLogItem(item.entity)
                is CallLogItem.Merged -> MergedCallLogItem(item.summary)
            }
        }
    }
}

@Composable
fun SingleCallLogItem(entity: CallLogEntity) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = entity.name?.takeUnless { it.isBlank() } ?: entity.phoneNumber,
                style = MaterialTheme.typography.titleMedium)
            Text(
                text = entity.time,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = "${entity.simInfo}->通话时长: ${CallLogUtil.formatDuration(entity.duration.toLong())}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MergedCallLogItem(summary: CallLogSummary) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 左侧显示名称/号码
            Text(
                text = if (!summary.name.isNullOrBlank()) summary.name else summary.phoneNumber,
                style = MaterialTheme.typography.titleMedium
            )
            // 右侧显示最近通话时间
            Text(
                text = "最近通话: ${
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(
                        Date(
                            summary.lastCallTime
                        )
                    )
                }",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 第二行显示归属地和运营商
        Row(
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(
                text = "${summary.belongPlace} ${summary.netName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 第三行显示通话次数和总时长
        Row(
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(
                text = "共${summary.callCount}个通话 总时长: ${CallLogUtil.formatDuration(summary.totalDuration.toLong())}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EmptyView() {
    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        Text(
            text = "暂无通话记录",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}