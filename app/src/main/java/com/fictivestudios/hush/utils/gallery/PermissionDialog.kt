package com.fictivestudios.hush.utils.gallery

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog


fun showPermissionDialog(
    context: Context,
    permissionTextProvider: PermissionTextProvider,
    isPermanentlyDeclined: Boolean,
    onDismiss: () -> Unit,
    onOkClick: () -> Unit,
    onGoToAppSettingsClick: () -> Unit
) {
    val dialogBuilder = AlertDialog.Builder(context)
    dialogBuilder.apply {
        setTitle("Permission required")
        setCancelable(false)
        setMessage(permissionTextProvider.getDescription(isPermanentlyDeclined))
        setPositiveButton(if (isPermanentlyDeclined) "Grant permission" else "OK") { dialogInterface: DialogInterface, _: Int ->
            if (isPermanentlyDeclined) {
                onGoToAppSettingsClick()
            } else {
                onOkClick()
            }
        }
    }
    val dialog = dialogBuilder.create()
    dialog.show()
}

interface PermissionTextProvider {
    fun getDescription(isPermanentlyDeclined: Boolean): String
}

class CameraPermissionTextProvider : PermissionTextProvider {
    override fun getDescription(isPermanentlyDeclined: Boolean): String {
        return if (isPermanentlyDeclined) {
            "It seems you permanently declined camera permission. " +
                    "You can go to the app settings to grant it."
        } else {
            "This app needs access to your camera to take picture for you profile."
        }
    }
}

class NotificationPermissionTextProvider : PermissionTextProvider {
    override fun getDescription(isPermanentlyDeclined: Boolean): String {
        return if (isPermanentlyDeclined) {
            "This app needs access to your notification to sent notification."
        } else {
            "This app needs access to your notification to sent notification."
        }
    }
}

class RecordAudioPermissionTextProvider : PermissionTextProvider {
    override fun getDescription(isPermanentlyDeclined: Boolean): String {
        return if (isPermanentlyDeclined) {
            "It seems you permanently declined audio permission. " +
                    "You can go to the app settings to grant it."
        } else {
            "This app needs access audio permission for calling."
        }
    }
}


class VideoPermissionTextProvider : PermissionTextProvider {
    override fun getDescription(isPermanentlyDeclined: Boolean): String {
        return if (isPermanentlyDeclined) {
            "It seems you permanently declined video permission. " +
                    "You can go to the app settings to grant it."
        } else {
            "This app needs access to your Gallery  to take video for you Gallery."
        }
    }
}


class ReadImagePermissionTextProvider : PermissionTextProvider {
    override fun getDescription(isPermanentlyDeclined: Boolean): String {
        return if (isPermanentlyDeclined) {
            "It seems you permanently declined Gallery Permission. " +
                    "You can go to the app settings to grant it."
        } else {
            "This app needs access to your Gallery Permission to take picture for you profile."
        }
    }
}