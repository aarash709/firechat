package com.arashdev.firechat

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.arashdev.firechat.screens.AuthScreen
import com.arashdev.firechat.screens.ContactProfile
import com.arashdev.firechat.screens.ChatScreen
import com.arashdev.firechat.screens.ContactsListScreen
import com.arashdev.firechat.screens.ConversationsScreen
import com.arashdev.firechat.screens.Profile
import com.arashdev.firechat.screens.Settings
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.serialization.Serializable
import timber.log.Timber


@Composable
fun AppNavigation() {
	val navController = rememberNavController()
	val isLoggedIn = Firebase.auth.currentUser != null

	NavHost(
		navController = navController,
		startDestination = if (isLoggedIn) Conversations else Auth
	) {
		composable<Auth> { AuthScreen { navController.navigate(Conversations) } }
		composable<Chat> { ChatScreen(
			onNavigateBack = {navController.popBackStack()}) }
		composable<Contacts> {
			ContactsListScreen(onUserSelected = { userID ->
				navController.navigate(Chat(otherUserID = userID)){
					popUpTo(Contacts){
						inclusive = true
					}
				}
			},
				onNavigateBack = { navController.popBackStack() })
		}
		composable<Conversations> {
			ConversationsScreen(
				onAddNewConversation = {
					navController.navigate(Contacts)
				},
				onConversationClick = { conversationID ->
					Timber.e("con:$conversationID")
					navController.navigate(Chat(otherUserID = conversationID))
				},
				onNavigateToProfile = { navController.navigate(Profile) },
				onNavigateToSettings = { navController.navigate(Settings) },
				onNavigateToContacts = { navController.navigate(Contacts) })
		}
		composable<Profile> { Profile { navController.popBackStack() } }
		composable<ChatProfile> { ContactProfile { navController.popBackStack() } }
		composable<Settings> { Settings { navController.popBackStack() } }
	}
}

@Serializable
object Auth

@Serializable
data class Chat(val otherUserID: String)

@Serializable
object Conversations

@Serializable
object Profile

@Serializable
object ChatProfile

@Serializable
object Settings

@Serializable
object Contacts
