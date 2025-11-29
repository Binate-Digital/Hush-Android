plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("com.google.dagger.hilt.android")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.fictivestudios.demo"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.fictivestudios.demo"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        buildConfig = true
    }
    @Suppress("UnstableApiUsage")
    buildFeatures {
        dataBinding = true
        viewBinding = true
        buildConfig = true
    }

}

dependencies {
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("com.google.maps.android:android-maps-utils:3.19.1")
    implementation("com.google.maps.android:maps-utils-ktx:5.2.1")
    implementation("com.google.android.libraries.places:places:5.1.1")



    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    //Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.9.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.6")
    implementation("androidx.navigation:navigation-dynamic-features-fragment:2.9.6")
    implementation("androidx.recyclerview:recyclerview:1.4.0")

    //MultiStateView
    implementation("com.github.Kennyc1012:MultiStateView:2.2.0")
    //Dagger Hilt
    implementation("com.google.dagger:hilt-android:2.57.2")
    kapt("com.google.dagger:hilt-compiler:2.57.2")

    //Coroutine
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    //retrofit
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.3.2")
    implementation("com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:0.9.2")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")

    implementation("androidx.lifecycle:lifecycle-common-java8:2.10.0")
    kapt("androidx.lifecycle:lifecycle-compiler:2.10.0")

    //Datastore Preference
    implementation("androidx.datastore:datastore-preferences:1.2.0")

    //Shimmer
    implementation("com.facebook.shimmer:shimmer:0.5.0")

    implementation("com.vanniktech:android-image-cropper:4.7.0")

    /* Country Code Picker Dependency*/
    implementation("com.hbb20:ccp:2.7.3")

    //Firebase Authentication
    implementation("com.google.firebase:firebase-auth-ktx:23.2.1")
    implementation("com.google.android.gms:play-services-auth:21.4.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.firebase:firebase-messaging-ktx:24.1.2")

    //Glide
    kapt("com.github.bumptech.glide:compiler:5.0.5")
    implementation("com.github.bumptech.glide:okhttp3-integration:5.0.5")
    implementation("com.github.bumptech.glide:glide:5.0.5")
    implementation("com.twilio:voice-android:6.9.0")
    implementation("com.google.guava:guava:33.5.0-android")
    implementation("com.android.volley:volley:1.2.1")


    implementation("androidx.camera:camera-core:1.5.1")
    implementation("androidx.camera:camera-camera2:1.5.1")
    implementation("androidx.camera:camera-view:1.5.1")
    implementation("androidx.camera:camera-lifecycle:1.5.1")
    implementation("androidx.camera:camera-extensions:1.5.1")
    implementation("androidx.biometric:biometric:1.2.0-alpha05")
    implementation("io.reactivex:rxjava:1.3.8")
    implementation("io.reactivex:rxandroid:1.2.1")
    implementation("com.writingminds:FFmpegAndroid:0.3.2")
    implementation("com.github.AbedElazizShe:LightCompressor:1.3.2")
    implementation("androidx.media3:media3-exoplayer:1.8.0")
    implementation("androidx.media3:media3-ui:1.8.0")


}

