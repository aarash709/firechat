package com.arashdev.firechat.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.arashdev.firechat.designsystem.FireChatTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun Settings(
	modifier: Modifier = Modifier,
	viewModel: SettingsViewModel = koinViewModel(),
	onNavigateBack: () -> Unit
) {
	Box(
		modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.surface),
		contentAlignment = Alignment.Center
	) {
		SettingsContent(onNavigateBack = { onNavigateBack() })
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(modifier: Modifier = Modifier, onNavigateBack: () -> Unit) {
	Scaffold(modifier = modifier, topBar = {
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
				IconButton(onClick = {
				}) {
					Icon(
						imageVector = Icons.Default.MoreVert,
						contentDescription = "conversations menu"
					)
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
		) {

			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 16.dp),
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(8.dp)
			) {
				Icon(
					imageVector = Icons.Outlined.AccountCircle,
					modifier = Modifier.size(80.dp),
					contentDescription = "Profile picture"
				)
				Text("Jack", style = MaterialTheme.typography.headlineMedium)
			}
			Text(
				"Account Status",
				modifier = Modifier
					.padding(horizontal = 16.dp),
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
			HorizontalDivider(Modifier.padding(4.dp))
			Column(
				verticalArrangement = Arrangement.spacedBy(8.dp),
				modifier = Modifier
			) {
				SettingsItem(
					icon = Icons.Outlined.ChatBubbleOutline, title = "Chat Setting",
					onSettingItemClick = { }
				)
				SettingsItem(
					icon = Icons.Outlined.Lock, title = "Privacy and Security",
					onSettingItemClick = { }
				)
				SettingsItem(
					icon = Icons.Outlined.Notifications, title = "Notification",
					onSettingItemClick = { }
				)
				SettingsItem(
					icon = Icons.Outlined.Language, title = "Language",
					onSettingItemClick = { }
				)
			}
			Text(
				text = "Fire Chat, v0.1Alpha(Demo), Developed by Arash Ebrahimzade",
				modifier = Modifier
					.padding(horizontal = 16.dp)
					.fillMaxWidth(),
				style = MaterialTheme.typography.bodyMedium,
			)
		}
	}
}

@Composable
private fun SettingsItem(icon: ImageVector, title: String, onSettingItemClick: () -> Unit) {
	Surface(onClick = { onSettingItemClick() }) {
		Row(
			horizontalArrangement = Arrangement.spacedBy(8.dp),
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Icon(
				imageVector = icon,
				modifier = Modifier.size(30.dp),
				contentDescription = ""
			)
			Text(title, style = MaterialTheme.typography.titleSmall)

		}
	}
}

@PreviewLightDark
@Composable
private fun SettingsPreview() {
	FireChatTheme {
		SettingsContent(onNavigateBack = {})
	}
}
