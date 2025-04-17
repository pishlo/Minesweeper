plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.minesweeper"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.minesweeper"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // JSON converter
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // OkHttp for logging (optional but useful)
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

    implementation ("com.google.android.material:material:1.6.0")
}