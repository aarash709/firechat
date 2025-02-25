package com.arashdev.firechat.screens

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.arashdev.firechat.Chat
import com.arashdev.firechat.model.Message
import com.arashdev.firechat.model.User
import com.arashdev.firechat.service.AuthService
import com.arashdev.firechat.utils.getConversationId
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.time.Instant

class ChatViewModel(
//	private val storageService: RemoteStorageService,
	private val authService: AuthService,
	savedStateHandle: SavedStateHandle
) : ViewModel() {

	//	private val authService = Firebase.auth
	private val storageService = Firebase.firestore

	val currentUserID: String = authService.currentUserId
	private val otherUserId = savedStateHandle.toRoute<Chat>().otherUserId
	private val conversationId =
		getConversationId(currentUserId = currentUserID, otherUserId = otherUserId)

	private val messagesCollection =
		storageService.collection("conversations/$conversationId/messages")
	private val contactInfo = storageService.collection("users")
		.document(otherUserId)

	private val _contact = MutableStateFlow(User())
	val contact: StateFlow<User> = _contact

	// State for messages
	private val _messages = mutableStateListOf<Message>()
	val messages: List<Message> = _messages

//	val messages: StateFlow<List<Message>> =
//		storageService.messages.stateIn(
//			scope = viewModelScope,
//			started = SharingStarted.WhileSubscribed(5_000),
//			initialValue = emptyList()
//		)


	init {
		Timber.e("userid : $otherUserId")
		getUserDetails()
		fetchMessages()

	}

	private fun getUserDetails() {
		viewModelScope.launch {
			contactInfo
				.addSnapshotListener { snapshot, error ->
					_contact.value = snapshot?.toObject(User::class.java)!!
				}
		}
	}

	private fun fetchMessages() {
		viewModelScope.launch {
			messagesCollection
				.orderBy("timestamp", Query.Direction.ASCENDING)
				.addSnapshotListener { snapshot, error ->
					Timber.e(error?.message)
					if (error != null) return@addSnapshotListener
					snapshot?.let {
						_messages.clear()
						_messages.addAll(it.toObjects(Message::class.java))
					}
				}
		}
	}

	fun sendMessage(text: String) {
		viewModelScope.launch {
			val time = Instant.now().epochSecond //UTC
			val message = Message(
				text = text,
				senderId = currentUserID,
				timestamp = time
			)
			messagesCollection.add(message).addOnSuccessListener {
				Timber.e("message sent successfully!")
			}.addOnFailureListener {
				Timber.e("message sent failed!")
			}.await()
			updateConversation(lastMessage = text, timeSeconds = time)
		}
	}

	private suspend fun updateConversation(lastMessage: String, timeSeconds: Long) {
		storageService.collection("conversations")
			.document(otherUserId).update(
				"lastMessage", lastMessage,
				"lastMessageTime", timeSeconds
			).addOnSuccessListener {
				Timber.e("conversation updated successfully!")
			}.addOnFailureListener {
				Timber.e(it.message)
				Timber.e("conversation could not be updated!")
			}.await()
	}
}