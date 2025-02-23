package com.arashdev.firechat.screens

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arashdev.firechat.service.AuthService
import com.arashdev.firechat.service.RemoteStorageService
import kotlinx.coroutines.launch
import timber.log.Timber

class LoginViewModel(
	private val auth: AuthService,
	private val firestore: RemoteStorageService
) : ViewModel() {

	private val _uiState = mutableStateOf<AuthUiState>(AuthUiState.Initial)
	val uiState: State<AuthUiState> = _uiState

	/**
		new user for login
	 */
	fun signupWithEmailAndPassword(email: String, password: String, userName: String) {
		viewModelScope.launch {
			try {
				auth.createNewAccount(email = email, password = password)
				auth.updateDisplayName(name = userName)
				firestore.createUser(userId = auth.currentUserId, userName = userName)
			} catch (e: Exception) {
				Timber.e("Signup filed${e.message}")
			}
		}
	}

	/**
		existing user for login
	 */
	fun signInWithEmailAndPassword(email: String, password: String) {
		_uiState.value = AuthUiState.Loading
		viewModelScope.launch {
			try {
				auth.signInWithEmailAndPassword(email = email, password = password)
				_uiState.value = AuthUiState.Success
			} catch (e: Exception) {
				Timber.e("Signup filed${e.message}")
				_uiState.value = AuthUiState.Error(e.message!!)

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
				firestore.createUser(auth.currentUserId) // default user name is anonymous
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