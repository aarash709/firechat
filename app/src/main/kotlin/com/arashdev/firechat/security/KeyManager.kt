package com.arashdev.firechat.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey

object KeyManager {
	private const val KEY_ALIAS = "chat_app_key"
	private const val ANDROID_KEYSTORE = "AndroidKeyStore"
	val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
		load(null)
	}

	fun generateKeyPair(): Pair<PublicKey, PrivateKey> {
		val keyPairGenerator = KeyPairGenerator.getInstance(
			KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEYSTORE
		)
		val spec = KeyGenParameterSpec.Builder(
			KEY_ALIAS,
			KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
		)
			.setKeySize(2048)
			.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
			.setDigests(KeyProperties.DIGEST_SHA256)
			.build()
		keyPairGenerator.initialize(spec)
		val keyPair = keyPairGenerator.generateKeyPair()
		return Pair(keyPair.public, keyPair.private)
	}

	fun getPublicKey(): PublicKey? {
		return keyStore.getCertificate(KEY_ALIAS)?.publicKey
	}

	fun getPrivateKey(): PrivateKey? {
		return keyStore.getKey(KEY_ALIAS, null) as? PrivateKey
	}
}