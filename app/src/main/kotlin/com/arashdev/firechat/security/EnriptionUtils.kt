package com.arashdev.firechat.security

import android.security.keystore.KeyProperties
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object EncryptionUtils {
	private const val AES_KEY_SIZE = 256
	private const val GCM_IV_LENGTH = 12
	private const val GCM_TAG_LENGTH = 16
	private const val AES_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
	private const val GCM_BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
	private const val ECB_BLOCK_MODE = KeyProperties.BLOCK_MODE_ECB
	private const val ENCRYPTION_NO_PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
	private const val RSA_ALGORITHM = KeyProperties.KEY_ALGORITHM_RSA
	private const val AES_TRANSFORMATION = "$AES_ALGORITHM/$GCM_BLOCK_MODE/$ENCRYPTION_NO_PADDING"
	private const val RSA_TRANSFORMATION =
		"$RSA_ALGORITHM/$ECB_BLOCK_MODE/OAEPWithSHA-256AndMGF1Padding"

	data class EncryptedData(
		val encryptedMessage: ByteArray,
		val encryptedAesKey: ByteArray,
		val iv: ByteArray
	)

	private val aesCipher = Cipher.getInstance(AES_TRANSFORMATION)
	private val rsaCipher = Cipher.getInstance(RSA_TRANSFORMATION)

	fun encryptMessage(message: String, recipientPublicKey: PublicKey): EncryptedData {
		// Generate AES key
		val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES)
		keyGenerator.init(AES_KEY_SIZE)
		val aesKey = keyGenerator.generateKey()

		// Generate IV
		val iv = ByteArray(GCM_IV_LENGTH).apply { java.security.SecureRandom().nextBytes(this) }
		val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)

		// Encrypt message with AES-GCM
		aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, gcmSpec)
		val encryptedMessage = aesCipher.doFinal(message.toByteArray())

		// Encrypt AES key with RSA
		aesCipher.init(Cipher.ENCRYPT_MODE, recipientPublicKey)
		val encryptedAesKey = aesCipher.doFinal(aesKey.encoded)

		return EncryptedData(encryptedMessage, encryptedAesKey, iv)
	}

	fun decryptMessage(
		encryptedMessage: ByteArray,
		encryptedAesKey: ByteArray,
		iv: ByteArray,
		privateKey: PrivateKey
	): String {
		// Decrypt AES key with RSA
		rsaCipher.init(Cipher.DECRYPT_MODE, privateKey)
		val aesKeyBytes = rsaCipher.doFinal(encryptedAesKey)
		val aesKey = SecretKeySpec(aesKeyBytes, AES_ALGORITHM)

		// Decrypt message with AES-GCM
		val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
		aesCipher.init(Cipher.DECRYPT_MODE, aesKey, gcmSpec)
		val decryptedMessage = aesCipher.doFinal(encryptedMessage)

		return String(decryptedMessage)
	}
}