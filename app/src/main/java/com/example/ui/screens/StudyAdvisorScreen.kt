package com.example.ui.screens

import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.IndigoSecondary
import com.example.ui.viewmodel.HomeworkViewModel

@Composable
fun StudyAdvisorScreen(
    viewModel: HomeworkViewModel,
    modifier: Modifier = Modifier
) {
    val advisorState by viewModel.advisorState.collectAsState()
    val historyList by viewModel.historyList.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        if (advisorState.studyPlan == null && !advisorState.isLoading) {
            viewModel.generateStudyAdvisor()
        }
    }

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
            Icon(Icons.Default.Insights, contentDescription = null, tint = IndigoPrimary, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Personalized Study Recommendations",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "AI Advisor analyzes your search history to build tailored study strategies.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // History Summary Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Questions Solved: ${historyList.size}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (historyList.isEmpty()) "Start asking questions to unlock custom advisor insights!" else "Advisor active for ${historyList.size} topics",
                        fontSize = 12.sp,
                        color = IndigoPrimary
                    )
                }

                Button(
                    onClick = { viewModel.generateStudyAdvisor() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Refresh Plan", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (advisorState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = IndigoPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("AI is analyzing your study patterns...", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            advisorState.studyPlan?.let { plan ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PsychologyAlt, contentDescription = null, tint = IndigoPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Your Academic Growth Strategy",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                        Text(
                            text = plan,
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
}
