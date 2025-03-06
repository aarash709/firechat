package com.arashdev.firechat.service

import android.net.Uri
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

	/**
	 * used to add photo to the users list in auth service, currently not used in this app, instead photos are compressed using base64 and uploaded to the users collection in firestore
	 */
	suspend fun updatePhotoUri(uri: Uri)
}