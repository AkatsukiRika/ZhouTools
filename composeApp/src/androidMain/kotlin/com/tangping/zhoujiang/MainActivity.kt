package com.tangping.zhoujiang

import App
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.Window
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import com.tangping.kotstore.KotStoreAndroidBase

class MainActivity : ComponentActivity() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        var context: Context? = null
        var window: Window? = null
        var imagePickerLauncher: ActivityResultLauncher<String>? = null
        var onImagePicked: ((String?) -> Unit)? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
        setTheme(R.style.TransparentSystemBars)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        MainActivity.window = window
        KotStoreAndroidBase.init(this)
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            onImagePicked?.invoke(uri?.toString())
            onImagePicked = null
        }

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}