package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.GoldDark
import com.example.ui.theme.IndigoDark
import com.example.ui.theme.VioletDark
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun VLearnCinematicSplash(
    onComplete: () -> Unit
) {
    // Stage controller for sequence choreography
    // 0 = Initial black/ambient particles
    // 1 = Holographic grid & Neural assembly (0.8s)
    // 2 = Logo convergence & 3D tilt reveal (2.2s)
    // 3 = Tagline fade-in & Metallic shine sweep (3.8s)
    // 4 = Complete fade out (5.2s)
    var splashStage by remember { mutableStateOf(0) }

    // Sequential timing trigger
    LaunchedEffect(Unit) {
        delay(300)
        splashStage = 1
        delay(1200)
        splashStage = 2
        delay(1800)
        splashStage = 3
        delay(1900)
        splashStage = 4
        onComplete()
    }

    // --- Animation states driven by stages ---

    // 1. Ambient Background Light Pulsation
    val infiniteTransition = rememberInfiniteTransition(label = "SplashInfinite")
    val ambientPulse by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AmbientPulse"
    )

    // 2. Holographic grid sliding displacement
    val gridOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 180f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "GridScroll"
    )

    // 3. Logo Assemble Zoom & Rotation (Stage-driven)
    val logoScale by animateFloatAsState(
        targetValue = when (splashStage) {
            0 -> 0.05f
            1 -> 0.45f
            2 -> 1.05f
            3, 4 -> 1.0f
            else -> 1.0f
        },
        animationSpec = tween(1800, easing = EaseOutBack),
        label = "LogoScale"
    )

    val logoRotationX by animateFloatAsState(
        targetValue = when (splashStage) {
            0 -> 60f
            1 -> 35f
            2 -> 8f
            3, 4 -> 0f
            else -> 0f
        },
        animationSpec = tween(2200, easing = EaseOutCubic),
        label = "LogoRotX"
    )

    val logoRotationY by animateFloatAsState(
        targetValue = when (splashStage) {
            0 -> -45f
            1 -> -20f
            2 -> -5f
            3, 4 -> 0f
            else -> 0f
        },
        animationSpec = tween(2200, easing = EaseOutCubic),
        label = "LogoRotY"
    )

    val logoAlpha by animateFloatAsState(
        targetValue = when (splashStage) {
            0 -> 0f
            1 -> 0.25f
            2, 3 -> 1.0f
            4 -> 0f
            else -> 0f
        },
        animationSpec = tween(1200, easing = LinearEasing),
        label = "LogoAlpha"
    )

    // 4. Volumetric Light / Glow Ring Size
    val bloomScale by infiniteTransition.animateFloat(
        initialValue = 1.05f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BloomPulse"
    )

    // 5. Metallic sweep shimmer offset
    val shimmerOffset by animateFloatAsState(
        targetValue = if (splashStage >= 3) 500f else -500f,
        animationSpec = tween(1500, easing = EaseInOutQuart),
        label = "Shimmer"
    )

    // 6. Tagline and brand tagline animations
    val taglineAlpha by animateFloatAsState(
        targetValue = if (splashStage >= 3) 1f else 0f,
        animationSpec = tween(1200, easing = EaseInQuint),
        label = "TaglineAlpha"
    )

    val taglineSpacing by animateFloatAsState(
        targetValue = if (splashStage >= 3) 4.5f else 1.5f,
        animationSpec = tween(2500, easing = EaseOutQuad),
        label = "TaglineSpacing"
    )

    // 7. Grid transparency fading as logo completes
    val gridAlpha by animateFloatAsState(
        targetValue = when (splashStage) {
            0 -> 0.0f
            1 -> 0.35f
            2 -> 0.2f
            3 -> 0.1f
            4 -> 0.0f
            else -> 0f
        },
        animationSpec = tween(1500),
        label = "GridAlpha"
    )

    // Real-time drifting particles background
    val particles = remember {
        List(40) {
            SplashParticle(
                x = (200..1000).random().toFloat(),
                y = (400..1600).random().toFloat(),
                speed = (2..11).random() / 10f,
                radius = (2..6).random().toFloat(),
                alpha = (15..70).random() / 100f,
                angle = (0..360).random() * (Math.PI / 180).toFloat()
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030712)) // Pure cinema obsidian black
            .testTag("cinematic_splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // --- LAYER 1: Deep Volumetric Colored Glows (Backdrop) ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            IndigoDark.copy(alpha = 0.22f * ambientPulse),
                            VioletDark.copy(alpha = 0.18f * ambientPulse),
                            Color.Transparent
                        ),
                        center = Offset.Unspecified,
                        radius = 1200f
                    )
                )
        )

        // Bottom spotlight accent
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF7C3AED).copy(alpha = 0.15f * ambientPulse),
                            Color.Transparent
                        ),
                        radius = 800f
                    )
                )
        )

        // --- LAYER 2: 3D Holographic Perspective Grid Canvas ---
        if (gridAlpha > 0f) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(gridAlpha)
            ) {
                val w = size.width
                val h = size.height
                val centerY = h * 0.7f

                // Draw perspective lines meeting at virtual horizon
                val linesCount = 14
                for (i in 0..linesCount) {
                    val progress = i.toFloat() / linesCount
                    val startX = w * progress
                    // Apply a perspective squeeze toward a central vanishing point
                    val endX = w * 0.5f + (startX - w * 0.5f) * 0.08f
                    drawLine(
                        color = Color(0xFF4F46E5),
                        start = Offset(startX, h),
                        end = Offset(endX, centerY),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                    )
                }

                // Scrolling horizontal grid lines
                val horizCount = 10
                for (i in 0..horizCount) {
                    // Animating horizontal perspective steps
                    val baseFactor = i.toFloat() / horizCount
                    val animFactor = (baseFactor + (gridOffset / 180f)) % 1.0f

                    // Perspective curve: lines are farther apart at the bottom, closer at top
                    val curveY = centerY + (h - centerY) * (animFactor * animFactor)
                    val widthScale = 0.08f + (0.92f * animFactor * animFactor)
                    val leftX = w * 0.5f - (w * 0.5f) * widthScale
                    val rightX = w * 0.5f + (w * 0.5f) * widthScale

                    drawLine(
                        color = Color(0xFF4F46E5).copy(alpha = animFactor),
                        start = Offset(leftX, curveY),
                        end = Offset(rightX, curveY),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
        }

        // --- LAYER 3: Interactive Drifting Ambient Stars & Digital Particles ---
        val frameTime by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(12000, easing = LinearEasing)
            ),
            label = "DriftFrame"
        )

        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            particles.forEach { p ->
                // Apply subtle drifting displacement
                val driftX = p.x + cos(p.angle + frameTime * 0.01f) * p.speed * 80f
                val driftY = p.y - (frameTime * p.speed * 0.4f) % p.y

                drawCircle(
                    color = if (p.radius > 4f) VioletDark.copy(alpha = p.alpha) else Color.White.copy(alpha = p.alpha),
                    radius = p.radius,
                    center = Offset(driftX % size.width, driftY)
                )
            }
        }

        // --- LAYER 4: Neural Network Connection Lines Converging ---
        if (splashStage in 1..2) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.45f)
            ) {
                val cx = size.width / 2f
                val cy = size.height / 2f

                // Neon constellation points floating near the core logo center
                val nodeCount = 8
                val radius = 240f
                val points = List(nodeCount) { index ->
                    val angleDeg = (index * 360f / nodeCount) + (frameTime * 0.25f)
                    val angleRad = Math.toRadians(angleDeg.toDouble())
                    Offset(
                        x = cx + cos(angleRad).toFloat() * radius * (1.1f - (logoScale * 0.1f)),
                        y = cy + sin(angleRad).toFloat() * radius * (1.1f - (logoScale * 0.1f))
                    )
                }

                // Draw connections with glowing cyan and indigo
                val path = Path()
                points.forEachIndexed { idx, pt ->
                    drawCircle(
                        color = IndigoDark,
                        radius = 5.dp.toPx(),
                        center = pt
                    )
                    // Connect lines to neighboring nodes and the center
                    val nextPt = points[(idx + 1) % nodeCount]
                    drawLine(
                        color = Color(0xFF67E8F9), // Cyber Cyan
                        start = pt,
                        end = nextPt,
                        strokeWidth = 1.5.dp.toPx()
                    )

                    // Converging line towards absolute center
                    drawLine(
                        color = VioletDark.copy(alpha = 1f - logoScale),
                        start = pt,
                        end = Offset(cx, cy),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
        }

        // --- LAYER 5: The Official Branded Logo in 3D Space ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .graphicsLayer {
                    scaleX = logoScale
                    scaleY = logoScale
                    rotationX = logoRotationX
                    rotationY = logoRotationY
                    cameraDistance = 14f * density
                }
                .alpha(logoAlpha)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(240.dp)
                    .padding(8.dp)
            ) {
                // Volumetric Bloom Halo (glowing enlarged replica underneath)
                Image(
                    painter = painterResource(id = R.drawable.vlearn_logo),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(bloomScale)
                        .blur(24.dp)
                        .alpha(0.7f),
                    colorFilter = ColorFilter.tint(VioletDark)
                )

                // Secondary soft shadow background glow ring
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(0.95f)
                        .blur(8.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(IndigoDark, VioletDark)
                            )
                        )
                        .alpha(0.18f)
                )

                // The crisp, high-res VLEARN Brand Logo
                Image(
                    painter = painterResource(id = R.drawable.vlearn_logo),
                    contentDescription = "VLEARN Cinematic Emblem",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(32.dp))
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(Color.White.copy(alpha = 0.5f), Color.Transparent, VioletDark.copy(alpha = 0.6f))
                            ),
                            shape = RoundedCornerShape(32.dp)
                        )
                )

                // Elegant Diagonal Metallic Shimmer/Shine Sweep
                if (splashStage >= 2) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(32.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.05f),
                                        Color.White.copy(alpha = 0.65f), // Shimmer apex
                                        Color.White.copy(alpha = 0.05f),
                                        Color.Transparent
                                    ),
                                    start = Offset(shimmerOffset - 250f, shimmerOffset - 250f),
                                    end = Offset(shimmerOffset + 250f, shimmerOffset + 250f),
                                    tileMode = TileMode.Clamp
                                )
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Brand Title with electric blue glow typography
            Text(
                text = "VLEARN AI",
                fontWeight = FontWeight.Black,
                letterSpacing = 5.sp,
                fontSize = 28.sp,
                color = Color.White,
                modifier = Modifier.graphicsLayer {
                    shadowElevation = 8f
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Premium tagline "Learn • Play • Level Up"
            Text(
                text = "Learn  •  Play  •  Level Up",
                fontWeight = FontWeight.Bold,
                letterSpacing = taglineSpacing.sp,
                fontSize = 13.sp,
                color = GoldDark,
                modifier = Modifier
                    .alpha(taglineAlpha)
                    .graphicsLayer {
                        translationY = (1f - taglineAlpha) * 15f
                    }
            )
        }

        // Skip Button on the top right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 24.dp)
                .alpha(if (splashStage < 4) 0.8f else 0f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.07f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                    .clickable { onComplete() }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Skip",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.FastForward,
                    contentDescription = "Skip Intro",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

// Sparkle/Particle container class
private data class SplashParticle(
    val x: Float,
    val y: Float,
    val speed: Float,
    val radius: Float,
    val alpha: Float,
    val angle: Float
)
