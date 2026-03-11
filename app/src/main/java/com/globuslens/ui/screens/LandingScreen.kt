package com.globuslens.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandingScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var startAnimation by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(500)
        showContent = true
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF2196F3), // Primary blue
                            Color(0xFF1976D2)  // Darker blue
                        )
                    )
                )
        ) {
            // Animated background circles
            AnimatedBackgroundCircles()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Section with Logo and Title
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(20.dp))

                    // Animated Logo
                    AnimatedLogo(
                        startAnimation = startAnimation,
                        modifier = Modifier.size(100.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // App Name with Animation
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn() + slideInVertically(
                            initialOffsetY = { it / 2 }
                        )
                    ) {
                        Text(
                            text = "GlobusLens",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Tagline
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(
                            animationSpec = tween(delayMillis = 300)
                        )
                    ) {
                        Text(
                            text = "Scan. Translate. Shop.",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Features Section with Flip Cards
                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(
                        animationSpec = tween(delayMillis = 600)
                    )
                ) {
                    FeatureFlipCards()
                }

                // Bottom Section with Get Started Button and Privacy Policy
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Get Started Button
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(
                            animationSpec = tween(delayMillis = 900)
                        ) + slideInVertically(
                            initialOffsetY = { it / 2 }
                        )
                    ) {
                        GetStartedButton(navController)
                    }

                    // Privacy Policy Link
                    AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn(
                            animationSpec = tween(delayMillis = 1000)
                        )
                    ) {
                        TextButton(
                            onClick = { navController.navigate("privacy_policy") },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text(
                                text = "Privacy Policy",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedBackgroundCircles() {
    val infiniteTransition = rememberInfiniteTransition()

    val scale1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val scale2 by infiniteTransition.animateFloat(
        initialValue = 1.2f,
        targetValue = 1.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val offset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Circle 1
        Box(
            modifier = Modifier
                .size(250.dp)
                .scale(scale1)
                .offset(x = offset1.dp, y = (-30).dp + offset1.dp)
                .align(Alignment.TopStart)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
        )

        // Circle 2
        Box(
            modifier = Modifier
                .size(350.dp)
                .scale(scale2)
                .offset(x = 50.dp + offset2.dp, y = 80.dp + offset2.dp)
                .align(Alignment.BottomEnd)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
        )

        // Circle 3 (smaller, additional)
        Box(
            modifier = Modifier
                .size(150.dp)
                .scale(scale1)
                .offset(x = (-20).dp, y = 150.dp)
                .align(Alignment.CenterStart)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
        )
    }
}

@Composable
fun AnimatedLogo(
    startAnimation: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Card(
        modifier = modifier
            .scale(if (startAnimation) 1f else 0f)
            .then(
                if (startAnimation) Modifier
                    .scale(scale)
                    .rotate(rotation) else Modifier
            ),
        shape = CircleShape,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = "GlobusLens Logo",
                modifier = Modifier.fillMaxSize(),
                tint = Color(0xFF2196F3)
            )
        }
    }
}

@Composable
fun FeatureFlipCards() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FlipCard(
            icon = Icons.Default.QrCodeScanner,
            title = "Smart Scanner",
            description = "Scan any product label with your camera",
            delay = 0
        )

        Spacer(modifier = Modifier.height(12.dp))

        FlipCard(
            icon = Icons.Default.Translate,
            title = "Instant Translation",
            description = "Translate text to your preferred language",
            delay = 200
        )

        Spacer(modifier = Modifier.height(12.dp))

        FlipCard(
            icon = Icons.Default.Favorite,
            title = "Favorites",
            description = "Save your favorite products for quick access",
            delay = 400
        )

        Spacer(modifier = Modifier.height(12.dp))

        FlipCard(
            icon = Icons.Default.ShoppingCart,
            title = "Shopping List",
            description = "Create and manage your shopping lists",
            delay = 600
        )
    }
}

@Composable
fun FlipCard(
    icon: ImageVector,
    title: String,
    description: String,
    delay: Int
) {
    var visible by remember { mutableStateOf(false) }
    var isFlipped by remember { mutableStateOf(false) }
    var hover by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        delay(delay.toLong())
        visible = true

        // Start flip animation cycle
        while (true) {
            delay(3000)
            isFlipped = !isFlipped
        }
    }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 600)
    )

    val scale by animateFloatAsState(
        targetValue = if (hover) 1.05f else 1f,
        animationSpec = tween(durationMillis = 300)
    )

    val elevation by animateFloatAsState(
        targetValue = if (hover) 12f else 4f,
        animationSpec = tween(durationMillis = 300)
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(durationMillis = 500)
        ) + slideInHorizontally(
            initialOffsetX = { if (delay % 400 == 0) -it else it }
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .scale(scale)
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 12f * density
                },
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2196F3).copy(alpha = 0.95f)
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(elevation.dp)
        ) {
            if (rotation <= 90f || rotation >= 270f) {
                // Front of card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon with background
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Text
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = description,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            } else {
                // Back of card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✨ Tap to explore",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun GetStartedButton(
    navController: NavController
) {
    val infiniteTransition = rememberInfiniteTransition()

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Button(
        onClick = {
            navController.navigate("scanner") {
                popUpTo("landing") { inclusive = true }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(pulseScale),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(8.dp)
    ) {
        Text(
            text = "Get Started",
            color = Color(0xFF2196F3),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}