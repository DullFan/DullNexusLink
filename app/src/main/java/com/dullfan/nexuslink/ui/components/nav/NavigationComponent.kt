package com.dullfan.nexuslink.ui.components.nav

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dullfan.nexuslink.entity.enum.NavigationBarEnum

@Composable
fun NavigationComponent(
    selectIndex: Int,
    navItemOnClick: (index: Int) -> Unit
) {
    NavigationBar {
        NavigationBarEnum.entries.forEachIndexed { index, nbEnum ->
            NavigationBarItem(
                selected = index == selectIndex,
                onClick = { navItemOnClick(index) },
                alwaysShowLabel = false,
                icon = {
                    Icon(
                        painter = painterResource(
                            id =
                            if (index == selectIndex)
                                nbEnum.selectIconId
                            else
                                nbEnum.unselectIconId
                        ),
                        contentDescription = stringResource(id = nbEnum.resId)
                    )
                },
                label = {
                    Text(text = stringResource(id = nbEnum.resId))
                })
        }
    }
}

@Preview
@Composable
fun NavigationComponentsPreview() {
    var selectIndex by remember {
        mutableIntStateOf(0)
    }

    NavigationComponent(selectIndex = selectIndex) {
        selectIndex = it
    }
}