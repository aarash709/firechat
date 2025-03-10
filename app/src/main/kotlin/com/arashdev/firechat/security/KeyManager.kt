package com.arashdev.firechat.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

object KeyManager {
	private const val KEY_ALIAS = "chat_app_key"
	private const val ANDROID_KEYSTORE = "AndroidKeyStore"
	private const val RSA_KEY_SIZE = 2048
	private val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
		load(null)
	}

	private fun generateKeyPair(): KeyPair {
		val keyPairGenerator = KeyPairGenerator.getInstance(
			KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEYSTORE
		)
		val spec = KeyGenParameterSpec.Builder(
			KEY_ALIAS,
			KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
		)
			.setKeySize(RSA_KEY_SIZE)
			.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
			.setDigests(KeyProperties.DIGEST_SHA256)
			.build()
		keyPairGenerator.initialize(spec)
		return keyPairGenerator.generateKeyPair()
	}

	fun generateSymmetricAESKey(keySize: Int): SecretKey {
		val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES)
		keyGenerator.init(keySize)
		return keyGenerator.generateKey()
	}

	fun getPublicKey(): PublicKey? {
		return keyStore.getCertificate(KEY_ALIAS)?.publicKey
	}

	fun getPrivateKey(): PrivateKey? {
		return keyStore.getKey(KEY_ALIAS, null) as? PrivateKey
	}
}