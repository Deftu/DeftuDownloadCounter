import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import groovy.lang.MissingPropertyException

plugins {
    id("com.github.johnrengelman.shadow") version("7.1.2")
    kotlin("jvm") version("1.7.10")
    id("net.kyori.blossom") version("1.3.1")
    application
}

group = extra["project.group"] ?: throw MissingPropertyException("The project group was not configured!")
version = extra["project.version"] ?: throw MissingPropertyException("The project version was not configured!")

val shade by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

blossom {
    replaceToken("@NAME@", name)
    replaceToken("@VERSION@", version)
}

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://jitpack.io/")
    maven("https://maven.unifycraft.xyz/releases/")
}

dependencies {
    // Language
    shade(kotlin("stdlib-jdk8"))

    // Discord
    shade("dev.kord:kord-core:${libs.versions.kord.get()}")

    // Platform
    shade("com.github.TheRandomLabs:CurseAPI:master-SNAPSHOT")

    // Utility
    shade(api("xyz.deftu.deftils:Deftils:${libs.versions.deftils.get()}")!!)
    shade(api("com.squareup.okhttp3:okhttp:${libs.versions.okhttp.get()}")!!)
    shade("com.google.code.gson:gson:${libs.versions.gson.get()}")
    shade("com.google.guava:guava:${libs.versions.guava.get()}")

    // Logging
    shade("org.apache.logging.log4j:log4j-api:${libs.versions.log4j.get()}")
    shade("org.apache.logging.log4j:log4j-core:${libs.versions.log4j.get()}")
    shade("org.apache.logging.log4j:log4j-slf4j2-impl:${libs.versions.log4j.get()}")
}

application {
    mainClass.set(extra["project.mainClass"]?.toString() ?: throw MissingPropertyException("No main class specified"))
}

tasks {
    named<Jar>("shadowJar") {
        archiveClassifier.set("")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from("LICENSE")
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}
