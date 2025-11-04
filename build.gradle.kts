@file:OptIn(ExperimentalWasmDsl::class)

import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    `maven-publish`
    application
    id("com.gradleup.shadow")
}

// FIXME adjust as needed
val ghUser = "jillesvangurp"
val ghProjectName = "kotlin-jvm-template"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io") {
        content {
            includeGroup("com.github.jillesvangurp")
        }
    }
    // FIXME adjust as needed
    // remove this if you don't use any of our multiplatform libraries
    maven("https://maven.tryformation.com/releases")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-common"))
    testImplementation(kotlin("test-annotations-common"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:_")
    testImplementation("io.kotest:kotest-assertions-core:_")
    testImplementation("ch.qos.logback:logback-classic:_")
    testImplementation("com.github.jillesvangurp:kotlin4example:_")
    testImplementation("org.junit.jupiter:junit-jupiter:_")
}

publishing {
    publications {
        withType<MavenPublication> {
            pom {
                url.set("https://github.com/$ghUser/$ghProjectName")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://github.com/$ghUser/$ghProjectName/blob/master/LICENSE")
                    }
                }

                developers {
                    developer {
                        id.set("jillesvangurp")
                        name.set("Jilles van Gurp")
                        email.set("jilles@no-reply.github.com")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/$ghUser/$ghProjectName.git")
                    developerConnection.set("scm:git:ssh://github.com:$ghUser/$ghProjectName.git")
                    url.set("https://github.com/$ghUser/$ghProjectName")
                }
            }
        }
    }
    repositories {
        // setup publishing repo for https://maven.tryformation.com/releases
        maven {
            // GOOGLE_APPLICATION_CREDENTIALS env var must be set for this to work
            // public repository is at https://maven.tryformation.com/releases
            url = uri("gcs://mvn-public-tryformation/releases")
            name = "FormationPublic"
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    // run tests in parallel
    systemProperties["junit.jupiter.execution.parallel.enabled"] = "true"
    // executes test classes concurrently
    systemProperties["junit.jupiter.execution.parallel.mode.default"] = "concurrent"
    // executes tests inside a class concurrently
    systemProperties["junit.jupiter.execution.parallel.mode.classes.default"] = "concurrent"
    systemProperties["junit.jupiter.execution.parallel.config.strategy"] = "dynamic"
    // random order of test class execution
    systemProperties["junit.jupiter.testclass.order.default"] = "org.junit.jupiter.api.ClassOrderer\$Random"

    testLogging.exceptionFormat = TestExceptionFormat.FULL
    testLogging.events = setOf(
        TestLogEvent.FAILED,
        TestLogEvent.PASSED,
        TestLogEvent.SKIPPED,
        TestLogEvent.STANDARD_ERROR,
        TestLogEvent.STANDARD_OUT
    )
    addTestListener(object : TestListener {
        val failures = mutableListOf<String>()
        override fun beforeSuite(desc: TestDescriptor) {
        }

        override fun afterSuite(desc: TestDescriptor, result: TestResult) {
        }

        override fun beforeTest(desc: TestDescriptor) {
        }

        override fun afterTest(desc: TestDescriptor, result: TestResult) {
            if (result.resultType == TestResult.ResultType.FAILURE) {
                val report =
                    """
                    TESTFAILURE ${desc.className} - ${desc.name}
                    ${
                        result.exception?.let { e ->
                            """
                            ${e::class.simpleName} ${e.message}
                        """.trimIndent()
                        }
                    }
                    -----------------
                    """.trimIndent()
                failures.add(report)
            }
        }
    })
}

application {
    mainClass.set("com.jillesvangurp.mainKt")
}




