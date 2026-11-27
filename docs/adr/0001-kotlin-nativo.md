# 0001. App Android nativo em Kotlin + Compose

Data: 24/11/2026 · Status: aceita

**Contexto.** O MVP é só Android; iOS vem depois. Muitos usuários têm celulares simples.

**Decisão.** Kotlin com Jetpack Compose, arquitetura MVVM com camadas (UI, ViewModel,
domínio, dados). Regras de negócio no módulo `core:domain`, em Kotlin puro.

**Consequências.** Melhor desempenho e padrão oficial do Google. Para o iOS, o módulo de
domínio pode ser reaproveitado com Kotlin Multiplatform; as telas terão de ser refeitas.
