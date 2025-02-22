package com.arashdev.firechat.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arashdev.firechat.model.User
import com.arashdev.firechat.service.AuthService
import com.arashdev.firechat.service.RemoteStorageService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

class SettingsViewModel(
	private val auth: AuthService,
	private val remoteStorage: RemoteStorageService
) : ViewModel() {

	private val userId = auth.currentUserId

	val user = auth.currentUser.stateIn(
		scope = viewModelScope,
		started = SharingStarted.WhileSubscribed(5_000L),
		initialValue = User()
	)

	fun deleteAccount() {
		viewModelScope.launch {
			auth.deleteAccount()
			remoteStorage.removeUserData(userId)
		}
	}

	fun logout() {
		viewModelScope.launch {
			auth.signOut()
		}
	}

	fun linkAccount(email: String, password: String, displayName: String) {
		viewModelScope.launch {
			try {
				auth.linkAccount(email = email, password = password)
				auth.updateDisplayName(displayName)
				remoteStorage.updateUsername(displayName, userId)
			} catch (e: Exception) {
				Timber.e("Account link Error ${e.message}")
			}
		}
	}

	fun updateDisplayName(name: String) {
		viewModelScope.launch {
			remoteStorage.updateUsername(userName = name, userId = userId)
		}
	}
}