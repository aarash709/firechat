package com.arashdev.firechat

import android.os.Bundle
import android.security.keystore.KeyProperties
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.arashdev.firechat.designsystem.FireChatTheme
import com.arashdev.firechat.service.RemoteStorageService
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.SecretKeySpec

class MainActivity : ComponentActivity() {

	private val database by inject<RemoteStorageService>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContent {
			FireChatTheme {
				AppNavigation()
//				Test()
			}
		}
	}

	override fun onStart() {
		super.onStart()
		lifecycleScope.launch { database.updateUserPresenceStatus(isOnline = true) }
	}

	override fun onStop() {
		super.onStop()
		lifecycleScope.launch { database.updateUserPresenceStatus(isOnline = false) }
	}
}


object EncryptionUtils {

	// Function to generate RSA Key Pair (Asymmetric)
	fun generateKeyPair(): KeyPair {
		return try {
			val keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA)
			keyPairGenerator.initialize(
				2048,
				SecureRandom()
			) // 2048 bits key size is generally secure
			val keyPair = keyPairGenerator.generateKeyPair()
			KeyPair(
				publicKey = Base64.getEncoder()
					.encodeToString(keyPair.public.encoded), // Encoding for easier handling as String in test
				privateKey = Base64.getEncoder().encodeToString(keyPair.private.encoded)
			)
		} catch (e: Exception) {
			println("Error generating RSA key pair: ${e.message}")
			throw e // Re-throw to indicate failure
		}
	}

	// Function to generate AES Secret Key (Symmetric)
	private fun generateSymmetricKey(): String {
		return try {
			val keyGenerator = KeyGenerator.getInstance("AES")
			keyGenerator.init(256, SecureRandom()) // 256 bits key size for AES is strong
			val secretKey = keyGenerator.generateKey()
			Base64.getEncoder()
				.encodeToString(secretKey.encoded) // Encoding for easier handling as String in test
		} catch (e: Exception) {
			println("Error generating AES key: ${e.message}")
			throw e // Re-throw to indicate failure
		}
	}

	data class KeyPair(val publicKey: String, val privateKey: String)

	data class EncryptedData(val encryptedSymmetricKey: String, val ciphertext: String)

	// Hybrid Encryption Function
	fun hybridEncrypt(plaintext: String, recipientPublicKeyBase64: String): EncryptedData {
		try {
			// 1. Generate a fresh AES symmetric key for this message
			val symmetricKeyBase64 = generateSymmetricKey()
			val symmetricKeyBytes = Base64.getDecoder().decode(symmetricKeyBase64)
			val symmetricKey = SecretKeySpec(symmetricKeyBytes, "AES")

			// 2. Encrypt the plaintext data using AES (Symmetric Encryption)
			val cipherAes =
				Cipher.getInstance("AES/ECB/PKCS5Padding") // ECB is used for simplicity in this example, consider GCM or CBC with IV in production
			cipherAes.init(Cipher.ENCRYPT_MODE, symmetricKey)
			val ciphertextBytes = cipherAes.doFinal(plaintext.toByteArray(Charsets.UTF_8))
			val ciphertextBase64 = Base64.getEncoder().encodeToString(ciphertextBytes)

			// 3. Encrypt the AES symmetric key using the recipient's RSA public key (Asymmetric Encryption)
			val publicKeyBytes = Base64.getDecoder().decode(recipientPublicKeyBase64)
			val publicKeySpec = java.security.spec.X509EncodedKeySpec(publicKeyBytes)
			val keyFactory = java.security.KeyFactory.getInstance("RSA")
			val publicKey = keyFactory.generatePublic(publicKeySpec)

			val cipherRsa = Cipher.getInstance("RSA/ECB/PKCS1Padding") // Or "RSA"
			cipherRsa.init(Cipher.ENCRYPT_MODE, publicKey)
			val encryptedSymmetricKeyBytes = cipherRsa.doFinal(symmetricKeyBytes)
			val encryptedSymmetricKeyBase64 =
				Base64.getEncoder().encodeToString(encryptedSymmetricKeyBytes)

			return EncryptedData(
				encryptedSymmetricKey = encryptedSymmetricKeyBase64,
				ciphertext = ciphertextBase64
			)

		} catch (e: Exception) {
			println("Error during hybrid encryption: ${e.message}")
			throw e // Re-throw to indicate failure
		}
	}

	// Hybrid Decryption Function
	fun hybridDecrypt(encryptedData: EncryptedData, recipientPrivateKeyBase64: String): String {
		try {
			// 1. Decrypt the AES symmetric key using the recipient's RSA private key
			val privateKeyBytes = Base64.getDecoder().decode(recipientPrivateKeyBase64)
			val privateKeySpec = java.security.spec.PKCS8EncodedKeySpec(privateKeyBytes)
			val keyFactory = java.security.KeyFactory.getInstance("RSA")
			val privateKey = keyFactory.generatePrivate(privateKeySpec)

			val cipherRsaDecrypt =
				Cipher.getInstance("RSA/ECB/PKCS1Padding") // Or "RSA" - should match encryption
			cipherRsaDecrypt.init(Cipher.DECRYPT_MODE, privateKey)
			val decryptedSymmetricKeyBytes = cipherRsaDecrypt.doFinal(
				Base64.getDecoder().decode(encryptedData.encryptedSymmetricKey)
			)
			val decryptedSymmetricKey = SecretKeySpec(decryptedSymmetricKeyBytes, "AES")


			// 2. Decrypt the ciphertext data using the decrypted AES symmetric key
			val cipherAesDecrypt =
				Cipher.getInstance("AES/ECB/PKCS5Padding") // Should match encryption
			cipherAesDecrypt.init(Cipher.DECRYPT_MODE, decryptedSymmetricKey)
			val decryptedTextBytes =
				cipherAesDecrypt.doFinal(Base64.getDecoder().decode(encryptedData.ciphertext))
			return String(decryptedTextBytes, Charsets.UTF_8)

		} catch (e: Exception) {
			println("Error during hybrid decryption: ${e.message}")
			throw e // Re-throw to indicate failure
		}
	}
}

@Composable
fun Test(modifier: Modifier = Modifier) {
	Column(
		modifier = Modifier
			.fillMaxSize()
			.systemBarsPadding()
	) {
		var text by remember {
			mutableStateOf("")
		}
		var encryptedData: EncryptionUtils.EncryptedData by remember {
			mutableStateOf(EncryptionUtils.EncryptedData("", ""))
		}
		var decryptedText by remember {
			mutableStateOf("")
		}
		var keyPair by remember {
			val senderKeyPair = EncryptionUtils.generateKeyPair()
			mutableStateOf(senderKeyPair)
		}

		OutlinedTextField(text, onValueChange = { text = it })
		Text("encripted :${encryptedData.ciphertext}")
//					Text("encripted :${encryptedData?.encryptedMessage?.decodeToString()}")
		Spacer(Modifier.height(32.dp))
		HorizontalDivider()
		Spacer(Modifier.height(32.dp))
		Text("DECRYPTED :$decryptedText")
		Spacer(Modifier.height(32.dp))
		Row {
			Button(onClick = {
//							Timber.e("public key size ${KeyManager.getPublicKey()?.encoded?.size}")
//							Timber.e("private key size ${KeyManager.getPrivateKey()?.encoded?.size}")
				//generate rsa

				Timber.e("Sender Public Key (Base64): ${keyPair.publicKey}")
				Timber.e("Sender Private Key (Base64): ${keyPair.privateKey}")
				//encrypt
				Timber.e("\n--- Encryption Process ---")
				encryptedData =
					EncryptionUtils.hybridEncrypt(text, keyPair.publicKey)
				Timber.e("Encrypted Symmetric Key (Base64): ${encryptedData.encryptedSymmetricKey}")
				Timber.e("Ciphertext (Base64): ${encryptedData.ciphertext}")
//							encryptedData = EncryptionUtils.encryptMessage(
//								message = text,
//								senderPublicKey = KeyManager.getPublicKey()!!,
//								KeyManager.getPublicKey()!!
//							)
			}) {
				Text("encrypt")
			}
			Button(onClick = {
				//decrypt
//							Timber.e("encrypted message :${encryptedData?.encryptedMessage!!.decodeToString()}")
//							Timber.e("sender key :${encryptedData?.recipientEncryptedAesKey!!.decodeToString()}")
//							Timber.e("iv :${encryptedData?.iv!!.decodeToString()}")
//							Timber.e("private key :${KeyManager.getPrivateKey()}")
//							Timber.e("public key :${KeyManager.getPublicKey()}")
//							Timber.e(
//								"contains alias? :${
//									KeyStore.getInstance("AndroidKeyStore").apply {
//										load(null)
//									}.containsAlias("chat_app_key")
//								}"
//							)
				Timber.e("\n--- Decryption Process ---")
				decryptedText =
					EncryptionUtils.hybridDecrypt(
						encryptedData,
						keyPair.privateKey
					)
				Timber.e("Decrypted Text: $decryptedText")

				// 5. Verify if Decrypted Text is the same as Original Text
				if (text == decryptedText) {
					Timber.e("\n--- Success! E2EE Hybrid Encryption and Decryption Test Passed ---")
				} else {
					Timber.e("\n--- Failure! Decryption did not result in the original text ---")
				}
//							try {
//								decryptedData = EncryptionUtils.decryptMessage(
//									encryptedData?.encryptedMessage!!,
//									encryptedData?.senderEncryptedAesKey!!,
//									KeyManager.getPrivateKey()!!,
//									encryptedData?.iv!!
//								)
//							} catch (e: Exception) {
//								Timber.e(e.message)
//							}
			}) {
				Text("decrypt")
			}
		}
	}

}