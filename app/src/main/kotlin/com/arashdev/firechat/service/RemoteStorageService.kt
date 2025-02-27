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

	fun observeMessages(otherContactId: String): Flow<List<Message>>

	suspend fun sendMessage(
		messageText: String, currentUserId: String,
		otherContactId: String
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

}