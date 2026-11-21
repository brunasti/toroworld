plugins {
    application
}

group = "io.github.brunasti"
version = "0.1.0-SNAPSHOT"

// Java release to compile for. Defaults to 25 (the project JDK).
// Override with -PjavaRelease=21 when building on an older JDK.
val javaRelease = providers.gradleProperty("javaRelease").getOrElse("25").toInt()

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile>().configureEach {
    options.release = javaRelease
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-Xlint:all", "-Werror"))
}

application {
    mainClass = "io.github.brunasti.toroworld.Toroworld"
}

tasks.test {
    useJUnitPlatform()
}
