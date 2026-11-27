// Módulo de domínio: regras de negócio em Kotlin puro, sem nada de Android.
// Por não depender do Android, roda testes rápido e poderá ser reaproveitado
// no iOS com Kotlin Multiplatform.
plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit)
}
