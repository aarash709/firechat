package com.arashdev.firechat.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccountCircle
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.arashdev.firechat.designsystem.FireChatTheme
import com.arashdev.firechat.model.User
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsListScreen(
	modifier: Modifier = Modifier,
	viewModel: ContactsListViewModel = koinInject(),
	onUserSelected: (userId: String) -> Unit,
	onNavigateBack: () -> Unit
) {
	val contacts = viewModel.contacts
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
					viewModel.createConversation(contact.userId) { conversationId ->
						onUserSelected(conversationId)
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
				Icon(
					imageVector = Icons.Outlined.AccountCircle,
					modifier = Modifier.size(50.dp),
					contentDescription = "contact profile pic"
				)
				Column(modifier = Modifier.padding(0.dp)) {
					Text(text = user.name, fontWeight = FontWeight.Bold)
					Text(text = user.userId, color = Color.Gray)
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