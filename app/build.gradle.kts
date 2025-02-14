plugins {
    alias(libs.plugins.conventionPlugins.android.application)
    alias(libs.plugins.conventionPlugins.android.compose.application)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.arashdev.firechat"

    defaultConfig {
        applicationId = "com.arashdev.firechat"
        versionCode = 1
        versionName = "0.1-alpha"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
        create("benchmark") {
            initWith(buildTypes.getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            isDebuggable = false
        }
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    //kotlin
    implementation(libs.kotlinx.serialization)

    //compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)

    //firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)

    //log
    implementation(libs.timber)

    //test
    testImplementation(testLibs.junit)
    androidTestImplementation(testLibs.androidx.junit)
    androidTestImplementation(testLibs.androidx.espresso.core)
    androidTestImplementation(platform(testLibs.androidx.compose.bom))
    androidTestImplementation(testLibs.androidx.compose.ui.test.junit4)
    debugImplementation(testLibs.androidx.compose.ui.test.manifest)
}