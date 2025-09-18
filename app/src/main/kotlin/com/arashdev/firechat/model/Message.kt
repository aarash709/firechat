package com.arashdev.firechat.model

//UI layer
data class Message(
	val text: String = "",
	val senderId: String = "",
	val timestamp: Long = 0
)
