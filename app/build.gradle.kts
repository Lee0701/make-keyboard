plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.android.kotlin)
    alias(libs.plugins.kotlinx.serialization)
}

android {
    namespace = "ee.oyatl.ime.make"
    compileSdk = 34

    defaultConfig {
        applicationId = "ee.oyatl.ime.make"
        minSdk = 21
        targetSdk = 34
        versionCode = 11
        versionName = "20240619-11-324f087"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation(libs.flexbox)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kaml)
    implementation(libs.play.services.oss.licenses)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

tasks.register("printVersionCode") {
    println(android.defaultConfig.versionCode)
}

tasks.register("printVersionName") {
    println(android.defaultConfig.versionName)
}

tasks.register("printPackageName") {
    println(android.defaultConfig.applicationId)
}
