package com.arashdev.firechat.screens


import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arashdev.firechat.R
import com.arashdev.firechat.designsystem.FireChatTheme
import com.arashdev.firechat.model.Conversation
import com.arashdev.firechat.model.User
import com.arashdev.firechat.utils.formatUtcToLocalTime
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun ConversationsScreen(
	modifier: Modifier = Modifier,
	viewModel: ConversationsViewModel = koinViewModel(),
	onAddNewConversation: () -> Unit,
	onConversationClick: (String) -> Unit,
	onNavigateToProfile: () -> Unit,
	onNavigateToSettings: () -> Unit,
	onNavigateToContacts: () -> Unit
) {
	val scope = rememberCoroutineScope()

	val conversations by viewModel.conversations.collectAsStateWithLifecycle()
	val user by viewModel.user.collectAsStateWithLifecycle()

	val drawerState = rememberDrawerState(
		DrawerValue.Closed
	)
	ModalNavigationDrawer(
		modifier = modifier,
		drawerContent = {
			ConversationsDrawer(
				user = user,
				accountStatus = user.isAnonymous.toString(),
				onThemeSwitch = { },
				onNavigateToProfile = { onNavigateToProfile() },
				onNavigateToSettings = { onNavigateToSettings() },
				onNavigateToContacts = { onNavigateToContacts() })
		},
		drawerState = drawerState
	) {

		val contentOffset by
		animateDpAsState(targetValue = if (drawerState.targetValue == DrawerValue.Open) 12.dp else 0.dp)
		Scaffold(
			modifier = modifier
				.offset {
					IntOffset(x = contentOffset.toPx().toInt(), y = 0)
				},
			topBar = {
				ConversationsTopBar(
					onToggleDrawer = {
						scope.launch {
							drawerState.open()
						}
					},
					onToggleContextMenu = {})
			},
			floatingActionButton = {
				FilledIconButton(onClick = { onAddNewConversation() }) {
					Icon(
						imageVector = Icons.Outlined.Add,
						contentDescription = "Add chat button"
					)
				}
			}
		) { padding ->
			Surface(modifier = Modifier.fillMaxSize()) {
				if (conversations.isNotEmpty()) {
					LazyColumn(
						modifier
							.fillMaxWidth()
							.padding(padding),
						verticalArrangement = Arrangement.spacedBy(8.dp),
						contentPadding = PaddingValues()
					) {
						items(conversations) { conversation ->
							ConversationItem(
								conversation = conversation,
								onConversationClick = {
									onConversationClick(conversation.participantIds.first { it != user.userId })
								}
							)
						}
					}
				} else {
					EmptyConversation()
				}
			}
		}

	}
}

@Composable
private fun ConversationsDrawer(
	user: User,
	accountStatus: String,
	onNavigateToProfile: () -> Unit,
	onNavigateToSettings: () -> Unit,
	onNavigateToContacts: () -> Unit,
	onThemeSwitch: (Boolean) -> Unit
) {
	var isChecked by remember {
		mutableStateOf(false)
	}
	val inDarkMode = isSystemInDarkTheme()
	ModalDrawerSheet(
		modifier = Modifier,
		drawerShape = RectangleShape,
		windowInsets = WindowInsets(0, 0, 0, 0)
	) {
		Card(modifier = Modifier, shape = RectangleShape) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.statusBarsPadding()
					.padding(horizontal = 16.dp, vertical = 16.dp),
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				Row(
					modifier = Modifier
						.fillMaxWidth(),
					horizontalArrangement = Arrangement.SpaceBetween,
					verticalAlignment = Alignment.CenterVertically
				) {
					Surface(
						onClick = { onNavigateToSettings() },
						Modifier
							.clip(CircleShape)
							.size(80.dp)
					) {
						if (user.profilePhotoBase64.isNotEmpty()) {
							val bitmap by remember(user.profilePhotoBase64) {
								val array = Base64.decode(
									user.profilePhotoBase64,
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

					Switch(
						checked = !inDarkMode,
						onCheckedChange = {
							isChecked = it
							onThemeSwitch(it)
						},
						thumbContent = {
							if (inDarkMode)
								Icon(
									imageVector = Icons.Default.NightlightRound,
									contentDescription = ""
								)
							else
								Icon(
									imageVector = Icons.Default.WbSunny,
									contentDescription = ""
								)

						})
				}
				Text(text = user.name, style = MaterialTheme.typography.titleLarge)
				Text(
					text = accountStatus,
					style = MaterialTheme.typography.titleSmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		}
		NavigationDrawerItem(
			label = { Text("Contacts") },
			selected = false,
			onClick = { onNavigateToContacts() },
			icon = {
				Icon(
					imageVector = Icons.Outlined.Person,
					contentDescription = "profile icon"
				)
			},
			shape = RectangleShape
		)
		NavigationDrawerItem(
			label = { Text("Settings") },
			selected = false,
			onClick = { onNavigateToSettings() },
			icon = {
				Icon(
					imageVector = Icons.Outlined.Settings,
					contentDescription = "profile icon"
				)
			},
			shape = RectangleShape
		)
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsTopBar(
	modifier: Modifier = Modifier,
	onToggleDrawer: () -> Unit,
	onToggleContextMenu: () -> Unit
) {
	TopAppBar(
		modifier = modifier.padding(horizontal = 0.dp),
		title = {
			Text(
				stringResource(R.string.app_name),
				style = MaterialTheme.typography.titleMedium
			)
		},
		navigationIcon = {
			IconButton(onClick = { onToggleDrawer() }) {
				Icon(
					imageVector = Icons.Outlined.Menu,
					contentDescription = "menu icon"
				)
			}
		}, actions = {
			IconButton(onClick = { onToggleContextMenu() }) {
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
}

@Composable
fun ConversationItem(
	modifier: Modifier = Modifier,
	conversation: Conversation,
	onConversationClick: () -> Unit
) {
	Surface(modifier = modifier.fillMaxWidth(), onClick = { onConversationClick() }) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 8.dp, vertical = 8.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			Row(
				modifier = Modifier.weight(4f),
				horizontalArrangement = Arrangement.spacedBy(8.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				Surface(
					onClick = { },
					Modifier
						.clip(CircleShape)
						.size(50.dp)
				) {
					if (conversation.contactPhotoBase64.isNotEmpty()) {
						val bitmap by remember(conversation.contactPhotoBase64) {
							val array = Base64.decode(
								conversation.contactPhotoBase64,
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

				Column(modifier = Modifier) {
					Text(
						text = conversation.contactName,
						style = MaterialTheme.typography.bodyLarge
					)
					Text(
						text = conversation.lastMessage,
						modifier = Modifier,
						style = MaterialTheme.typography.bodyMedium,
						overflow = TextOverflow.Ellipsis,
						maxLines = 1
					)
				}
			}
			Row(
				horizontalArrangement = Arrangement.End,
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier
					.height(IntrinsicSize.Max)
					.weight(1.5f),
			) {
				Icon(
					imageVector = Icons.Default.Check,
					modifier = Modifier.size(20.dp), contentDescription = "received checkmark"
				)
				Text(formatUtcToLocalTime(conversation.lastMessageTime), fontSize = 12.sp)
			}
		}
	}
}

@Composable
fun EmptyConversation(modifier: Modifier = Modifier) {
	Column(
		modifier = modifier.fillMaxWidth(),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		Card {
			Text("add a new conversations by tapping the button below")
		}
	}
}

@PreviewLightDark
@Composable
private fun ConversationsPreview() {
	FireChatTheme {
		ConversationsScreen(
			onAddNewConversation = {},
			onConversationClick = {},
			modifier = Modifier,
			onNavigateToProfile = {},
			onNavigateToSettings = {},
			onNavigateToContacts = {}
		)
	}
}