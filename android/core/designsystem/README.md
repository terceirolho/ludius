# core:designsystem

Identidade visual do Ludius em Jetpack Compose. Os valores vêm do protótipo aprovado
(versão 5, 27/11/2026).

| Pasta / arquivo | O que tem |
|---|---|
| `theme/Color.kt` | Paleta clara e escura (verde-limão, fundo branco-gelo, texto quase preto) |
| `theme/Type.kt` | Fontes: Outfit nos títulos, Inter nos textos |
| `theme/Shape.kt` | Cantos arredondados (12, 16 e 22 dp) |
| `theme/Theme.kt` | `LudiusTheme { }`: envolve todas as telas |
| `components/` | Botão, campo de texto, selos de status e selo "Já contratei" |
| `res/font/` | Arquivos das fontes (licença OFL em `licencas-fontes/`) |

Regras:
- Botão verde sempre com texto preto (branco sobre esse verde fica difícil de ler).
- O verde aparece só em botões, selos e itens ativos.
- Nunca escreva uma cor direto numa tela: use `LudiusTheme.colors`.

Depende de: Compose Material 3.
