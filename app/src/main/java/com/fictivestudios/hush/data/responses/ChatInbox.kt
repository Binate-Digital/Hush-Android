package com.fictivestudios.hush.data.responses

data class ChatInbox(
    val id: String?="",
    val phoneNumber: String?="",
    val contactName: String?="",
    val contactImage: String?="",
    val lastMessage: String?="",
    val lastMessageAt: String?="",
    val messageCount: Int?=0,
    val unreadCount: Int?=0,
    val isRead: Boolean?=false,
)
