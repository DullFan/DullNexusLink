package com.dullfan.nexuslink.ui.page.recent_calls

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dullfan.nexuslink.ui.components.load.LoadComponent
import com.dullfan.nexuslink.ui.page.main.MainViewModel

@Composable
fun RecentCallPage(viewModel: MainViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    // 显示加载状态
    if (state.isCallLogLoading) {
        LoadComponent()
    } else {
        LazyColumn(
            state = rememberLazyListState(),
            contentPadding = PaddingValues(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(
                state.callLogEntityList,
                key = { _, item -> item.callLogId }) { _, item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = item.name?.ifBlank { item.phoneNumber } ?: item.phoneNumber)
                }
            }
        }
    }
}