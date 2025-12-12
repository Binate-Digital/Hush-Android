package com.fictivestudios.hush.pushNotification

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.fictivestudios.hush.base.preference.DataPreference
import com.fictivestudios.hush.ui.activities.DashBoardActivity
import com.fictivestudios.hush.ui.activities.IncomingCallActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.twilio.voice.Voice
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton
import java.net.URLDecoder
@Singleton
@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {
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

        val type = remoteMessage.data["type"]
        Log.d("FCM DATA TYPE",type.toString())
        Log.d("FCM DATA remoteMessage",remoteMessage.data.toString())
        if (type == "incoming_call") {

            // URL-decode the token
            val encodedToken = remoteMessage.data["twi_token"] ?: ""
//            val decodedToken = URLDecoder.decode(encodedToken, "UTF-8")
            val decodedJson = URLDecoder.decode(encodedToken, "UTF-8")
            val tokenJson = JSONObject(decodedJson)
            val twilioToken = tokenJson.getString("parentCallInfoToken")

            // If you need JSON object (optional)
            // val tokenJson = JSONObject(decodedToken)

            val callSid = remoteMessage.data["twi_call_sid"] ?: ""
            val from = remoteMessage.data["twi_from"] ?: ""

            val intent = Intent(this, IncomingCallActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra("token", twilioToken) // pass decoded token
                putExtra("callSid", callSid)
                putExtra("from", from)
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