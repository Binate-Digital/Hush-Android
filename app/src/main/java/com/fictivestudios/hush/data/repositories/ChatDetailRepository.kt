package com.fictivestudios.hush.data.repositories

import android.util.Log
import com.fictivestudios.hush.base.network.SocketManager
import com.fictivestudios.hush.base.preference.DataPreference
import com.fictivestudios.hush.base.repository.BaseRepository
import com.fictivestudios.hush.data.networks.AuthApi
import com.fictivestudios.hush.data.responses.SmsMessage
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.String

@Singleton
class ChatDetailRepository @Inject constructor(
    private val socketManager: SocketManager,
    api: AuthApi,
    preferences: DataPreference
) : BaseRepository(api, preferences) {

    fun initSocket(
        userId: String,
        contactId: String,
        onSuccess: (List<SmsMessage>) -> Unit,
        onError: (JSONObject) -> Unit
    ) {
        socketManager.initSocket()
        socketManager.connect()

        // Listen for socket success responses
        socketManager.listen("response") { data ->
            val jsonObj = data
            val dataArray = jsonObj.optJSONArray("data") ?: return@listen
            Log.d("chat chit","$dataArray")
            Log.d("chat chit2","$jsonObj")
            val chatList = dataArray.toChatInboxList()
            onSuccess(chatList)
        }

        // Listen for error
        socketManager.listen("error") { data ->
            onError(data)
        }

        // Send request
        val json = JSONObject().apply {
            put("user_id", userId)
            put("contact_id", contactId)
        }
        socketManager.emit("get_sms_messages", json)
    }

    fun sendMessage(message: String,userId: String,contactId: String) {

        val json = JSONObject().apply {
            put("user_id", userId)
            put("contact_id", contactId)
            put("message", message)
        }
        socketManager.emit("send_sms_message", json)
    }

    fun JSONArray.toChatInboxList(): List<SmsMessage> {
        val chats = mutableListOf<SmsMessage>()

        for (i in 0 until length()) {
            val item = optJSONObject(i) ?: continue

            chats.add(
                SmsMessage(
                    sender = item.optString("sender"),
                    message = item.optString("message"),
                    timestamp = item.optString("timestamp"),
                    twilioMessageSid = item.optString("twilioMessageSid"),
                    _id = item.optString("_id")
                )
            )
        }

        return chats
    }
}
