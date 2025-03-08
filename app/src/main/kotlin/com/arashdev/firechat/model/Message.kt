package com.arashdev.firechat.model

data class Message(
	val id: String = "",
	val text: String = "",
	val encryptedMessage: ByteArray,
	val encryptedAesKey: ByteArray,
	val iv: ByteArray,
	val senderId: String = "",
	val timestamp: Long = System.currentTimeMillis()
)
