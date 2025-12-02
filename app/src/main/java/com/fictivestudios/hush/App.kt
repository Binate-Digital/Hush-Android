package com.fictivestudios.hush

import android.app.Application
import com.fictivestudios.hush.base.network.SocketManager
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {
    @Inject
    lateinit var socketManager: SocketManager

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        // Initialize and connect socket immediately when app opens
        socketManager.initSocket()
        socketManager.connect()
    }

}