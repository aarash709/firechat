import io.gitlab.arturbosch.detekt.Detekt

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(testLibs.plugins.android.test) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlinx.ksp) apply false
    alias(libs.plugins.baselineprofile) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.detekt) apply false
//    alias(libs.plugins.kotlinter) apply false
    alias(libs.plugins.secrets) apply false
	alias(libs.plugins.google.gms.google.services) apply false
}

subprojects {
//    apply(from = "../buildscripts/detekt.gradle.kts")
    apply(plugin = "io.gitlab.arturbosch.detekt")
    tasks.withType(Detekt::class.java).configureEach {
        exclude("**/resources/**")
        exclude("**/build/**")
        config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
        buildUponDefaultConfig = false
        parallel = true
    }
}