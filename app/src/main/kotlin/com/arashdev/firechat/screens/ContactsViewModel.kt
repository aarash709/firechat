package com.arashdev.firechat.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arashdev.firechat.model.Conversation
import com.arashdev.firechat.model.User
import com.arashdev.firechat.service.AuthService
import com.arashdev.firechat.service.RemoteStorageService
import com.arashdev.firechat.utils.getConversationId
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.time.Instant

class ContactsListViewModel(
	private val storageService: RemoteStorageService,
	private val authService: AuthService
) : ViewModel() {
	private val db = Firebase.firestore
	private val currentUserId = authService.currentUserId

	private val conversationCollection = db.collection("conversations")

	val contacts: StateFlow<List<User>> = storageService.users.map { users ->
		// Exclude current user
		users.filterNot { it.userId == currentUserId }
	}.stateIn(
		scope = viewModelScope,
		started = SharingStarted.WhileSubscribed(5000),
		initialValue = listOf()
	)

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
				conversationExists =
					conversationCollection.document(conversationId).id == conversationId
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
				val contactName = contacts.value.first { it.userId == otherUserId }.name
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