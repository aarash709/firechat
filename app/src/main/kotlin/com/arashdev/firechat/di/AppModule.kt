package com.arashdev.firechat.di

import com.arashdev.firechat.screens.LoginViewModel
import com.arashdev.firechat.screens.ConversationsViewModel
import com.arashdev.firechat.screens.ChatViewModel
import com.arashdev.firechat.screens.ContactsListViewModel
import com.arashdev.firechat.screens.ProfileViewmodel
import com.arashdev.firechat.screens.SettingsViewModel
import com.arashdev.firechat.service.AuthService
import com.arashdev.firechat.service.AuthServiceImpl
import com.arashdev.firechat.service.RemoteStorageService
import com.arashdev.firechat.service.RemoteStorageServiceImpl
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
	viewModelOf(::LoginViewModel)
	viewModelOf(::ConversationsViewModel)
	viewModelOf(::ChatViewModel)
	viewModelOf(::ContactsListViewModel)
	viewModelOf(::ProfileViewmodel)
	viewModelOf(::SettingsViewModel)
	singleOf(::AuthServiceImpl) { bind<AuthService>() }
	singleOf(::RemoteStorageServiceImpl) { bind<RemoteStorageService>() }
}