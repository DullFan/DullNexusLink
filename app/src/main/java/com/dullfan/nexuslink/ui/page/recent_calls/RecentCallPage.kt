package com.dullfan.nexuslink.ui.page.recent_calls

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dullfan.nexuslink.ui.components.load.LoadComponent
import com.dullfan.nexuslink.ui.page.main.MainViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.dullfan.nexuslink.R
import com.dullfan.nexuslink.ui.page.recent_calls.entity.CallTypeIcon
import com.dullfan.nexuslink.ui.theme.iconSize20Dp
import com.dullfan.nexuslink.ui.theme.iconSize25Dp
import com.dullfan.nexuslink.ui.theme.iconSize40Dp
import com.dullfan.nexuslink.ui.theme.padding0Dp
import com.dullfan.nexuslink.ui.theme.padding16Dp
import com.dullfan.nexuslink.ui.theme.padding2Dp
import com.dullfan.nexuslink.ui.theme.padding4Dp
import com.dullfan.nexuslink.ui.theme.padding8Dp
import com.dullfan.nexuslink.ui.theme.roundSize20Dp
import com.dullfan.nexuslink.ui.theme.shadow0Dp
import com.dullfan.nexuslink.ui.theme.shadow3Dp
import com.dullfan.nexuslink.utils.formatTimestamp
import com.example.communication.room.entity.CallLogEntity

@Composable
fun RecentCallPage(mainViewModel: MainViewModel = viewModel()) {
    val state by mainViewModel.state.collectAsState()
    val context = LocalContext.current

    when {
        state.isCallLogLoading -> LoadComponent()
        state.callLogItems.isEmpty() -> EmptyView()
        else -> CallLogList(items = state.callLogItems)
    }
}

@Composable
fun CallLogList(items: List<CallLogItem>) {
    var expandedItemId by remember { mutableStateOf<Long?>(null) }

    LazyColumn(modifier = Modifier.fillMaxHeight()) {
        items(items = items, key = { item ->
            item.summary.entity.callLogId
        }) { item ->
            CallLogItem(
                item = item,
                isExpanded = expandedItemId == item.summary.entity.callLogId,
                onExpand = { expandedItemId = it }
            )
        }
    }
}

@Composable
fun CallLogItem(
    item: CallLogItem,
    isExpanded: Boolean,
    onExpand: (Long?) -> Unit
) {
    val entity = item.summary.entity
    val paddingDp by animateDpAsState(if (isExpanded) padding8Dp else padding0Dp)
    val shadowDp by animateDpAsState(if (isExpanded) shadow3Dp else shadow0Dp)
    val backgroundColor by animateColorAsState(
        if (isExpanded) MaterialTheme.colorScheme.secondaryContainer
        else MaterialTheme.colorScheme.background
    )

    TimeLabelText(item.timeLabel)

    Column(
        modifier = Modifier
            .padding(start = paddingDp, end = paddingDp, bottom = padding4Dp)
            .shadow(shadowDp, RoundedCornerShape(roundSize20Dp))
            .clip(RoundedCornerShape(roundSize20Dp))
            .background(backgroundColor)
            .clickable {
                onExpand(if (isExpanded) null else entity.callLogId)
            }
            .padding(horizontal = padding16Dp, vertical = padding4Dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CallLogContent(Modifier.weight(1f), entity)
            CallLogIcon(entity)
        }
        StyleAfterClicking(isExpanded, entity)
    }
}

@Composable
fun StyleAfterClicking(flag: Boolean, entity: CallLogEntity) {
    Column {
        AnimatedVisibility(visible = flag) {
            Row(
                modifier = Modifier
                    .padding(top = padding4Dp)
                    .fillMaxWidth(),

                ) {
                if (entity.contactId != 0L) {
                    MoreActions(
                        Modifier.weight(1f),
                        R.drawable.video_call_24px,
                        R.string.video_call
                    )
                } else {
                    MoreActions(
                        Modifier.weight(1f),
                        R.drawable.person_add_24px,
                        R.string.add_contact
                    )
                }
                MoreActions(Modifier.weight(1f), R.drawable.chat_24px, R.string.send_message)

                MoreActions(Modifier.weight(1f), R.drawable.history_24px, R.string.record)
            }
        }
    }
}

@Composable
fun CallLogContent(modifier: Modifier, entity: CallLogEntity) {
    val context = LocalContext.current

    val callTypeIcons = mapOf(
        1 to CallTypeIcon(
            R.drawable.call_received_24px, MaterialTheme.colorScheme.onSurfaceVariant
        ),
        2 to CallTypeIcon(R.drawable.call_made_24px, MaterialTheme.colorScheme.onSurfaceVariant),
        3 to CallTypeIcon(R.drawable.round_call_missed_24, MaterialTheme.colorScheme.error),
        4 to CallTypeIcon(
            R.drawable.round_voicemail_24, MaterialTheme.colorScheme.onSurfaceVariant
        ),
        5 to CallTypeIcon(
            R.drawable.round_phone_disabled_24, MaterialTheme.colorScheme.onSurfaceVariant
        ),
        6 to CallTypeIcon(
            R.drawable.round_phone_disabled_24, MaterialTheme.colorScheme.onSurfaceVariant
        )
    )

    Column(modifier = modifier) {
        Text(text = entity.name?.takeUnless { it.isBlank() } ?: entity.phoneNumber,
            style = MaterialTheme.typography.titleMedium)

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            callTypeIcons[entity.type]?.let { icon ->
                Icon(
                    painter = painterResource(icon.iconRes),
                    modifier = Modifier
                        .size(iconSize20Dp)
                        .padding(end = padding4Dp),
                    contentDescription = "",
                    tint = icon.tint
                )
            }

            Text(
                text = formatTimestamp(context, entity.timestamp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = entity.simInfo,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = padding4Dp)
        )
    }
}

@Composable
fun MoreActions(modifier: Modifier, @DrawableRes iconId: Int, @StringRes stringId: Int) {
    Column(modifier = modifier
        .clip(RoundedCornerShape(padding8Dp))
        .clickable {

        }
        .padding(padding4Dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            painter = painterResource(iconId),
            modifier = Modifier.size(iconSize25Dp),
            contentDescription = "",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(stringId),
            modifier = Modifier.padding(top = padding2Dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

}


@Composable
fun CallLogIcon(entity: CallLogEntity) {
    Icon(painter = painterResource(R.drawable.call_24px),
        modifier = Modifier
            .size(iconSize40Dp)
            .clip(CircleShape)
            .clickable { }
            .padding(padding8Dp),
        contentDescription = "",
        tint = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
fun TimeLabelText(timeLabel: TimeLabel) {
    val text = when (timeLabel) {
        TimeLabel.NONE -> return
        TimeLabel.TODAY -> stringResource(R.string.today)
        TimeLabel.YESTERDAY -> stringResource(R.string.yesterday)
        TimeLabel.EARLIER -> stringResource(R.string.earlier)
    }

    Text(
        text = text,
        modifier = Modifier.padding(horizontal = padding16Dp, vertical = padding8Dp),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}


@Composable
fun EmptyView() {
    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.no_record_of_calls),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}