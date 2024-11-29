package com.dullfan.nexuslink.ui.page.collect

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dullfan.nexuslink.entity.enum.NavigationBarEnum

@Composable
fun CollectPage() {
    Column {
        Text(text = stringResource(id = NavigationBarEnum.COLLECT.resId))
        Text(text = stringResource(id = NavigationBarEnum.COLLECT.resId))
        Text(text = stringResource(id = NavigationBarEnum.COLLECT.resId))
    }
}