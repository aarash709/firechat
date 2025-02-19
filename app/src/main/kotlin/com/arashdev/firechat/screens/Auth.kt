package com.arashdev.firechat.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.arashdev.firechat.designsystem.FireChatTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun AuthScreen(viewModel: AuthViewModel = koinViewModel(), onNavigateToChat: () -> Unit) {
	val uiState = viewModel.uiState.value
	AuthContent(
		uiState = uiState,
		onSignupWithEmailPassword = {
		},
		onNavigateToChat = { onNavigateToChat() },
		onSignupAnonymously = { viewModel.signInAnonymously() }
	)
}

@Composable
fun AuthContent(
	modifier: Modifier = Modifier,
	uiState: AuthUiState,
	onSignupWithEmailPassword: () -> Unit,
	onSignupAnonymously: () -> Unit,
	onNavigateToChat: () -> Unit
) {
	var email by remember { mutableStateOf("") }
	var password by remember { mutableStateOf("") }
	val focusManager = LocalFocusManager.current
	val keyboardController = LocalSoftwareKeyboardController.current
	Surface(modifier = modifier.fillMaxSize()) {
		Column(
			modifier = Modifier
				.padding(16.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center
		) {
			val isEnabled = uiState != AuthUiState.Loading
			Spacer(Modifier.height(32.dp))
			TextField(
				value = email,
				enabled = isEnabled,
				onValueChange = { email = it },
				label = { Text("Email") },
				keyboardActions = KeyboardActions(onNext = {
					focusManager.moveFocus(FocusDirection.Down)
				}),
				keyboardOptions = KeyboardOptions(
					showKeyboardOnFocus = true,
					keyboardType = KeyboardType.Email,
					imeAction = ImeAction.Next
				),
				colors = TextFieldDefaults.colors(
					focusedIndicatorColor = Color.Transparent,
					errorIndicatorColor = Color.Transparent,
					disabledIndicatorColor = Color.Transparent,
					unfocusedIndicatorColor = Color.Transparent
				)
			)
			Spacer(Modifier.height(8.dp))
			TextField(
				value = password,
				enabled = isEnabled,
				onValueChange = { password = it },
				label = { Text("Password") },
				keyboardActions = KeyboardActions(onDone = {
					focusManager.clearFocus()
					keyboardController?.hide()
				}),
				keyboardOptions = KeyboardOptions(
					keyboardType = KeyboardType.Password,
					imeAction = ImeAction.Done
				),
				colors = TextFieldDefaults.colors(
					focusedIndicatorColor = Color.Transparent,
					errorIndicatorColor = Color.Transparent,
					disabledIndicatorColor = Color.Transparent,
					unfocusedIndicatorColor = Color.Transparent
				)
			)
			Spacer(Modifier.height(8.dp))
			Row {
				Button(
					onClick = {
						onSignupWithEmailPassword()
					},
					enabled = isEnabled
				) {
					AnimatedContent(uiState) {
						when (it) {
							AuthUiState.Initial, is AuthUiState.Error -> {
								Text("Sign up")
							}

							AuthUiState.Loading -> {
								Text("Signing up")
							}

							AuthUiState.Success -> {
								onNavigateToChat()
							}
						}
					}
				}
				if (uiState == AuthUiState.Loading) {
					CircularProgressIndicator()
				}
			}
			TextButton(
				onClick = {
					onSignupAnonymously()
				},
				enabled = isEnabled
			) {
				AnimatedContent(uiState) {
					when (it) {
						AuthUiState.Initial, is AuthUiState.Error -> {
							Text("Anonymous sign in")
						}

						AuthUiState.Loading -> {
							Text("Signing up")
						}

						AuthUiState.Success -> {
							onNavigateToChat()
						}
					}
				}
			}
		}
	}

}

@PreviewLightDark
@Composable
private fun AuthPreview() {
	FireChatTheme {
		val uiState by remember {
			mutableStateOf(AuthUiState.Initial)
		}
		AuthContent(
			uiState = uiState,
			onSignupWithEmailPassword = {},
			onNavigateToChat = {},
			onSignupAnonymously = {})
	}
}