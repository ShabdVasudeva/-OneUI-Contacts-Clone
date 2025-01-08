
plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    namespace = "apw.sec.android.contacts"
    compileSdk = 34
    
    defaultConfig {
        applicationId = "apw.sec.android.contacts"
        minSdk = 26
        targetSdk = 31
        versionCode = 150172
        versionName = "15.01.72"
        
        vectorDrawables { 
            useSupportLibrary = true
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        viewBinding = true
    }
    
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "11"
}

configurations.all {
    // remove necessary modules 
}

dependencies {
    implementation("androidx.core:core-ktx:1.10.0")
    implementation("io.github.oneuiproject:design:1.2.3")
    implementation("io.github.oneuiproject.sesl:indexscroll:1.0.3")
    implementation("io.github.oneuiproject:icons:1.0.1")
    implementation("androidx.core:core-splashscreen:1.0.0")
    implementation("io.github.oneuiproject.sesl:appcompat:1.3.0")
    implementation("io.github.oneuiproject.sesl:material:1.4.0")
    implementation("io.github.oneuiproject.sesl:recyclerview:1.3.0")
    implementation("io.github.oneuiproject.sesl:preference:1.1.0")
    implementation("com.github.bumptech.glide:glide:4.14.2")
    annotationProcessor("com.github.bumptech.glide:compiler:4.14.2")
    // TODO: add/update/remove your libraries according to you
}