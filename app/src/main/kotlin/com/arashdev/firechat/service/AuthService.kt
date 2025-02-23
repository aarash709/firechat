package com.arashdev.firechat.service

import com.arashdev.firechat.model.User
import kotlinx.coroutines.flow.Flow

interface AuthService {

	val currentUserId: String

	val currentUser: Flow<User>

	val doesUserExists: Boolean

	suspend fun createAnonymousAccount()

	suspend fun createNewAccount(email: String, password: String)

	fun hasUser() : Boolean

	suspend fun deleteAccount()

	suspend fun linkAccount(email: String, password: String)

	suspend fun signInWithEmailAndPassword(email: String, password: String)

	suspend fun signOut()

	suspend fun updateDisplayName(name: String)
}