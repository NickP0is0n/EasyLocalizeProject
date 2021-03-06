import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.21"
    id("org.jetbrains.compose") version "1.0.0-alpha3"
}

group = "me.nickp0is0n"
version = "0.2.2"

repositories {
    jcenter()
    mavenCentral()
    google()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.junit.jupiter:junit-jupiter:5.7.0")
    //implementation("androidx.annotation:annotation:1.2.0")
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "EasyLocalize"
            packageVersion = "1.2.2"
            copyright = "Copyright (c) 2021, Mykola Chaikovskyi"
            macOS {
                bundleID = "me.nickp0is0n.easylocalize"
                dockName = "EasyLocalize"
                iconFile.set(project.file("easylocalize_logo.icns"))
                signing {
                    sign.set(true)
                    identity.set("nickchaywork@gmail.com")
                    keychain.set("/Users/mykolachaikovskyi/Library/Keychains/login.keychain-db")
                }
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}