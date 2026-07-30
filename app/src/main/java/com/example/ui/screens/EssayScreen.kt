package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IndigoPrimary
import com.example.ui.viewmodel.HomeworkViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EssayScreen(
    viewModel: HomeworkViewModel,
    modifier: Modifier = Modifier
) {
    val essayState by viewModel.essayState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    val modes = listOf("Draft", "Proofread", "Outline", "Tone Polish")
    val languages = listOf("English", "Spanish", "French", "German", "Chinese", "Japanese", "Hindi")

    var showLanguageMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.EditNote, contentDescription = null, tint = IndigoPrimary, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Essay & Writing Assistant",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    text = "Write essays, proofread grammar, generate outlines & polish academic tone.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Writing Mode Selector Chips
        Text(text = "Writing Tool Mode", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(modes) { mode ->
                val isSelected = essayState.mode == mode
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.updateEssayMode(mode) },
                    label = { Text(mode, fontSize = 13.sp) },
                    leadingIcon = {
                        val icon = when (mode) {
                            "Proofread" -> Icons.Default.Spellcheck
                            "Outline" -> Icons.Default.FormatListNumbered
                            "Tone Polish" -> Icons.Default.AutoAwesome
                            else -> Icons.Default.Create
                        }
                        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IndigoPrimary,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Topic or Text Input Field
        OutlinedTextField(
            value = essayState.prompt,
            onValueChange = { viewModel.updateEssayPrompt(it) },
            label = {
                Text(
                    when (essayState.mode) {
                        "Proofread" -> "Paste your essay text to proofread..."
                        "Outline" -> "Enter your essay title or topic..."
                        "Tone Polish" -> "Paste paragraph to refine academic tone..."
                        else -> "Enter essay topic or prompt..."
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 140.dp),
            shape = RoundedCornerShape(16.dp),
            trailingIcon = {
                Box {
                    IconButton(onClick = { showLanguageMenu = true }) {
                        Icon(Icons.Default.Language, contentDescription = "Language", tint = IndigoPrimary)
                    }
                    DropdownMenu(
                        expanded = showLanguageMenu,
                        onDismissRequest = { showLanguageMenu = false }
                    ) {
                        languages.forEach { lang ->
                            DropdownMenuItem(
                                text = { Text(lang) },
                                onClick = {
                                    viewModel.updateEssayLanguage(lang)
                                    showLanguageMenu = false
                                }
                            )
                        }
                    }
                }
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Language: ${essayState.language}",
                fontSize = 11.sp,
                color = IndigoPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${essayState.prompt.length} characters",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Button
        Button(
            onClick = { viewModel.generateEssay() },
            enabled = !essayState.isLoading,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (essayState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("AI is writing...", fontWeight = FontWeight.Bold)
            } else {
                Icon(Icons.Default.AutoFixHigh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Writing Assistance", fontWeight = FontWeight.Bold)
            }
        }

        essayState.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = error, color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 13.sp, modifier = Modifier.padding(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Essay Result Display
        essayState.result?.let { resultText ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Generated ${essayState.mode}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = IndigoPrimary
                        )

                        IconButton(onClick = {
                            clipboardManager.setText(AnnotatedString(resultText))
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy text", tint = IndigoPrimary)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    Text(
                        text = resultText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 22.sp,
                            fontSize = 14.sp
                        )
                    )
                }
            }
        }
    }
}
