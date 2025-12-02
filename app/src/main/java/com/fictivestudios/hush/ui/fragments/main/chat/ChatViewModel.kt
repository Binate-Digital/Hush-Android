package com.fictivestudios.hush.ui.fragments.main.chat

import androidx.lifecycle.viewModelScope
import com.fictivestudios.hush.R
import com.fictivestudios.hush.base.viewModel.BaseViewModel
import com.fictivestudios.hush.data.repositories.AuthRepository
import com.fictivestudios.hush.data.repositories.ChatRepository
import com.fictivestudios.hush.data.responses.ChatListResponse
import com.fictivestudios.hush.data.responses.LoginUserResponse
import com.fictivestudios.hush.data.responses.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(private val repository: ChatRepository) :
    BaseViewModel(repository) {


    private val _messages = MutableStateFlow<List<JSONObject>>(emptyList())
    val messages: StateFlow<List<JSONObject>> = _messages


    var chatList = ArrayList<ChatListResponse>()
    var messageList = ArrayList<Message>()

    var userData: LoginUserResponse? = null
    fun getUserData()= viewModelScope.launch {
        userData = getLoginUserData()
        userData?.let {
            init(it._id!!)
        }

    }


    fun init(id:String){
        repository.initSocket(id)
        collectMessages()
    }



    private fun collectMessages() {
        viewModelScope.launch {
            repository.messagesFlow.collectLatest { msg ->
                val currentList = _messages.value.toMutableList()
                currentList.add(msg)
                _messages.value = currentList
            }
        }
    }

    fun sendMessage(message: String) {
        repository.sendMessage(message)
    }


}