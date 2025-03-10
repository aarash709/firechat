package com.arashdev.firechat.model

//backend DTO
data class EncryptedMessage(
	val senderId: String,
	val encryptedMessage: String,
	val encryptedAesKey: String,
	val iv: String,
	val timestamp: Long = System.currentTimeMillis()
)