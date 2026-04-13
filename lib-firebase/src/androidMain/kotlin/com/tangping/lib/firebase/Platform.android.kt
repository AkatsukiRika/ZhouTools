package com.tangping.lib.firebase

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.coroutines.flow.MutableStateFlow

private val isActivationSucceed = MutableStateFlow<Boolean?>(null)

actual fun initFirebaseRemoteConfig(onComplete: (() -> Unit)?) {
    val remoteConfig = Firebase.remoteConfig
    val configSettings = remoteConfigSettings {
        minimumFetchIntervalInSeconds = 60
    }
    remoteConfig.setConfigSettingsAsync(configSettings)
    remoteConfig.fetchAndActivate()
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val updated = task.result
                Log.d("Firebase", "Config params updated: $updated")
                isActivationSucceed.value = true
            } else {
                Log.d("Firebase", "Fetch failed")
                isActivationSucceed.value = false
            }
            onComplete?.invoke()
        }
}

actual fun getHomeTabList(): String {
    return if (isActivationSucceed.value != true) {
        DEFAULT_HOME_TAB_LIST
    } else {
        val remoteConfig = Firebase.remoteConfig
        remoteConfig.getString("home_tab_list")
    }
}