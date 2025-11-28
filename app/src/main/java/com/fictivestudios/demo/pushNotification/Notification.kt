package com.fictivestudios.demo.pushNotification

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.fictivestudios.demo.R
import com.fictivestudios.demo.ui.activities.DashBoardActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class Notification @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private const val channelId = "111"
        private const val NOTIFICATION_ID_MULTIPLIER = 3437
        private const val NOTIFICATION_FLAGS = PendingIntent.FLAG_UPDATE_CURRENT
        private const val NOTIFICATION_CHANNEL_NAME = "Hush"

        @RequiresApi(Build.VERSION_CODES.N)
        private const val NOTIFICATION_IMPORTANCE = NotificationManager.IMPORTANCE_HIGH
    }


    fun notify( message: String) {
        val isRunning = isAppRunning("com.fictivestudios.demo")
        val intent: Intent?
        if (isRunning) {
            intent = Intent(context, DashBoardActivity::class.java).apply {
                putExtra("notificationType", "notificationType")
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

        } else {
            intent = Intent(context, DashBoardActivity::class.java).apply {
                putExtra("notificationType", "notificationType")
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            intent.action = "com.fictivestudios.demo.NOTIFICATION_ACTION"
            intent.action = "android.intent.action.MAIN"
            intent.action = "android.intent.category.LAUNCHER"
        }


        val pendingFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE or NOTIFICATION_FLAGS
        } else {
            NOTIFICATION_FLAGS
        }

        val pendingIntent = PendingIntent.getActivity(context, 0, intent, pendingFlag)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

        val notificationBuilder = NotificationCompat.Builder(context, channelId).apply {
            setSmallIcon(R.mipmap.logo)
            setContentTitle("Hush App")
            setContentText(message)
            setAutoCancel(true)
            priority = NotificationCompat.PRIORITY_HIGH
            setSound(defaultSoundUri)
            setContentIntent(pendingIntent)
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                NOTIFICATION_CHANNEL_NAME,
                NOTIFICATION_IMPORTANCE
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationId = System.currentTimeMillis().toInt() * Random.nextInt(
            NOTIFICATION_ID_MULTIPLIER
        )
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun isAppRunning(packageName: String): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
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