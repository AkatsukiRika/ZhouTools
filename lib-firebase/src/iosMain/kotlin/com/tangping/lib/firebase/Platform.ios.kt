package com.tangping.lib.firebase

actual fun initFirebaseRemoteConfig(onComplete: (() -> Unit)?) {}

actual fun getHomeTabList(): String {
    return DEFAULT_HOME_TAB_LIST
}