package com.arashdev.firechat.screens

import android.text.format.DateUtils
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arashdev.firechat.model.User
import com.arashdev.firechat.service.AuthService
import com.arashdev.firechat.service.RemoteStorageService
import com.arashdev.firechat.utils.getConversationId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
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

	@OptIn(ExperimentalCoroutinesApi::class)
	val contacts: StateFlow<List<User>> = storageService.users.map { users ->
		// Exclude current user
		users.filterNot { it.userId == currentUserId }
	}.map { filteredUsers ->
		// Create a flow for each user's presence status
		filteredUsers.map { user ->
			storageService.getUserPresenceStatus(user.userId).map { presence ->
				val isOnline = presence.first
				val lastSeen = if (isOnline) {
					"Online"
				} else {
					buildString {
						append("last seen")
						append(" ")
						append(
							DateUtils.getRelativeTimeSpanString(
								presence.second,
								System.currentTimeMillis(),
								DateUtils.SECOND_IN_MILLIS,
							)
						)
					}
				}
				user.copy(userId = lastSeen)
			}
		}
	}
		.flatMapLatest { presenceFlows ->
			// Combine all presence flows into a single flow emitting a list of UserWithPresence
			combine(presenceFlows) { it.toList() }
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

//sealed class ConversationState {
//	data object Idle : ConversationState()
//	data object Loading : ConversationState()
//	data class Success(val conversationId: String) : ConversationState()
//	data class Error(val message: String) : ConversationState()
//}