package com.example.todoapp.ui.analytics

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todoapp.ui.components.NeonCard
import com.example.todoapp.ui.tasks.TaskViewModel
import com.example.todoapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: TaskViewModel,
    onBack: () -> Unit
) {
    val stats by viewModel.completionStats.collectAsState()
    val completed = stats.first
    val total = stats.second
    val progress = if (total > 0) completed.toFloat() / total else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "progress_animation"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("App Analytics", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DeepSpace,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = DeepSpace
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NeonCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Completion Rate",
                        style = MaterialTheme.typography.headlineMedium,
                        color = NeonCyan
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.size(200.dp),
                            color = BorderGray,
                            strokeWidth = 12.dp,
                        )
                        CircularProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.size(200.dp),
                            color = NeonPurple,
                            strokeWidth = 12.dp,
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.headlineLarge,
                                color = TextPrimary
                            )
                            Text(
                                text = "$completed / $total Tasks",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            NeonCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Category Breakdown", style = MaterialTheme.typography.titleMedium, color = NeonCyan)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val categoryStats by viewModel.categoryStats.collectAsState()
                    categoryStats.forEach { (category, stats) ->
                        val catCompleted = stats.first
                        val catTotal = stats.second
                        val catProgress = if (catTotal > 0) catCompleted.toFloat() / catTotal else 0f
                        
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(category, color = TextPrimary, style = MaterialTheme.typography.bodyMedium)
                                Text("$catCompleted/$catTotal", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { catProgress },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                                color = NeonPurple,
                                trackColor = BorderGray
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            NeonCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("7-Day Productivity", style = MaterialTheme.typography.titleMedium, color = ElectricBlue)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val history by viewModel.productivityHistory.collectAsState()
                    Row(
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        history.forEachIndexed { index, count ->
                            val maxCount = history.maxOrNull()?.coerceAtLeast(1) ?: 1
                            val barHeight = (count.toFloat() / maxCount * 80).dp
                            
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (count > 0) {
                                    Text(
                                        text = count.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (index == 6) NeonCyan else Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .width(20.dp)
                                        .height(barHeight)
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(if (index == 6) NeonCyan else NeonPurple)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (index == 6) "T" else (6 - index).toString() + "d",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            NeonCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Insights",
                        style = MaterialTheme.typography.headlineSmall,
                        color = HotPink
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (progress >= 0.8f) "You're a productivity machine! Your focus is incredible."
                        else if (progress >= 0.5f) "Solid consistency. You're outperforming most users today."
                        else "Every small task counts. Completing just one more will boost your momentum!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}
