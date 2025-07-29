plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    kotlin("android") version "2.0.0"
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Add logging configuration
        manifestPlaceholders["loggingEnabled"] = "false"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            
            // Ensure proguard rules are properly formatted with correct paths
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
                "proguard-logging.pro"
            )
            
            // Configure logging related options for release
            buildConfigField("boolean", "ENABLE_LOGGING", "false")
            
            // Optimize dex
            isShrinkResources = true
            
            // Additional logging suppressions
            manifestPlaceholders["loggingEnabled"] = "false"
        }
        
        debug {
            // Even in debug, use some optimization to reduce logs
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // But enable logging for debugging
            buildConfigField("boolean", "ENABLE_LOGGING", "true")
            // Add logging-related flags
            manifestPlaceholders["loggingEnabled"] = "true"
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    // Add ProGuard configuration options to make it less strict
    packaging {
        resources {
            excludes += listOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/ASL2.0",
                "META-INF/*.kotlin_module"
            )
        }
    }
}

dependencies {
    // Firebase dependencies - using BoM (Bill of Materials) to manage versions
    implementation(platform("com.google.firebase:firebase-bom:34.0.0"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-analytics")
    
    // Firebase Crashlytics
    implementation("com.google.firebase:firebase-crashlytics")
    
    // Elimină versiunile explicite ale Firebase pentru a evita conflictele
    // implementation("com.google.firebase:firebase-auth:22.3.0") - șters
    // implementation(libs.firebase.database) // Removed as we're using BoM version
    
    // Flogger dependencies for proper logging configuration
    implementation("com.google.flogger:flogger:0.7.4")
    implementation("com.google.flogger:flogger-system-backend:0.7.4")
    implementation("com.google.guava:guava:31.1-android")
    
    // Networking
    implementation("com.android.volley:volley:1.2.1")
    
    implementation("com.google.android.gms:play-services-maps:18.1.0")
    implementation("com.google.maps.android:android-maps-utils:2.3.0")

    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.gridlayout:gridlayout:1.0.0")
    implementation("com.github.chrisbanes:PhotoView:2.3.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.4.1")
    implementation("androidx.lifecycle:lifecycle-livedata:2.4.1")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.google.android.gms:play-services-base:18.3.0")
    implementation("com.google.android.gms:play-services-basement:18.3.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.activity)
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("nl.dionsegijn:konfetti-xml:2.0.3")
    // MPAndroidChart library for data visualization
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    
    // Additional missing dependencies
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.cardview:cardview:1.0.0")
    
    // Security dependencies
    implementation("androidx.security:security-crypto:1.1.0-alpha06") // Encrypted SharedPreferences
    implementation("com.scottyab:secure-preferences-lib:0.1.7") // Alternative secure preferences
    implementation("com.google.crypto.tink:tink-android:1.8.0") // Google's cryptographic library
    implementation("androidx.biometric:biometric:1.2.0-alpha05") // Biometric authentication

    // Onboarding dependencies
    implementation("com.tbuonomo:dotsindicator:5.0") // Dots indicator for ViewPager2
    implementation("de.hdodenhof:circleimageview:3.1.0") // Circle ImageView for profile pictures
    
    // ExoPlayer dependencies for video tutorials
    implementation("com.google.android.exoplayer:exoplayer-core:2.18.7")
    implementation("com.google.android.exoplayer:exoplayer-ui:2.18.7")
    
    // MotionLayout for animations (already included above)
    
    // Voice command and speech recognition
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    
    // In-app review API
    implementation("com.google.android.play:review:2.0.1")
    implementation("com.google.android.play:review-ktx:2.0.1")
    
    // Material Tap Target Prompt for onboarding
    implementation("uk.co.samuelwall:material-tap-target-prompt:3.3.2")
    
    // Room persistence library
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    
    // uCrop image cropping library
    implementation("com.github.yalantis:ucrop:2.2.7")
    
    // Activity Result APIs
    implementation("androidx.activity:activity:1.8.2")
    implementation("androidx.fragment:fragment:1.7.0")
    
    // Transition animations
    implementation("androidx.transition:transition:1.4.1")

    // Unit Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.3.1")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    testImplementation("androidx.room:room-testing:2.6.1")
    testImplementation("org.robolectric:robolectric:4.11.1")
    
    // Android Testing
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
    androidTestImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
    
    // Material Design Testing
    androidTestImplementation("com.google.android.material:material:1.11.0")
    
    // Firebase Testing
    androidTestImplementation("com.google.firebase:firebase-firestore")
    androidTestImplementation("com.google.firebase:firebase-auth")
    
    // Performance Testing
    androidTestImplementation("androidx.benchmark:benchmark-junit4:1.2.0")
    
    // Security Testing
    testImplementation("androidx.security:security-crypto:1.1.0-alpha06")
}
