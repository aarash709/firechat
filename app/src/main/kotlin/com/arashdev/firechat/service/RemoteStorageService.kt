package com.arashdev.firechat.service

import com.arashdev.firechat.model.Conversation
import com.arashdev.firechat.model.EncryptedMessage
import com.arashdev.firechat.model.Message
import com.arashdev.firechat.model.User
import kotlinx.coroutines.flow.Flow

interface RemoteStorageService {

	val users: Flow<List<User>>

	val conversations: Flow<List<Conversation>>

	val contacts: Flow<List<User>>

	val messages: Flow<List<Message>>

	fun getUser(userId: String): Flow<User?>

	fun observeMessages(conversationId: String): Flow<List<EncryptedMessage>>

	suspend fun sendMessage(
		encryptedMessage: EncryptedMessage,
		conversationId: String
	)

	suspend fun createUser(userId: String, userName: String, base64PublicKey: String)

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

	fun getUserConnectedStatus(userId: String): Flow<Boolean>

	fun getUserLastSeenStatus(userId: String): Flow<Long>

	suspend fun updateProfilePhoto(base64String: String, userId: String)

	//cryptography
	suspend fun uploadPublicKey(userId: String, base64PublicKey: String)

	suspend fun getPublicKey(userId: String): ByteArray
}