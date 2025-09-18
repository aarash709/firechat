package com.arashdev.firechat.security

import android.security.keystore.KeyProperties
import com.arashdev.firechat.model.EncryptedData
import timber.log.Timber
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object EncryptionUtils {
	private const val AES_KEY_SIZE = 256
	private const val GCM_TAG_LENGTH = 16
	private const val GCM_IV_LENGTH = 12
	private const val AES_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
	private const val GCM_BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
	private const val ECB_BLOCK_MODE = KeyProperties.BLOCK_MODE_ECB
	private const val ENCRYPTION_NO_PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
	private const val RSA_ALGORITHM = KeyProperties.KEY_ALGORITHM_RSA
	private const val AES_TRANSFORMATION = "$AES_ALGORITHM/$GCM_BLOCK_MODE/$ENCRYPTION_NO_PADDING"
	private const val RSA_TRANSFORMATION =
		"$RSA_ALGORITHM/$ECB_BLOCK_MODE/OAEPWithSHA-256AndMGF1Padding"

	fun encryptMessage(
		message: String,
		recipientPublicKey: PublicKey
	): EncryptedData {
		try {// Generate AES key
			val aesKey = KeyManager.generateSymmetricAESKey(256)

			val aesCipher = Cipher.getInstance(AES_TRANSFORMATION)
			val rsaCipher = Cipher.getInstance(RSA_TRANSFORMATION)

			Timber.e("generate aes key: ${String(aesKey.encoded)}")
			Timber.e("generated aes size: ${aesKey.encoded.size}")

			// Generate IV
			val ivBytes = ByteArray(GCM_IV_LENGTH).apply { SecureRandom().nextBytes(this) }
			val nonceBase64 = Base64.getEncoder().encodeToString(ivBytes)
			Timber.e("iv size: ${ivBytes.size}")

			// Encrypt message with AES-GCM
			val gcmSpec = GCMParameterSpec(128, ivBytes)
			aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, gcmSpec)
			val encryptedMessageBytes = aesCipher.doFinal(message.toByteArray(Charsets.UTF_8))
			val encryptedMessageBase64 = Base64.getEncoder().encodeToString(encryptedMessageBytes)

			// Recipient Encrypt AES key with RSA
			rsaCipher.init(Cipher.ENCRYPT_MODE, recipientPublicKey)
			val recipientEncryptedAesKeyBytes = rsaCipher.doFinal(aesKey.encoded)
			val recipientEncryptedAesKeyBase64 =
				Base64.getEncoder().encodeToString(recipientEncryptedAesKeyBytes)
			Timber.e("recipient encrypted aes key: ${recipientEncryptedAesKeyBytes.size}")

			return EncryptedData(
				encryptedMessage = encryptedMessageBase64,
				encryptedAesKey = recipientEncryptedAesKeyBase64,
				iv = nonceBase64
			)
		} catch (e: Exception) {
			Timber.e("ERROR! Hybrid encryption failed!: ${e.message}")
			throw e
		}
	}

	fun decryptMessage(
		encryptedData: EncryptedData,
		privateKey: PrivateKey,
	): String {
		// Decrypt AES key with RSA
		val aesCipher = Cipher.getInstance(AES_TRANSFORMATION)
		val rsaCipher = Cipher.getInstance(RSA_TRANSFORMATION)
		Timber.e("Encrypted sender AES Key: ${encryptedData.encryptedAesKey}")
		Timber.e("Encrypted Private Key size: ${privateKey.encoded?.size}")

		rsaCipher.init(Cipher.DECRYPT_MODE, privateKey)
		val encryptedAESKey = Base64.getDecoder().decode(encryptedData.encryptedAesKey)
		val aesKeyBytes = rsaCipher.doFinal(encryptedAESKey)

		Timber.e("rsa cipher iv size: ${rsaCipher.iv.size}")
		Timber.e("Decrypted RSA Key byte size: ${aesKeyBytes.size}")

		val aesKey = SecretKeySpec(aesKeyBytes, AES_ALGORITHM)

		Timber.e("Decrypted secret AES Key length: ${aesKey.encoded.size}")
		Timber.e("aes cipher iv size: ${aesCipher.iv.size}")

		// Decrypt message with AES-GCM
		val ivBytes = Base64.getDecoder().decode(encryptedData.iv)
		val encryptedMessage = Base64.getDecoder().decode(encryptedData.encryptedMessage)
		val gcmSpec = GCMParameterSpec(128, ivBytes)
		aesCipher.init(Cipher.DECRYPT_MODE, aesKey, gcmSpec)
		val decryptedMessage = aesCipher.doFinal(encryptedMessage)

		return String(decryptedMessage, Charsets.UTF_8)
	}
}