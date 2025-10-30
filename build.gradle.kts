plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.opc.demo"
    compileSdk = 34
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    defaultConfig {
        applicationId = "com.opc.demo"
        minSdk = 24
        targetSdk = 34
        versionCode = 9972
        versionName = "9.9.72"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            abiFilters += listOf("armeabi-v7a")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            buildConfigField("boolean", "LOG_DEBUG", "true")
            isDebuggable = true
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            signingConfig = null // 置空
        }
    }
    productFlavors {
        flavorDimensions.add("versionCode")
        create("RKPlayer") {
            signingConfig = signingConfigs.getByName("rkRelease")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    packaging.resources {
        excludes += listOf(
            "META-INF/DEPENDENCIES",
            "META-INF/LICENSE",
            "META-INF/LICENSE.txt",
            "META-INF/LICENSE.md",
            "META-INF/license.txt",
            "META-INF/NOTICE",
            "META-INF/NOTICE.txt",
            "META-INF/NOTICE.md",
            "META-INF/notice.txt",
            "META-INF/ASL2.0",
            "META-INF/*.kotlin_module",
            "META-INF/io.netty.versions.properties",
            "META-INF/INDEX.LIST"
        )
    }
}

dependencies {
    implementation (files("libs/opc-ua-stack-1.4.1.1-SNAPSHOT.jar"))
    implementation (files("libs/opc-ua-stack-1.4.1.1-SNAPSHOT-sources.jar"))
    implementation("org.slf4j:slf4j-android:1.7.30")

    implementation(group = "com.madgag.spongycastle", name = "prov", version = "1.58.0.0")
    implementation(group = "com.madgag.spongycastle", name = "core", version = "1.58.0.0")
    implementation(group = "com.madgag.spongycastle", name = "bcpkix-jdk15on", version = "1.58.0.0")

//    implementation(group = "org.bouncycastle", name = "bcprov-jdk15on", version = "1.64") {

    implementation("org.bouncycastle:bcprov-jdk18on:1.77")
    implementation("org.bouncycastle:bcpkix-jdk18on:1.77")
//    implementation(group = "org.bouncycastle", name = "bcpkix-jdk15on", version = "1.64")

    implementation(group = "org.apache.httpcomponents", name = "httpcore-nio", version = "4.4.13")
    implementation(group = "org.apache.httpcomponents", name = "httpcore", version = "4.4.13")

    implementation("commons-codec:commons-codec:1.16.1")

//    implementation("org.eclipse.milo:sdk-client:0.6.12")
//    implementation("org.eclipse.milo:sdk-server:0.6.12")
    implementation(libs.glide)
    implementation(libs.androidx.lifecycle.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
}