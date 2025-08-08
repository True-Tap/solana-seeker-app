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
            buildConfigField("boolean", "DEMO_MODE", "true")
            val heliusDev = System.getenv("HELIUS_DEVNET_KEY") ?: ""
            buildConfigField("String", "RPC_PRIMARY", "\"https://api.devnet.solana.com\"")
            buildConfigField("String", "RPC_SECONDARY", "\"https://rpc.helius.xyz/?api-key=$heliusDev\"")
            buildConfigField("String", "RPC_TERTIARY", "\"https://api.mainnet-beta.solana.com\"")
            buildConfigField("String", "BUILD_FLAVOR", "\"dev\"")
            
            resValue("string", "app_name", "TrueTap (Dev)")
            manifestPlaceholders["appLabel"] = "TrueTap (Dev)"
        }
        
        create("prod") {
            dimension = "environment"
            
            buildConfigField("boolean", "USE_FAKE_SEED_VAULT", "false")
            buildConfigField("boolean", "DEMO_MODE", "false")
            val heliusMain = System.getenv("HELIUS_MAINNET_KEY") ?: ""
            buildConfigField("String", "RPC_PRIMARY", "\"https://rpc.helius.xyz/?api-key=$heliusMain\"")
            buildConfigField("String", "RPC_SECONDARY", "\"https://api.mainnet-beta.solana.com\"")
            buildConfigField("String", "RPC_TERTIARY", "\"https://api.quicknode.com\"")
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

    // Increase DX/D8 memory to avoid OOMs in CI during dex merging
    // CI can also set ORG_GRADLE_JVM_ARGS but this provides a sane default
    tasks.withType<com.android.build.gradle.internal.tasks.DexMergingTask>().configureEach {
        // Limit worker count to reduce peak memory
        this.workerExecutor.noIsolation()
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
    // Use icons-extended without explicit version; rely on Compose BOM for alignment
    implementation("androidx.compose.material:material-icons-extended")
    
    // Navigation
    implementation(libs.androidx.navigation.compose)
    
    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    
    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
        // Hilt + WorkManager integration (for @HiltWorker)
        implementation("androidx.hilt:hilt-work:1.2.0")
        ksp("androidx.hilt:hilt-compiler:1.2.0")
    
    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    
    // Serialization
    implementation(libs.kotlinx.serialization.json)
    
    // DataStore
    implementation(libs.androidx.datastore.preferences)
        // Encrypted storage for auth/session tokens
        implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    
    // Networking
    implementation(libs.retrofit)
    implementation(libs.okhttp)
        // WorkManager
        implementation("androidx.work:work-runtime-ktx:2.9.0")
    
    // Image Loading
    implementation(libs.coil.compose)
    
    // Phosphor Icons
    implementation(libs.phosphor.icons)
    
    // QR Code Generation
    implementation(libs.zxing.core)
    
    // Google Services
    implementation(libs.google.services.auth)
    
        // Solana
    implementation(libs.solana.seed.vault)
    implementation("com.solanamobile:mobile-wallet-adapter-common:2.0.8")
    implementation("com.solanamobile:mobile-wallet-adapter-clientlib-ktx:2.0.8")
    implementation(libs.solana.kmp)
    
    // Deep Link Wallet Targeting  
    implementation("org.bitcoinj:bitcoinj-core:0.16.3") // For base58 encoding/decoding
    
    // Fake Seed Vault Provider for development/testing
    "devImplementation"(libs.fake.seed.vault.provider)
    
    // Testing
    testImplementation(libs.junit)
        testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
        testImplementation("org.mockito:mockito-core:5.18.0")
        testImplementation("org.mockito.kotlin:mockito-kotlin:6.0.0")
        // Compose UI testing in local unit tests (via Robolectric)
        testImplementation(platform(libs.androidx.compose.bom))
        testImplementation(libs.androidx.ui.test.junit4)
        testImplementation("androidx.test:core:1.6.1")
        testImplementation("org.robolectric:robolectric:4.12.2")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}