plugins {
    kotlin("jvm") version "2.1.0"
    id("org.jetbrains.compose") version "1.7.3"
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("org.xerial:sqlite-jdbc:3.41.2.2")
    implementation("org.jetbrains.jewel:jewel-int-ui-standalone-242:0.27.0")
    implementation("org.jetbrains.jewel:jewel-int-ui-decorated-window-242:0.27.0")
    implementation("dev.chrisbanes.haze:haze:0.7.3")
}

compose.desktop {
    application {
        mainClass = "com.fileflow.MainKt"
        nativeDistributions {
            targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg, org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi, org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb)
            packageName = "FileFlow"
            packageVersion = "1.0.0"
            macOS { iconFile.set(project.file("src/main/resources/app-icon.png")) }
            windows { iconFile.set(project.file("src/main/resources/app-icon.png")) }
            linux { iconFile.set(project.file("src/main/resources/app-icon.png")) }
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
