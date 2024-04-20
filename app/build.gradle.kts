plugins {
    alias(libs.plugins.android.app)
    alias(libs.plugins.android.kotlin)
}

android {
    namespace = "io.github.zyrouge.symphony"
    compileSdk = libs.versions.compile.sdk.get().toInt()

    defaultConfig {
        applicationId = "io.github.zyrouge.symphony"
        minSdk = libs.versions.min.sdk.get().toInt()
        targetSdk = libs.versions.target.sdk.get().toInt()

        versionCode = 111
        versionName = "2024.4.111"
        versionName = System.getenv("APP_VERSION_NAME") ?: versionName

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        register("release") {
            val storeFileEnv = System.getenv("SIGNING_KEYSTORE_FILE")
            storeFile = if (null != storeFileEnv) rootProject.file(storeFileEnv) else null
            storePassword = System.getenv("SIGNING_KEYSTORE_PASSWORD")
            keyAlias = System.getenv("SIGNING_KEY_ALIAS")
            keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.findByName("release")
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
        }
        getByName("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }
    }
}

dependencies {
    implementation(libs.activity.compose)
    implementation(libs.coil)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.material3)
    implementation(libs.compose.navigation)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.core)
    implementation(libs.core.splashscreen)
    implementation(libs.fuzzywuzzy)
    implementation(libs.jaudiotagger)
    implementation(libs.lifecycle.runtime)
    implementation(libs.media)
    implementation(libs.okhttp3)

    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}
