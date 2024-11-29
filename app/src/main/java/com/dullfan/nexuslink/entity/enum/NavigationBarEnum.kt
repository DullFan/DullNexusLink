package com.dullfan.nexuslink.entity.enum

import com.dullfan.nexuslink.R

enum class NavigationBarEnum(val resId: Int,val selectIconId: Int,val unselectIconId: Int) {
    RECENT_CALL(
        R.string.bottom_nav_recent_call,
        R.drawable.quantum_ic_watch_later_vd_theme_24,
        R.drawable.quantum_gm_ic_access_time_vd_theme_24
    ),
    CONTACT(
        R.string.bottom_nav_contact,
        R.drawable.quantum_gm_ic_people_alt_vd_theme_24,
        R.drawable.quantum_gm_ic_people_vd_theme_24
    ),
    COLLECT(
        R.string.bottom_nav_collect,
        R.drawable.quantum_gm_ic_star_vd_theme_24,
        R.drawable.quantum_gm_ic_star_outline_vd_theme_24
    ),
    SETTING(
        R.string.bottom_nav_setting,
        R.drawable.baseline_settings_24,
        R.drawable.outline_settings_24
    ),
}
