package com.example.todoapp.ui.tasks

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.todoapp.data.model.Task
import com.example.todoapp.ui.components.CategoryChip
import com.example.todoapp.ui.components.NeonCard
import com.example.todoapp.ui.components.pulsingGlow
import com.example.todoapp.ui.components.pulsingGlow
import com.example.todoapp.util.HapticHelper
import com.example.todoapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: TaskViewModel,
    onAnalyticsClick: () -> Unit,
    onFocusClick: (Task) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val haptic = remember { HapticHelper(context) }
    
    val tasks by viewModel.allTasks.collectAsState()
    var showTaskDialog by remember { mutableStateOf<Task?>(null) }
    var isNewTask by remember { mutableStateOf(true) }
    
    val categories = listOf("All", "Work", "Personal", "Study", "Health", "Focus")
    var selectedCategory by remember { mutableStateOf("All") }
    
    val filteredTasks = if (selectedCategory == "All") tasks 
                        else tasks.filter { it.category == selectedCategory }

    Scaffold(
        topBar = {
            Column {
                LargeTopAppBar(
                    title = { Text("My Tasks", fontWeight = FontWeight.Bold) },
                    actions = {
                        TextButton(onClick = onAnalyticsClick) {
                            Text("Analytics", color = NeonCyan)
                        }
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = DeepSpace,
                        titleContentColor = TextPrimary
                    )
                )
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        CategoryChip(
                            category = category,
                            isSelected = selectedCategory == category,
                            onClick = { selectedCategory = category }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    showTaskDialog = Task(title = "")
                    isNewTask = true
                },
                containerColor = NeonPurple,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        },
        containerColor = DeepSpace
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredTasks, key = { it.id }) { task ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = {
                        when (it) {
                            SwipeToDismissBoxValue.StartToEnd -> {
                                haptic.success()
                                viewModel.toggleTaskCompletion(task)
                                false
                            }
                            SwipeToDismissBoxValue.EndToStart -> {
                                haptic.click()
                                viewModel.deleteTask(task)
                                true
                            }
                            else -> false
                        }
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        val color = when (dismissState.dismissDirection) {
                            SwipeToDismissBoxValue.StartToEnd -> NeonCyan.copy(alpha = 0.5f)
                            SwipeToDismissBoxValue.EndToStart -> NeonRed.copy(alpha = 0.5f)
                            else -> Color.Transparent
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                                .background(color)
                                .padding(horizontal = 20.dp),
                            contentAlignment = when (dismissState.dismissDirection) {
                                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                                else -> Alignment.CenterEnd
                            }
                        ) {
                            Icon(
                                imageVector = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) 
                                    Icons.Default.Check else Icons.Default.Delete,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    },
                    content = {
                        TaskItem(
                            task = task,
                            onToggle = { 
                                haptic.success()
                                viewModel.toggleTaskCompletion(task) 
                            },
                            onEdit = { 
                                showTaskDialog = task
                                isNewTask = false
                            },
                            onDelete = { viewModel.deleteTask(task) },
                            onFocus = { onFocusClick(task) }
                        )
                    }
                )
            }
        }
    }

    val currentTask = showTaskDialog
    if (currentTask != null) {
        TaskActionDialog(
            task = currentTask,
            isNewTask = isNewTask,
            onDismiss = { showTaskDialog = null },
            onConfirm = { updatedTask ->
                if (isNewTask) {
                    viewModel.addTask(updatedTask)
                } else {
                    viewModel.updateTask(updatedTask)
                }
                showTaskDialog = null
            }
        )
    }
}

@Composable
fun TaskItem(
    task: Task,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onFocus: () -> Unit
) {
    NeonCard(
        modifier = Modifier
            .fillMaxWidth()
            .pulsingGlow(
                color = when (task.priority) {
                    com.example.todoapp.data.model.TaskPriority.HIGH -> HotPink
                    else -> Color.Transparent
                },
                enabled = task.priority == com.example.todoapp.data.model.TaskPriority.HIGH && !task.isCompleted
            )
            .animateContentSize(animationSpec = tween(300))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Toggle completion",
                    tint = if (task.isCompleted) NeonCyan else TextSecondary
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (task.isCompleted) TextSecondary else TextPrimary,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    fontWeight = FontWeight.Bold
                )
                if (task.description.isNotEmpty()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                }
                if (task.dueDate != null) {
                    val isOverdue = !task.isCompleted && task.dueDate < System.currentTimeMillis()
                    Text(
                        text = if (isOverdue) "Overdue" else "Due: ${java.text.SimpleDateFormat("dd MMM, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(task.dueDate))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOverdue) NeonRed else NeonCyan,
                        fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
            Row {
                IconButton(onClick = { onEdit() }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondary, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onFocus) {
                    Icon(Icons.Default.Timer, contentDescription = "Focus", tint = NeonCyan, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = NeonRed, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskActionDialog(
    task: Task,
    isNewTask: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Task) -> Unit
) {
    var title by remember { mutableStateOf(task.title) }
    var desc by remember { mutableStateOf(task.description) }
    var category by remember { mutableStateOf(task.category) }
    var priority by remember { mutableStateOf(task.priority) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(task.dueDate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNewTask) "New Task" else "Edit Task", color = TextPrimary) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = BorderGray,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = BorderGray,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                Text("Category", color = TextSecondary, style = MaterialTheme.typography.labelMedium)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val categories = listOf("Work", "Personal", "Study", "Health", "Focus")
                    items(categories) { cat ->
                        CategoryChip(
                            category = cat,
                            isSelected = category == cat,
                            onClick = { category = cat }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Priority", color = TextSecondary, style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    com.example.todoapp.data.model.TaskPriority.values().forEach { prio ->
                        val color = when (prio) {
                            com.example.todoapp.data.model.TaskPriority.HIGH -> HotPink
                            com.example.todoapp.data.model.TaskPriority.MEDIUM -> NeonCyan
                            com.example.todoapp.data.model.TaskPriority.LOW -> NeonPurple
                        }
                        FilterChip(
                            selected = priority == prio,
                            onClick = { priority = prio },
                            label = { Text(prio.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = color,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { showDatePicker = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (selectedDate == null) "Set Due Date" 
                               else "Due: ${java.text.SimpleDateFormat("dd MMM, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(selectedDate!!))}",
                        color = NeonCyan
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (title.isNotBlank()) {
                        onConfirm(task.copy(
                            title = title, 
                            description = desc, 
                            category = category,
                            priority = priority,
                            dueDate = selectedDate
                        ))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
            ) {
                Text(if (isNewTask) "Add" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = DeepSpace
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate ?: System.currentTimeMillis()
        )
        val timePickerState = rememberTimePickerState(
            initialHour = if (selectedDate != null) {
                val cal = java.util.Calendar.getInstance().apply { timeInMillis = selectedDate!! }
                cal.get(java.util.Calendar.HOUR_OF_DAY)
            } else 12,
            initialMinute = if (selectedDate != null) {
                val cal = java.util.Calendar.getInstance().apply { timeInMillis = selectedDate!! }
                cal.get(java.util.Calendar.MINUTE)
            } else 0
        )
        var showTimeByDate by remember { mutableStateOf(false) }

        if (!showTimeByDate) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = { showTimeByDate = true }) { Text("Next") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        } else {
            AlertDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        val dateMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                        val cal = java.util.Calendar.getInstance().apply {
                            timeInMillis = dateMillis
                            set(java.util.Calendar.HOUR_OF_DAY, timePickerState.hour)
                            set(java.util.Calendar.MINUTE, timePickerState.minute)
                        }
                        selectedDate = cal.timeInMillis
                        showDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showTimeByDate = false }) { Text("Back") }
                },
                title = { Text("Select Time", color = TextPrimary) },
                text = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        TimePicker(state = timePickerState)
                    }
                },
                containerColor = DeepSpace
            )
        }
    }
}
