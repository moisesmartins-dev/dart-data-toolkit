plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    id("org.jetbrains.intellij.platform") version "2.2.0"
}

group = "com.kxaca"
version = "1.2.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2023.3.1")
        bundledPlugin("com.intellij.java")
    }
    testImplementation("junit:junit:4.13.2")
}

intellijPlatform {
    pluginConfiguration {
        name = "Dart Data Toolkit"
        version = "1.2.0"
        ideaVersion {
            sinceBuild = "233"
            untilBuild = "261.*"
        }
    }
}

tasks {
    buildPlugin {
        archiveBaseName.set("dart-data-toolkit")
    }

    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

}
