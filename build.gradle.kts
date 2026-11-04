plugins {
    id("java-library")
    id("maven-publish")
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("com.gradleup.shadow") version "9.6.1"
}

group = "dev.laststudio.lib"
version = "0.2"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.by1337.space/repository/maven-releases/")
    maven("https://jitpack.io")
    maven("https://repo.rosewooddev.io/repository/public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
    compileOnly("com.google.code.gson:gson:2.10.1")
    compileOnly("dev.by1337.yaml:byaml-bukkit:1.6")
    compileOnly("dev.by1337.cmd:BCmd:1.4")
    compileOnly("dev.by1337.plc:BPlaceholder:1.3")
    compileOnly("dev.by1337.particle:render-bukkit:1.8")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") { exclude(group = "org.bukkit", module = "bukkit") }
    compileOnly("org.black_ixx:playerpoints:3.3.4")
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

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name.lowercase()
            version = project.version.toString()

            artifact(tasks.named("shadowJar"))
        }
    }
    repositories {
        maven {
            url = uri("https://repo.laststudio.space/releases")
            credentials {
                username = project.findProperty("repoUser") as String?
                password = project.findProperty("repoPass") as String?
            }
        }
    }
}