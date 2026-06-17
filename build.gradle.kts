plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    id("org.jetbrains.intellij.platform") version "2.2.0"
}

group = "com.kxaca"
version = "1.0.0"

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
        version = "1.0.0"
        description = """
            Automatically generates essential methods for Dart classes directly from the editor.<br/><br/>
            <b>Features:</b>
            <ul>
                <li><b>copyWith()</b> — creates modified copies of the class</li>
                <li><b>toJson()</b> — serializes the class to a JSON map</li>
                <li><b>fromJson()</b> — deserializes a class from a JSON map</li>
                <li><b>toString()</b> — formatted multiline string representation</li>
                <li><b>Dart Class from JSON</b> — generates a complete Dart class from a pasted JSON object</li>
                <li><b>Barrel File</b> — exports selected Dart files from a folder into a single index file</li>
            </ul>
        """
        changeNotes = """
            <b>1.0.0</b>
            <ul>
                <li>Dart method generator (copyWith, toJson, fromJson, toString)</li>
                <li>Barrel file generator (index.dart with 3 styles)</li>
                <li>Interactive dialog to choose methods</li>
                <li>Configurable dialog for barrel files</li>
                <li>Keyboard shortcuts (Ctrl+Alt+D and Ctrl+Alt+B)</li>
                <li>Nullable type support</li>
                <li>Recursive directory support</li>
            </ul>
        """
        ideaVersion {
            sinceBuild = "233"
            untilBuild = "261.*"
        }
    }
}

tasks {
    buildPlugin {
        archiveBaseName.set("dart-data-plugin")
    }

    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

}
