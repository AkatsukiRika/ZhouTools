package com.tangping.lib.firebase

const val DEFAULT_HOME_TAB_LIST = "[\"tab_time_card\",\"tab_schedule\",\"tab_memo\",\"tab_deposit\",\"tab_settings\"]"

expect fun initFirebaseRemoteConfig(onComplete: (() -> Unit)? = null)

expect fun getHomeTabList(): String