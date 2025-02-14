package com.arashdev.firechat

import android.app.Application
import com.arashdev.firechat.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

class FireChatApplication : Application() {
	override fun onCreate() {
		super.onCreate()
		Timber.plant(Timber.DebugTree())
		startKoin {
			androidLogger()
			androidContext(applicationContext)
			modules(appModule)
		}
	}
}