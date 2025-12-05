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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatDetailViewModel @Inject constructor(
    private val repository: ChatDetailRepository
) : BaseViewModel(repository) {

    private val _messages = MutableStateFlow<List<ChatInbox>>(emptyList())
    val messages: StateFlow<List<ChatInbox>> = _messages

    var messageList = ArrayList<Message>()

    var userData: LoginUserResponse? = null

    fun getUserData(contactId : String) = viewModelScope.launch {
        userData = getLoginUserData()
        userData?.let { init(it._id!!,contactId) }
    }

    fun sendSms(message: String,userId: String,contactId: String){
        Log.d("userId","$userId")
        Log.d("contactId","$contactId")
        Log.d("message","$message")
        repository.sendMessage(message,userId,contactId)
    }

    fun init(userId: String,contactId : String) {

        repository.initSocket(
            userId,
            contactId,
            onSuccess = { chatList ->
                Log.d("onSuccess ChatDetailViewModel", "$chatList")
                _messages.value = chatList     // ← UPDATE FLOW HERE
            },
            onError = { error ->
                Log.d("onError ChatDetailViewModel", "$error")
            }
        )
    }
}
