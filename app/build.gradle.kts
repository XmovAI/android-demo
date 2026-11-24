import java.text.SimpleDateFormat
import java.util.Date
import java.util.Properties
import java.util.TimeZone

// Optional local signing config, kept out of version control.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id("com.google.protobuf") version "0.9.4"
}

android {
    namespace = "com.xmov.ai.echomind.demo"
    compileSdk = 36

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    defaultConfig {
        applicationId = "com.xmov.ai.echomind.demo"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Use a local keystore.properties if provided; otherwise fall back to debug signing.
            signingConfig = if (keystorePropertiesFile.exists()) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }

    applicationVariants.configureEach {
        outputs.configureEach {
            val format = SimpleDateFormat("yyyyMMddHHmm")
            format.timeZone = TimeZone.getTimeZone("Asia/Shanghai")
            val time = format.format(Date())
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl)
                .outputFileName = "XingYunAvatarAgentSDKDemo_${time}.apk"
        }
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.21.12"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
            }
        }
    }
}

dependencies {

    implementation(project(":lib_core"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.concentus)
    implementation("com.google.code.gson:gson:2.13.1")
    implementation("com.squareup.okhttp3:okhttp:5.1.0")
    implementation("io.socket:socket.io-client:2.1.0")
    implementation("org.msgpack:msgpack-core:0.9.3")
    implementation("javax.vecmath:vecmath:1.5.2")
    implementation("com.google.protobuf:protobuf-javalite:3.21.12")
    implementation("androidx.media3:media3-exoplayer:1.9.0")
    implementation(files("libs/xingyun_avatar_agent-v0.0.2.aar"))
}