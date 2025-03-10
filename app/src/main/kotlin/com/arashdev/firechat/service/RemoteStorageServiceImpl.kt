package com.arashdev.firechat.service

import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import com.arashdev.firechat.model.Conversation
import com.arashdev.firechat.model.Message
import com.arashdev.firechat.model.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
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
	private val firestore = Firebase.firestore

	override suspend fun updateUserPresenceStatus(isOnline: Boolean) {
		database.child("users").child(authService.currentUserId).child("connected")
			.setValue(isOnline).await()
		database.child("users").child(authService.currentUserId).child("lastSeen")
			.setValue(ServerValue.TIMESTAMP).await()
	}

	override fun getUserLastSeenStatus(userId: String): Flow<Long> = callbackFlow {
		val lastSeenState = mutableLongStateOf(0L)

		val lastSeenRef = database
			.child("users")
			.child(userId)
			.child("lastSeen")

		val lastSeenListener = object : ValueEventListener {
			override fun onDataChange(snapshot: DataSnapshot) {
				val lastSeen = snapshot.getValue(Long::class.java) ?: 0L
				lastSeenState.longValue = lastSeen
				trySend(lastSeenState.longValue) // Emit the updated state
				Timber.e("last seen: $lastSeen")
			}

			override fun onCancelled(error: DatabaseError) {
				Timber.e("Last seen listener cancelled: ${error.message}")
			}
		}

		lastSeenRef.addValueEventListener(lastSeenListener)

		awaitClose {
			lastSeenRef.removeEventListener(lastSeenListener)
		}
	}

	override fun getUserConnectedStatus(userId: String): Flow<Boolean> =
		callbackFlow {
			val isConnectedState = mutableStateOf(false)

			// Firebase references for connected and lastSeen
			val connectedRef = database
				.child("users")
				.child(userId)
				.child("connected")

			val connectedListener = object : ValueEventListener {
				override fun onDataChange(snapshot: DataSnapshot) {
					val isConnected = snapshot.getValue(Boolean::class.java) ?: false
					isConnectedState.value = isConnected
					trySend(isConnectedState.value) // Emit the updated state
				}

				override fun onCancelled(error: DatabaseError) {
					Timber.e("Connected listener cancelled: ${error.message}")
				}
			}

			connectedRef.addValueEventListener(connectedListener)

			awaitClose {
				connectedRef.removeEventListener(connectedListener)
			}
		}

	override suspend fun updateProfilePhoto(base64String: String, userId: String) {
		val data = hashMapOf("profilePhotoBase64" to base64String)
		firestore.collection(USERS_COLLECTION).document(userId)
			.set(data, SetOptions.merge()).await()
	}

	override val users: Flow<List<User>>
		get() = firestore.collection(USERS_COLLECTION)
			.dataObjects()

	override val conversations: Flow<List<Conversation>>
		get() = firestore.collection(CONVERSATIONS_COLLECTION)
			.whereArrayContains("participantIds", authService.currentUserId)
			.dataObjects()

	override val contacts: Flow<List<User>>
		get() = firestore.collection(USERS_COLLECTION)
			.whereArrayContains("participantIds", Firebase.auth.currentUser?.uid.orEmpty())
			.dataObjects()

	override val messages: Flow<List<Message>>
		get() = firestore.collection(MESSAGE_COLLECTION)
			.whereArrayContains("participantIds", Firebase.auth.currentUser?.uid.orEmpty())
			.dataObjects()

	override fun getUser(userId: String): Flow<User?> {
		return firestore.collection(USERS_COLLECTION).document(userId).dataObjects()
	}

	override fun observeMessages(conversationId: String): Flow<List<Message>> {
		return firestore.collection("conversations/$conversationId/messages")
			.orderBy("timestamp", Query.Direction.ASCENDING)
			.dataObjects()
	}

	override suspend fun sendMessage(
		message: Message,
		conversationId: String
	) {
		firestore
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
		firestore.collection("conversations")
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

	override suspend fun createUser(userId: String, userName: String, base64PublicKey: String) {
		val user = User(
			userId = userId,
			name = userName.ifEmpty { "Anonymous User" },
			profilePhotoBase64 = "",
			base64PublicKey = base64PublicKey.ifEmpty { "" },
			createdAt = Instant.now().epochSecond, //utc
			bio = "",
		)
		firestore.collection(USERS_COLLECTION)
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
		firestore.collection(USERS_COLLECTION).document(userId)
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
		firestore.collection(CONVERSATIONS_COLLECTION)
			.document(conversationId)
			.set(conversation)
			.addOnSuccessListener {
				onConversationCreationSuccessful()
			}.await()
	}

	override suspend fun removeUserData(userId: String) {
		firestore.collection(USERS_COLLECTION).document(userId).delete().await()
	}

	override suspend fun conversationExists(conversationId: String): Boolean {
		return firestore.collection(CONVERSATIONS_COLLECTION)
			.document(conversationId).get().await().id == conversationId
	}

	override suspend fun uploadPublicKey(userId: String, base64PublicKey: String) {
		firestore.collection("users").document(userId).set(
			mapOf("publicKeyBase64" to base64PublicKey), SetOptions.merge()
		)
	}

	override suspend fun getPublicKey(userId: String): ByteArray {
		val key = firestore.collection("users").document(userId).get().await().get("publicKey")
		return key as ByteArray
	}

	companion object {
		private const val CONVERSATIONS_COLLECTION = "conversations"
		private const val USERS_COLLECTION = "users"
		private const val MESSAGE_COLLECTION = "messages"
	}
}
