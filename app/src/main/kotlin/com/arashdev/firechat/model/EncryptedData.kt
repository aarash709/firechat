package com.arashdev.firechat.model

data class EncryptedData(
	val encryptedMessage: String,
	val encryptedAesKey: String,
	val iv: String,
)
