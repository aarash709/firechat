package com.arashdev.firechat.screens

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.arashdev.firechat.Chat
import com.arashdev.firechat.model.Message
import com.arashdev.firechat.model.User
import com.arashdev.firechat.service.AuthService
import com.arashdev.firechat.service.RemoteStorageService
import com.arashdev.firechat.utils.getConversationId
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Instant

class ChatViewModel(
	private val storageService: RemoteStorageService,
	private val authService: AuthService,
	savedStateHandle: SavedStateHandle
) : ViewModel() {

	val currentUserID: String = authService.currentUserId

	private val otherUserId = savedStateHandle.toRoute<Chat>().otherUserId
	private val conversationId =
		getConversationId(currentUserId = currentUserID, otherUserId = otherUserId)

	val contact: StateFlow<User> = storageService
		.getUser(otherUserId)
		.mapNotNull {
			it
		}
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(5000),
			initialValue = User()
		)

	val messages: StateFlow<List<Message>> =
		storageService.observeMessages(conversationId = conversationId)
			.map { it.reversed() }
			.stateIn(
				scope = viewModelScope,
				started = SharingStarted.WhileSubscribed(5000),
				initialValue = listOf()
			)

	init {
		Timber.e("other userId : $otherUserId")
	}

	fun sendMessage(text: String) {
		viewModelScope.launch {
			val time = Instant.now().epochSecond //UTC
			val message = Message(
				text = text,
				senderId = currentUserID,
				timestamp = time
			)
			storageService.sendMessage(
				message,
				conversationId = conversationId
			)
			updateConversation(
				lastMessage = text,
				timeSeconds = time,
				conversationId = conversationId
			)
		}
	}

	private suspend fun updateConversation(
		lastMessage: String,
		timeSeconds: Long,
		conversationId: String
	) {
		storageService.updateConversation(
			conversationId = conversationId,
			lastMessage = lastMessage,
			timeSeconds = timeSeconds
		)
	}
}