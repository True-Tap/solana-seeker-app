pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
    }
}

rootProject.name = "solana-seeker-app"
include(":app")
 