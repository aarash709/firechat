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

	val currentUserId = authService.currentUserId
	val conversations: StateFlow<List<Conversation>> =
		storageService.conversations.map { conversationList ->
			conversationList.map { conversation ->
				//get name using contact id
				val contactId =
					conversation.participantIds.first { id -> id != authService.currentUserId }
				val contactName =
					Firebase.firestore.collection("users").document(contactId).dataObjects<User>()
						.first()?.name
				val contactUser =
					storageService.getUser(conversation.participantIds.firstOrNull { it != currentUserId }
						.orEmpty())
						.first()
				conversation.copy(
					contactName = contactName!!,
					contactPhotoBase64 = contactUser?.profilePhotoBase64.orEmpty()
				)
			}
		}.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(5_000),
			initialValue = emptyList()
		)

	val user = authService.currentUser
		.map {
			val user = storageService.getUser(authService.currentUserId).first()
			it.copy(profilePhotoBase64 = user?.profilePhotoBase64.orEmpty())
		}.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(5_000),
			initialValue = User()
		)

}

