package com.dullfan.nexuslink.ui.components.load

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.dullfan.nexuslink.ui.theme.padding100Dp

@Composable
fun LoadComponent(modifier: Modifier = Modifier.padding(horizontal = padding100Dp).fillMaxSize()) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        LinearProgressIndicator()
    }
}


@Preview
@Composable
fun NavigationComponentsPreview() {
    LoadComponent()
}