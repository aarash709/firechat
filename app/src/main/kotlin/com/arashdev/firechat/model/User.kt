package com.arashdev.firechat.model

data class User(
	val userId: String = "",  // Matches Firebase UID
	val name: String = "",
	val profilePhotoBase64: String = "",
	val createdAt: Long = 0,
	val isAnonymous: Boolean = false,
	val lastSeen: String = "",
	val bio: String = ""
)