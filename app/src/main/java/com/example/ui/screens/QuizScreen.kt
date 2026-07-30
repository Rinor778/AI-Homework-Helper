package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.IndigoSecondary
import com.example.ui.theme.IndigoTertiary
import com.example.ui.viewmodel.HomeworkViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    viewModel: HomeworkViewModel,
    modifier: Modifier = Modifier
) {
    val quizState by viewModel.quizState.collectAsState()
    val scrollState = rememberScrollState()

    val subjects = listOf("Math", "Science", "Biology", "History", "English", "Programming")
    val defaultTopics = listOf("Quadratic Equations", "Photosynthesis", "World War II", "Cellular Respiration", "Python Data Structures")

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
            Icon(Icons.Default.Quiz, contentDescription = null, tint = IndigoPrimary, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Quiz & Test Prep",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    text = "Interactive flashcards and practice test questions tailored to your subject.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Subject selector
        Text(text = "Select Subject", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(subjects) { subject ->
                val isSelected = quizState.subject.equals(subject, ignoreCase = true)
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.updateQuizSubject(subject) },
                    label = { Text(subject, fontSize = 13.sp) },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = IndigoPrimary, selectedLabelColor = Color.White)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Topic Input
        OutlinedTextField(
            value = quizState.topic,
            onValueChange = { viewModel.updateQuizTopic(it) },
            label = { Text("Study Topic or Exam Name") },
            placeholder = { Text("e.g. Mitochondria & ATP, Linear Algebra, Midterm Chapter 3") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Topic chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(defaultTopics) { sample ->
                SuggestionChip(
                    onClick = { viewModel.updateQuizTopic(sample) },
                    label = { Text(sample, fontSize = 12.sp) },
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Generate Quiz Button
        Button(
            onClick = { viewModel.generateQuiz() },
            enabled = !quizState.isLoading,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (quizState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Creating Flashcards & Quiz...", fontWeight = FontWeight.Bold)
            } else {
                Icon(Icons.Default.Style, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Flashcards & Quiz", fontWeight = FontWeight.Bold)
            }
        }

        quizState.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = error, color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 13.sp, modifier = Modifier.padding(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quiz Result Display
        quizState.result?.let { quizContent ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Study Material: ${quizState.topic}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = IndigoPrimary
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = IndigoSecondary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "PRACTICE READY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = IndigoPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    // Interactive Flashcard Card Component
                    FlashcardPreviewComponent(topic = quizState.topic)

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = quizContent,
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

@Composable
fun FlashcardPreviewComponent(topic: String) {
    var isFlipped by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(targetValue = if (isFlipped) 180f else 0f, label = "cardFlip")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clickable { isFlipped = !isFlipped }
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12 * density
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (rotation <= 90f) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🎴 FLASHCARD FRONT (TAP TO FLIP)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = IndigoPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Core Concept for $topic",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.graphicsLayer { rotationY = 180f }
                ) {
                    Text(
                        text = "✨ ANSWER / DEFINITION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = IndigoTertiary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Key takeaway & definition generated above. Tap again to reset.",
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
