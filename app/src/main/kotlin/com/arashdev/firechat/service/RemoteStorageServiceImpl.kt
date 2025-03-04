package com.arashdev.firechat.service

import androidx.compose.runtime.mutableStateOf
import com.arashdev.firechat.model.Conversation
import com.arashdev.firechat.model.Message
import com.arashdev.firechat.model.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.dataObjects
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.time.Instant


class RemoteStorageServiceImpl(private val authService: AuthService) : RemoteStorageService {
	private val database = Firebase.database.reference

	override suspend fun updateUserPresenceStatus(isOnline: Boolean) {
		database.child("users").child(authService.currentUserId).child("connected")
			.setValue(isOnline).await()
		database.child("users").child(authService.currentUserId).child("lastSeen")
			.setValue(ServerValue.TIMESTAMP).await()
	}

	override fun getUserPresenceStatus(userId: String): Flow<Pair<Boolean, Long>> =
		callbackFlow {
			val presence = mutableStateOf(Pair(false, 0L))

			// Firebase references for connected and lastSeen
			val connectedRef = database.child("users").child(userId).child("connected")
			val lastSeenRef = database.child("users").child(userId).child("lastSeen")

			// Listener for online status
			val connectedListener = object : ValueEventListener {
				override fun onDataChange(snapshot: DataSnapshot) {
					val isOnline = snapshot.getValue<Boolean>() ?: false
					presence.value = Pair(isOnline, presence.value.second)
					trySend(presence.value) // Emit the updated state
				}

				override fun onCancelled(error: DatabaseError) {
					Timber.e("Connected listener cancelled: ${error.message}")
				}
			}

			// Listener for last seen timestamp
			val lastSeenListener = object : ValueEventListener {
				override fun onDataChange(snapshot: DataSnapshot) {
					val lastSeen = snapshot.getValue<Long>() ?: 0L
					presence.value = Pair(presence.value.first, lastSeen)
					trySend(presence.value) // Emit the updated state
				}

				override fun onCancelled(error: DatabaseError) {
					Timber.e("Last seen listener cancelled: ${error.message}")
				}
			}

			// Attach listeners
			connectedRef.addValueEventListener(connectedListener)
			lastSeenRef.addValueEventListener(lastSeenListener)

			// Cleanup when Flow collection stops
			awaitClose {
				connectedRef.removeEventListener(connectedListener)
				lastSeenRef.removeEventListener(lastSeenListener)
			}
		}

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

	override fun getUser(userId: String): Flow<User?> {
		return Firebase.firestore.collection(CONTACTS_COLLECTION).document(userId).dataObjects()
	}

	override fun observeMessages(conversationId: String): Flow<List<Message>> {
		return Firebase.firestore.collection("conversations/$conversationId/messages")
			.orderBy("timestamp", Query.Direction.ASCENDING)
			.dataObjects()
	}

	override suspend fun sendMessage(
		message: Message,
		conversationId: String
	) {
		Firebase.firestore
			.collection("conversations/$conversationId/messages")
			.add(message)
			.addOnSuccessListener {
				Timber.e("message sent successfully!")
			}.addOnFailureListener {
				Timber.e("message sent failed!")
			}.await()
	}

	override suspend fun updateConversation(
		conversationId: String,
		lastMessage: String,
		timeSeconds: Long
	) {
		Firebase.firestore.collection("conversations")
			.document(conversationId).update(
				"lastMessage", lastMessage,
				"lastMessageTime", timeSeconds
			).addOnSuccessListener {
				Timber.e("conversation updated successfully!")
			}.addOnFailureListener {
				Timber.e("conversation could not be updated!")
				Timber.e("reason: ${it.message}")
			}.await()
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

	override suspend fun createNewConversation(
		conversationId: String,
		currentUserId: String,
		otherContactId: String,
		onConversationCreationSuccessful: () -> Unit
	) {
		val conversation = Conversation(
			id = conversationId,
			participantIds = listOf(currentUserId, otherContactId),
			lastMessage = "",
			contactName = "",
			createdAt = Instant.now().epochSecond,
			lastMessageTime = Instant.now().epochSecond
		)
		// Create or update the conversation document
		Firebase.firestore.collection(CONVERSATIONS_COLLECTION)
			.document(conversationId)
			.set(conversation)
			.addOnSuccessListener {
				onConversationCreationSuccessful()
			}.await()
	}

	override suspend fun removeUserData(userId: String) {
		Firebase.firestore.collection(CONTACTS_COLLECTION).document(userId).delete().await()
	}

	override suspend fun conversationExists(conversationId: String): Boolean {
		return Firebase.firestore.collection(CONVERSATIONS_COLLECTION)
			.document(conversationId).get().await().id == conversationId
	}

	companion object {
		private const val CONVERSATIONS_COLLECTION = "conversations"
		private const val CONTACTS_COLLECTION = "users"
		private const val MESSAGE_COLLECTION = "messages"
	}
}