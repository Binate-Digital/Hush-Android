package com.fictivestudios.hush.data.repositories

import com.fictivestudios.hush.base.network.SocketManager
import com.fictivestudios.hush.base.preference.DataPreference
import com.fictivestudios.hush.base.repository.BaseRepository
import com.fictivestudios.hush.data.networks.AuthApi
import com.fictivestudios.hush.data.responses.ChatInbox
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val socketManager: SocketManager,
    api: AuthApi,
    preferences: DataPreference
) : BaseRepository(api, preferences) {

    fun initSocket(
        userId: String,
        onSuccess: (List<ChatInbox>) -> Unit,
        onError: (JSONObject) -> Unit
    ) {
        socketManager.initSocket()
        socketManager.connect()

        // Listen for socket success responses
        socketManager.listen("response") { data ->
            val jsonObj = data
            val dataArray = jsonObj.optJSONArray("data") ?: return@listen
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
        }
        socketManager.emit("get_sms_chats", json)
    }


    fun JSONArray.toChatInboxList(): List<ChatInbox> {
        val chats = mutableListOf<ChatInbox>()

        for (i in 0 until length()) {
            val item = optJSONObject(i) ?: continue

            chats.add(
                ChatInbox(
                    _id = item.optString("_id"),
                    contactId = item.optString("contactId"),
                    phoneNumber = item.optString("phoneNumber"),
                    contactImage = item.optString("contactImage"),
                    contactName = item.optString("contactName"),
                    lastMessage = item.optString("lastMessage"),
                    lastMessageAt = item.optString("lastMessageAt"),
                    messageCount = item.optInt("messageCount"),
                    unreadCount = item.optInt("unreadCount"),
                    isRead = item.optBoolean("isRead")
                )
            )
        }

        return chats
    }
}
