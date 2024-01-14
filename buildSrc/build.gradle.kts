import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    `kotlin-dsl`
}

tasks.named<KotlinCompilationTask<KotlinJvmCompilerOptions>>("compileKotlin").configure {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
}

java {
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

gradlePlugin {
    plugins {
        register("check-lib-versions") {
            id = "check-lib-versions"
            implementationClass = "CheckLibVersionsPlugin"
        }
    }
}
