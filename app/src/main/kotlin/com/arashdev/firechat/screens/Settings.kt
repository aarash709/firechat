package com.arashdev.firechat.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arashdev.firechat.designsystem.FireChatTheme
import com.arashdev.firechat.model.User
import org.koin.androidx.compose.koinViewModel

@Composable
fun Settings(
	modifier: Modifier = Modifier,
	viewModel: SettingsViewModel = koinViewModel(),
	onNavigateBack: () -> Unit,
) {
	val user by viewModel.user.collectAsStateWithLifecycle()
	Box(
		modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.surface),
		contentAlignment = Alignment.Center
	) {
		var showDeleteAccountDialog by remember { mutableStateOf(false) }
		var showLogoutDialog by remember { mutableStateOf(false) }
		if (showDeleteAccountDialog) {
			AlertDialog(
				onDismissRequest = { showDeleteAccountDialog = !showDeleteAccountDialog },
				title = { Text("Delete Account?") },
				modifier = Modifier,
				confirmButton = {
					IconButton(onClick = {/*delete account*/ }) {
						Text("Yes")
					}
				},
				dismissButton = {
					IconButton(onClick = { showDeleteAccountDialog = !showDeleteAccountDialog }) {
						Text("No")
					}
				}
			)
		}
		if (showLogoutDialog) {
			AlertDialog(
				onDismissRequest = { showLogoutDialog = !showLogoutDialog },
				title = { Text("Logout?") },
				modifier = Modifier,
				confirmButton = {
					IconButton(onClick = {/*logout?*/ }) {
						Text("Yes")
					}
				},
				dismissButton = {
					IconButton(onClick = { showLogoutDialog = !showLogoutDialog }) {
						Text("No")
					}
				}
			)
		}
		SettingsContent(modifier = Modifier, user, onNavigateBack = { onNavigateBack() },
			onLogout = { showLogoutDialog = !showLogoutDialog },
			onDeleteAccount = { showDeleteAccountDialog = !showDeleteAccountDialog },
			onLinkAccount = { })
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
	modifier: Modifier = Modifier,
	user: User,
	onNavigateBack: () -> Unit,
	onLinkAccount: () -> Unit,
	onLogout: () -> Unit,
	onDeleteAccount: () -> Unit
) {
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
				var expanded by remember {
					mutableStateOf(false)
				}
				Box {
					IconButton(onClick = {
						expanded = !expanded
					}) {
						Icon(
							imageVector = Icons.Default.MoreVert,
							contentDescription = "conversations menu"
						)
					}
					DropdownMenu(
						expanded = expanded,
						onDismissRequest = { expanded = !expanded },
						tonalElevation = 4.dp,
						shadowElevation = 8.dp,
						containerColor = MaterialTheme.colorScheme.surface
					) {
						if (user.isAnonymous) {
							DropdownMenuItem(
								text = { Text("Link Account") },
								onClick = { onLinkAccount() },
								leadingIcon = {
									Icon(
										imageVector = Icons.Outlined.Link,
										contentDescription = "link account icon"
									)
								})
						}
						DropdownMenuItem(text = {
							Text(
								"Log out",
								color = MaterialTheme.colorScheme.error
							)
						},
							onClick = { onLogout() },
							leadingIcon = {
								Icon(
									imageVector = Icons.AutoMirrored.Outlined.Logout,
									contentDescription = "logout icon",
									tint = MaterialTheme.colorScheme.error
								)
							})
						DropdownMenuItem(
							text = {
								Text(
									"Delete Account",
									color = MaterialTheme.colorScheme.error
								)
							},
							onClick = { onDeleteAccount() },
							leadingIcon = {
								Icon(
									imageVector = Icons.Outlined.DeleteForever,
									contentDescription = "delete icon",
									tint = MaterialTheme.colorScheme.error
								)
							})
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
		) {
			Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.spacedBy(8.dp)
				) {
					Icon(
						imageVector = Icons.Outlined.AccountCircle,
						modifier = Modifier.size(80.dp),
						contentDescription = "Profile picture"
					)
					Text(user.name, style = MaterialTheme.typography.headlineMedium)
				}
			}
			AccountInfo(user = user, onClick = {})
			Spacer(Modifier.height(8.dp))
			Text(
				text = "Settings",
				modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
				style = MaterialTheme.typography.titleSmall,
			)
			LazyColumn(
				verticalArrangement = Arrangement.Top,
				modifier = Modifier.fillMaxSize()
			) {
				item {
					SettingsItem(
						icon = Icons.Outlined.ChatBubbleOutline, title = "Chat Setting",
						onSettingItemClick = { }
					)
				}
				item {
					SettingsItem(
						icon = Icons.Outlined.Lock, title = "Privacy and Security",
						onSettingItemClick = { }
					)
				}
				item {
					SettingsItem(
						icon = Icons.Outlined.Notifications, title = "Notification",
						onSettingItemClick = { }
					)
				}
				item {
					SettingsItem(
						icon = Icons.Outlined.Language, title = "Language",
						onSettingItemClick = { }
					)
				}
				item {
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
	}
}

@Composable
private fun AccountInfo(modifier: Modifier = Modifier, user: User, onClick: () -> Unit) {
	Surface(modifier = modifier, shadowElevation = 4.dp) {
		val linePadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
		Column(modifier = Modifier) {
			Spacer(Modifier.height(8.dp))
			Text(
				text = "Account",
				modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
				style = MaterialTheme.typography.titleSmall,
			)
			SettingAccountInfoItem(
				type = "Name",
				text = if (user.isAnonymous) "Anonymous Account" else user.name,
				onClick = { onClick() }
			)
			HorizontalDivider(Modifier.padding(linePadding))
			SettingAccountInfoItem(
				type = "ID",
				text = user.userId,
				onClick = { onClick() }
			)
			HorizontalDivider(Modifier.padding(linePadding))
			SettingAccountInfoItem(
				type = "Bio",
				text = user.bio.ifEmpty { "Describe yourself" },
				onClick = { onClick() }
			)
		}
	}
}

@Composable
private fun SettingAccountInfoItem(
	modifier: Modifier = Modifier,
	type: String,
	text: String,
	onClick: () -> Unit
) {
	Surface(onClick = { onClick() }) {
		Column(
			modifier = modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp, vertical = 8.dp)
		) {
			Text(
				text = type,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				style = MaterialTheme.typography.titleSmall
			)
			Spacer(Modifier.height(4.dp))
			Text(
				text, style = MaterialTheme.typography.titleSmall,
				maxLines = 1
			)
		}
	}
}

@Composable
private fun SettingsItem(icon: ImageVector, title: String, onSettingItemClick: () -> Unit) {
	Surface(onClick = { onSettingItemClick() }, modifier = Modifier.fillMaxWidth()) {
		Row(
			horizontalArrangement = Arrangement.spacedBy(8.dp),
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp, vertical = 16.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Icon(
				imageVector = icon,
				modifier = Modifier,
				contentDescription = "setting item icon"
			)
			Text(title, style = MaterialTheme.typography.titleSmall)
		}
	}
}

@PreviewLightDark
@Composable
private fun SettingsPreview() {
	FireChatTheme {
		SettingsContent(
			modifier = Modifier,
			onNavigateBack = {},
			user = User(
				name = "Jack",
				userId = "asdfpoiho212",
				isAnonymous = false,
				bio = "Very nice person doing great things",
			),
			onLogout = {},
			onDeleteAccount = {},
			onLinkAccount = {}
		)
	}
}
