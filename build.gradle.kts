/*
 * Copyright (c) 2019, Lucas <https://github.com/Lucwousin>
 *               2022, Owain van Brakel <https://github.com/Owain94>
 * All rights reserved.
 *
 * This code is licensed under GPL3, see the complete license in
 * the LICENSE file in the root directory of this source tree.
 */
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-gradle-plugin")
    kotlin("jvm") version "1.7.0"
    `maven-publish`
}

val oprsver = "2.0.0"

group = "net.unethicalite"
version = "3.0.0"

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        url = uri("https://repo.runelite.net")
        url = uri("https://raw.githubusercontent.com/open-osrs/hosting/master")
        url = uri("https://repo.openosrs.com/repository/maven")
        url = uri("https://jitpack.io")
    }
}

dependencies {
    annotationProcessor(group = "org.projectlombok", name = "lombok", version = "1.18.20")
    compileOnly(group = "org.projectlombok", name = "lombok", version = "1.18.20")

    implementation(group = "org.ow2.asm", name = "asm", version = "9.0")
    implementation(group = "org.ow2.asm", name = "asm-util", version = "9.0")
    implementation(group = "org.jetbrains", name = "annotations", version = "22.0.0")
    implementation(group = "com.google.guava", name = "guava", version = "30.1.1-jre") {
        exclude(group = "com.google.code.findbugs", module = "jsr305")
        exclude(group = "com.google.errorprone", module = "error_prone_annotations")
        exclude(group = "com.google.j2objc", module = "j2objc-annotations")
        exclude(group = "org.codehaus.mojo", module = "animal-sniffer-annotations")
    }
    implementation("net.unethicalite:deobfuscator:${oprsver}") {
        isTransitive = false
    }

    testCompileOnly(group = "net.unethicalite", name = "injection-annotations", version = "1.1")
    testImplementation(group = "junit", name = "junit", version = "4.12")
}

gradlePlugin {
    plugins {
        create("injectorPlugin") {
            id = "net.unethicalite.injector"
            implementationClass = "com.openosrs.injector.InjectPlugin"
        }
    }
}

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

publishing {
    repositories {
        mavenLocal()
        maven { url = uri("$buildDir/repo") }
        if (System.getenv("REPO_URL") != null) {
            maven {
                url = uri(System.getenv("REPO_URL"))
                credentials {
                    username = System.getenv("REPO_USERNAME")
                    password = System.getenv("REPO_PASSWORD")
                }
            }
        }
    }
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            artifact(sourcesJar.get())
        }
    }
}
