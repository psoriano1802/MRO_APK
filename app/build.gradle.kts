plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.lacteos_flores"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.lacteos_flores"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    flavorDimensions += "environment"
    productFlavors {
        create("beta"){
            dimension = "environment"
            applicationId = "com.example.lacteos_flores.beta"
            versionNameSuffix = "-BETA"
            buildConfigField("String","ENVIROMENT","\"BETA\"")
            buildConfigField("String","BASE_URL", "\"http://keplerqro.dnsalias.com:1960/\"")
            resValue("string", "app_name", "LF Ruta")
        }
        create("prod"){
            dimension = "environment"
            applicationId = "com.example.lacteos_flores"
            buildConfigField("String","ENVIROMENT","\"test\"")
            buildConfigField("String","BASE_URL", "\"http://kepler.lacteosflores.com:8080/\"")
            resValue("string", "app_name", "LF Ruta test")
        }
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true  // Importante para usar BuildConfig
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //lifecycle
    implementation(libs.lifecycle.runtime.ktx)
    //room
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    //retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    //coroutines
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
    implementation(libs.androidx.room.ktx)
    //ML kit
    implementation(libs.face.detection)
    implementation(libs.barcode.scanning)
    //camera x
    implementation(libs.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)

    //GPS
    implementation(libs.services.location)

}