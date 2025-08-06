plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.truetap.solana.seeker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.truetap.solana.seeker"
        minSdk = 33
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            
            buildConfigField("boolean", "USE_FAKE_SEED_VAULT", "true")
            buildConfigField("String", "BUILD_FLAVOR", "\"dev\"")
            
            resValue("string", "app_name", "TrueTap (Dev)")
            manifestPlaceholders["appLabel"] = "TrueTap (Dev)"
        }
        
        create("prod") {
            dimension = "environment"
            
            buildConfigField("boolean", "USE_FAKE_SEED_VAULT", "false")
            buildConfigField("String", "BUILD_FLAVOR", "\"prod\"")
            
            resValue("string", "app_name", "TrueTap")
            manifestPlaceholders["appLabel"] = "TrueTap"
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }
    
    kotlin {
        jvmToolchain(18)
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.6.0"
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            // Fix duplicate namespace warning
            excludes += "META-INF/versions/9/previous-compilation-data.bin"
        }
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    
    // Compose BOM and UI
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    
    // Navigation
    implementation(libs.androidx.navigation.compose)
    
    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    
    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    
    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    
    // Serialization
    implementation(libs.kotlinx.serialization.json)
    
    // DataStore
    implementation(libs.androidx.datastore.preferences)
    
    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    
    // Networking
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    
    // Image Loading
    implementation(libs.coil.compose)
    
    // Phosphor Icons
    implementation(libs.phosphor.icons)
    
    // Google Services
    implementation(libs.google.services.auth)
    
    // Solana
    implementation(libs.solana.seed.vault)
    implementation("com.solanamobile:mobile-wallet-adapter-clientlib:2.0.8")
    implementation("com.solanamobile:mobile-wallet-adapter-common:2.0.8")
    implementation("com.solanamobile:mobile-wallet-adapter-clientlib-ktx:2.0.8")
    implementation(libs.solana.kmp)
    
    // Deep Link Wallet Targeting  
    implementation("org.bitcoinj:bitcoinj-core:0.16.3") // For base58 encoding/decoding
    
    // Fake Seed Vault Provider for development/testing
    "devImplementation"(libs.fake.seed.vault.provider)
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}