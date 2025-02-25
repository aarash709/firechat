package com.arashdev.firechat.screens

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arashdev.firechat.model.Conversation
import com.arashdev.firechat.model.User
import com.arashdev.firechat.service.RemoteStorageService
import com.arashdev.firechat.utils.getConversationId
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.time.Instant

class ContactsListViewModel(private val storageService: RemoteStorageService) : ViewModel() {
	private val db = Firebase.firestore
	private val auth = Firebase.auth
	private val currentUserId = auth.currentUser?.uid

	// List of all users except the current user
	private val _users = mutableStateListOf<User>()
	val contacts: List<User> = _users

	init {
		fetchUsers()
	}

	private fun fetchUsers() {
		db.collection("users")
			.addSnapshotListener { snapshot, error ->
				if (error != null) return@addSnapshotListener
				snapshot?.let {
					_users.clear()
					_users.addAll(it.toObjects(User::class.java)
						.filterNot { user ->
							user.userId == currentUserId // Exclude current user
						})
				}
			}
	}

	fun createConversation(
		otherUserId: String,
		onNavigateToChat: (conversationId: String) -> Unit
	) {
		viewModelScope.launch {
			val currentUserId = currentUserId ?: return@launch
			val conversationId = getConversationId(currentUserId, otherUserId)
			var conversationExists: Boolean? = null

			//check if there is a conversation with the given id
			Timber.e("created conversation ID:$conversationId")
			try {
				Timber.e("Checking conversation existence!")
				val collection = db.collection("conversations").document(conversationId).get()
				conversationExists = collection.await().exists()
			} catch (e: Exception) {
				Timber.e("failed to check conversation reason:${e.message}")
			}

			if (conversationExists == true) {
				Timber.e("conversation exists! navigating to chat!")
				db.collection("conversations")
					.whereArrayContains("participantIds", currentUserId).get()
					.addOnSuccessListener {
						onNavigateToChat(conversationId)
					}.await()
				return@launch
			} else {
				Timber.e("Conversation does not exist! creating a new conversation!")
				val contactName = contacts.first { it.userId == otherUserId }.name
				val conversation = Conversation(
					id = conversationId,
					participantIds = listOf(currentUserId, otherUserId),
					contactName = contactName,
					lastMessage = "",
					lastMessageTime = Instant.now().epochSecond,
					createdAt = Instant.now().epochSecond
				)

				db.collection("conversations")
					.document(conversationId)
					.set(conversation)
					.addOnSuccessListener {
						Timber.e("Conversation created id: $conversationId!")
						onNavigateToChat(conversationId)
					}
					.addOnFailureListener {
						Timber.e("failed to create conversation!")
					}.await()
			}
		}
	}

	fun validateUserExists(otherUserId: String, onSuccess: () -> Unit) {
		db.collection("users")
			.document(otherUserId).get()
			.addOnSuccessListener { snapshot ->
				if (snapshot.exists()) {
					onSuccess()
				} else {
					// Show error: "User does not exist"
				}
			}
	}
}

sealed class ConversationState {
	data object Idle : ConversationState()
	data object Loading : ConversationState()
	data class Success(val conversationId: String) : ConversationState()
	data class Error(val message: String) : ConversationState()
}