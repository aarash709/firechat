package com.arashdev.firechat.service

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

	override val currentUserId: String
		get() = Firebase.auth.currentUser?.uid.orEmpty()

	override val currentUser: Flow<User>
		get() = callbackFlow {
			val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
				trySend(firebaseAuth.currentUser.toUser())
			}
			Firebase.auth.addAuthStateListener(authListener)
			awaitClose {
				Firebase.auth.removeAuthStateListener(authListener)
			}
		}

	override fun hasUser(): Boolean {
		return Firebase.auth.currentUser != null
	}

	override val doesUserExists: Boolean
		get() = Firebase.auth.currentUser != null

	override suspend fun createAnonymousAccount() {
		Firebase.auth.signInAnonymously().await()
	}

	override suspend fun deleteAccount() {
		Firebase.auth.currentUser?.delete()?.await()
	}

	override suspend fun linkAccount(email: String, password: String) {
		val credential = EmailAuthProvider.getCredential(email, password)
		Firebase.auth.currentUser!!.linkWithCredential(credential).await()
	}

	override suspend fun signIn(email: String, password: String) {
		Firebase.auth.signInWithEmailAndPassword(email, password).await()
	}

	override suspend fun signOut() {
		Firebase.auth.signOut()
	}

	override suspend fun updateDisplayName(name: String) {
		Firebase.auth.currentUser?.updateProfile(
			UserProfileChangeRequest.Builder()
				.setDisplayName(name).build()
		)
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
			createdAt = this?.metadata?.creationTimestamp ?: 0,
			isAnonymous = this?.isAnonymous ?: false,
			bio = ""
		)
	}
}