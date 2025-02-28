rootProject.name = "my-new-kotlin-project"

pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

plugins {
    id("de.fayard.refreshVersions") version "0.60.5"
}

refreshVersions {
}
