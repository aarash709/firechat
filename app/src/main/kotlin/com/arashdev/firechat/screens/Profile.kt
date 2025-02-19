package com.arashdev.firechat.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arashdev.firechat.designsystem.FireChatTheme
import com.arashdev.firechat.model.User
import org.koin.androidx.compose.koinViewModel

/**
The user`s profile and "editing" status and profile pic
 */
@Composable
fun EditProfile(
	modifier: Modifier = Modifier,
	viewModel: ProfileViewmodel = koinViewModel(),
	onNavigateBack: () -> Unit
) {
	val user by viewModel.currentUser.collectAsStateWithLifecycle()
	Box(
		modifier = modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.surface),
		contentAlignment = Alignment.Center
	) {
		EditProfileContent(user = user, onNavigateBack = { onNavigateBack() }, onSaveChanges = {})
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileContent(
	modifier: Modifier = Modifier,
	user: User,
	onNavigateBack: () -> Unit,
	onSaveChanges: (User) -> Unit
) {
	Scaffold(topBar = {
		TopAppBar(
			modifier = modifier.padding(horizontal = 0.dp),
			title = {},
			navigationIcon = {
				IconButton(onClick = { onNavigateBack() }) {
					Icon(
						imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
						contentDescription = "menu icon"
					)
				}
			}, actions = {
				//visible if values change and need a save
				AnimatedVisibility(visible = true) {
					IconButton(onClick = {
						val newUser = User()
						onSaveChanges(newUser)
					}) {
						Icon(
							imageVector = Icons.Default.Check,
							contentDescription = "conversations menu"
						)
					}
				}
			},
			colors = TopAppBarDefaults.topAppBarColors(
				containerColor = MaterialTheme.colorScheme.surfaceContainer,
				navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
				actionIconContentColor = MaterialTheme.colorScheme.onSurface
			)
		)

	}) { padding ->
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(padding)
				.padding(top = 16.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp)
		) {
			var value by remember { mutableStateOf("Hello") }
			Text("Name")
			TextField(
				modifier = Modifier.fillMaxWidth(),
				value = value, onValueChange = { value = it },
				colors = TextFieldDefaults.colors(
					focusedIndicatorColor = Color.Transparent,
					errorIndicatorColor = Color.Transparent,
					disabledIndicatorColor = Color.Transparent,
					unfocusedIndicatorColor = Color.Transparent
				)
			)
			Text("Bio")
			TextField(
				modifier = Modifier.fillMaxWidth(),
				value = value, onValueChange = { value = it },
				colors = TextFieldDefaults.colors(
					focusedIndicatorColor = Color.Transparent,
					errorIndicatorColor = Color.Transparent,
					disabledIndicatorColor = Color.Transparent,
					unfocusedIndicatorColor = Color.Transparent
				)
			)
			Text("Birthday")
			TextField(
				modifier = Modifier.fillMaxWidth(),
				value = value, onValueChange = { value = it },
				colors = TextFieldDefaults.colors(
					focusedIndicatorColor = Color.Transparent,
					errorIndicatorColor = Color.Transparent,
					disabledIndicatorColor = Color.Transparent,
					unfocusedIndicatorColor = Color.Transparent
				)
			)
		}
	}

}

/**
The contact(s) inside of a chat profile status and profile pic
 */
@Composable
fun ContactProfile(modifier: Modifier = Modifier, back: () -> Unit) {
	Box(
		modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.surface),
		contentAlignment = Alignment.Center
	) {
		Surface(modifier = modifier.fillMaxSize()) {
			Row(verticalAlignment = Alignment.CenterVertically) {
				Button(onClick = { back() }) {
					Icon(
						imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
						contentDescription = "",
					)
				}
				Icon(
					imageVector = Icons.Outlined.Warning,
					contentDescription = "",
					tint = Color.Yellow
				)
				Text("Chat Profile Under Construction")
			}
		}
	}
}

@PreviewLightDark
@Composable
private fun PreviewUserProfile(modifier: Modifier = Modifier) {
	FireChatTheme {
		EditProfileContent(
			modifier = modifier,
			user = User(name = "Jack"),
			onNavigateBack = { },
			onSaveChanges = { _ -> }
		)
	}
}
