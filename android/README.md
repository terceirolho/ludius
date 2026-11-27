# android/

App Android do Ludius em Kotlin com Jetpack Compose.

| Módulo | O que faz | Depende de |
|---|---|---|
| `app` | Telas, navegação, configuração do APK | designsystem, domain |
| `core:designsystem` | Tema e componentes visuais únicos | Compose |
| `core:domain` | Regras de negócio em Kotlin puro | nada |

Na fase 1 entram módulos por funcionalidade (`feature:cadastro`, `feature:busca`, ...)
e um `core:data` para falar com o Supabase.

## Abrir e rodar

1. No Android Studio: **File > Open** e escolha esta pasta `android/`.
2. Espere o Gradle sincronizar (barra de progresso embaixo).
3. Escolha um emulador e clique em **Run ▶**.

Comandos úteis (no terminal, dentro de `android/`):

```bash
./gradlew testDebugUnitTest :core:domain:test   # roda os testes
./gradlew assembleDebug                         # gera o APK de teste
```

## Chaves do Supabase

Quando o app começar a falar com o banco (fase 1), crie o arquivo `android/local.properties`
(ele não vai para o Git) com as chaves **públicas** do projeto:

```properties
supabase.url=https://SEU-PROJETO.supabase.co
supabase.anonKey=COLE_A_CHAVE_ANON_AQUI
```

Nunca use a chave `service_role` no app.
