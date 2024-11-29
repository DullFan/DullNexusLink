package com.dullfan.nexuslink.ui.page.contact

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dullfan.nexuslink.R
import com.dullfan.nexuslink.ui.components.load.LoadComponent
import com.dullfan.nexuslink.ui.page.main.MainViewModel

@Composable
fun ContactPage(viewModel: MainViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    // 显示加载状态
    if (state.isContactPersonLoading) {
        LoadComponent()
    } else {
        LazyColumn(
            state = rememberLazyListState(),
            contentPadding = PaddingValues(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(
                state.contactPersonEntityList,
                key = { _, item -> item.hashCode() }
            ) { _, item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    item.avatar?.asImageBitmap()?.let { bitmap ->
                        Image(
                            bitmap = bitmap,
                            contentDescription = stringResource(R.string.avatar),
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                        )
                    }
                    Text(text = item.disPlayName ?: "")
                }
            }
        }

    }
}