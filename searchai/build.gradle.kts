plugins {
    java
}

allprojects {
    group = "com.sleekydz86"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(27)
            vendor = JvmVendorSpec.AZUL
        }
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}
