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
            email = "support@ijson.com"
        }

        description = """
            IJSON - IntelliJ IDEA JSON 操作插件
            
            功能特性:
            1. 校验 JSON - 验证选中文本或文件是否为合法 JSON
            2. 美化 JSON - 格式化 JSON，并以可交互树形结构展示，支持节点收缩/展开、增加/删除节点
            3. 新建 JSON - 任意生成 JSON 字符串，或根据输入内容智能生成 JSON
        """.trimIndent()

        changeNotes = """
            <h3>1.0.0</h3>
            <ul>
                <li>支持 JSON 校验</li>
                <li>支持 JSON 美化与交互式树编辑（收缩/展开、增删节点）</li>
                <li>支持新建与智能生成 JSON</li>
            </ul>
        """.trimIndent()
    }

    signing {
        // Optional: configure for publishing
    }

    publishing {
        // Optional
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
