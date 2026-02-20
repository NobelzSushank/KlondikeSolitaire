// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    // Compose compiler plugin (Kotlin 2.0+ requirement for Compose projects)
    alias(libs.plugins.kotlin.compose) apply false
}