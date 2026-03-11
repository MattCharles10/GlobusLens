package com.globuslens.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.globuslens.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    navController: NavController
) {
    val scrollState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var isHeaderVisible by remember { mutableStateOf(true) }
    var selectedSection by remember { mutableStateOf<Int?>(null) }

    // Animated values for letter spacing and effects (only for content, not title)
    val infiniteTransition = rememberInfiniteTransition(label = "infinite")
    val titleLetterSpacing by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "titleLetterSpacing"
    )

    val contentAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "contentAlpha"
    )

    // Gradient background for visual interest
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)
        )
    )

    // Policy sections data
    val policySections = remember {
        listOf(
            PolicySectionData(
                title = "1. Information We Collect",
                icon = Icons.Default.Policy,
                content = "We collect the following information when you use GlobusLens:\n\n" +
                        "• Camera feed for text recognition (processed locally, not stored)\n" +
                        "• Product names and details you choose to save\n" +
                        "• Favorites and shopping list items\n" +
                        "• Crash reports and analytics to improve the app"
            ),
            PolicySectionData(
                title = "2. How We Use Your Information",
                icon = Icons.Default.Policy,
                content = "Your information is used to:\n\n" +
                        "• Provide text recognition and translation services\n" +
                        "• Save your favorites and shopping lists\n" +
                        "• Improve app performance and fix crashes\n" +
                        "• Analyze usage patterns to enhance features"
            ),
            PolicySectionData(
                title = "3. Data Storage",
                icon = Icons.Default.Policy,
                content = "• All product data is stored locally on your device\n" +
                        "• No personal data is uploaded to our servers\n" +
                        "• Translation requests are sent to third-party APIs (LibreTranslate, MyMemory)\n" +
                        "• Crash reports are sent to Firebase Crashlytics"
            ),
            PolicySectionData(
                title = "4. Third-Party Services",
                icon = Icons.Default.Policy,
                content = "We use the following third-party services:\n\n" +
                        "• **LibreTranslate** - Free translation API\n" +
                        "• **MyMemory** - Backup translation API\n" +
                        "• **Firebase Crashlytics** - Crash reporting\n" +
                        "• **Google ML Kit** - On-device text recognition"
            ),
            PolicySectionData(
                title = "5. Your Rights",
                icon = Icons.Default.Policy,
                content = "You can:\n\n" +
                        "• Delete any saved product at any time\n" +
                        "• Clear all app data through device settings\n" +
                        "• Uninstall the app to remove all local data"
            ),
            PolicySectionData(
                title = "6. Changes to This Policy",
                icon = Icons.Default.Policy,
                content = "We may update this privacy policy from time to time. We will notify you of any changes by posting the new policy in the app."
            ),
            PolicySectionData(
                title = "7. Contact Us",
                icon = Icons.Default.Policy,
                content = "This app is developed and maintained by Mathew Charles.\n\n" +
                        "If you have questions about this privacy policy, please contact:\n\n" +
                        "**Mathew Charles**\n" +
                        "Email: privacy@globuslens.com"
            )
        )
    }

    // Fix for derivedStateOf - moved inside composable and properly defined
    val showScrollToTop by remember {
        derivedStateOf {
            scrollState.firstVisibleItemIndex > 0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // CHANGED: Static title without animations
                    Text(
                        text = "Privacy Policy",
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                // Animate out before navigating
                                delay(100)
                                navController.navigateUp()
                            }
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.scale(
                                if (isHeaderVisible) 1f else 0.9f
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBrush)
                .padding(paddingValues)
        ) {
            // Background decorative element
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    )
            )

            LazyColumn(
                state = scrollState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header with date
                item {
                    HeaderSection(
                        titleLetterSpacing = titleLetterSpacing,
                        contentAlpha = contentAlpha
                    )
                }

                // Policy sections with animations
                items(
                    items = policySections,
                    key = { it.title }
                ) { section ->
                    PolicySectionCard(
                        section = section,
                        isSelected = selectedSection == policySections.indexOf(section),
                        onSectionClick = {
                            selectedSection = if (selectedSection == policySections.indexOf(section)) {
                                null
                            } else {
                                policySections.indexOf(section)
                            }
                        },
                        sectionIndex = policySections.indexOf(section)
                    )
                }

                // Footer
                item {
                    FooterSection(contentAlpha = contentAlpha)
                }
            }

            // Scroll to top button (appears when scrolled)
            AnimatedContent(
                targetState = showScrollToTop,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "scrollButtonAnimation"
            ) { visible ->
                if (visible) {
                    FloatingActionButton(
                        onClick = {
                            scope.launch {
                                scrollState.animateScrollToItem(0)
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .scale(if (visible) 1f else 0f),
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Text("↑", fontSize = 20.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderSection(
    titleLetterSpacing: Float,
    contentAlpha: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = "Privacy Policy",
            style = MaterialTheme.typography.headlineMedium.copy(
                letterSpacing = titleLetterSpacing.sp,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.alpha(contentAlpha)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            shape = RoundedCornerShape(8.dp),
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Last Updated: March 10, 2026",
                style = MaterialTheme.typography.bodyMedium.copy(
                    letterSpacing = 0.3.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
private fun PolicySectionCard(
    section: PolicySectionData,
    isSelected: Boolean,
    onSectionClick: () -> Unit,
    sectionIndex: Int
) {
    var isVisible by remember { mutableStateOf(false) }

    // Fixed LaunchedEffect with proper key
    LaunchedEffect(key1 = sectionIndex) {
        delay(100 * (sectionIndex + 1).toLong())
        isVisible = true
    }

    AnimatedContent(
        targetState = isVisible,
        transitionSpec = {
            (fadeIn(animationSpec = tween(500, delayMillis = 100))
                    togetherWith fadeOut()).using(
                SizeTransform(clip = false)
            )
        },
        label = "sectionCardAnimation"
    ) { visible ->
        if (visible) {
            Card(
                onClick = onSectionClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = FastOutSlowInEasing
                        )
                    ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (isSelected) 4.dp else 2.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Title with animated letter spacing
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            letterSpacing = if (isSelected) 1.2.sp else 0.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Content with animated appearance
                    AnimatedContent(
                        targetState = isSelected,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith
                                    fadeOut(animationSpec = tween(200))
                        },
                        label = "contentAnimation"
                    ) { expanded ->
                        if (expanded) {
                            Text(
                                text = buildAnnotatedString {
                                    val lines = section.content.split("\n")
                                    lines.forEachIndexed { index, line ->
                                        if (index > 0) append("\n")
                                        if (line.startsWith("•")) {
                                            withStyle(
                                                style = SpanStyle(
                                                    fontWeight = FontWeight.Normal,
                                                    letterSpacing = 0.3.sp
                                                )
                                            ) {
                                                append(line)
                                            }
                                        } else if (line.contains("**")) {
                                            val parts = line.split("**")
                                            parts.forEachIndexed { partIndex, part ->
                                                if (partIndex % 2 == 1) {
                                                    withStyle(
                                                        style = SpanStyle(
                                                            fontWeight = FontWeight.Bold,
                                                            letterSpacing = 0.2.sp
                                                        )
                                                    ) {
                                                        append(part)
                                                    }
                                                } else {
                                                    append(part)
                                                }
                                            }
                                        } else {
                                            withStyle(
                                                style = SpanStyle(
                                                    fontWeight = FontWeight.Medium,
                                                    letterSpacing = 0.2.sp
                                                )
                                            ) {
                                                append(line)
                                            }
                                        }
                                    }
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            )
                        } else {
                            Text(
                                text = section.content.take(100) + "...",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    letterSpacing = 0.2.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                            )
                        }
                    }

                    // Expand/collapse indicator
                    if (!isSelected) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Tap to expand",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 0.5.sp
                                ),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FooterSection(
    contentAlpha: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Divider(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(1.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "© 2026 GlobusLens. All rights reserved.",
            style = MaterialTheme.typography.bodySmall.copy(
                letterSpacing = 0.3.sp
            ),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            textAlign = TextAlign.Center,
            modifier = Modifier.alpha(contentAlpha)
        )
    }
}

data class PolicySectionData(
    val title: String,
    val icon: ImageVector,
    val content: String
)