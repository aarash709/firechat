package com.arashdev.firechat

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.arashdev.firechat.screens.AuthScreen
import com.arashdev.firechat.screens.ChatScreen
import com.arashdev.firechat.screens.ContactProfile
import com.arashdev.firechat.screens.ContactsListScreen
import com.arashdev.firechat.screens.ConversationsScreen
import com.arashdev.firechat.screens.EditProfile
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
		composable<Auth> {
			AuthScreen {
				navController.navigate(Conversations) {
					launchSingleTop = true
					popUpTo(Auth){
						inclusive = true
					}
				}
			}
		}
		composable<Chat>(
			enterTransition = { slideInHorizontally(tween(transitionTime)) { it } },
			exitTransition = { slideOutHorizontally(tween(transitionTime)) { it } }
		) {
			ChatScreen(
				onNavigateBack = { navController.popBackStack() })
		}
		composable<Contacts>(
			enterTransition = { slideInHorizontally(tween(transitionTime)) { it } },
			exitTransition = {
				when {
					targetState.destination.hierarchy.any {
						it.hasRoute<Chat>()
					} -> {
						slideOutPartialToLeft
					}

					else -> {
						slideOutOfContainer(
							towards = AnimatedContentTransitionScope.SlideDirection.Right,
							animationSpec = tween(transitionTime)
						)
					}
				}
			}) {
			ContactsListScreen(onUserSelected = { userID ->
				navController.navigate(Chat(otherUserID = userID)) {
					launchSingleTop = true
					popUpTo(Contacts) {
						inclusive = true

					}
				}
			},
				onNavigateBack = { navController.popBackStack() })
		}
		composable<Conversations>(
			enterTransition = {

				slideInPartialToRight

			},
			exitTransition = { slideOutPartialToLeft }
		) {
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
		composable<Profile>(
			enterTransition = { slideInHorizontally(tween(transitionTime)) { it } },
			exitTransition = { slideOutHorizontally(tween(transitionTime)) { it } }
		) { EditProfile { navController.popBackStack() } }
		composable<ChatProfile> { ContactProfile { navController.popBackStack() } }
		composable<Settings>(enterTransition = { slideInHorizontally(tween(transitionTime)) { it } },
			exitTransition = { slideOutHorizontally(tween(transitionTime)) { it } }) {
			Settings(
				onNavigateBack = {
					navController.navigate(Auth) {
						popUpTo(Auth) {
							inclusive = true
						}

					}
				},
				onLogout = { navController.navigate(Auth) }
			) { navController.popBackStack() }
		}
	}
}

const val transitionTime = 250

val slideInPartialToLeft = slideInHorizontally(tween(transitionTime)) { it / 3 }
val slideInPartialToRight = slideInHorizontally(tween(transitionTime)) { -it / 3 }
val slideOutPartialToRight = slideOutHorizontally(tween(transitionTime)) { it / 3 }
val slideOutPartialToLeft = slideOutHorizontally(tween(transitionTime)) { -it / 3 }

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
