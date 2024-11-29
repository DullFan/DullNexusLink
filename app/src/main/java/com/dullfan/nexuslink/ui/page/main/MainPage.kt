package com.dullfan.nexuslink.ui.page.main

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dullfan.nexuslink.entity.enum.NavigationBarEnum
import com.dullfan.nexuslink.ui.components.nav.NavigationComponent
import com.dullfan.nexuslink.ui.components.no_permissions.NoPermissionsComponent
import com.dullfan.nexuslink.ui.page.collect.CollectPage
import com.dullfan.nexuslink.ui.page.contact.ContactPage
import com.dullfan.nexuslink.ui.page.recent_calls.RecentCallPage
import com.dullfan.nexuslink.ui.page.setting.SettingPage
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


