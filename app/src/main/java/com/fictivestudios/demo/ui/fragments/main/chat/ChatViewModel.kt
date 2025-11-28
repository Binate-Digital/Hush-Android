package com.fictivestudios.demo.ui.fragments.main.chat

import com.fictivestudios.demo.R
import com.fictivestudios.demo.base.viewModel.BaseViewModel
import com.fictivestudios.demo.data.repositories.AuthRepository
import com.fictivestudios.demo.data.responses.ChatListResponse
import com.fictivestudios.demo.data.responses.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(private val repository: AuthRepository) :
    BaseViewModel(repository) {

    var chatList = ArrayList<ChatListResponse>()
    var messageList = ArrayList<Message>()


    init {

        chatList.add(ChatListResponse("Json", "Hey Json Whats Up", "2:35PM", R.drawable.persons))
        chatList.add(
            ChatListResponse(
                "Alexa Mathina",
                "Alexa!!",
                "9:30PM",
                R.drawable.user_image_1
            )
        )
        chatList.add(
            ChatListResponse(
                "Robin Hood",
                "Did you do my work?",
                "2:55PM",
                R.drawable.user_image_2
            )
        )
        chatList.add(ChatListResponse("Ana Ward", "Hello!!", "8:35PM", R.drawable.user_image_3))
        chatList.add(
            ChatListResponse(
                "James Mark",
                "Where are you?",
                "6:15PM",
                R.drawable.user_image_4
            )
        )
        messageList.add(Message(1,"Alexa Mathina","hello",R.drawable.user_image_1))
        messageList.add(Message(2,"Json","hey!!",R.drawable.user_image_2))
        messageList.add(Message(1,"Alexa Mathina","whats app",R.drawable.user_image_1))
        messageList.add(Message(2,"Json","I am good!",R.drawable.user_image_2))
        messageList.add(Message(1,"Alexa Mathina","Can i call you",R.drawable.user_image_1))
        messageList.add(Message(2,"Json","Yeah sure! why not.",R.drawable.user_image_2))
    }

}