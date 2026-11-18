plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
}

kotlin {
    androidLibrary {
        namespace = "io.github.zyrouge.symphony.core"
        compileSdk = libs.versions.compile.sdk.get().toInt()
        minSdk = libs.versions.min.sdk.get().toInt()
    }

    jvm()

    jvmToolchain(17)

    sourceSets {
        val jvmSharedMain by creating {
            dependsOn(commonMain.get())
        }
        androidMain.get().dependsOn(jvmSharedMain)
        jvmMain.get().dependsOn(jvmSharedMain)
    }
}
