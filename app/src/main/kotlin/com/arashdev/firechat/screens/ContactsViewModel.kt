package com.arashdev.firechat.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arashdev.firechat.model.User
import com.arashdev.firechat.service.AuthService
import com.arashdev.firechat.service.RemoteStorageService
import com.arashdev.firechat.utils.getConversationId
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

class ContactsListViewModel(
	private val storageService: RemoteStorageService,
	private val authService: AuthService
) : ViewModel() {

	private val currentUser = authService.currentUser.stateIn(
		scope = viewModelScope,
		started = SharingStarted.WhileSubscribed(5000),
		initialValue = User()
	)

	private val currentUserId = authService.currentUserId

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
			val currentUserId = currentUserId
			val conversationId = getConversationId(currentUserId, otherUserId)
			var conversationExists: Boolean? = null

			//check if there is a conversation with the given id
			Timber.e("created conversation ID:$conversationId")
			try {
				Timber.e("Checking conversation existence!")
				conversationExists =
					storageService.conversationExists(conversationId)
			} catch (e: Exception) {
				Timber.e("failed to check conversation reason:${e.message}")
			}

			if (conversationExists == true) {
				Timber.e("conversation exists! navigating to chat!")
				onNavigateToChat(conversationId)
				return@launch
			} else {
				Timber.e("Conversation does not exist! creating a new conversation!")
//				val contactName = contacts.value.first { it.userId == otherUserId }.name
//				val participantsNames =
//					mapOf(currentUserId to currentUser.value.name, otherUserId to contactName)
				storageService.createNewConversation(
					conversationId = conversationId,
					currentUserId = currentUserId,
					otherContactId = otherUserId,
					onConversationCreationSuccessful = {
						Timber.e("navigating to chat!")
						onNavigateToChat(conversationId)
						Timber.e("Conversation created id: $conversationId!")
					})
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