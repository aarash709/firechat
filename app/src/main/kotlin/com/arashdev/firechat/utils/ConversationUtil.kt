package com.arashdev.firechat.utils

fun getConversationId(currentUserId: String, otherUserId: String): String {
	val ids = listOf(currentUserId, otherUserId).sorted()
	return "${ids[0]},${ids[1]}"
}