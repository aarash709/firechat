package com.arashdev.firechat.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arashdev.firechat.model.Conversation
import com.arashdev.firechat.model.User
import com.arashdev.firechat.service.AuthService
import com.arashdev.firechat.service.RemoteStorageService
import com.google.firebase.firestore.dataObjects
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ConversationsViewModel(
	private val storageService: RemoteStorageService,
	private val authService: AuthService
) : ViewModel() {

	val conversations: StateFlow<List<Conversation>> =
		storageService.conversations.map { conversationList ->
			conversationList.map { conversation ->
				//get name using contact id
				val contactId =
					conversation.participantIds.first { id -> id != authService.currentUserId }
				val contactName =
					Firebase.firestore.collection("users").document(contactId).dataObjects<User>()
						.first()?.name

				conversation.copy(contactName = contactName!!)
			}
		}.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(5_000),
			initialValue = emptyList()
		)

	val user = authService.currentUser.stateIn(
		scope = viewModelScope,
		started = SharingStarted.WhileSubscribed(5_000),
		initialValue = User()
	)

}

