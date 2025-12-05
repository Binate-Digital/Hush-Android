package com.fictivestudios.hush.data.responses

data class ChatInbox(
    val _id: String?="",
    val contactId: String?="",
    val phoneNumber: String?="",
    val contactImage: String?="",
    val contactName: String?="",
    val lastMessage: String?="",
    val lastMessageAt: String?="",
    val messageCount: Int?=0,
    val unreadCount: Int?=0,
    val isRead: Boolean?=false
)

data class SmsMessage(
    val sender: String,
    val message: String,
    val timestamp: String,
    val twilioMessageSid: String,
    val _id: String
)
