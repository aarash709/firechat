package com.arashdev.firechat.security

import android.security.keystore.KeyProperties
import java.security.PublicKey
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object EncryptionUtils {
	private const val AES_KEY_SIZE = 256
	private const val GCM_TAG_LENGTH = 16
	private const val AES_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
	private const val GCM_BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
	private const val ECB_BLOCK_MODE = KeyProperties.BLOCK_MODE_ECB
	private const val ENCRYPTION_NO_PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
	private const val RSA_ALGORITHM = KeyProperties.KEY_ALGORITHM_RSA
	private const val AES_TRANSFORMATION = "$AES_ALGORITHM/$GCM_BLOCK_MODE/$ENCRYPTION_NO_PADDING"
	private const val RSA_TRANSFORMATION =
		"$RSA_ALGORITHM/$ECB_BLOCK_MODE/OAEPWithSHA-256AndMGF1Padding"

	private val aesCipher = Cipher.getInstance(AES_TRANSFORMATION)
	private val rsaCipher = Cipher.getInstance(RSA_TRANSFORMATION)

	fun encryptMessage(message: String, recipientPublicKey: PublicKey): EncryptedMessage {
		// Generate AES key
		val aesKey = KeyManager.generateSymmetricAESKey(AES_KEY_SIZE)

		// Generate IV
		val ivBytes = aesCipher.iv
		val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, ivBytes)

		// Encrypt message with AES-GCM
		aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, gcmSpec)
		val encryptedMessageBytes = aesCipher.doFinal(message.toByteArray())

		// Encrypt AES key with RSA
		rsaCipher.init(Cipher.ENCRYPT_MODE, recipientPublicKey)
		val encryptedAesKeyBytes = aesCipher.doFinal(aesKey.encoded)

		return EncryptedMessage(encryptedMessageBytes, encryptedAesKeyBytes, ivBytes)
	}

	fun decryptMessage(
		encryptedMessage: EncryptedMessage
	): String {
		// Decrypt AES key with RSA
		val privateKey = KeyManager.getPrivateKey()
		rsaCipher.init(Cipher.DECRYPT_MODE, privateKey)
		val aesKeyBytes = rsaCipher.doFinal(encryptedMessage.encryptedAesKey)
		val aesKey = SecretKeySpec(aesKeyBytes, AES_ALGORITHM)

		val iv = Base64.getDecoder().decode(encryptedMessage.iv)
		// Decrypt message with AES-GCM
		val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
		aesCipher.init(Cipher.DECRYPT_MODE, aesKey, gcmSpec)
		val decryptedMessage = aesCipher.doFinal(encryptedMessage.encryptedMessage)

		return String(decryptedMessage)
	}
}

data class EncryptedMessage(
	val encryptedMessage: ByteArray,
	val encryptedAesKey: ByteArray,
	val iv: ByteArray
)