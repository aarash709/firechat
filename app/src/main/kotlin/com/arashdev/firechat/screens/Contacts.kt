package com.arashdev.firechat.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arashdev.firechat.designsystem.FireChatTheme
import com.arashdev.firechat.model.User
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsListScreen(
	modifier: Modifier = Modifier,
	viewModel: ContactsListViewModel = koinInject(),
	onUserSelected: (otherUserId: String) -> Unit,
	onNavigateBack: () -> Unit
) {
	val contacts by viewModel.contacts.collectAsStateWithLifecycle()
	Scaffold(
		modifier = modifier,
		topBar = {
			TopAppBar(
				title = { Text("New Message", style = MaterialTheme.typography.titleMedium) },
				modifier = Modifier,
				navigationIcon = {
					IconButton(onClick = { onNavigateBack() }) {
						Icon(
							imageVector = Icons.AutoMirrored.Filled.ArrowBack,
							contentDescription = "navigation icon",
						)
					}
				},
				actions = {
					IconButton(onClick = {}) {
						Icon(
							imageVector = Icons.Default.Search,
							contentDescription = "search icon",
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
		LazyColumn(
			Modifier
				.padding(padding)
				.navigationBarsPadding()
		) {
			items(contacts) { contact ->
				UserItem(user = contact, onContactClick = {
					viewModel.createConversation(contact.userId) { _ ->
						onUserSelected(contact.userId)
					}
				})
			}
		}
	}
}

@Composable
fun UserItem(modifier: Modifier = Modifier, user: User, onContactClick: () -> Unit) {
	Surface(modifier = modifier.fillMaxWidth(), onClick = { onContactClick() }) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(8.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(8.dp)
			) {
				Surface(
					onClick = { },
					Modifier
						.clip(CircleShape)
						.size(50.dp)
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
						Box(Modifier.background(Color.Gray), contentAlignment = Alignment.Center) {
							Icon(
								imageVector = Icons.Default.Person,
								modifier = Modifier
									.fillMaxSize()
									.padding(8.dp),
								contentDescription = null
							)
						}

					}

				}
				Column(modifier = Modifier.padding(0.dp)) {
					Text(text = user.name, fontWeight = FontWeight.Bold)
					Text(text = user.userId, color = MaterialTheme.colorScheme.onSurfaceVariant)
				}
			}
		}
	}
}

@PreviewLightDark
@Composable
private fun ItemPreview() {
	FireChatTheme {
		UserItem(user = User(userId = "tractatos", name = "Chris McMahon", createdAt = 3770)) { }
	}
}