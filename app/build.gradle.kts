plugins{
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("jacoco")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.sanosysalvosv2"
    compileSdk = 36

    // Set a full backend URL here for your active API provider.
    // Emulator default keeps local development working.
    val backendBaseUrl = providers.gradleProperty("BFF_BASE_URL").orElse("http://10.0.2.2:8080/").get()

    defaultConfig {
        applicationId = "com.example.sanosysalvosv2"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "BFF_BASE_URL", "\"$backendBaseUrl\"")
        buildConfigField("String", "XANO_BASE_URL", "\"https://x8ki-letl-twmt.n7.xano.io/\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "BFF_BASE_URL", "\"http://10.0.2.2:8080/\"")
        }

        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("com.jakewharton.timber:timber:5.0.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.navigation:navigation-compose:2.8.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("org.osmdroid:osmdroid-android:6.1.18")
    implementation(platform("com.google.firebase:firebase-bom:34.14.1"))
    implementation("com.google.firebase:firebase-messaging-ktx:23.3.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    // Unit Testing
    testImplementation(libs.junit)
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    
    // Android Testing
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

// JaCoCo code coverage configuration
jacoco {
    version = "0.8.10"
}

// Register JaCoCo test report task
tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    group = "verification"
    description = "Generate JaCoCo code coverage report"

    reports {
        xml.required.set(true)
        html.required.set(true)
        xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/jacocoTestReport/jacocoTestReport.xml"))
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/jacocoTestReport/html"))
    }

    classDirectories.setFrom(
        fileTree(layout.buildDirectory.dir("intermediates/classes/debug")) {
            exclude(
                "**/R.class",
                "**/R$*.class",
                "**/BuildConfig.*",
                "**/Manifest*.*",
                "**/*Test*.*",
                "**/ui/theme/**",
                "**/*Preview*.*"
            )
        }
    )

    sourceDirectories.setFrom(files("src/main/java", "src/main/kotlin"))
    executionData.setFrom(files(fileTree(layout.buildDirectory.dir("jacoco")) {
        include("*.exec")
    }))
}
