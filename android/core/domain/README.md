# core:domain

Regras de negócio do Ludius em Kotlin puro (não depende do Android).

| Arquivo | O que faz |
|---|---|
| `TaxIdValidator.kt` | Valida CPF e CNPJ pelo dígito verificador |
| `FeeCalculator.kt` | Calcula a comissão: 10% do artista + 5% do contratante |
| `CancellationPolicy.kt` | Diz quanto o contratante recebe de volta ao cancelar |
| `OrderStatus.kt` | Status do pedido e quais mudanças são permitidas |

**Importante:** o banco (pasta `supabase/`) tem as mesmas regras e é ele que vale.
Aqui elas servem para o app mostrar valores e avisos antes de mandar algo ao servidor.
Se mudar uma regra, mude nos dois lugares e nos testes dos dois.

Depende de: nada.
