package com.dullfan.nexuslink.ui.page.recent_calls

sealed class RecentCallIntent {
    data object LoadContactList : RecentCallIntent()
}
