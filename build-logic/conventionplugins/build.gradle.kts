plugins {
    `kotlin-dsl`
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies{
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradleplugin)
}

gradlePlugin{
    plugins {
        register("compose-application-convention"){
            id = "plugins.compose.application"
            implementationClass = "ComposeApplicationConventionPlugin"
        }
        register("compose-library-convention"){
            id = "plugins.compose.library"
            implementationClass = "ComposeLibraryConventionPlugin"
        }
        register("application-convention"){
            id = "plugins.android.application"
            implementationClass = "ApplicationConventionPlugin"
        }
        register("library-convention"){
            id = "plugins.android.library"
            implementationClass = "LibraryConventionPlugin"
        }
    }
}