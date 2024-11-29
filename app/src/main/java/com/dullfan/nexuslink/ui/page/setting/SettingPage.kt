package com.dullfan.nexuslink.ui.page.setting

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dullfan.nexuslink.entity.enum.NavigationBarEnum

@Composable
fun SettingPage(){
    Column {
        Text(text = stringResource(id = NavigationBarEnum.SETTING.resId))
        Text(text = stringResource(id = NavigationBarEnum.SETTING.resId))
        Text(text = stringResource(id = NavigationBarEnum.SETTING.resId))
        Text(text = stringResource(id = NavigationBarEnum.SETTING.resId))
    }
}