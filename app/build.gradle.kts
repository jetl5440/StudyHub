plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.seg2105.studyhub"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.seg2105.studyhub"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
}
