plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.dynamic.feature) apply false
    alias(libs.plugins.ksp) apply false
}

/**
 * Code-style gate. ktlint runs from its own classpath instead of a plugin, which keeps it
 * independent of the Gradle/AGP version this project is pinned to, and lets CI call
 * `./gradlew ktlintCheck` as one more quality step next to `lint`, `test`, and coverage.
 */
val ktlint: Configuration by configurations.creating

dependencies {
    ktlint(libs.ktlint.cli) {
        attributes {
            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        }
    }
}

private val ktlintSources = listOf(
    "app/src/**/*.kt",
    "core/src/**/*.kt",
    "favorite/src/**/*.kt",
    "!**/build/**",
)

tasks.register<JavaExec>("ktlintCheck") {
    group = "verification"
    description = "Checks the Kotlin sources against the official Kotlin code style."
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")
    args = ktlintSources + listOf(
        "--reporter=plain",
        "--reporter=checkstyle,output=${layout.buildDirectory.get()}/reports/ktlint/ktlint.xml",
    )
}

tasks.register<JavaExec>("ktlintFormat") {
    group = "formatting"
    description = "Rewrites the Kotlin sources into the official Kotlin code style."
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")
    jvmArgs("--add-opens=java.base/java.lang=ALL-UNNAMED")
    args = listOf("-F") + ktlintSources
}
