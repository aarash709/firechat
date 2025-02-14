package com.arashdev.firechat.screens

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arashdev.firechat.service.AuthService
import com.arashdev.firechat.service.RemoteStorageService
import kotlinx.coroutines.launch

class AuthViewModel(
	private val auth: AuthService,
	private val firestore: RemoteStorageService
) : ViewModel() {

	private val _uiState = mutableStateOf<AuthUiState>(AuthUiState.Initial)
	val uiState: State<AuthUiState> = _uiState

	fun signInWithEmailAndPassword(email: String, password: String) {
		viewModelScope.launch {
			try {
				auth.signIn(email = email, password = password)
//			firestore.createUser(auth.currentUserId)
				//update user name and other data
			} catch (e: Exception) {
				TODO("Not yet implemented")
			}
		}
	}

	fun signInAnonymously() {
		viewModelScope.launch {
			_uiState.value = AuthUiState.Loading
			try {
				if (auth.hasUser()) {
					_uiState.value = AuthUiState.Success
					return@launch
				}
				auth.createAnonymousAccount()
				firestore.createUser(auth.currentUserId)
				_uiState.value = AuthUiState.Success
			} catch (e: Exception) {
				_uiState.value = AuthUiState.Error(e.message ?: "Login failed")
			}
		}
	}
}

sealed class AuthUiState {
	data object Initial : AuthUiState()
	data object Loading : AuthUiState()
	data object Success : AuthUiState()
	data class Error(val message: String) : AuthUiState()
}