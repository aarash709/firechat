package com.arashdev.firechat.model

data class User(
	val userId: String = "",  // Matches Firebase UID
	val name: String = "",
	val createdAt: Long = 0
)