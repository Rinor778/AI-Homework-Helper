package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import com.example.ui.viewmodel.HomeworkViewModel
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolveScreen(
    viewModel: HomeworkViewModel,
    onOpenPro: () -> Unit,
    modifier: Modifier = Modifier
) {
    val solveState by viewModel.solveState.collectAsState()
    val isPro by viewModel.isPro.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    val subjects = listOf("Math", "Physics", "Chemistry", "Biology", "English", "History", "Programming", "General")
    val languages = listOf("English", "Spanish", "French", "German", "Chinese", "Japanese", "Hindi", "Portuguese")
    val quickPrompts = listOf(
        "Solve this step-by-step",
        "Explain the formula used",
        "How do I find x?",
        "Check my work & fix errors"
    )

    // Image Picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                viewModel.updateSelectedImage(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    var showLanguageMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Hero Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(20.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_homework_hero_1785438544564),
                contentDescription = "Homework Hero",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp)
            ) {
                Surface(
                    color = IndigoPrimary,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "AI STEP-BY-STEP TUTOR",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Text(
                    text = "Snap or Type Your Homework Question",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Subject Selector Chips
        Text(
            text = "Select Subject",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(subjects) { subject ->
                val isSelected = solveState.subject.equals(subject, ignoreCase = true)
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.updateSubject(subject) },
                    label = { Text(subject, fontSize = 13.sp) },
                    leadingIcon = {
                        val icon = when (subject) {
                            "Math" -> Icons.Default.Calculate
                            "Physics" -> Icons.Default.Bolt
                            "Chemistry" -> Icons.Default.Science
                            "Biology" -> Icons.Default.Park
                            "English" -> Icons.Default.MenuBook
                            "History" -> Icons.Default.HistoryEdu
                            "Programming" -> Icons.Default.Code
                            else -> Icons.Default.AutoAwesome
                        }
                        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IndigoPrimary,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Photo Upload / Camera Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = IndigoPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Solve Questions from Photos",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Box {
                        AssistChip(
                            onClick = { showLanguageMenu = true },
                            label = { Text(solveState.selectedLanguage, fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Translate, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                        DropdownMenu(
                            expanded = showLanguageMenu,
                            onDismissRequest = { showLanguageMenu = false }
                        ) {
                            languages.forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text(lang) },
                                    onClick = {
                                        viewModel.updateLanguage(lang)
                                        showLanguageMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (solveState.selectedImage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, IndigoPrimary, RoundedCornerShape(14.dp))
                    ) {
                        Image(
                            bitmap = solveState.selectedImage!!.asImageBitmap(),
                            contentDescription = "Homework photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { viewModel.updateSelectedImage(null) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                .size(32.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove photo", tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload Photo", fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                // Simulation of sample problem photo for instant testing
                                val sampleBitmap = Bitmap.createBitmap(400, 200, Bitmap.Config.ARGB_8888)
                                val canvas = android.graphics.Canvas(sampleBitmap)
                                canvas.drawColor(android.graphics.Color.WHITE)
                                val paint = android.graphics.Paint().apply {
                                    color = android.graphics.Color.BLACK
                                    textSize = 32f
                                    isAntiAlias = true
                                }
                                canvas.drawText("Solve: 3x^2 + 5x - 2 = 0", 20f, 100f, paint)
                                viewModel.updateSelectedImage(sampleBitmap)
                                viewModel.updateQuestion("Solve for x: 3x^2 + 5x - 2 = 0")
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sample Photo", fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Question Input Text Field
        OutlinedTextField(
            value = solveState.question,
            onValueChange = { viewModel.updateQuestion(it) },
            label = { Text("Type your homework question...") },
            placeholder = { Text("e.g., What is photosynthesis? or Solve 2x + 5 = 15") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp),
            shape = RoundedCornerShape(16.dp),
            trailingIcon = {
                if (solveState.question.isNotEmpty()) {
                    IconButton(onClick = { viewModel.updateQuestion("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear text")
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Prompts
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(quickPrompts) { prompt ->
                SuggestionChip(
                    onClick = {
                        val currentText = solveState.question
                        if (currentText.isBlank()) {
                            viewModel.updateQuestion(prompt)
                        } else {
                            viewModel.updateQuestion("$currentText ($prompt)")
                        }
                    },
                    label = { Text(prompt, fontSize = 12.sp) },
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Solve Button
        Button(
            onClick = {
                focusManager.clearFocus()
                viewModel.solveQuestion()
            },
            enabled = !solveState.isLoading,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
        ) {
            if (solveState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Analyzing Homework with AI...", fontWeight = FontWeight.Bold)
            } else {
                Icon(Icons.Default.Psychology, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (solveState.selectedImage != null) "Solve Photo & Explain" else "Get Step-by-Step Solution",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        // Error Banner if any
        solveState.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f)
                    )
                    if (error.contains("Free limit")) {
                        TextButton(onClick = onOpenPro) {
                            Text("PRO", fontWeight = FontWeight.Bold, color = IndigoPrimary)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Solution Display Card
        solveState.solutionResult?.let { solution ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = IndigoTertiary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Step-by-Step Solution",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Row {
                            IconButton(onClick = {
                                clipboardManager.setText(AnnotatedString(solution))
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy solution", tint = IndigoPrimary)
                            }
                            solveState.currentSavedItem?.let { saved ->
                                IconButton(onClick = { viewModel.toggleFavorite(saved) }) {
                                    Icon(
                                        imageVector = if (saved.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "Favorite",
                                        tint = GoldGradientStart
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    Text(
                        text = solution,
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
