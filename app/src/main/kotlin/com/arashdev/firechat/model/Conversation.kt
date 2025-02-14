package com.arashdev.firechat.model

import java.time.Instant

data class Conversation(
	val id: String = "",
	val participantIds: List<String> = emptyList(),
	val lastMessage: String = "",
	val lastMessageTime: Long = 0,
	val createdAt: Long = Instant.now().epochSecond
)

