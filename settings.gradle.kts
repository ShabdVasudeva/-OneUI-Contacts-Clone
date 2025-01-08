pluginManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        maven { url = uri("https://jitpack.io") }
        maven {
            url = uri("https://maven.pkg.github.com/tribalfs/sesl-androidx")
            credentials {
                username = "ShabdVasudeva"
                password = "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"
            }
        }
        maven {
            url = uri("https://maven.pkg.github.com/tribalfs/sesl-material-components-android")
            credentials {
                username = "ShabdVasudeva"
                password = "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"
            }
        }
        maven {
            url = uri("https://maven.pkg.github.com/tribalfs/oneui-design")
            credentials {
                username = "ShabdVasudeva"
                password = "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"
            }
        }
    }
}

rootProject.name = "Contacts"

include(":app")