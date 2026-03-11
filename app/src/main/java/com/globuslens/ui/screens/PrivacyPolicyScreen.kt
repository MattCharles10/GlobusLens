package com.globuslens.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.globuslens.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = "Privacy Policy",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Last Updated: March 10, 2026",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                PolicySection(
                    title = "1. Information We Collect",
                    content = "We collect the following information when you use GlobusLens:\n\n" +
                            "• Camera feed for text recognition (processed locally, not stored)\n" +
                            "• Product names and details you choose to save\n" +
                            "• Favorites and shopping list items\n" +
                            "• Crash reports and analytics to improve the app"
                )

                PolicySection(
                    title = "2. How We Use Your Information",
                    content = "Your information is used to:\n\n" +
                            "• Provide text recognition and translation services\n" +
                            "• Save your favorites and shopping lists\n" +
                            "• Improve app performance and fix crashes\n" +
                            "• Analyze usage patterns to enhance features"
                )

                PolicySection(
                    title = "3. Data Storage",
                    content = "• All product data is stored locally on your device\n" +
                               "• No personal data is uploaded to our servers\n" +
                              "• Translation requests are sent to third-party APIs (LibreTranslate, MyMemory)\n" +
                              "• Crash reports are sent to Firebase Crashlytics"
                )

                PolicySection(
                    title = "4. Third-Party Services",
                    content = "We use the following third-party services:\n\n" +
                            "• **LibreTranslate** - Free translation API\n" +
                            "• **MyMemory** - Backup translation API\n" +
                            "• **Firebase Crashlytics** - Crash reporting\n" +
                            "• **Google ML Kit** - On-device text recognition"
                )

                PolicySection(
                    title = "5. Your Rights",
                    content = "You can:\n\n" +
                            "• Delete any saved product at any time\n" +
                            "• Clear all app data through device settings\n" +
                            "• Uninstall the app to remove all local data"
                )

                PolicySection(
                    title = "6. Changes to This Policy",
                    content = "We may update this privacy policy from time to time. We will notify you of any changes by posting the new policy in the app."
                )

                PolicySection(
                    title = "7. Contact Us",
                    content = "This app is developed and maintained by Mathew Charles.\n\n" +
                              "If you have questions about this privacy policy, please contact:\n\n" +
                              "Mathew Charles\n" +
                              "Email: privacy@globuslens.com"
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun PolicySection(
    title: String,
    content: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}