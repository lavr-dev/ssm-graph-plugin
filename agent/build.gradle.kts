plugins {
    java
}

group = "com.lavr.ssmgraphagent"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.bytebuddy:byte-buddy:1.15.10")
    implementation("net.bytebuddy:byte-buddy-agent:1.15.10")
    compileOnly("org.springframework.statemachine:spring-statemachine-core:4.0.0")
}

tasks.jar {
    manifest.attributes(
        "Premain-Class"           to "com.lavr.ssmgraphagent.SSMGraphAgent",
        "Can-Redefine-Classes"    to "true",
        "Can-Retransform-Classes" to "true"
    )
}