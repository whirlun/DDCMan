import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.panteleyev.jpackage.ImageType

plugins {
    kotlin("jvm") version "1.9.22"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.graalvm.buildtools.native") version "0.10.1"
    id("org.panteleyev.jpackageplugin") version "1.6.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api("org.ktorm:ktorm-core:3.6.0")
    implementation("com.formdev:flatlaf:3.4")
    implementation("com.formdev:flatlaf-intellij-themes:3.4")
    implementation("com.fifesoft:rsyntaxtextarea:3.4.0")
    implementation("org.apache.xmlgraphics:batik-transcoder:1.17")
    implementation("com.h2database:h2:2.2.220")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.graalvm.polyglot:polyglot:23.1.3")
    implementation("org.graalvm.polyglot:js-community:23.1.3")
    implementation("com.auth0:java-jwt:4.4.0")
    implementation("com.github.scribejava:scribejava:8.3.3")
    implementation("io.reactivex.rxjava3:rxkotlin:3.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test")

}

tasks.test {
    useJUnitPlatform()
}

tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("shadow")
    mergeServiceFiles()
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes(
            "Main-Class" to "ddcMan.MainKt",
            "Implementation-Title" to "Gradle",
            "Implementation-Version" to archiveVersion
        )
    }


    from(sourceSets.main.get().output)
}

graalvmNative {
    binaries {
        named("main") {
            imageName.set("DDCMan")
            mainClass.set("ddcMan.MainKt")
        }
    }
    binaries.all {
        buildArgs.add("--verbose")
        buildArgs.add("-H:ConfigurationFileDirectories=/Users/bbrabbit/IdeaProjects/DDCMan/config")
        buildArgs.add("-Djava.awt.headless=false")
    }
}

task("copyDependencies", Copy::class) {
    from(configurations.runtimeClasspath).into("$buildDir/jmods")
}

task("copyJar", Copy::class) {
    from(tasks.jar).into("$buildDir/jmods")
}

tasks.jpackage {
    dependsOn("shadowJar", "copyDependencies", "copyJar")

    input  = "$buildDir/libs"
    destination = "$buildDir/dist"

    appName = "DDCMan"
    appVersion = "1.0"
    mainJar = "DDCMan-1.0-SNAPSHOT-shadow.jar"
    mainClass = "ddcMan.MainKt"
    addModules = listOf(
        "java.base",
        "java.desktop",
        "java.naming",
        "java.prefs",
        "java.sql",
        "java.sql.rowset",
        "jdk.unsupported",
        "java.management",
        "jdk.management",
        "java.logging",
        "jdk.internal.vm.ci",
        "jdk.internal.vm.compiler",
        "java.scripting",
        "java.net.http",
        "jdk.jfr",
    )
    type = ImageType.DMG
    modulePaths = listOf("$buildDir/jmods")
    javaOptions = listOf("-Dfile.encoding=UTF-8")


    windows {
        icon = "icons/icons.ico"
        winMenu = true
        winDirChooser = true
    }
}

kotlin {
    jvmToolchain(21)
}
