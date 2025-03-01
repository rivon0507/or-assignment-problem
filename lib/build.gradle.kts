/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java library project to get you started.
 * For more details on building Java & JVM projects, please refer to https://docs.gradle.org/8.12/userguide/building_java_projects.html in the Gradle documentation.
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    // Apply the java-library plugin for API and implementation separation.
    `java-library`
    id("io.freefair.lombok") version "8.12.1"
    `maven-publish`
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

group = "io.github.rivon0507"

dependencies {
    // This dependency is exported to consumers, that is to say found on their compile classpath.
    api(libs.commons.math3)

    // This dependency is used internally, and not exposed to consumers on their own compile classpath.
    implementation(libs.guava)
    implementation("org.jetbrains:annotations:24.0.0")
}

testing {
    suites {
        // Configure the built-in test suite
        @Suppress("UnstableApiUsage")
        val test by getting(JvmTestSuite::class) {
            // Use JUnit Jupiter test framework
            useJUnitJupiter("5.11.1")
        }
    }
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
        targetCompatibility = JavaVersion.VERSION_21
    }
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "io.github.rivon0507"
            artifactId = "or-assignment-problem"
            from(components["java"])

            pom {
                name = "or-assignment-problem"
                description = "A Java library for solving the Assignment Problem using cost minimization or productivity maximization."
                url = "https://github.com/rivon0507/or-assignment-problem"
                inceptionYear = "2025"
                licenses {
                    license {
                        name = "The MIT License"
                        url = "https://opensource.org/licenses/MIT"
                    }
                }
                developers {
                    developer {
                        id = "rivon0507"
                        name = "Flavien TSIRIHERIVONJY"
                    }
                }
                scm {
                    connection = "scm:git:https://github.com/rivon0507/or-assignment-problem.git"
                    developerConnection = "scm:git:ssh://github.com/rivon0507/or-assignment-problem.git"
                    url = "https://github.com/rivon0507/or-assignment-problem"
                }
            }
        }
    }

    repositories {
        mavenLocal()
        maven {
            url = uri(layout.buildDirectory.dir("staging-deploy"))
        }
    }
}