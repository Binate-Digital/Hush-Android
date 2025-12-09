package com.fictivestudios.hush.ui.fragments.main.chat

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.fictivestudios.hush.base.viewModel.BaseViewModel
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
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository
) : BaseViewModel(repository) {

    private val _messages = MutableStateFlow<List<ChatInbox>>(emptyList())
    val messages: StateFlow<List<ChatInbox>> = _messages

    var userData: LoginUserResponse? = null

    fun getUserData() = viewModelScope.launch {
        userData = getLoginUserData()
        userData?.let { init(it._id!!) }
    }

    fun init(userId: String) {
        repository.initSocket(
            userId,
            onSuccess = { chatList ->
                Log.d("onSuccess", "$chatList")
                _messages.value = chatList     // ← UPDATE FLOW HERE
            },
            onError = { error ->
                Log.d("onError", "$error")
            }
        )
    }
}
