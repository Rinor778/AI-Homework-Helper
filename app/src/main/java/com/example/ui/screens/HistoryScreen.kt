package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.local.HomeworkItem
import com.example.ui.theme.GoldGradientStart
import com.example.ui.theme.IndigoPrimary
import com.example.ui.viewmodel.HomeworkViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HomeworkViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val favoriteList by viewModel.favoriteList.collectAsState()

    var selectedTab by remember { mutableStateOf("All") } // "All", "Favorites", "Math", "Science", "English"
    val tabs = listOf("All", "Favorites", "Math", "Science", "English")

    val filteredList = when (selectedTab) {
        "Favorites" -> favoriteList
        "All" -> searchResults
        else -> searchResults.filter { it.subject.equals(selectedTab, ignoreCase = true) }
    }

    var showClearDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, contentDescription = null, tint = IndigoPrimary, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Saved History",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            if (searchResults.isNotEmpty()) {
                IconButton(onClick = { showClearDialog = true }) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "Clear All", tint = MaterialTheme.colorScheme.error)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = { Text("Search solved questions or topics...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear search")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tabs
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(tabs) { tab ->
                val isSelected = selectedTab == tab
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedTab = tab },
                    label = { Text(tab, fontSize = 13.sp) },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IndigoPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (selectedTab == "Favorites") "No favorite items saved yet." else "No solved questions found.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredList, key = { it.id }) { item ->
                    HistoryCardItem(
                        item = item,
                        onToggleFavorite = { viewModel.toggleFavorite(item) },
                        onDelete = { viewModel.deleteItem(item) }
                    )
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All History?") },
            text = { Text("Are you sure you want to delete all saved homework questions?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearHistory()
                    showClearDialog = false
                }) {
                    Text("Delete All", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun HistoryCardItem(
    item: HomeworkItem,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val dateFormat = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = IndigoPrimary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = item.subject.uppercase(),
                        color = IndigoPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Text(
                    text = dateFormat.format(Date(item.timestamp)),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.question,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { expanded = !expanded },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = if (expanded) "Hide Solution" else "View Step-by-Step Solution",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = IndigoPrimary
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = IndigoPrimary
                    )
                }

                Row {
                    IconButton(onClick = { clipboardManager.setText(AnnotatedString(item.explanation)) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite",
                            tint = GoldGradientStart,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = item.explanation,
                        style = MaterialTheme.typography.bodySmall.copy(
                            lineHeight = 20.sp,
                            fontSize = 13.sp
                        )
                    )
                }
            }
        }
    }
}
