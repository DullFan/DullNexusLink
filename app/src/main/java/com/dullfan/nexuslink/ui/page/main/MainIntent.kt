package com.dullfan.nexuslink.ui.page.main

sealed class MainIntent {
    data object LoadContent : MainIntent()
    data object RequestPermissions : MainIntent()
}