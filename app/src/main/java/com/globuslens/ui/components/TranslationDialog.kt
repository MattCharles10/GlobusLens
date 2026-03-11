package com.globuslens.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun TranslationDialog(
    originalText: String,
    translatedText: String?,
    isLoading: Boolean,
    sourceLanguage: String,
    targetLanguage: String,
    onDismiss: () -> Unit,
    onTranslate: (String, String) -> Unit
) {
    var textToTranslate by remember { mutableStateOf(originalText) }
    var selectedTargetLang by remember { mutableStateOf(targetLanguage) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Translate",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Source Text
                OutlinedTextField(
                    value = textToTranslate,
                    onValueChange = { textToTranslate = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Text to translate") },
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Translated Text
                if (isLoading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.width(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Translating...")
                    }
                } else {
                    OutlinedTextField(
                        value = translatedText ?: "",
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Translation") },
                        minLines = 3,
                        readOnly = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Translate Button
                Button(
                    onClick = { onTranslate(textToTranslate, selectedTargetLang) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = textToTranslate.isNotBlank()
                ) {
                    Text("Translate")
                }
            }
        }
    }
}