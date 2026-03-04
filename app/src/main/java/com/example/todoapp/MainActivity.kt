package com.example.todoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.todoapp.data.local.TaskDatabase
import com.example.todoapp.data.repository.TaskRepository
import com.example.todoapp.ui.analytics.AnalyticsScreen
import com.example.todoapp.ui.onboarding.OnboardingScreen
import com.example.todoapp.ui.tasks.*
import android.os.Build
import android.Manifest
import androidx.activity.result.contract.ActivityResultContracts
import com.example.todoapp.util.NotificationHelper
import com.example.todoapp.ui.focus.FocusScreen
import com.example.todoapp.ui.theme.TodoAppTheme

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        NotificationHelper(this) // Initialize channel

        val database = TaskDatabase.getDatabase(this)
        val repository = TaskRepository(database.taskDao())
        val factory = TaskViewModelFactory(repository, application)

        setContent {
            TodoAppTheme {
                val navController = rememberNavController()
                val viewModel: TaskViewModel = viewModel(factory = factory)

                NavHost(navController = navController, startDestination = "onboarding") {
                    composable("onboarding") {
                        OnboardingScreen(onFinish = {
                            navController.navigate("taskList") {
                                popUpTo("onboarding") { inclusive = true }
                            }
                        })
                    }
                    composable("taskList") {
                        TaskListScreen(
                            viewModel = viewModel,
                            onAnalyticsClick = { navController.navigate("analytics") },
                            onFocusClick = { task ->
                                navController.navigate("focus/${task.id}")
                            }
                        )
                    }
                    composable("analytics") {
                        AnalyticsScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(
                        "focus/{taskId}",
                        arguments = listOf(navArgument("taskId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val taskId = backStackEntry.arguments?.getString("taskId")
                        val tasks by viewModel.allTasks.collectAsState()
                        val task = tasks.find { it.id == taskId }
                        if (task != null) {
                            FocusScreen(
                                task = task,
                                onClose = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}