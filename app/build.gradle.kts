plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.xingce.fupan"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.xingce.fupan"
        minSdk = 26
        targetSdk = 35
        versionCode = 5
        versionName = "1.2.0"
        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        release {
            // 用 debug 签名，保证能直接覆盖安装手里的调试包，不丢数据
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
        compose = true
    }

    packaging {
        resources.excludes += setOf("/META-INF/{AL2.0,LGPL2.1}")
    }

    lint {
        // AGP 8.7 内置 lint 的 Kotlin 分析器不识别 Kotlin 2.2 元数据，release 构建直接跳过
        abortOnError = false
        checkReleaseBuilds = false
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.10.01"))
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    // MIUI / HyperOS 设计组件库
    implementation("top.yukonga.miuix.kmp:miuix-android:0.7.2")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
