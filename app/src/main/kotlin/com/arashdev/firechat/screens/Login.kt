package com.arashdev.firechat.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.arashdev.firechat.designsystem.FireChatTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
	viewModel: LoginViewModel = koinViewModel(),
	onNavigateToConversations: () -> Unit
) {
	val uiState = viewModel.uiState.value
	LoginContent(
		uiState = uiState,
		onSignInWithEmailPassword = { email, password ->
			viewModel.signInWithEmailAndPassword(email = email, password = password)
		},
		onNavigateToConversations = { onNavigateToConversations() },
		onSignUp = { email, pass, name ->
			viewModel.signupWithEmailAndPassword(email = email, password = pass, userName = name)
		}
	)
}

@Composable
fun LoginContent(
	modifier: Modifier = Modifier,
	uiState: AuthUiState,
	onSignInWithEmailPassword: (String, String) -> Unit,
	onSignUp: (String, String, String) -> Unit,
	onNavigateToConversations: () -> Unit
) {
	var name by remember { mutableStateOf("") }
	var email by remember { mutableStateOf("") }
	var password by remember { mutableStateOf("") }
	val focusManager = LocalFocusManager.current
	val keyboardController = LocalSoftwareKeyboardController.current
	Surface(modifier = modifier.fillMaxSize()) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.systemBarsPadding()
				.padding(horizontal = 16.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.SpaceBetween
		) {
			val isEnabled = uiState != AuthUiState.Loading
			Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
				Text("Sign in", style = MaterialTheme.typography.headlineLarge)
			}
			Column(
				modifier = Modifier,
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.SpaceBetween
			) {
				OutlinedTextField(
					value = name,
					enabled = isEnabled,
					onValueChange = { name = it },
					label = { Text("Name") },
					keyboardActions = KeyboardActions(onNext = {
						focusManager.moveFocus(FocusDirection.Down)
					}),
					keyboardOptions = KeyboardOptions(
						showKeyboardOnFocus = true,
						keyboardType = KeyboardType.Email,
						imeAction = ImeAction.Next
					),
					//				colors = TextFieldDefaults.colors(
					//					focusedIndicatorColor = Color.Transparent,
					//					errorIndicatorColor = Color.Transparent,
					//					disabledIndicatorColor = Color.Transparent,
					//					unfocusedIndicatorColor = Color.Transparent
					//				)
				)
				OutlinedTextField(
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
					//				colors = TextFieldDefaults.colors(
					//					focusedIndicatorColor = Color.Transparent,
					//					errorIndicatorColor = Color.Transparent,
					//					disabledIndicatorColor = Color.Transparent,
					//					unfocusedIndicatorColor = Color.Transparent
					//				)
				)
				Spacer(Modifier.height(8.dp))
				OutlinedTextField(
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
					//				colors = TextFieldDefaults.colors(
					//					focusedIndicatorColor = Color.Transparent,
					//					errorIndicatorColor = Color.Transparent,
					//					disabledIndicatorColor = Color.Transparent,
					//					unfocusedIndicatorColor = Color.Transparent
					//				)
				)
				Spacer(Modifier.height(8.dp))
				Box(
					modifier = Modifier.fillMaxWidth(),
					contentAlignment = Alignment.BottomCenter
				) {
					Button(
						onClick = {
							onSignInWithEmailPassword(email, password)
						},
						enabled = isEnabled
					) {
						AnimatedContent(uiState) {
							when (it) {
								AuthUiState.Initial, is AuthUiState.Error -> {
									Text("Log in")
								}

								AuthUiState.Loading -> {
									Text("Logging in")
								}

								AuthUiState.Success -> {
									onNavigateToConversations()
								}
							}
						}
					}
					if (uiState == AuthUiState.Loading) {
						CircularProgressIndicator()
					}
				}
			}
			Column(horizontalAlignment = Alignment.CenterHorizontally) {
				Text("Don`t have an account?")
				TextButton(
					onClick = {
						onSignUp(email, password, name)
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
								//							onNavigateToConversations()
							}
						}
					}
				}
				// TODO: Remove anonymous sing in
				TextButton(
					onClick = {
//						onSignUp(email, password, name)
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
								//							onNavigateToConversations()
							}
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
		LoginContent(
			uiState = uiState,
			onNavigateToConversations = {},
			onSignUp = { _, _, _ -> },
			onSignInWithEmailPassword = { _, _ -> }
		)
	}
}