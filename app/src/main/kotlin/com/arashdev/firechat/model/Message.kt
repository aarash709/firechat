package com.arashdev.firechat.model

data class Message(
	val id: String = "",
	val text: String = "",
	val senderId: String = "",
	val timestamp: Long = System.currentTimeMillis()
)
