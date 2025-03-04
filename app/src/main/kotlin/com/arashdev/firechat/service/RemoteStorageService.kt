package com.arashdev.firechat.service

import com.arashdev.firechat.model.Conversation
import com.arashdev.firechat.model.Message
import com.arashdev.firechat.model.User
import kotlinx.coroutines.flow.Flow

interface RemoteStorageService {

	val users: Flow<List<User>>

	val conversations: Flow<List<Conversation>>

	val contacts: Flow<List<User>>

	val messages: Flow<List<Message>>

	fun getUser(userId: String): Flow<User?>

	fun observeMessages(conversationId: String): Flow<List<Message>>

	suspend fun sendMessage(
		message: Message,
		conversationId: String
	)

	suspend fun createUser(userId: String, userName: String = "")

	suspend fun updateUsername(userName: String, userId: String)

	suspend fun createNewConversation(
		conversationId: String,
		currentUserId: String,
		otherContactId: String,
		onConversationCreationSuccessful: () -> Unit
	)

	suspend fun removeUserData(userId: String)

	suspend fun conversationExists(conversationId: String): Boolean

	suspend fun updateConversation(conversationId: String, lastMessage: String, timeSeconds: Long)

	suspend fun updateUserPresenceStatus(isOnline: Boolean)

	fun getUserPresenceStatus(userId: String): Flow<Pair<Boolean, Long>>
}