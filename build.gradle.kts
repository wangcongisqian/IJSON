plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.2.1"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    // Jackson for JSON processing
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    implementation("com.fasterxml.jackson.core:jackson-core:2.17.2")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.17.2")

    intellijPlatform {
        intellijIdeaCommunity(providers.gradleProperty("platformVersion"))
        // No extra bundled plugins needed for basic JSON ops
    }
}

intellijPlatform {
    pluginConfiguration {
        id = "com.ijson.IJSON"
        name = providers.gradleProperty("pluginName")
        version = providers.gradleProperty("pluginVersion")

        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
            untilBuild = providers.gradleProperty("pluginUntilBuild")
        }

        vendor {
            name = "IJSON Team"
            email = "alen.mark.wang@gmail.com"
            url = "https://github.com/wangcongisqian/IJSON"
        }

        description = """
            <h2>IntelliJ IDEA JSON</h2>
            <p>A powerful JSON processing plugin with support for:</p>
            <ul>
                <li><b>JSON validation</b>: Validate selected text or the current file</li>
                <li><b>JSON formatting</b>: Format JSON and explore it in an interactive tree with collapsible nodes and support for adding, deleting, and editing values</li>
                <li><b>JSON creation</b>: Generate JSON from templates or input such as key-value pairs and descriptive text</li>
            </ul>
            <p>Use the editor context menu or the Tools → IJSON menu to access these features.</p>
        """.trimIndent()

        changeNotes = """
            <h3>1.0.0</h3>
            <ul>
                <li>Validate selected text or the current file</li>
                <li>Format JSON and explore it in an interactive tree with collapsible nodes and support for adding, deleting, and editing values</li>
                <li>Generate JSON from templates or input such as key-value pairs and descriptive text</li>
            </ul>
        """.trimIndent()
    }

    signing {
        // Optional: configure for publishing
    }

    publishing {
        // Optional
    }

    pluginVerification {
        ides {
            ide("IC", providers.gradleProperty("platformVersion").get())
        }
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
        options.encoding = "UTF-8"
    }

    // Run IDE with plugin
    runIde {
        // autoReloadPlugins.set(true)
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
