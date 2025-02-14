package com.arashdev.firechat.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.koin.androidx.compose.koinViewModel

/**
The user`s profile and "editing" status and profile pic
 */
@Composable
fun Profile(
	modifier: Modifier = Modifier,
	viewModel: ProfileViewmodel = koinViewModel(),
	onNavigateBack: () -> Unit
) {
	Box(
		modifier = modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.surface),
		contentAlignment = Alignment.Center
	) {
		ProfileContent(onNavigateBack = { onNavigateBack() })
	}
}

@Composable
fun ProfileContent(modifier: Modifier = Modifier, onNavigateBack: () -> Unit) {
	Surface(modifier = modifier) {
		Row(verticalAlignment = Alignment.CenterVertically) {
			Button(onClick = { onNavigateBack() }) {
				Icon(
					imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
					contentDescription = "",
				)
			}
			Icon(
				imageVector = Icons.Outlined.Warning,
				contentDescription = "",
				tint = Color.Yellow
			)
			Text("Profile Under Construction")
		}
	}

}

/**
The contact(s) inside of a chat profile status and profile pic
 */
@Composable
fun ContactProfile(modifier: Modifier = Modifier, back: () -> Unit) {
	Box(
		modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.surface),
		contentAlignment = Alignment.Center
	) {
		Surface(modifier = modifier.fillMaxSize()) {
			Row(verticalAlignment = Alignment.CenterVertically) {
				Button(onClick = { back() }) {
					Icon(
						imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
						contentDescription = "",
					)
				}
				Icon(
					imageVector = Icons.Outlined.Warning,
					contentDescription = "",
					tint = Color.Yellow
				)
				Text("Chat Profile Under Construction")
			}
		}
	}
}
