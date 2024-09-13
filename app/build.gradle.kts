plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.android.kotlin)
    alias(libs.plugins.kotlinx.serialization)
}

android {
    namespace = "ee.oyatl.ime.make"
    compileSdk = 35

    defaultConfig {
        applicationId = "ee.oyatl.ime.make"
        minSdk = 21
        targetSdk = 35
        versionCode = 40
        versionName = "20240913-40-06f175d"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        resValue("string", "app_version", versionName.orEmpty())
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
    lint {
        baseline = file("lint-baseline.xml")
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
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
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
