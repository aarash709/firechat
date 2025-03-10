package com.arashdev.firechat.screens

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arashdev.firechat.security.KeyManager
import com.arashdev.firechat.service.AuthService
import com.arashdev.firechat.service.RemoteStorageService
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Base64

class LoginViewModel(
	private val auth: AuthService,
	private val firestore: RemoteStorageService
) : ViewModel() {

	private val _uiState = mutableStateOf<AuthUiState>(AuthUiState.Initial)
	val uiState: State<AuthUiState> = _uiState

	/**
	new user authentication
	 */
	fun signupWithEmailAndPassword(email: String, password: String, userName: String) {
		viewModelScope.launch {
			try {
				val base64PublicKey = getUserPublicKeyString()
				auth.createNewAccount(email = email, password = password)
				auth.updateDisplayName(name = userName)
				firestore.createUser(
					userId = auth.currentUserId,
					userName = userName,
					base64PublicKey = base64PublicKey
				)
				firestore.uploadPublicKey(
					userId = auth.currentUserId,
					base64PublicKey = base64PublicKey
				)
			} catch (e: Exception) {
				Timber.e("Signup filed${e.message}")
			}
		}
	}

	/**
	existing user login
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
				val base64PublicKey = getUserPublicKeyString()
				auth.createAnonymousAccount()
				firestore.createUser(
					auth.currentUserId,
					base64PublicKey = base64PublicKey,
					userName = "Anonymous User"
				) // default user name is anonymous
				_uiState.value = AuthUiState.Success
			} catch (e: Exception) {
				_uiState.value = AuthUiState.Error(e.message ?: "Login failed")
			}
		}
	}

	private fun getUserPublicKeyString(): String {
		val publicKey = KeyManager.getPublicKey()?.encoded
		return Base64.getEncoder().encodeToString(publicKey)
	}
}

sealed class AuthUiState {
	data object Initial : AuthUiState()
	data object Loading : AuthUiState()
	data object Success : AuthUiState()
	data class Error(val message: String) : AuthUiState()
}