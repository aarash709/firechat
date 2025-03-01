package com.arashdev.firechat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.arashdev.firechat.designsystem.FireChatTheme
import com.arashdev.firechat.service.RemoteStorageService
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

	private val database by inject<RemoteStorageService>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		lifecycleScope.launch { database.updateUserPresenceStatus(isOnline = true) }
		enableEdgeToEdge()
		setContent {
			FireChatTheme {
				AppNavigation()
			}
		}
	}

	override fun onStop() {
		super.onStop()
		lifecycleScope.launch { database.updateUserPresenceStatus(isOnline = false) }

	}
}
