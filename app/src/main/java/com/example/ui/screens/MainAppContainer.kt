package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.ui.components.ProUpgradeDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.HomeworkViewModel

enum class AppTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Solve("Solve", Icons.Default.Psychology),
    Essay("Essay", Icons.Default.EditNote),
    Quiz("Quiz & Prep", Icons.Default.Quiz),
    Advisor("Advisor", Icons.Default.Insights),
    History("History", Icons.Default.History)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(
    viewModel: HomeworkViewModel
) {
    var selectedTab by remember { mutableStateOf(AppTab.Solve) }

    val questionsUsed by viewModel.questionsUsed.collectAsState()
    val imagesUsed by viewModel.imagesUsed.collectAsState()
    val isPro by viewModel.isPro.collectAsState()
    val showProDialog by viewModel.showProDialog.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = IndigoPrimary,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.School,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Homework",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                },
                actions = {
                    // Weekly Free Quota / Pro Status Pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isPro) GoldGradientStart.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clickable { viewModel.openProDialog() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (isPro) Icons.Default.Star else Icons.Default.ElectricBolt,
                                contentDescription = null,
                                tint = if (isPro) GoldGradientStart else IndigoPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isPro) "PRO" else "${100 - questionsUsed} Left",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isPro) GoldGradientStart else IndigoPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                windowInsets = WindowInsets.navigationBars,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                AppTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = IndigoPrimary,
                            selectedTextColor = IndigoPrimary,
                            indicatorColor = IndigoPrimary.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                AppTab.Solve -> SolveScreen(viewModel = viewModel, onOpenPro = { viewModel.openProDialog() })
                AppTab.Essay -> EssayScreen(viewModel = viewModel)
                AppTab.Quiz -> QuizScreen(viewModel = viewModel)
                AppTab.Advisor -> StudyAdvisorScreen(viewModel = viewModel)
                AppTab.History -> HistoryScreen(viewModel = viewModel)
            }
        }
    }

    if (showProDialog) {
        ProUpgradeDialog(
            questionsUsed = questionsUsed,
            imagesUsed = imagesUsed,
            isPro = isPro,
            onDismiss = { viewModel.closeProDialog() },
            onUpgradeClicked = { viewModel.setProUser(!isPro) },
            onResetQuotaClicked = { viewModel.resetFreeQuota() }
        )
    }
}
