plugins {
    `java-library`
    `maven-publish`
    id("fabric-loom") version "1.0-SNAPSHOT"
    id("com.modrinth.minotaur") version "2.+"
}

version = "1.0.0"
group = "shateq.mods"
base.archivesName.set("letitsnow")
description = "Winter Weather Mod for Fabric"

dependencies {
    minecraft("com.mojang:minecraft:${project.extra["mc"]}")
    mappings("net.fabricmc:yarn:${project.extra["yarn"]}:v2")
    //mappings(loom.officialMojangMappings())

    modImplementation("net.fabricmc:fabric-loader:${project.extra["loader"]}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.extra["fapi"]}")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

loom {
    mixin.defaultRefmapName.set("modid.refmap.json")
}

tasks {
    jar {
        from("LICENSE") {
            rename { "${it}_$archiveBaseName" }
        }
    }
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }
    // fabric.mod.json
    processResources {
        filteringCharset = Charsets.UTF_8.name()
        filesMatching("fabric.mod.json") {
            expand(
                "version" to project.version,
                "description" to project.description
            )
        }
    }
}

modrinth {
    //Remember to have the MODRINTH_TOKEN environment variable set or else this will fail, or set it to whatever you want - just make sure it stays private!
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set("my-project")

    versionName.set("${project.version} for ${project.extra["mc"]}")
    versionNumber.set(version.toString())
    versionType.set("beta")

    uploadFile.set(tasks["remapJar"])
    gameVersions.addAll("1.19", "1.19.1", "1.19.2")
    dependencies {
        // scope.type: can be `required`, `optional`, `incompatible`, or `embedded`
        required.project("fabric-api")
    }
}
