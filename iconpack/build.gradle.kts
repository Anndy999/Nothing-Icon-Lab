plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.anndy999.nothingicons"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.anndy999.nothingicons"
        minSdk = 24
        targetSdk = 35
        versionCode = 200
        versionName = "0.2.0"
    }
    signingConfigs {
        create("release") {
            val storeFilePath = System.getenv("ANDROID_KEYSTORE_FILE")
            val storePasswordEnv = System.getenv("ANDROID_KEYSTORE_PASSWORD")
            val keyAliasEnv = System.getenv("ANDROID_KEY_ALIAS")
            val keyPasswordEnv = System.getenv("ANDROID_KEY_PASSWORD")
            if (!storeFilePath.isNullOrBlank() && !storePasswordEnv.isNullOrBlank() && !keyAliasEnv.isNullOrBlank() && !keyPasswordEnv.isNullOrBlank()) {
                storeFile = file(storeFilePath)
                storePassword = storePasswordEnv
                keyAlias = keyAliasEnv
                keyPassword = keyPasswordEnv
            }
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            val releaseSigning = signingConfigs.getByName("release")
            signingConfig = if (releaseSigning.storeFile != null) releaseSigning else signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    lint { abortOnError = false }
}
