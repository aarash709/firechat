package com.arashdev.firechat.screens

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arashdev.firechat.designsystem.FireChatTheme
import com.arashdev.firechat.model.Message
import com.arashdev.firechat.utils.formatUtcToHoursAndMinutes
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
	modifier: Modifier = Modifier,
	viewModel: ChatViewModel = koinViewModel(),
	onNavigateBack: () -> Unit,
) {
	val messages by viewModel.messages.collectAsStateWithLifecycle()
	val contact by viewModel.contact.collectAsStateWithLifecycle()
	val lastSeen = contact.lastSeen

	var messageText by remember { mutableStateOf("") }

	val listState = rememberLazyListState()

	LaunchedEffect(true, contact) {
		if (messages.isNotEmpty()) {
			listState.animateScrollToItem(0)
		}
	}

	Scaffold(
		modifier = modifier.imePadding(),
		topBar = {
			TopAppBar(
				modifier = modifier.padding(horizontal = 0.dp),
				title = {
					Row(
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.spacedBy(8.dp)
					) {
						Surface(
							onClick = { /*onContactSelected()*/ },
							Modifier
								.clip(CircleShape)
								.size(40.dp)
						) {
							if (contact.profilePhotoBase64.isNotEmpty()) {
								val bitmap by remember(contact.profilePhotoBase64) {
									val array = Base64.decode(
										contact.profilePhotoBase64,
										Base64.DEFAULT
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
								Box(
									Modifier.background(Color.Gray),
									contentAlignment = Alignment.Center
								) {
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
						Column {
							Text(text = contact.name, style = MaterialTheme.typography.titleLarge)
							Text(
								text = lastSeen,
								style = MaterialTheme.typography.bodyMedium,
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}
					}
				},
				navigationIcon = {
					IconButton(onClick = { onNavigateBack() }) {
						Icon(
							imageVector = Icons.AutoMirrored.Filled.ArrowBack,
							contentDescription = "navigate back icon",
						)
					}
				}, actions = {
					IconButton(onClick = { /*onToggleContextMenu()*/ }) {
						Icon(
							imageVector = Icons.Default.MoreVert,
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
		},
		bottomBar = {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.navigationBarsPadding()
			) {
//				BasicTextField()
				TextField(
					value = messageText,
					onValueChange = { messageText = it },
					modifier = Modifier
						.weight(1f)
						.padding(horizontal = 16.dp)
						.onKeyEvent {
							if (it.key == Key.Enter) {
								viewModel.sendMessage(text = messageText)
								messageText = ""
								true
							} else false
						},
					placeholder = { Text("Message", style = MaterialTheme.typography.bodyMedium) },
					singleLine = false,
					maxLines = 5,
					shape = RoundedCornerShape(32.dp),
					keyboardActions = KeyboardActions(onSend = {
						viewModel.sendMessage(text = messageText)
						messageText = ""
					}),
					trailingIcon = {
						IconButton(onClick = {
							viewModel.sendMessage(text = messageText)
							messageText = ""
						}) {
							Icon(
								imageVector = Icons.Default.Send,
								contentDescription = "send button"
							)
						}
					},
					keyboardOptions = KeyboardOptions(
						keyboardType = KeyboardType.Text,
						imeAction = ImeAction.Send
					),
					colors = TextFieldDefaults.colors(
						focusedIndicatorColor = Color.Transparent,
						errorIndicatorColor = Color.Transparent,
						disabledIndicatorColor = Color.Transparent,
						unfocusedIndicatorColor = Color.Transparent
					)
				)
			}
		}) { padding ->
		LazyColumn(
			modifier = Modifier
				.fillMaxSize()
				.padding(padding),
			state = listState,
			reverseLayout = true,
			verticalArrangement = Arrangement.Bottom,
		) {
			items(messages) { message ->
				MessageBubble(
					modifier = Modifier.padding(16.dp, vertical = 4.dp),
					message = message,
					currentUserId = viewModel.currentUserID
				)
			}
		}
	}
}

@Composable
fun MessageBubble(modifier: Modifier = Modifier, message: Message, currentUserId: String) {
	val isCurrentUser = message.senderId == currentUserId
	Box(
		modifier = modifier
			.fillMaxWidth(),
		contentAlignment = if (isCurrentUser) Alignment.BottomEnd else Alignment.BottomStart
	) {
		Column(
			modifier = Modifier
				.background(
					if (isCurrentUser) Color.Blue else Color.Gray,
					RoundedCornerShape(8.dp)
				)
				.widthIn(100.dp, max = 400.dp)
				.padding(8.dp)

		) {
			Text(
				text = message.text,
				modifier = Modifier,
				color = Color.White
			)
			Text(
				modifier = Modifier.align(Alignment.End),
				text = formatUtcToHoursAndMinutes(message.timestamp),
				style = MaterialTheme.typography.bodySmall,
				color = Color.White
			)
		}
	}
}

@PreviewLightDark
@Composable
private fun BubblePreview() {
	FireChatTheme {
		val modifier = Modifier
		MessageBubble(
			modifier = modifier,
			message = Message(
				id = "id",
				text = "This is a sample text",
				senderId = "senderid",
				timestamp = 1739346011,
				encryptedMessage = "",
				encryptedAesKey = "",
				iv = ""
			), currentUserId = "penatibus"
		)
	}
}