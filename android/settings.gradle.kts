// Configuração do projeto Android do Ludius.
// Lista os módulos e de onde o Gradle baixa plugins e bibliotecas.
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // Bibliotecas só podem vir destes repositórios (segurança da cadeia de dependências).
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Ludius"

include(":app")                 // o aplicativo: telas e navegação
include(":core:designsystem")   // cores, fontes e componentes visuais reutilizáveis
include(":core:domain")         // regras de negócio em Kotlin puro (sem Android)
