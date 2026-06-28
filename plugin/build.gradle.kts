plugins {
    java
    id("org.jetbrains.intellij.platform") version "2.11.0"
    kotlin("jvm")
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
        snapshots()
    }
}

dependencies {
    intellijPlatform {
        create("IU", "2026.1")
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
            sinceBuild = "261"
        }
    }

    pluginVerification {
        ides {
            create("IU", "262.6653.22")
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