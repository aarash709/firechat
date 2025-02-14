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

@Composable
fun Settings(
	modifier: Modifier = Modifier,
	viewModel: SettingsViewModel = koinViewModel(),
	back: () -> Unit
) {
	Box(
		modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.surface),
		contentAlignment = Alignment.Center
	) {
		Surface() {
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
				Text("Settings is Under Construction")
			}
		}
	}

}
