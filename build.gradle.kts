plugins {
    `java-library`
    `maven-publish`
}

val vanillaVersion = providers.gradleProperty("vanillaVersion").get()
val forkRevision = providers.gradleProperty("forkRevision").get()
val isSnapshot = providers.gradleProperty("snapshot").map(String::toBoolean).getOrElse(false)
version = "$vanillaVersion+papermc.$forkRevision" + if (isSnapshot) "-SNAPSHOT" else ""
description = "PaperMC's fork of Mojang's brigadier command parser & dispatcher, shared by Paper and Velocity."

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.mockito:mockito-core:5.22.0")
    testImplementation("com.google.guava:guava:33.6.0-jre")
    testImplementation("com.google.guava:guava-testlib:33.6.0-jre")
    testImplementation("org.openjdk.jmh:jmh-core:1.37")
    testAnnotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.37")
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 21
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc>().configureEach {
    options.encoding = "UTF-8"
    (options as StandardJavadocDocletOptions).apply {
        addStringOption("Xdoclint:none", "-quiet")
    }
}

tasks.jar {
    manifest {
        attributes(
            "Automatic-Module-Name" to "com.mojang.brigadier",
            "Implementation-Title" to "brigadier",
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "PaperMC",
        )
    }
}

tasks.test {
    testLogging {
        events("failed", "skipped")
        showStandardStreams = true
        showExceptions = true
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name = "brigadier"
                description = project.description
                url = "https://github.com/PaperMC/brigadier"
                licenses {
                    license {
                        name = "MIT"
                        url = "https://github.com/PaperMC/brigadier/blob/main/LICENSE"
                    }
                }
                scm {
                    url = "https://github.com/PaperMC/brigadier"
                    connection = "scm:git:https://github.com/PaperMC/brigadier.git"
                    developerConnection = "scm:git:ssh://git@github.com/PaperMC/brigadier.git"
                }
            }
        }
    }
    repositories {
        maven {
            name = "papermc"
            val base = "https://repo.papermc.io/repository/maven"
            url = uri(if (isSnapshot) "$base-snapshots/" else "$base-releases/")
            credentials(PasswordCredentials::class)
        }
    }
}
