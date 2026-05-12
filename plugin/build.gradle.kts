plugins {
    java
    id("org.jetbrains.intellij.platform") version "2.11.0"
    kotlin("jvm")
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        create("IC", "2025.1")
        bundledPlugin("com.intellij.java")
    }
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.apache.commons:commons-text:1.10.0")
    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
}

intellijPlatform {
    projectName = rootProject.name

    pluginConfiguration {
        version = rootProject.version.toString()
        ideaVersion {
            sinceBuild = "251"
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.prepareSandbox {
    from(project(":agent").tasks.jar) {
        into(rootProject.name + "/lib")
    }
}