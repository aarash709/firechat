package com.arashdev.firechat.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.runtime.mutableStateOf
import androidx.core.graphics.scale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arashdev.firechat.model.User
import com.arashdev.firechat.service.AuthService
import com.arashdev.firechat.service.RemoteStorageService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.ByteArrayOutputStream

class SettingsViewModel(
	private val auth: AuthService,
	private val remoteStorage: RemoteStorageService
) : ViewModel() {

	val userId = auth.currentUserId

	val user = auth.currentUser.map {
//		val photoByteArray = loadCurrentProfilePicture(userId)
		val user = remoteStorage.getUser(userId).first()
		Timber.e("photoarray is  empty: ${user?.profilePhotoBase64?.isEmpty()}")
		it.copy(profilePhotoBase64 = user?.profilePhotoBase64.orEmpty())
	}.stateIn(
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
				updateDisplayName(name = displayName)
			} catch (e: Exception) {
				Timber.e("Account link Error ${e.message}")
			}
		}
	}

	private suspend fun updateDisplayName(name: String) {
		auth.updateDisplayName(name = name)
		remoteStorage.updateUsername(userName = name, userId = userId)

	}

	private fun loadCurrentProfilePicture(userId: String): String {
		val currentUserProfilePhotoBase64 = mutableStateOf("")
		viewModelScope.launch {
			try {
				val user = remoteStorage.getUser(userId).first()
				Timber.e(user?.profilePhotoBase64)
				currentUserProfilePhotoBase64.value = user?.profilePhotoBase64 ?: ""
			} catch (e: Exception) {
				// Handle error
				Timber.e(e.message)
			}
		}
		return currentUserProfilePhotoBase64.value
	}

	fun uploadProfilePhoto(uri: String, userId: String) {
		viewModelScope.launch {
			try {
//				val stream = File(uri).inputStream()
				Timber.e(uri)
				val bitmap = BitmapFactory.decodeFile(uri)
				val base64PhotoString = bitmapToBase64String(bitmap.scale(100, 100, false))
				remoteStorage.updateProfilePhoto(base64String = base64PhotoString, userId = userId)
			} catch (e: Exception) {
				Timber.e(e.message)
			}
		}
	}

	private fun bitmapToBase64String(bitmap: Bitmap): String {
		val outputStream = ByteArrayOutputStream()
		bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
		val byteArray = outputStream.toByteArray()
		return Base64.encodeToString(byteArray, Base64.DEFAULT)
	}
}