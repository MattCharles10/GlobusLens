package com.globuslens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.globuslens.navigation.NavGraph
import com.globuslens.navigation.Screen
import com.globuslens.ui.components.BottomNavBar
import com.globuslens.ui.theme.GlobusLensTheme
import com.globuslens.utils.Constants
import com.globuslens.utils.LanguagePreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var languagePreferences: LanguagePreferences

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val splashScreen = installSplashScreen()
        var isReady = false
        splashScreen.setKeepOnScreenCondition { !isReady }

        lifecycleScope.launch {
            delay(1000)
            isReady = true
        }

        setContent {
            // Alternative way to detect system dark theme
            val context = LocalContext.current
            val isSystemDarkTheme = remember {
                context.resources.configuration.uiMode and
                        android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                        android.content.res.Configuration.UI_MODE_NIGHT_YES
            }

            var useDarkTheme by remember { mutableStateOf(isSystemDarkTheme) }

            // Language state
            var targetLanguage by remember { mutableStateOf(Constants.DEFAULT_TARGET_LANG) }

            // Load saved language preference
            LaunchedEffect(Unit) {
                languagePreferences.targetLanguageFlow.collect { savedLang ->
                    targetLanguage = savedLang
                }
            }

            GlobusLensTheme(darkTheme = useDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreenContent(
                        isDarkTheme = useDarkTheme,
                        targetLanguage = targetLanguage,
                        onThemeChange = { useDarkTheme = it },
                        onLanguageChange = { newLang ->
                            targetLanguage = newLang
                            lifecycleScope.launch {
                                languagePreferences.setTargetLanguage(newLang)
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(
    isDarkTheme: Boolean,
    targetLanguage: String,
    onThemeChange: (Boolean) -> Unit,
    onLanguageChange: (String) -> Unit
) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val isBottomNavVisible = remember(currentRoute) {
        Screen.bottomNavigationScreens.contains(currentRoute)
    }

    Scaffold(
        bottomBar = {
            if (isBottomNavVisible) {
                BottomNavBar(
                    selectedRoute = currentRoute ?: "",
                    onItemSelected = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            NavGraph(
                navController = navController,
                modifier = Modifier.fillMaxSize(),
                isDarkTheme = isDarkTheme,
                targetLanguage = targetLanguage,
                onThemeChange = onThemeChange,
                onLanguageChange = onLanguageChange
            )
        }
    }
}