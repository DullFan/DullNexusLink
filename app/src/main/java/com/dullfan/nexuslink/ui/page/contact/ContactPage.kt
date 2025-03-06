package com.dullfan.nexuslink.ui.page.contact

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dullfan.nexuslink.ui.components.load.LoadComponent
import com.dullfan.nexuslink.ui.page.main.MainState
import com.dullfan.nexuslink.ui.page.main.MainViewModel
import com.dullfan.nexuslink.ui.theme.padding16Dp
import com.dullfan.nexuslink.ui.theme.padding4Dp
import com.dullfan.nexuslink.ui.theme.padding8Dp
import com.dullfan.nexuslink.ui.theme.width20Dp
import com.dullfan.nexuslink.ui.theme.width30Dp
import com.example.communication.room.entity.ContactPersonEntity
import kotlinx.coroutines.launch

@Composable
fun ContactPage(viewModel: MainViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // 显示加载状态
    if (state.isContactPersonLoading) {
        LoadComponent()
    } else {
        ContactDirectory(state)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContactDirectory(state: MainState) {
    // 联系人列表的滚动状态

    val listState = rememberLazyListState()
    val charList = listOf('#') + ('A'..'Z').toList()
    // 当前选中的字母在 charList 中的索引
    var targetIndex by remember { mutableIntStateOf(-1) }
    val coroutineScope = rememberCoroutineScope()

    // 计算当前可见的首字母，基于列表滚动位置
    val currentInitial by remember {
        derivedStateOf {
            val firstVisibleIndex = listState.firstVisibleItemIndex
            var cumulativeIndex = 0
            state.contactInitialsMap.entries.forEachIndexed { _, entry ->
                val itemCount = entry.value.size + 1 // 包括 stickyHeader
                if (firstVisibleIndex < cumulativeIndex + itemCount) {
                    return@derivedStateOf entry.key
                }
                cumulativeIndex += itemCount
            }
            '#'
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()
    ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(horizontal = padding16Dp),
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
        ) {
            state.contactInitialsMap.forEach { (initial, contactsForInitial) ->
                stickyHeader {
                    ContactHeader(initial.toString())
                }

                itemsIndexed(
                    contactsForInitial,
                    key = { _, item -> item.baseInfo.contactId }) { _, item ->
                    ContactItem(item)
                }
            }
        }

        Box(
            modifier = Modifier
                .width(width30Dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
        ) {
            // 获取屏幕密度，计算文字高度以及总高度
            val density = LocalDensity.current
            val itemHeightPx =
                with(density) { padding4Dp.toPx() + MaterialTheme.typography.bodySmall.lineHeight.toPx() }
            val totalHeightPx = itemHeightPx * charList.size
            // 是否正在拖动
            var isDragging by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(onDragStart = { isDragging = true },
                            onDragEnd = { isDragging = false },
                            onVerticalDrag = { change, _ ->
                                change.consume()
                                val yPosition = change.position.y.coerceIn(0f, totalHeightPx)
                                val charIndex = (yPosition / itemHeightPx).toInt()
                                    .coerceIn(0, charList.size - 1)
                                targetIndex = charIndex // 更新拖动目标索引
                                val selectedChar = charList[charIndex]

                                // 实时联动：如果所选字母有数据，滚动到对应位置
                                if (state.contactInitialsMap.containsKey(selectedChar)) {
                                    var scrollToPosition = 0
                                    state.contactInitialsMap.entries.takeWhile { it.key != selectedChar }
                                        .forEach { entry ->
                                            scrollToPosition += entry.value.size + 1
                                        }
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(scrollToPosition)
                                    }
                                }
                            })
                    }, verticalArrangement = Arrangement.spacedBy(padding4Dp)
            ) {
                charList.forEachIndexed { index, char ->
                    // 选中状态：拖动时跟随目标索引，非拖动时跟随当前首字母
                    val isSelected = if (isDragging) {
                        index == targetIndex
                    } else {
                        char == currentInitial
                    }

                    // 字母索引项
                    Text(text = char.toString(),
                        style = if (isSelected) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        else MaterialTheme.typography.bodySmall,
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .width(width30Dp)
                            .clickable {
                                targetIndex = index
                                if (state.contactInitialsMap.containsKey(char)) {
                                    var scrollToPosition = 0
                                    state.contactInitialsMap.entries.takeWhile { it.key != char }
                                        .forEach { entry ->
                                            scrollToPosition += entry.value.size + 1
                                        }
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(scrollToPosition)
                                    }
                                }
                            })
                }
            }
        }
    }
}

@Composable
fun ContactHeader(initial: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            initial,
            modifier = Modifier
                .width(width = width30Dp)
                .background(MaterialTheme.colorScheme.primary)
                .padding(vertical = padding4Dp, horizontal = padding8Dp),
            color = MaterialTheme.colorScheme.onPrimary,
            textAlign = TextAlign.Center,
        )
    }
}


@Composable
fun ContactItem(item: ContactPersonEntity) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = item.baseInfo.disPlayName, modifier = Modifier.padding(vertical = padding8Dp)
        )
    }
}
