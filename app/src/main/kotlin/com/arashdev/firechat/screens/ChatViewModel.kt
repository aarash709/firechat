package com.arashdev.firechat.screens

import android.text.format.DateUtils
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.arashdev.firechat.Chat
import com.arashdev.firechat.model.EncryptedMessage
import com.arashdev.firechat.model.Message
import com.arashdev.firechat.model.User
import com.arashdev.firechat.security.EncryptionUtils
import com.arashdev.firechat.service.AuthService
import com.arashdev.firechat.service.RemoteStorageService
import com.arashdev.firechat.utils.getConversationId
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.time.Instant
import java.util.Base64

class ChatViewModel(
	private val storageService: RemoteStorageService,
	private val authService: AuthService,
	savedStateHandle: SavedStateHandle
) : ViewModel() {

	val currentUserID: String = authService.currentUserId

	private val otherUserId = savedStateHandle.toRoute<Chat>().otherUserId
	private val conversationId =
		getConversationId(currentUserId = currentUserID, otherUserId = otherUserId)

	val contact: StateFlow<User> = storageService
		.getUser(otherUserId)
		.map { contact ->
			val targetUserConnectedStatus = storageService
				.getUserConnectedStatus(contact!!.userId)
				.first()
			val targetUserLastSeenStatus = storageService
				.getUserLastSeenStatus(contact.userId)
				.first()
			val lastSeen = lastSeenString(targetUserConnectedStatus, targetUserLastSeenStatus)
			contact.copy(lastSeen = lastSeen)
		}
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(5000),
			initialValue = User()
		)

	val messages: StateFlow<List<Message>> =
		storageService.observeMessages(conversationId = conversationId)
			.map {
				it.reversed().map { encryptedMessage ->
					val message = Base64.getDecoder().decode(encryptedMessage.encryptedMessage)
					val encryptedAesKey =
						Base64.getDecoder().decode(encryptedMessage.encryptedAesKey)
					val iv = Base64.getDecoder().decode(encryptedMessage.iv)
					//decrypt!
					val text = EncryptionUtils.decryptMessage(message, encryptedAesKey, iv)
					Message(
						text = text,
						senderId = encryptedMessage.senderId,
						timestamp = encryptedMessage.timestamp,
					)
				}
			}
			.stateIn(
				scope = viewModelScope,
				started = SharingStarted.WhileSubscribed(5000),
				initialValue = listOf()
			)

	fun sendMessage(text: String) {
		viewModelScope.launch {
			val time = Instant.now().epochSecond //UTC
			val publicKeyBytes = storageService.getPublicKey(userId = otherUserId)
			//to public key from bytearray
			val keyFactory = KeyFactory.getInstance("RSA")
			val recipientPublicKey = keyFactory.generatePublic(X509EncodedKeySpec(publicKeyBytes))

			// Encrypt message
			val encryptedMessageData = EncryptionUtils.encryptMessage(
				message = text,
				recipientPublicKey = recipientPublicKey
			)
			//ready to send as string values
			val messageString =
				Base64.getEncoder().encodeToString(encryptedMessageData.encryptedMessage)
			val aesKeyString =
				Base64.getEncoder().encodeToString(encryptedMessageData.encryptedAesKey)
			val ivString = Base64.getEncoder().encodeToString(encryptedMessageData.iv)
			val encryptedMessage = EncryptedMessage(
				senderId = currentUserID,
				timestamp = time,
				encryptedMessage = messageString,
				encryptedAesKey = aesKeyString,
				iv = ivString
			)

			storageService.sendMessage(
				encryptedMessage = encryptedMessage,
				conversationId = conversationId
			)
			updateConversation(
				lastMessage = text,
				timeSeconds = time,
				conversationId = conversationId
			)
		}
	}

	private suspend fun updateConversation(
		lastMessage: String,
		timeSeconds: Long,
		conversationId: String
	) {
		storageService.updateConversation(
			conversationId = conversationId,
			lastMessage = lastMessage,
			timeSeconds = timeSeconds
		)
	}
}

private fun lastSeenString(
	targetUserConnectedStatus: Boolean,
	targetUserLastSeenStatus: Long
) = if (targetUserConnectedStatus) {
	"Online"
} else {
	buildString {
		append("last seen")
		append(" ")
		append(
			DateUtils.getRelativeTimeSpanString(
				targetUserLastSeenStatus,
				System.currentTimeMillis(),
				DateUtils.SECOND_IN_MILLIS,
			)
		)
	}
}

sealed class MessageState {
	data object Idle : MessageState()
	data object Loading : MessageState()
	data class Success(val messages: List<Message>) : MessageState()
	data class Error(val message: String) : MessageState()
}