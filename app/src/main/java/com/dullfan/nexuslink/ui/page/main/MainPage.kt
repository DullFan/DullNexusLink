package com.dullfan.nexuslink.ui.page.main

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dullfan.nexuslink.R
import com.dullfan.nexuslink.entity.enum.NavigationBarEnum
import com.dullfan.nexuslink.ui.components.nav.NavigationComponent
import com.dullfan.nexuslink.ui.components.no_permissions.NoPermissionsComponent
import com.dullfan.nexuslink.ui.page.collect.CollectPage
import com.dullfan.nexuslink.ui.page.contact.ContactPage
import com.dullfan.nexuslink.ui.page.recent_calls.RecentCallPage
import com.dullfan.nexuslink.ui.page.setting.SettingPage
import com.dullfan.nexuslink.ui.theme.iconSize25Dp
import com.dullfan.nexuslink.ui.theme.padding10Dp
import com.dullfan.nexuslink.ui.theme.padding16Dp
import com.dullfan.nexuslink.ui.theme.padding4Dp
import com.dullfan.nexuslink.ui.theme.padding8Dp
import com.dullfan.nexuslink.utils.PermissionUtil
import com.dullfan.nexuslink.utils.PermissionUtil.permissions
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun MainPage(modifier: Modifier, viewModel: MainViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    var selectIndex by remember {
        mutableIntStateOf(0)
    }

    val pagerState = rememberPagerState(pageCount = {
        NavigationBarEnum.entries.size
    })

    // 验证是否存在权限
    CheckPermissions()

    if (state.hasPermissions == false) {
        // 无权限页面
        NoPermissionsComponent()
    }

    LaunchedEffect(key1 = pagerState.currentPage) {
        snapshotFlow { pagerState.currentPage }.distinctUntilChanged().collect { pageIndex ->
            selectIndex = pageIndex
        }
    }

    LaunchedEffect(key1 = selectIndex) {
        pagerState.scrollToPage(selectIndex)
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.Top) {
        SearchComponent(pagerState)
        Box(modifier = Modifier.weight(1f)) {
            this@Column.AnimatedVisibility(visible = state.hasPermissions == true) {
                HorizontalPagerComponent(pagerState, Modifier.fillMaxSize())
            }
        }

        AnimatedVisibility(visible = state.hasPermissions == true) {
            NavigationComponent(selectIndex = selectIndex) {
                if (selectIndex != it) {
                    selectIndex = it
                }
            }
        }
    }
}

@Composable
fun SearchComponent(pagerState: PagerState) {
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(padding8Dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = padding16Dp, vertical = padding8Dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { }
                .padding(padding10Dp)) {
            Icon(
                imageVector = Icons.Rounded.Search,
                modifier = Modifier.size(iconSize25Dp),
                contentDescription = "",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.search_contact),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )

            // TODO 控制搜索栏
            when (pagerState.currentPage) {
                0 -> {
                    Icon(
                        painter = painterResource(R.drawable.dashboard_24px),
                        modifier = Modifier
                            .size(iconSize25Dp)
                            .clip(RoundedCornerShape(padding4Dp))
                            .clickable {

                            },
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                1 -> {}
                2 -> {}
                3 -> {}
            }

            Icon(
                imageVector = Icons.Rounded.MoreVert,
                modifier = Modifier.size(iconSize25Dp),
                contentDescription = "",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun HorizontalPagerComponent(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
) {
    HorizontalPager(state = pagerState, modifier = modifier) { page ->
        Column {
            when (NavigationBarEnum.entries[page]) {
                NavigationBarEnum.RECENT_CALL -> RecentCallPage()
                NavigationBarEnum.CONTACT -> ContactPage()
                NavigationBarEnum.COLLECT -> CollectPage()
                NavigationBarEnum.SETTING -> SettingPage()
            }
        }
    }
}

/**
 * 请求通话记录权限
 */
@Composable
internal fun CheckPermissions(
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    if (!PermissionUtil.checkPermissions(context)) {
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            if (result.values.contains(false)) {
                viewModel.onIntent(MainIntent.RequestPermissions)
            } else {
                viewModel.onIntent(MainIntent.LoadContent)
            }
        }

        LaunchedEffect(Unit) {
            permissionLauncher.launch(permissions)
        }
    } else {
        viewModel.onIntent(MainIntent.LoadContent)
    }
}
