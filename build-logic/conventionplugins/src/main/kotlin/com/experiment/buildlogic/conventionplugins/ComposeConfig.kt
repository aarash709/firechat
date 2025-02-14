package com.experiment.buildlogic.conventionplugins

import com.android.build.api.dsl.CommonExtension
import getLibs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.composeBuildConfiguration(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
) {
    commonExtension.apply {
        buildFeatures {
            compose = true
        }
        dependencies {
            val composeBom = getLibs.findLibrary("androidx-compose-bom").get()
            add("implementation", platform(composeBom))
            add("androidTestImplementation", platform(composeBom))
        }
    }
}