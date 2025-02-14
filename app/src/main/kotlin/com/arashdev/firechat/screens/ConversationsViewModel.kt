package com.arashdev.firechat.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arashdev.firechat.model.Conversation
import com.arashdev.firechat.service.RemoteStorageService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ConversationsViewModel(private val storageService: RemoteStorageService) : ViewModel() {

	val conversations: StateFlow<List<Conversation>> = storageService.conversations.stateIn(
		scope = viewModelScope,
		started = SharingStarted.WhileSubscribed(5_000),
		initialValue = emptyList()
	)

}

