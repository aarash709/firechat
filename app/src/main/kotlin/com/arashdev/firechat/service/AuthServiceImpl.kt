package com.arashdev.firechat.service

import android.net.Uri
import com.arashdev.firechat.model.User
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthServiceImpl : AuthService {
	private val auth = Firebase.auth

	override val currentUserId: String
		get() = auth.currentUser?.uid.orEmpty()

	override val currentUser: Flow<User>
		get() = callbackFlow {
			val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
				trySend(firebaseAuth.currentUser.toUser())
			}
			auth.addAuthStateListener(authListener)
			awaitClose {
				auth.removeAuthStateListener(authListener)
			}
		}

	override fun hasUser(): Boolean {
		return auth.currentUser != null
	}

	override val doesUserExists: Boolean
		get() = auth.currentUser != null

	override suspend fun createAnonymousAccount() {
		auth.signInAnonymously().await()
	}

	override suspend fun createNewAccount(email: String, password: String) {
		auth.createUserWithEmailAndPassword(email, password).await()
	}

	override suspend fun deleteAccount() {
		auth.currentUser?.delete()?.await()
	}

	override suspend fun linkAccount(email: String, password: String) {
		val credential = EmailAuthProvider.getCredential(email, password)
		auth.currentUser!!.linkWithCredential(credential).await()
	}

	override suspend fun signInWithEmailAndPassword(email: String, password: String) {
		val provider = EmailAuthProvider.getCredential(email, password)
		auth.signInWithCredential(provider).await()
	}

	override suspend fun signOut() {
		auth.signOut()
	}

	override suspend fun updateDisplayName(name: String) {
		auth.currentUser?.updateProfile(
			UserProfileChangeRequest.Builder()
				.setDisplayName(name).build()
		)
	}

	override suspend fun updatePhotoUri(uri: Uri) {
		val userProfileChangeRequest = UserProfileChangeRequest.Builder().setPhotoUri(uri).build()
		auth.currentUser?.updateProfile(userProfileChangeRequest)
	}

	private fun FirebaseUser?.toUser(): User {
		val userName = if (this?.isAnonymous == true) {
			"Anonymous"
		} else {
			this?.displayName.orEmpty()
		}
		return User(
			userId = this?.uid ?: "",
			name = userName,
			profilePhotoBase64 = "",
			createdAt = this?.metadata?.creationTimestamp ?: 0,
			isAnonymous = this?.isAnonymous ?: false,
			bio = ""
		)
	}
}