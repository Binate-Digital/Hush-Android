package com.fictivestudios.hush

import android.app.Application
import android.util.Log
import androidx.activity.viewModels
import com.fictivestudios.hush.base.network.SocketManager
import com.fictivestudios.hush.base.repository.BaseRepository
import com.fictivestudios.hush.ui.activities.MainViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.twilio.voice.RegistrationException
import com.twilio.voice.RegistrationListener
import com.twilio.voice.Voice
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.getValue

@HiltAndroidApp
class App : Application() {

    @Inject
    lateinit var socketManager: SocketManager

    @Inject
    lateinit var repo: BaseRepository

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        // Initialize and connect socket immediately
        socketManager.initSocket()
        socketManager.connect()

        // Get FCM token first
        FirebaseMessaging.getInstance().token.addOnSuccessListener { fcmToken ->
            Log.d("App", "FCM token: $fcmToken")

            // Launch a coroutine to get the suspend call token
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val callToken = repo.getCallToken()
                    repo.register(fcmToken)
                    Log.d("App", "Call token: $callToken")
                    registerWithTwilio(fcmToken, callToken)
                } catch (e: Exception) {
                    Log.e("App", "Failed to get call token: ${e.message}")
                }
            }
        }.addOnFailureListener { e ->
            Log.e("App", "Failed to get FCM token: ${e.message}")
        }
    }

    private fun registerWithTwilio(fcmToken: String, accessToken: String) {
//        Log.d("App", "Registering with Twilio: accessToken=$accessToken, fcmToken=$fcmToken")
//
//        Voice.register(
//            accessToken,
//            Voice.RegistrationChannel.FCM,
//            fcmToken,
//            object : RegistrationListener {
//                override fun onRegistered(accessToken: String, fcmToken: String) {
//                    Log.d("Twilio", "Successfully registered with Twilio")
//                }
//
//                override fun onError(error: RegistrationException, accessToken: String, fcmToken: String) {
//                    Log.e("Twilio", "Twilio registration failed: ${error.message}")
//                }
//            }
//        )
    }
}
