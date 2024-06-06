package com.tangping.kotstore

import android.annotation.SuppressLint
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import java.io.File

@SuppressLint("StaticFieldLeak")
object KotStoreAndroidBase {
    var context: Context? = null
        private set

    fun init(context: Context) {
        this.context = context
    }
}

actual fun createDataStore(name: String): DataStore<Preferences> {
    val context = KotStoreAndroidBase.context ?: throw IllegalStateException("Android Context must be initialized!")
    return createDataStoreWithDefaults {
        File(context.applicationContext.filesDir, name).path
    }
}