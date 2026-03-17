plugins {
    id("java")
    id("kotlin")
    id("org.jetbrains.intellij") version "1.16.1"
}

group = "com.foutlook"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    // JSON 解析
    implementation("com.google.code.gson:gson:2.10.1")
    // 通过流处理 CLI 输出
    implementation("commons-io:commons-io:2.13.0")
}

// IDE 版本配置
intellij {
    version.set("2023.2")
    type.set("IU") // IntelliJ Ultimate
    plugins.set(listOf("com.intellij.java"))
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        changeNotes.set("""
            Initial release with support for OpenCode CLI, Codex CLI, and Claude Code CLI.
        """.trimIndent())
    }
}
