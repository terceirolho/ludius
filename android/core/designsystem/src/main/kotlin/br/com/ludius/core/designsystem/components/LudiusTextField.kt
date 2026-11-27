package br.com.ludius.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import br.com.ludius.core.designsystem.theme.LudiusTheme

/**
 * Campo de formulário do Ludius: rótulo em cima, caixa branca arredondada,
 * dica ou erro embaixo e, se tiver [maxLength], contador "12/240".
 */
@Composable
fun LudiusTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    hint: String? = null,
    error: String? = null,
    maxLength: Int? = null,
    singleLine: Boolean = true,
    isPassword: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    val c = LudiusTheme.colors
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = c.ink)

        OutlinedTextField(
            value = value,
            onValueChange = { novo ->
                // Corta no limite em vez de deixar digitar além (ex.: bio com 240).
                onValueChange(if (maxLength != null) novo.take(maxLength) else novo)
            },
            placeholder = { Text(placeholder, color = c.inkSecondary) },
            singleLine = singleLine,
            minLines = if (singleLine) 1 else 3,
            isError = error != null,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = keyboardOptions,
            shape = MaterialTheme.shapes.medium,
            textStyle = MaterialTheme.typography.bodyLarge,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = c.surface,
                unfocusedContainerColor = c.surface,
                focusedBorderColor = c.ink,
                unfocusedBorderColor = c.line,
                errorBorderColor = c.danger,
                cursorColor = c.ink,
                focusedTextColor = c.ink,
                unfocusedTextColor = c.ink,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            val message = error ?: hint
            Text(
                text = message.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = if (error != null) c.danger else c.inkSecondary,
                modifier = Modifier.weight(1f),
            )
            if (maxLength != null) {
                Text("${value.length}/$maxLength", style = MaterialTheme.typography.labelSmall, color = c.inkSecondary)
            }
        }
    }
}
