package com.fictivestudios.hush.pushNotification

import android.app.ActivityManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.fictivestudios.hush.R
import com.fictivestudios.hush.base.preference.DataPreference
import com.fictivestudios.hush.base.repository.BaseRepository
import com.fictivestudios.hush.ui.activities.IncomingCallActivity
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.twilio.voice.CallException
import com.twilio.voice.CallInvite
import com.twilio.voice.CancelledCallInvite
import com.twilio.voice.MessageListener
import com.twilio.voice.RegistrationException
import com.twilio.voice.RegistrationListener
import com.twilio.voice.Voice
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

    @Inject
    lateinit var repo: BaseRepository
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

            // Get FCM token first
            FirebaseMessaging.getInstance().token.addOnSuccessListener { fcmToken ->
                Log.d("App", "FCM token: $fcmToken")

                // Launch a coroutine to get the suspend call token
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val callToken = repo.getCallToken()
                        repo.register(fcmToken)
                        Log.d("AppFCM", "Call token: $callToken")
                        registerWithTwilio(fcmToken, callToken)
                    } catch (e: Exception) {
                        Log.e("AppFCM", "Failed to get call token: ${e.message}")
                    }
                }
            }.addOnFailureListener { e ->
                Log.e("App", "Failed to get FCM token: ${e.message}")
            }
            Voice.handleMessage(this,  remoteMessage.data, object : MessageListener {
                override fun onCallInvite(callInvite: CallInvite) {
                    // Incoming call invite
                    handleIncomingCall(callInvite)
                    Log.d("onCallInvite","called $callInvite")
                }

                override fun onCancelledCallInvite(
                    cancelledCallInvite: CancelledCallInvite,
                    callException: CallException?
                ) {
                    Log.d("onCancelledCallInvite","called $callException")
                }
            })
//            // URL-decode the token
//            val encodedToken = remoteMessage.data["twi_token"] ?: ""
////            val decodedToken = URLDecoder.decode(encodedToken, "UTF-8")
//            val decodedJson = URLDecoder.decode(encodedToken, "UTF-8")
//            val tokenJson = JSONObject(decodedJson)
//            val twilioToken = tokenJson.getString("parentCallInfoToken")
//
//            // If you need JSON object (optional)
//            // val tokenJson = JSONObject(decodedToken)
//
//            val callSid = remoteMessage.data["twi_call_sid"] ?: ""
//            val from = remoteMessage.data["twi_from"] ?: ""
//
//            val intent = Intent(this, IncomingCallActivity::class.java).apply {
//                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                putExtra("token", twilioToken) // pass decoded token
//                putExtra("callSid", callSid)
//                putExtra("from", from)
//            }
//            startActivity(intent)

    }

    private fun handleIncomingCall(callInvite: CallInvite) {

        CallManager.currentCallInvite = callInvite

        val intent = Intent(this, IncomingCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("from", callInvite.from)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "CALL_CHANNEL")
            .setSmallIcon(R.drawable.ic_call)
            .setContentTitle("Incoming call")
            .setContentText(callInvite.from)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(pendingIntent, true)
            .setAutoCancel(true)
            .build()

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(1001, notification)
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

    private fun registerWithTwilio(fcmToken: String, accessToken: String) {
        Log.d("App", "Registering with Twilio: accessToken=$accessToken, fcmToken=$fcmToken")

        Voice.register(
            accessToken,
            Voice.RegistrationChannel.FCM,
            fcmToken,
            object : RegistrationListener {
                override fun onRegistered(accessToken: String, fcmToken: String) {
                    Log.d("TwilioFCM", "Successfully registered with Twilio")
                }

                override fun onError(error: RegistrationException, accessToken: String, fcmToken: String) {
                    Log.e("TwilioFCM", "Twilio registration failed: ${error.message}")
                }
            }
        )
    }
}

object CallManager {
    var currentCallInvite: CallInvite? = null
}