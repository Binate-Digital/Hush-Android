package com.fictivestudios.hush.pushNotification

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.fictivestudios.hush.base.preference.DataPreference
import com.fictivestudios.hush.ui.activities.DashBoardActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@AndroidEntryPoint
class FirebaseMessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var notifications: Notification

    @Inject
    lateinit var preferences: DataPreference
    private val applicationScope = ProcessLifecycleOwner.get().lifecycleScope


    override fun onNewToken(token: String) {
        super.onNewToken(token)
        applicationScope.launch {
            preferences.saveDeviceTokens(token)
            delay(100)
            Log.d("FCMs Token", preferences.deviceToken.first())
        }

    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("messageType", remoteMessage.data.toString())
        val notificationType = remoteMessage.data["notificationType"]
        if (notificationType == "number_notify") {
            notifications.notify(
                remoteMessage.notification?.body.toString()
            )
            val phoneNumber = remoteMessage.data["phoneNumber"]
            val status = remoteMessage.data["status"]
            val sid = remoteMessage.data["sid"]
            Log.d("notificationType", notificationType.toString())
            if (status == "success") {
                applicationScope.launch {
                    preferences.updatePhoneNoRegisterData(sid ?: "", phoneNumber)
                    delay(1500)
                }
            }
            val isRunning = isAppRunning("com.fictivestudios.hush")

            val intent: Intent?
            if (isRunning) {
                intent = Intent(applicationContext, DashBoardActivity::class.java).apply {
                    putExtra("notificationType", "notificationType")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

            } else {
                intent = Intent(applicationContext, DashBoardActivity::class.java).apply {
                    putExtra("notificationType", "notificationType")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                intent.action = "com.fictivestudios.hush.NOTIFICATION_ACTION"
                intent.action = "android.intent.action.MAIN"
                intent.action = "android.intent.category.LAUNCHER"
            }

            startActivity(intent)
        }
    }

    private fun isAppRunning(packageName: String): Boolean {
        val activityManager = applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningAppProcesses = activityManager.runningAppProcesses

        if (runningAppProcesses != null) {
            for (processInfo in runningAppProcesses) {
                if (processInfo.processName == packageName) {
                    // Found a running process with the package name
                    return true
                }
            }
        }

        // No running process matches the package name
        return false
    }
}