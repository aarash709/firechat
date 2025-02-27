package com.arashdev.firechat.utils

fun getConversationId(currentUserId: String, otherUserId: String): String {
	require(currentUserId.isNotEmpty() || otherUserId.isNotEmpty()) { "currentUserId or otherUserId should not be empty" }
	val ids = listOf(currentUserId, otherUserId).sorted()
	return "${ids[0]},${ids[1]}"
}