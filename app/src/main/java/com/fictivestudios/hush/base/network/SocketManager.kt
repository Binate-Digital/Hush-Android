package com.fictivestudios.hush.base.network

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketManager @Inject constructor() {

   private val baseUrl = "https://host2.appsstaging.com:3012/"
    private var socket: Socket? = null

    fun initSocket() {
        if (socket == null) {
            try {
                val options = IO.Options().apply {
                    reconnection = true
                    reconnectionAttempts = 10
                    reconnectionDelay = 1000
                    forceNew = true
                    secure = true
                }
                socket = IO.socket(baseUrl, options)
            } catch (e: Exception) {
                Log.e("SocketManager", "Error creating socket: ${e.message}")
            }
        }
    }

    fun connect() {
        socket?.connect()
        socket?.on(Socket.EVENT_CONNECT) {
            Log.d("SocketManager", "Socket connected")
        }
        socket?.on(Socket.EVENT_DISCONNECT) {
            Log.d("SocketManager", "Socket disconnected")
        }
    }

    fun disconnect() {
        socket?.disconnect()
    }

    fun isSocketConnected() = socket?.isActive?:false

    fun listen(event: String, callback: (JSONObject) -> Unit) {
        socket?.on(event) { args ->
            if (args.isNotEmpty()) {
                val data = args[0] as JSONObject
                callback(data)
            }
        }
    }

    fun emit(event: String, data: JSONObject) {
        socket?.emit(event, data)
    }
}
