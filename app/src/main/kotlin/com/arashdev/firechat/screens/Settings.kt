package com.arashdev.firechat.screens

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arashdev.firechat.R
import com.arashdev.firechat.designsystem.FireChatTheme
import com.arashdev.firechat.model.User
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.io.encoding.ExperimentalEncodingApi

@Composable
fun Settings(
	modifier: Modifier = Modifier,
	viewModel: SettingsViewModel = koinViewModel(),
	onNavigateBack: () -> Unit,
	onLogout: () -> Unit,
	onDeleteAccount: () -> Unit
) {
	val context = LocalContext.current
	val user by viewModel.user.collectAsStateWithLifecycle()

	fun getRealPathFromURI(uri: Uri, context: Context): String? {
		val returnCursor = context.contentResolver.query(uri, null, null, null, null)
		val nameIndex = returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
		val sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE)
		returnCursor.moveToFirst()
		val name = returnCursor.getString(nameIndex)
		val size = returnCursor.getLong(sizeIndex).toString()
		val file = File(context.filesDir, name)
		try {
			val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
			val outputStream = FileOutputStream(file)
			var read = 0
			val maxBufferSize = 1 * 1024 * 1024
			val bytesAvailable: Int = inputStream?.available() ?: 0
			//int bufferSize = 1024;
			val bufferSize = Math.min(bytesAvailable, maxBufferSize)
			val buffers = ByteArray(bufferSize)
			while (inputStream?.read(buffers).also {
					if (it != null) {
						read = it
					}
				} != -1) {
				outputStream.write(buffers, 0, read)
			}
			Log.e("File Size", "Size " + file.length())
			inputStream?.close()
			outputStream.close()
			Log.e("File Path", "Path " + file.path)

		} catch (e: java.lang.Exception) {
			Log.e("Exception", e.message!!)
		}
		return file.path
	}

	val pickImageLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.PickVisualMedia(),
		onResult = { uri ->
			Timber.e("URI IS :$uri")
			val path = getRealPathFromURI(uri = uri!!, context)
			Timber.e("PATH IS :$path")
			uri?.let {
			}
			viewModel.uploadProfilePhoto(path!!, userId = viewModel.userId)
		})
	Box(
		modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.surface),
		contentAlignment = Alignment.Center
	) {
		var showDeleteAccountDialog by remember { mutableStateOf(false) }
		var showLogoutDialog by remember { mutableStateOf(false) }
		var showLinkAccountDialog by remember { mutableStateOf(false) }
		if (showLinkAccountDialog) {
			var email by remember { mutableStateOf("") }
			var password by remember { mutableStateOf("") }
			var name by remember { mutableStateOf("") }
			AlertDialog(
				onDismissRequest = { showLinkAccountDialog = !showLinkAccountDialog },
				modifier = Modifier,
				title = { Text("Submit your Email and password") },
				text = {
					Column {
						OutlinedTextField(
							value = email,
							label = { Text("Email") },
							onValueChange = { email = it },
							maxLines = 1,
							singleLine = true
						)
						OutlinedTextField(
							value = password,
							label = { Text("Password") },
							onValueChange = { password = it },
							visualTransformation = PasswordVisualTransformation(),
							maxLines = 1,
							singleLine = true
						)
						OutlinedTextField(
							value = name,
							label = { Text("Name") },
							onValueChange = {
								if (it.length <= 20) {
									name = it
								}
							},
							maxLines = 1,
							singleLine = true
						)
					}
				},
				confirmButton = {
					Button(
						onClick = {
							viewModel.linkAccount(
								email = email,
								password = password,
								displayName = name
							)
							showLinkAccountDialog = !showLinkAccountDialog
						},
						enabled = email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty()
					) {
						Text("Link Account")
					}
				},
				dismissButton = {
					TextButton(onClick = { showLinkAccountDialog = !showLinkAccountDialog }) {
						Text("Dismiss")
					}
				}
			)
		}
		if (showDeleteAccountDialog) {
			AlertDialog(
				onDismissRequest = { showDeleteAccountDialog = !showDeleteAccountDialog },
				title = { Text("Delete Account?") },
				modifier = Modifier,
				confirmButton = {
					Button(onClick = {
						showDeleteAccountDialog = !showDeleteAccountDialog
						viewModel.deleteAccount()
						onDeleteAccount()
					}) {
						Text("Delete")
					}
				},
				dismissButton = {
					TextButton(onClick = { showDeleteAccountDialog = !showDeleteAccountDialog }) {
						Text("Dismiss")
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
					Button(onClick = {
						showLogoutDialog = !showLogoutDialog
						viewModel.logout()
						onLogout()
					}) {
						Text("Logout")
					}
				},
				dismissButton = {
					TextButton(onClick = { showLogoutDialog = !showLogoutDialog }) {
						Text("Dismiss")
					}
				}
			)
		}
		SettingsContent(
			modifier = Modifier,
			user = user,
			onNavigateBack = { onNavigateBack() },
			onLogout = { showLogoutDialog = !showLogoutDialog },
			onDeleteAccount = { showDeleteAccountDialog = !showDeleteAccountDialog },
			onLinkAccount = { showLinkAccountDialog = !showLinkAccountDialog },
			onProfilePhoto = {
				pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
			}
		)
	}
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalEncodingApi::class)
@Composable
fun SettingsContent(
	modifier: Modifier = Modifier,
	user: User,
	onNavigateBack: () -> Unit,
	onLinkAccount: () -> Unit,
	onLogout: () -> Unit,
	onDeleteAccount: () -> Unit,
	onProfilePhoto: () -> Unit
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
								onClick = {
									expanded = !expanded
									onLinkAccount()
								},
								leadingIcon = {
									Icon(
										imageVector = Icons.Outlined.Link,
										contentDescription = "link account icon"
									)
								})
						}
						DropdownMenuItem(
							text = {
								Text(
									text = "Log out",
								)
							},
							onClick = {
								expanded = !expanded
								onLogout()
							},
							leadingIcon = {
								Icon(
									imageVector = Icons.AutoMirrored.Outlined.Logout,
									contentDescription = "logout icon",
								)
							},
							enabled = !user.isAnonymous
						)
						DropdownMenuItem(
							text = {
								Text(
									"Delete Account",
									color = MaterialTheme.colorScheme.error
								)
							},
							onClick = {
								expanded = !expanded
								onDeleteAccount()
							},
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
					Surface(
						onClick = { onProfilePhoto() },
						Modifier
							.clip(CircleShape)
							.size(80.dp)
					) {
						if (user.profilePhotoBase64.isNotEmpty()) {
							val bitmap by remember(user.profilePhotoBase64) {
								val array = android.util.Base64.decode(
									user.profilePhotoBase64,
									android.util.Base64.DEFAULT
								)
								mutableStateOf(
									BitmapFactory.decodeByteArray(array, 0, array.size)
										.asImageBitmap()
								)
							}
							Image(
								bitmap,
								contentDescription = ""
							)
						} else {
							Image(
								painterResource(R.drawable.ic_launcher_foreground),
								contentDescription = ""
							)

						}
					}
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
			onLinkAccount = {},
			onProfilePhoto = {}
		)
	}
}
