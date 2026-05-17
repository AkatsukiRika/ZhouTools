package com.tangping.lib.firebase

private var isActivationSucceed: Boolean? = null
private var homeTabList: String = DEFAULT_HOME_TAB_LIST
private val completionCallbacks = mutableListOf<() -> Unit>()

actual fun initFirebaseRemoteConfig(onComplete: (() -> Unit)?) {
    if (onComplete == null) {
        return
    }
    if (isActivationSucceed != null) {
        onComplete()
    } else {
        completionCallbacks += onComplete
    }
}

actual fun getHomeTabList(): String {
    return if (isActivationSucceed == true) {
        homeTabList
    } else {
        DEFAULT_HOME_TAB_LIST
    }
}

fun updateFirebaseRemoteConfigHomeTabList(value: String?) {
    isActivationSucceed = !value.isNullOrBlank()
    homeTabList = value?.takeIf { it.isNotBlank() } ?: DEFAULT_HOME_TAB_LIST

    val callbacks = completionCallbacks.toList()
    completionCallbacks.clear()
    callbacks.forEach { it() }
}
