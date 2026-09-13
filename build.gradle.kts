plugins {
    id("java-library")
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("com.gradleup.shadow") version "9.6.1"
}

group = "dev.laststudio.lib"
version = "0.1"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.by1337.space/repository/maven-releases/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
    compileOnly("com.google.code.gson:gson:2.10.1")
    compileOnly("dev.by1337.yaml:byaml-bukkit:1.6")
    compileOnly("dev.by1337.cmd:BCmd:1.4")
    compileOnly("dev.by1337.plc:BPlaceholder:1.3")
    compileOnly("dev.by1337.particle:render-bukkit:1.8")
    implementation("org.bstats:bstats-bukkit:3.2.1")

}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks {
    processResources {
        val props = mapOf("version" to version, "description" to project.description)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
    shadowJar {
        archiveClassifier.set("")
        relocate("org.bstats", project.group.toString())
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}