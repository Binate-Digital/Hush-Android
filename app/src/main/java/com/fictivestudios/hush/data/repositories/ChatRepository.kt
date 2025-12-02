package com.fictivestudios.hush.data.repositories

import com.fictivestudios.hush.base.network.SocketManager
import com.fictivestudios.hush.base.preference.DataPreference
import com.fictivestudios.hush.base.repository.BaseRepository
import com.fictivestudios.hush.data.networks.AuthApi
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

@Singleton
class ChatRepository @Inject constructor(
    private val socketManager: SocketManager,
    api: AuthApi,
    preferences: DataPreference

) : BaseRepository(api, preferences) {


    private val _messagesChannel = Channel<JSONObject>(Channel.BUFFERED)
    val messagesFlow: Flow<JSONObject> = _messagesChannel.receiveAsFlow()

    fun initSocket(userId: String) {
        socketManager.initSocket()
        socketManager.connect()

        // Listen for incoming messages
        socketManager.listen("response") { data ->
            _messagesChannel.trySend(data)
        }

        // Emit user_id to get messages
        val json = JSONObject().apply {
            put("user_id", userId)
        }
        socketManager.emit("get_sms_chats", json)
    }

    fun sendMessage(message: String) {
        val json = JSONObject().apply {
            put("object_type", "send_sms_message")
            put("message", message)
        }
        socketManager.emit("send_sms_message", json)
    }

    fun disconnectSocket() {
        socketManager.disconnect()
    }
}
