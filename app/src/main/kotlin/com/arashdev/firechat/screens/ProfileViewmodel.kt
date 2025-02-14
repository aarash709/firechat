package com.arashdev.firechat.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arashdev.firechat.service.AuthService
import com.arashdev.firechat.service.RemoteStorageService
import kotlinx.coroutines.launch
import timber.log.Timber

class ProfileViewmodel(private val auth: AuthService, firestore: RemoteStorageService) :
	ViewModel() {

	val currentUser = auth.currentUser
	val currentUserId = auth.currentUserId

	fun linkAccount(email: String, password: String) {
		viewModelScope.launch {
			try {
				auth.linkAccount(email = email, password = password)
			} catch (e: Exception) {
				Timber.e("link account failed, reason: ${e.message}")
			}
		}
	}

	fun signOut(onSignOut: () -> Unit) {
		viewModelScope.launch {
			try {
				auth.signOut()
				//go to login page and clear backstack
				onSignOut()
			} catch (e: Exception) {
				//show error and do nothing
				Timber.e("sign out failed, reason: ${e.message}")
			}
		}
	}
}