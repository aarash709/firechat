package com.arashdev.firechat.screens

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.arashdev.firechat.Chat
import com.arashdev.firechat.model.Message
import com.arashdev.firechat.service.RemoteStorageService
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.time.Instant

class ChatViewModel(
	private val storageService: RemoteStorageService,
	savedStateHandle: SavedStateHandle
) : ViewModel() {

	private val auth = Firebase.auth
	private val db = Firebase.firestore

	val currentUserID: String = auth.currentUser!!.uid
	private val conversationId = savedStateHandle.toRoute<Chat>().otherUserID
//	private val uniqueID =
//		getConversationId(currentUserId = currentUserID, otherUserId = otherUserID)

	private val messagesCollection = db.collection("conversations/$conversationId/messages")

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
		Timber.e("userid : $conversationId")
		fetchMessages()
	}

	private fun fetchMessages() {
		viewModelScope.launch {
			messagesCollection
				.orderBy("timestamp", Query.Direction.ASCENDING)
//				.get()
//				.addOnSuccessListener {
//					val objects = it.toObjects<Message>()
//					_messages.clear()
//					_messages.addAll(objects)
//				}.addOnFailureListener { exception ->
//					Timber.e(exception.message)
//				}.await()
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
		 db.collection("conversations")
			.document(conversationId).update(
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