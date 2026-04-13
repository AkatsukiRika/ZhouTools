import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import platform.Foundation.NSBundle
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIKeyboardWillHideNotification
import platform.UIKit.UIKeyboardWillShowNotification
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerImageURL
import platform.UIKit.UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIPasteboard
import platform.UIKit.endEditing
import platform.darwin.NSObject

private var currentPhotoLibraryPickerDelegate: PhotoLibraryPickerDelegate? = null

private class PhotoLibraryPickerDelegate(
    private val onImageSelected: (String?) -> Unit
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {

    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>
    ) {
        val imageUrl = didFinishPickingMediaWithInfo[UIImagePickerControllerImageURL] as? NSURL
        picker.dismissViewControllerAnimated(true, completion = null)
        onImageSelected(imageUrl?.absoluteString)
        currentPhotoLibraryPickerDelegate = null
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true, completion = null)
        onImageSelected(null)
        currentPhotoLibraryPickerDelegate = null
    }
}

actual fun isIOS(): Boolean = true

actual fun getAppVersion(): String {
    val nsBundle = NSBundle.mainBundle()
    val version = nsBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? NSString
    return version?.toString() ?: ""
}

actual fun setClipboardContent(text: String) {
    val pasteboard = UIPasteboard.generalPasteboard()
    pasteboard.string = text
}

actual fun openSystemPhotoLibrary(onImageSelected: (String?) -> Unit) {
    if (!UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceTypePhotoLibrary)) {
        onImageSelected(null)
        return
    }
    val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController ?: run {
        onImageSelected(null)
        return
    }
    var topViewController = rootViewController
    while (topViewController.presentedViewController != null) {
        topViewController = topViewController.presentedViewController!!
    }
    val pickerDelegate = PhotoLibraryPickerDelegate(onImageSelected)
    currentPhotoLibraryPickerDelegate = pickerDelegate
    val pickerController = UIImagePickerController().apply {
        sourceType = UIImagePickerControllerSourceTypePhotoLibrary
        delegate = pickerDelegate
    }
    topViewController.presentViewController(pickerController, true, null)
}

actual fun hideSoftwareKeyboard() {
    val sharedApp = UIApplication.sharedApplication()
    val window = sharedApp.keyWindow
    window?.endEditing(true)
}

actual fun setStatusBarColor(colorStr: String, isLight: Boolean) {}

actual fun setNavigationBarColor(colorStr: String, isLight: Boolean) {}

@Composable
actual fun rememberKeyboardVisibilityState(): State<Boolean> {
    val isKeyboardVisible = remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val notificationCenter = NSNotificationCenter.defaultCenter

        val keyboardWillShowObserver = notificationCenter.addObserverForName(
            UIKeyboardWillShowNotification,
            null,
            NSOperationQueue.mainQueue
        ) { _ ->
            isKeyboardVisible.value = true
        }

        val keyboardWillHideObserver = notificationCenter.addObserverForName(
            UIKeyboardWillHideNotification,
            null,
            NSOperationQueue.mainQueue
        ) { _ ->
            isKeyboardVisible.value = false
        }

        onDispose {
            notificationCenter.removeObserver(keyboardWillShowObserver)
            notificationCenter.removeObserver(keyboardWillHideObserver)
        }
    }
    return isKeyboardVisible
}

actual fun initFirebaseRemoteConfig() {
}