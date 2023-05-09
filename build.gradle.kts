plugins {
    val kotlinVersion = "1.7.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    //id("me.him188.maven-central-publish") version "1.0.0-dev-3"
    id("maven-publish")
    id("net.mamoe.mirai-console") version "2.14.0"
}

group = "io.github.absdf15.qbot.core"
version = "0.1.1"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies{
    // 缓存类
    implementation("com.google.guava:guava:31.1-android")
}


publishing {
    repositories {
        mavenLocal()
    }
    publications {
        create<MavenPublication>("myLibrary") {
            artifact(file("build/mirai/QBotCore-0.1.0.mirai2.jar"))
        }
    }
}
