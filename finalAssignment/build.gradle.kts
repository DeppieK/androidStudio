buildscript {

    dependencies {
        classpath("com.google.gms:google-services:4.4.0")
        repositories {
            mavenCentral()
            google()
            maven { url = uri("https://jitpack.io") }

        }

    }
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.0" apply false
}