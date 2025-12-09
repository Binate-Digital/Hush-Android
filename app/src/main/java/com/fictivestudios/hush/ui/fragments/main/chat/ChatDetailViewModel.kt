package com.fictivestudios.hush.ui.fragments.main.chat

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.fictivestudios.hush.base.viewModel.BaseViewModel
import com.fictivestudios.hush.data.repositories.ChatDetailRepository
import com.fictivestudios.hush.data.repositories.ChatRepository
import com.fictivestudios.hush.data.responses.ChatInbox
import com.fictivestudios.hush.data.responses.ChatListResponse
import com.fictivestudios.hush.data.responses.LoginUserResponse
import com.fictivestudios.hush.data.responses.Message
import com.fictivestudios.hush.data.responses.SmsMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class ChatDetailViewModel @Inject constructor(
    private val repository: ChatDetailRepository
) : BaseViewModel(repository) {

    private val _messages = MutableStateFlow<List<SmsMessage>>(emptyList())
    val messages: StateFlow<List<SmsMessage>> = _messages


    var userData: LoginUserResponse? = null

    fun getUserData(contactId: String) = viewModelScope.launch {
        userData = getLoginUserData()
        userData?.let { init(it._id!!, contactId) }
    }

    fun sendSms(
        message: String, userId: String, contactId: String, onSuccess: () -> Unit,
        onError: (JSONObject) -> Unit
    ) {
        repository.sendMessage(
            message, userId, contactId, onSuccess = {
                val newMessage = _messages.value + it
                _messages.value = newMessage
                onSuccess()
            },
            onError = {
                viewModelScope.launch(Dispatchers.Main) {
                    onError(it)
                }
            }
        )
    }

    fun init(userId: String, contactId: String) {

        repository.initSocket(
            userId,
            contactId,
            onSuccess = { chatList ->
                _messages.value = chatList
            },
            onError = { error ->
                Log.d("onError ChatDetail", "$error")
            }
        )
    }
}
