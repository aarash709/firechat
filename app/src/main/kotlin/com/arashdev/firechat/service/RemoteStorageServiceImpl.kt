package com.arashdev.firechat.service

import com.arashdev.firechat.model.Conversation
import com.arashdev.firechat.model.Message
import com.arashdev.firechat.model.User
import com.arashdev.firechat.utils.getConversationId
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.dataObjects
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.time.Instant


class RemoteStorageServiceImpl(private val authService: AuthService) : RemoteStorageService {

	override val users: Flow<List<User>>
		get() = Firebase.firestore.collection(CONTACTS_COLLECTION)
			.dataObjects()

	override val conversations: Flow<List<Conversation>>
		get() = Firebase.firestore.collection(CONVERSATIONS_COLLECTION)
			.whereArrayContains("participantIds", authService.currentUserId)
			.dataObjects()

	override val contacts: Flow<List<User>>
		get() = Firebase.firestore.collection(CONTACTS_COLLECTION)
			.whereArrayContains("participantIds", Firebase.auth.currentUser?.uid.orEmpty())
			.dataObjects()

	override val messages: Flow<List<Message>>
		get() = Firebase.firestore.collection(MESSAGE_COLLECTION)
			.whereArrayContains("participantIds", Firebase.auth.currentUser?.uid.orEmpty())
			.dataObjects()

	override fun observeMessages(otherContactId: String): Flow<List<Message>> {
		return Firebase.firestore.collection("conversations/$otherContactId/messages")
			.whereArrayContains("participantIds", Firebase.auth.currentUser?.uid.orEmpty())
			.dataObjects()
	}

	override suspend fun sendMessage(
		messageText: String,
		currentUserId: String,
		otherContactId: String
	) {
		val message = Message(
			text = messageText,
			senderId = currentUserId,
			timestamp = System.currentTimeMillis()
		)
		Firebase.firestore.collection("conversations/$otherContactId/messages").add(message).await()

	}

	override suspend fun createUser(userId: String, userName: String) {
		val user = hashMapOf(
			"userId" to userId,
			"name" to userName.ifEmpty { "Anonymous User" },
			"createdAt" to Instant.now().epochSecond //utc
		)
		Firebase.firestore.collection(CONTACTS_COLLECTION)
			.document(userId)
			.set(user)
			.addOnSuccessListener {
				Timber.e("User added id: $userId}")
			}
			.addOnFailureListener {
				Timber.e("cannot add user: ${it.message}")
			}.await()

	}

	override suspend fun updateUsername(userName: String, userId: String) {
		Firebase.firestore.collection(CONTACTS_COLLECTION).document(userId)
			.set(hashMapOf("name" to userName), SetOptions.merge()).await()
	}

	override suspend fun createConversation(
		message: String,
		currentUserId: String,
		otherContactId: String,
		onConversationCreationSuccessfull: () -> Unit
	) {
		val conversationId = getConversationId(currentUserId, otherContactId)
//		val conversationExists =
//			db.collection("conversations").document(conversationId).id == conversationId
//		if (conversationExists) {
//			return
//		}
		val conversation = Conversation(
			id = conversationId,
			participantIds = listOf(currentUserId, otherContactId),
			lastMessage = "",
			createdAt = Instant.now().epochSecond
		)

		// Create or update the conversation document
		Firebase.firestore.collection(CONVERSATIONS_COLLECTION)
			.document(conversationId)
			.set(conversation)
			.addOnSuccessListener {
				onConversationCreationSuccessfull()
			}

	}

	override suspend fun removeUserData(userId: String) {
		Firebase.firestore.collection(CONTACTS_COLLECTION).document(userId).delete().await()
	}

	companion object {
		private const val CONVERSATIONS_COLLECTION = "conversations"
		private const val CONTACTS_COLLECTION = "users"
		private const val MESSAGE_COLLECTION = "messages"
	}
}