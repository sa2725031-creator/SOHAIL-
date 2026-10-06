package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.SereneViewModel
import com.example.ui.components.PostSessionDialog
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.BreathingScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.JournalScreen
import com.example.ui.screens.MeditationTimerScreen
import com.example.ui.theme.SereneTheme

enum class SereneTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home, "nav_tab_home"),
    TIMER("Meditate", Icons.Filled.SelfImprovement, Icons.Outlined.SelfImprovement, "nav_tab_timer"),
    BREATHE("Breathe", Icons.Filled.Air, Icons.Outlined.Air, "nav_tab_breathe"),
    TRACKING("Track", Icons.Filled.BarChart, Icons.Outlined.BarChart, "nav_tab_tracking"),
    JOURNAL("Journal", Icons.Filled.Book, Icons.Outlined.Book, "nav_tab_journal")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SereneTheme {
                SereneApp()
            }
        }
    }
}

@Composable
fun SereneApp(
    viewModel: SereneViewModel = viewModel()
) {
    var currentTab by remember { mutableStateOf(SereneTab.HOME) }

    val sessions by viewModel.allSessions.collectAsState()
    val reflections by viewModel.allReflections.collectAsState()
    val dailyGoalMin by viewModel.dailyGoalMinutes.collectAsState()

    val showPostSessionDialog by viewModel.showPostSessionDialog.collectAsState()
    val completedDurationSec by viewModel.completedDurationSec.collectAsState()
    val sessionType by viewModel.selectedSessionType.collectAsState()

    // BackHandler: if not on HOME, back press returns to HOME
    BackHandler(enabled = currentTab != SereneTab.HOME) {
        currentTab = SereneTab.HOME
    }

    if (showPostSessionDialog) {
        PostSessionDialog(
            durationSeconds = completedDurationSec,
            sessionTitle = sessionType,
            onSave = { mood, notes ->
                viewModel.saveCompletedSession(mood, notes)
            },
            onDismiss = {
                viewModel.dismissPostSessionDialog()
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("serene_bottom_nav")
            ) {
                SereneTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title
                            )
                        },
                        label = { Text(tab.title) },
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        val screenModifier = Modifier.padding(innerPadding)

        when (currentTab) {
            SereneTab.HOME -> {
                HomeScreen(
                    sessions = sessions,
                    dailyGoalMin = dailyGoalMin,
                    onStartQuickTimer = { minutes, type ->
                        viewModel.selectPresetMinutes(minutes)
                        viewModel.setSessionType(type)
                        currentTab = SereneTab.TIMER
                    },
                    onNavigateBreathing = {
                        currentTab = SereneTab.BREATHE
                    },
                    onNavigateTimer = {
                        currentTab = SereneTab.TIMER
                    },
                    modifier = screenModifier
                )
            }
            SereneTab.TIMER -> {
                MeditationTimerScreen(
                    viewModel = viewModel,
                    modifier = screenModifier
                )
            }
            SereneTab.BREATHE -> {
                BreathingScreen(
                    viewModel = viewModel,
                    modifier = screenModifier
                )
            }
            SereneTab.TRACKING -> {
                AnalyticsScreen(
                    viewModel = viewModel,
                    sessions = sessions,
                    dailyGoalMin = dailyGoalMin,
                    modifier = screenModifier
                )
            }
            SereneTab.JOURNAL -> {
                JournalScreen(
                    viewModel = viewModel,
                    reflections = reflections,
                    modifier = screenModifier
                )
            }
        }
    }
}
