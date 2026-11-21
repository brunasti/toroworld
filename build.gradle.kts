plugins {
    application
}

group = "it.brunasti.simulations"
version = "0.1.0-SNAPSHOT"

// Java release to compile for. Defaults to 25 (the project JDK).
// Override with -PjavaRelease=21 when building on an older JDK.
val javaRelease = providers.gradleProperty("javaRelease").getOrElse("25").toInt()

repositories {
    mavenCentral()
}

dependencies {
    // Markdown to HTML, for the manual window
    implementation("org.commonmark:commonmark:0.32.0")
    implementation("org.commonmark:commonmark-ext-gfm-tables:0.32.0")

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
    mainClass = "it.brunasti.simulations.toroworld.Toroworld"
}

// Bundle the documentation, so the viewer's Manual button shows the same text as docs/.
tasks.processResources {
    from("docs") {
        include("*.md", "images/**")
        into("manual")
    }
}

tasks.test {
    useJUnitPlatform()
}
