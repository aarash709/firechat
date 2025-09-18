package com.arashdev.firechat.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import timber.log.Timber
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

object KeyManager {
	private const val KEY_ALIAS = "chat_app_key"
	private const val ANDROID_KEYSTORE = "AndroidKeyStore"
	private const val RSA_KEY_SIZE = 2048

	fun generateKeyPair() {
		try {
			val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
				load(null)
			}
			if (!keyStore.containsAlias(KEY_ALIAS)) return
			//generate new keypair
			val keyPairGenerator = KeyPairGenerator.getInstance(
				KeyProperties.KEY_ALGORITHM_RSA,
				ANDROID_KEYSTORE
			)
			val spec = KeyGenParameterSpec.Builder(
				KEY_ALIAS,
				KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
			)
				.setKeySize(RSA_KEY_SIZE)
				.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
				.setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
				.build()
			keyPairGenerator.initialize(spec)
			keyPairGenerator.generateKeyPair()
		} catch (e: Exception) {
			Timber.e("keypair generation failed!")
			Timber.e(e.message)
		}

	}

	fun deleteKeypair(keyAlias: String) {
		val keystore = KeyStore.getInstance(ANDROID_KEYSTORE)
		if (!keystore.containsAlias(KEY_ALIAS)) {
			Timber.e("Key alias-> $keyAlias, removal failed, Key alias does not exists.")
			return
		} else {
			keystore.deleteEntry(KEY_ALIAS)
			Timber.e("Key alias-> $keyAlias, removed!")
		}
	}

	fun generateSymmetricAESKey(keySize: Int): SecretKey {
		return try {
			val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES)
			keyGenerator.init(keySize, SecureRandom())
			keyGenerator.generateKey()
		} catch (e: Exception) {
			println("Error generating AES key: ${e.message}")
			throw e
		}

	}

	fun getPublicKey(): String? {
		return try {
			val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
			val publicKey = keyStore.getCertificate(KEY_ALIAS)?.publicKey
			publicKey?.let { Base64.getEncoder().encodeToString(it.encoded) }
		} catch (e: Exception) {
			println("Error retrieving Public Key from Keystore: ${e.message}")
			e.printStackTrace()
			null
		}
	}

	fun getPrivateKey(): PrivateKey? {
		return try {
			val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
			keyStore.getKey(KEY_ALIAS, null) as? PrivateKey
		} catch (e: Exception) {
			println("Error retrieving Private Key from Keystore: ${e.message}")
			e.printStackTrace()
			null
		}
	}
}