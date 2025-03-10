package com.arashdev.firechat.model

data class EncryptedData(
	val encryptedMessage: ByteArray,
	val encryptedAesKey: ByteArray,
	val iv: ByteArray,
)
