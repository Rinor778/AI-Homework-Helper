package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.ui.theme.*

@Composable
fun ProUpgradeDialog(
    questionsUsed: Int,
    imagesUsed: Int,
    isPro: Boolean,
    onDismiss: () -> Unit,
    onUpgradeClicked: () -> Unit,
    onResetQuotaClicked: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Banner Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(20.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_pro_banner_1785438557797),
                        contentDescription = "Pro Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color(0xCC000000))
                                )
                            )
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = IndigoTertiary
                        ) {
                            Text(
                                text = "PRO ACCESS",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Unlock AI Homework Pro",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Learn faster without limits, step-by-step math solver & advanced AI tutors.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Free Quota Indicator Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Weekly Free Quota",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (isPro) "UNLIMITED" else "Free Plan",
                                fontSize = 11.sp,
                                color = if (isPro) IndigoSecondary else IndigoPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Question Progress
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Questions: $questionsUsed / 100 this week", fontSize = 12.sp)
                        }
                        LinearProgressIndicator(
                            progress = { (questionsUsed / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = IndigoPrimary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Image Progress
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Photo Solutions: $imagesUsed / 10 this week", fontSize = 12.sp)
                        }
                        LinearProgressIndicator(
                            progress = { (imagesUsed / 10f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = IndigoSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pro Features List
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProFeatureRow(icon = Icons.Default.AllInclusive, text = "Unlimited questions & photo solutions")
                    ProFeatureRow(icon = Icons.Default.Psychology, text = "Gemini Pro STEM & Math step-by-step solver")
                    ProFeatureRow(icon = Icons.Default.Create, text = "Unlimited Essay, Quiz & Study Advisor tools")
                    ProFeatureRow(icon = Icons.Default.Speed, text = "Priority fast AI processing without waiting")
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Button(
                    onClick = onUpgradeClicked,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = IndigoPrimary
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = GoldGradientStart)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPro) "Pro Active (Switch Mode)" else "Upgrade to Pro ($4.99/mo)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onResetQuotaClicked) {
                        Text(text = "Reset Free Quota (Demo)", fontSize = 12.sp, color = IndigoPrimary)
                    }
                    TextButton(onClick = onDismiss) {
                        Text(text = "Close", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProFeatureRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(IndigoPrimary.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = IndigoPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            )
        )
    }
}
