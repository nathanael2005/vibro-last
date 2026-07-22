package com.example.model

data class ChatMessage(
    val sender: String,
    val content: String,
    val time: String
)

data class Review(
    val sellerName: String,
    val reviewerName: String,
    val rating: Int,
    val comment: String,
    val timeAgo: String
)

data class ChatThread(
    val id: String,
    val senderName: String,
    val lastMessage: String,
    val time: String,
    val messages: List<ChatMessage>
)
