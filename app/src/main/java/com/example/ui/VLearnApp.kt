package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.layout.Layout
import com.example.R
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VLearnAppContent(viewModel: VLearnViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val selectedCourse by viewModel.selectedCourse.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    var showRoleSwitcher by remember { mutableStateOf(false) }
    var showSplash by remember { mutableStateOf(true) }

    if (showSplash) {
        VLearnCinematicSplash(onComplete = { showSplash = false })
    } else {
        if (userProfile == null || !userProfile!!.isLoggedIn || !userProfile!!.isOnboarded) {
            VLearnAuthAndOnboardingContainer(
                viewModel = viewModel,
                onComplete = {
                    // Handled automatically by ViewModel StateFlow
                }
            )
        } else {
            val syncStatus by viewModel.cloudSyncStatus.collectAsStateWithLifecycle()
            val isSynced by viewModel.isSessionSynced.collectAsStateWithLifecycle()

            LaunchedEffect(isSynced) {
                if (!isSynced) {
                    viewModel.triggerStartupSync()
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.vlearn_logo),
                            contentDescription = "VLEARN Logo",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Column {
                            Text(
                                "VLEARN",
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 20.sp
                            )
                            userProfile?.let {
                                Text(
                                    it.schoolName,
                                    fontSize = 11.sp,
                                    color = SlateGrey,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Gamification stats in header
                    userProfile?.let { profile ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            // Streak count
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .clickable {
                                        viewModel.triggerDailyStreak()
                                    }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Whatshot,
                                    contentDescription = "Streak",
                                    tint = GoldDark,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "${profile.streak}d",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }

                            // Coins
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFFFB300).copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Paid,
                                    contentDescription = "Coins",
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "${profile.coins}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFFFFB300)
                                )
                            }

                            // Role Switcher Dropdown
                            Box {
                                Button(
                                    onClick = { showRoleSwitcher = true },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        contentColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier
                                        .height(32.dp)
                                        .testTag("role_switcher_button")
                                ) {
                                    Text(
                                        profile.role,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Switch Role",
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = showRoleSwitcher,
                                    onDismissRequest = { showRoleSwitcher = false }
                                ) {
                                    val roles = listOf("Student", "Teacher", "Parent", "Admin")
                                    roles.forEach { role ->
                                        DropdownMenuItem(
                                            text = { Text(role, fontWeight = FontWeight.Bold) },
                                            onClick = {
                                                viewModel.switchRole(role)
                                                showRoleSwitcher = false
                                                viewModel.navigateTo("dashboard")
                                            }
                                        )
                                    }
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                    DropdownMenuItem(
                                        text = { Text("Sign Out", fontWeight = FontWeight.Bold, color = CoralRed) },
                                        leadingIcon = { Icon(Icons.Default.Logout, contentDescription = "Sign Out", tint = CoralRed) },
                                        onClick = {
                                            viewModel.logoutUser()
                                            showRoleSwitcher = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                userProfile?.let { profile ->
                    NavigationBarItem(
                        selected = currentScreen == "dashboard",
                        onClick = {
                            viewModel.selectCourse(null)
                            viewModel.navigateTo("dashboard")
                        },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )

                    NavigationBarItem(
                        selected = currentScreen == "ai_tutor",
                        onClick = { viewModel.navigateTo("ai_tutor") },
                        icon = { Icon(Icons.Default.Psychology, contentDescription = "AI Tutor") },
                        label = { Text("AI Tutor", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )

                    NavigationBarItem(
                        selected = currentScreen == "courses",
                        onClick = { viewModel.navigateTo("courses") },
                        icon = { Icon(Icons.Default.Class, contentDescription = "Courses") },
                        label = { Text("Courses", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )

                    NavigationBarItem(
                        selected = currentScreen == "game_mode",
                        onClick = { viewModel.navigateTo("game_mode") },
                        icon = { Icon(Icons.Default.SportsEsports, contentDescription = "Battle") },
                        label = { Text("Battle", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )

                    NavigationBarItem(
                        selected = currentScreen == "productivity",
                        onClick = { viewModel.navigateTo("productivity") },
                        icon = { Icon(Icons.Default.EventNote, contentDescription = "Planner") },
                        label = { Text("Tools", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )

                    NavigationBarItem(
                        selected = currentScreen == "social",
                        onClick = { viewModel.navigateTo("social") },
                        icon = { Icon(Icons.Default.Forum, contentDescription = "Social") },
                        label = { Text("Social", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                    )

                    // Extra Role-based Navigation Item in Bottom Bar for immediate access
                    if (profile.role == "Teacher") {
                        NavigationBarItem(
                            selected = currentScreen == "teacher_panel",
                            onClick = { viewModel.navigateTo("teacher_panel") },
                            icon = { Icon(Icons.Default.AutoStories, contentDescription = "Teacher") },
                            label = { Text("Teacher Hub", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                    } else if (profile.role == "Parent") {
                        NavigationBarItem(
                            selected = currentScreen == "parent_panel",
                            onClick = { viewModel.navigateTo("parent_panel") },
                            icon = { Icon(Icons.Default.SupervisorAccount, contentDescription = "Parent") },
                            label = { Text("Parent Panel", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                    } else if (profile.role == "Admin") {
                        NavigationBarItem(
                            selected = currentScreen == "admin_panel",
                            onClick = { viewModel.navigateTo("admin_panel") },
                            icon = { Icon(Icons.Default.Shield, contentDescription = "Admin") },
                            label = { Text("Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    "dashboard" -> {
                        userProfile?.let { profile ->
                            when (profile.role) {
                                "Student" -> StudentDashboard(viewModel)
                                "Teacher" -> TeacherPanelScreen(viewModel)
                                "Parent" -> ParentPanelScreen(viewModel)
                                "Admin" -> AdminPanelScreen(viewModel)
                                else -> StudentDashboard(viewModel)
                            }
                        }
                    }
                    "ai_tutor" -> AiTutorScreen(viewModel)
                    "courses" -> {
                        if (selectedCourse != null) {
                            CourseDetailScreen(viewModel, selectedCourse!!)
                        } else {
                            CoursesScreen(viewModel)
                        }
                    }
                    "productivity" -> ProductivityScreen(viewModel)
                    "social" -> SocialScreen(viewModel)
                    "game_mode" -> BattleArenaScreen(viewModel)
                    "teacher_panel" -> TeacherPanelScreen(viewModel)
                    "parent_panel" -> ParentPanelScreen(viewModel)
                    "admin_panel" -> AdminPanelScreen(viewModel)
                    else -> StudentDashboard(viewModel)
                }
            }
        }
        }
    }
    syncStatus?.let { status ->
        CloudSyncProgressOverlay(statusMessage = status)
    }
}
}
}

// ==========================================
// STUDENT DASHBOARD
// ==========================================
// ==========================================
// GAMIFIED GAME LOBBY STUDENT DASHBOARD
// ==========================================

data class GameLobbyParticle(
    var x: Float,
    var y: Float,
    val speed: Float,
    val radius: Float,
    val maxAlpha: Float,
    var alpha: Float,
    var angle: Float,
    val color: Color
)

@Composable
fun MagicalParticleBackground(modifier: Modifier = Modifier) {
    var particles by remember {
        mutableStateOf(List(15) {
            GameLobbyParticle(
                x = (100..900).random().toFloat(),
                y = (100..1400).random().toFloat(),
                speed = (1..3).random().toFloat() * 0.4f,
                radius = (4..12).random().toFloat(),
                maxAlpha = (3..8).random().toFloat() / 10f,
                alpha = (3..8).random().toFloat() / 10f,
                angle = (0..360).random().toFloat(),
                color = when ((0..3).random()) {
                    0 -> Color(0xFFA855F7) // Purple
                    1 -> Color(0xFF6366F1) // Indigo
                    2 -> Color(0xFFFFD700) // Gold
                    else -> Color(0xFF00D2FF) // Sky Cyan
                }
            )
        })
    }

    val infiniteTransition = rememberInfiniteTransition(label = "lobby_particles")
    val timeStep by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "timestep"
    )

    LaunchedEffect(timeStep) {
        particles = particles.map { p ->
            val nextY = p.y - p.speed
            val nextAngle = p.angle + 0.05f
            val nextX = p.x + kotlin.math.sin(nextAngle.toDouble()).toFloat() * 0.6f
            
            p.copy(
                y = if (nextY < -30) 1500f else nextY,
                x = if (nextX < -30) 1000f else if (nextX > 1030) -30f else nextX,
                angle = nextAngle
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { p ->
            drawCircle(
                color = p.color,
                radius = p.radius,
                center = androidx.compose.ui.geometry.Offset(p.x, p.y),
                alpha = p.alpha
            )
        }
    }
}

@Composable
fun AnimatedHeroPose(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "lobby_hero")
    val verticalOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bobbing"
    )
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = modifier
            .size(180.dp)
            .offset(y = verticalOffset.dp),
        contentAlignment = Alignment.Center
    ) {
        // Glowing magic aura
        Box(
            modifier = Modifier
                .size(130.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFA855F7).copy(alpha = 0.3f * glowScale),
                            Color.Transparent
                        )
                    )
                )
        )

        Canvas(modifier = Modifier.size(130.dp)) {
            val w = size.width
            val h = size.height

            // 1. Wizard staff (Left side)
            drawLine(
                color = Color(0xFF6366F1),
                start = androidx.compose.ui.geometry.Offset(w * 0.25f, h * 0.85f),
                end = androidx.compose.ui.geometry.Offset(w * 0.25f, h * 0.15f),
                strokeWidth = 6f
            )
            // Staff light
            drawCircle(
                color = Color(0xFF00E5FF),
                radius = 12f,
                center = androidx.compose.ui.geometry.Offset(w * 0.25f, h * 0.15f)
            )
            drawCircle(
                color = Color.White,
                radius = 6f,
                center = androidx.compose.ui.geometry.Offset(w * 0.25f, h * 0.15f)
            )

            // 2. Scholar Robe (Indigo/Teal blend)
            val robePath = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * 0.35f, h * 0.85f)
                lineTo(w * 0.65f, h * 0.85f)
                lineTo(w * 0.60f, h * 0.45f)
                lineTo(w * 0.40f, h * 0.45f)
                close()
            }
            drawPath(path = robePath, color = Color(0xFF1E1B4B))

            // Gold tunic details
            drawLine(
                color = Color(0xFFFFD700),
                start = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.45f),
                end = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.85f),
                strokeWidth = 3f
            )

            // 3. Head & Face (warm tone)
            drawCircle(
                color = Color(0xFFFFE0B2),
                radius = 20f,
                center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.35f)
            )

            // Eyes of knowledge
            drawCircle(
                color = Color(0xFF00FFCC),
                radius = 3.5f,
                center = androidx.compose.ui.geometry.Offset(w * 0.45f, h * 0.33f)
            )
            drawCircle(
                color = Color(0xFF00FFCC),
                radius = 3.5f,
                center = androidx.compose.ui.geometry.Offset(w * 0.55f, h * 0.33f)
            )

            // Cute academic glasses
            drawCircle(
                color = Color.White,
                radius = 7f,
                center = androidx.compose.ui.geometry.Offset(w * 0.45f, h * 0.33f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
            )
            drawCircle(
                color = Color.White,
                radius = 7f,
                center = androidx.compose.ui.geometry.Offset(w * 0.55f, h * 0.33f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
            )
            drawLine(
                color = Color.White,
                start = androidx.compose.ui.geometry.Offset(w * 0.48f, h * 0.33f),
                end = androidx.compose.ui.geometry.Offset(w * 0.52f, h * 0.33f),
                strokeWidth = 2f
            )

            // 4. Wizard hat
            val hatPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * 0.35f, h * 0.28f)
                lineTo(w * 0.65f, h * 0.28f)
                lineTo(w * 0.50f, h * 0.05f)
                close()
            }
            drawPath(path = hatPath, color = Color(0xFF4C1D95))

            // Hat rim
            drawLine(
                color = Color(0xFF6D28D9),
                start = androidx.compose.ui.geometry.Offset(w * 0.32f, h * 0.28f),
                end = androidx.compose.ui.geometry.Offset(w * 0.68f, h * 0.28f),
                strokeWidth = 5f
            )

            // Golden star on hat
            drawCircle(
                color = Color(0xFFFFD700),
                radius = 4f,
                center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.18f)
            )

            // 5. Hand holding glowing pink tome of science
            drawCircle(
                color = Color(0xFFFFE0B2),
                radius = 7f,
                center = androidx.compose.ui.geometry.Offset(w * 0.65f, h * 0.55f)
            )

            drawRoundRect(
                color = Color(0xFFDB2777),
                topLeft = androidx.compose.ui.geometry.Offset(w * 0.63f, h * 0.52f),
                size = androidx.compose.ui.geometry.Size(26f, 20f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
            )
            drawRoundRect(
                color = Color.White,
                topLeft = androidx.compose.ui.geometry.Offset(w * 0.65f, h * 0.54f),
                size = androidx.compose.ui.geometry.Size(22f, 16f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
            )
        }
    }
}

@Composable
fun StudentDashboard(viewModel: VLearnViewModel) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val enrolledCourses by viewModel.enrolledCourses.collectAsStateWithLifecycle()
    val allCourses by viewModel.allCourses.collectAsStateWithLifecycle()
    val studyPlans by viewModel.allStudyPlans.collectAsStateWithLifecycle()

    var lobbySubScreen by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("lobby") }
    val scope = rememberCoroutineScope()

    // Audio & Settings Control
    var isMusicOn by remember { mutableStateOf(false) }
    var isSfxOn by remember { mutableStateOf(true) }

    fun playLobbySfx(tone: Int) {
        if (!isSfxOn) return
        try {
            val toneGen = android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 60)
            toneGen.startTone(tone, 100)
        } catch (e: Exception) {}
    }

    // Music loop
    LaunchedEffect(isMusicOn) {
        if (isMusicOn) {
            try {
                val toneGen = android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 40)
                while (isMusicOn) {
                    toneGen.startTone(android.media.ToneGenerator.TONE_PROP_BEEP, 100)
                    delay(450)
                    toneGen.startTone(android.media.ToneGenerator.TONE_PROP_BEEP2, 120)
                    delay(450)
                    toneGen.startTone(android.media.ToneGenerator.TONE_CDMA_PIP, 100)
                    delay(900)
                }
            } catch (e: Exception) {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // High-contrast game Slate Blue background
    ) {
        // Floating magical particles in the background
        MagicalParticleBackground()

        AnimatedContent(
            targetState = lobbySubScreen,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            },
            label = "lobby_sub_screens"
        ) { subScreen ->
            when (subScreen) {
                "lobby" -> {
                    userProfile?.let { profile ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // --- TOP NAVIGATION ---
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                border = BorderStroke(1.dp, Color(0xFF334155))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        // Player Avatar & Bio
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(52.dp)
                                                    .clip(CircleShape)
                                                    .background(Brush.linearGradient(listOf(IndigoDark, VioletDark))),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    profile.name.take(2).uppercase(),
                                                    fontWeight = FontWeight.Black,
                                                    color = Color.White,
                                                    fontSize = 18.sp
                                                )
                                            }
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        profile.name,
                                                        fontWeight = FontWeight.Black,
                                                        color = Color.White,
                                                        fontSize = 17.sp
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(Color(0xFFA855F7).copy(alpha = 0.2f))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            "LVL ${profile.level}",
                                                            color = Color(0xFFC084FC),
                                                            fontWeight = FontWeight.Black,
                                                            fontSize = 11.sp
                                                        )
                                                    }
                                                }
                                                Text(
                                                    "★★★★★ Elite Scholar",
                                                    color = Color(0xFFFFD700),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        // Audio Controllers
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    isMusicOn = !isMusicOn
                                                    playLobbySfx(android.media.ToneGenerator.TONE_PROP_BEEP)
                                                },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isMusicOn) Icons.Default.MusicNote else Icons.Default.MusicOff,
                                                    contentDescription = "Toggle Music",
                                                    tint = if (isMusicOn) Color(0xFF38BDF8) else Color.Gray,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    isSfxOn = !isSfxOn
                                                    if (isSfxOn) {
                                                        try {
                                                            val toneGen = android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 60)
                                                            toneGen.startTone(android.media.ToneGenerator.TONE_PROP_BEEP, 100)
                                                        } catch (e: Exception) {}
                                                    }
                                                },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isSfxOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                                    contentDescription = "Toggle SFX",
                                                    tint = if (isSfxOn) Color(0xFF34D399) else Color.Gray,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Resources indicators: Gems, Coins, Energy
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Gems
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFF22D3EE).copy(alpha = 0.12f))
                                                .border(1.dp, Color(0xFF22D3EE).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                                .clickable {
                                                    playLobbySfx(android.media.ToneGenerator.TONE_PROP_BEEP)
                                                    viewModel.awardPlayer(0, 50, 0)
                                                }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text("💎", fontSize = 14.sp)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                String.format("%,d", profile.gems),
                                                fontWeight = FontWeight.Black,
                                                fontSize = 13.sp,
                                                color = Color(0xFF22D3EE)
                                            )
                                        }

                                        // Coins
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFFFBBF24).copy(alpha = 0.12f))
                                                .border(1.dp, Color(0xFFFBBF24).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                                .clickable {
                                                    playLobbySfx(android.media.ToneGenerator.TONE_PROP_BEEP)
                                                    viewModel.awardPlayer(500, 0, 0)
                                                }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text("🪙", fontSize = 14.sp)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                String.format("%,d", profile.coins),
                                                fontWeight = FontWeight.Black,
                                                fontSize = 13.sp,
                                                color = Color(0xFFFBBF24)
                                            )
                                        }

                                        // Energy
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFFF87171).copy(alpha = 0.12f))
                                                .border(1.dp, Color(0xFFF87171).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                                .clickable {
                                                    playLobbySfx(android.media.ToneGenerator.TONE_PROP_BEEP)
                                                    // Fully recover energy
                                                    playLobbySfx(android.media.ToneGenerator.TONE_PROP_BEEP2)
                                                }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text("⚡", fontSize = 14.sp)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                "98/100",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 13.sp,
                                                color = Color(0xFFF87171)
                                            )
                                        }
                                    }
                                }
                            }

                            // --- HERO BANNER & MAIN ADVENTURE ---
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                                border = BorderStroke(1.5.dp, Color(0xFFC084FC).copy(alpha = 0.5f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "CURRENT ADVENTURE",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp,
                                        letterSpacing = 1.5.sp,
                                        color = Color(0xFFA855F7)
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Centered bobbing vector hero
                                    AnimatedHeroPose()

                                    // Continue Adventure detail panel
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.8f))
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(
                                                        "ACTIVE WORLD",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.Gray,
                                                        letterSpacing = 1.sp
                                                    )
                                                    Text(
                                                        "Quantum Kingdom",
                                                        fontWeight = FontWeight.Black,
                                                        fontSize = 16.sp,
                                                        color = Color.White
                                                    )
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color(0xFFE11D48).copy(alpha = 0.2f))
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        "BOSS FIGHT",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = Color(0xFFFB7185)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Text(
                                                "Current Quest: Defeat the Gravity Titan",
                                                color = Color(0xFFE2E8F0),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    "World Mastery Progress",
                                                    fontSize = 11.sp,
                                                    color = Color.Gray,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    "73%",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFFC084FC),
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            LinearProgressIndicator(
                                                progress = { 0.73f },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(8.dp)
                                                    .clip(RoundedCornerShape(4.dp)),
                                                color = Color(0xFFA855F7),
                                                trackColor = Color(0xFF334155)
                                            )

                                            Spacer(modifier = Modifier.height(12.dp))

                                            Button(
                                                onClick = {
                                                    playLobbySfx(android.media.ToneGenerator.TONE_PROP_BEEP2)
                                                    viewModel.navigateTo("game_mode")
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(48.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.PlayArrow,
                                                        contentDescription = "Continue",
                                                        tint = Color(0xFF0F172A)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        "▶ CONTINUE ADVENTURE",
                                                        fontWeight = FontWeight.Black,
                                                        color = Color(0xFF0F172A),
                                                        fontSize = 13.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Text(
                                "STUDENT COMMAND DECK",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Gray,
                                letterSpacing = 1.5.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )

                            // --- GRID OF CARDS (12 EPIC DECKS) ---
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        LobbyActionCard(
                                            title = "📚 Upload Material",
                                            subtitle = "AI creates adventure from PDFs/DOCX!",
                                            accentColor = Color(0xFF38BDF8),
                                            icon = Icons.Default.CloudUpload,
                                            onClick = {
                                                playLobbySfx(android.media.ToneGenerator.TONE_PROP_BEEP)
                                                lobbySubScreen = "upload"
                                            }
                                        )
                                    }
                                    Box(modifier = Modifier.weight(1f)) {
                                        LobbyActionCard(
                                            title = "🌍 My AI Worlds",
                                            subtitle = "6 dynamic customized worlds",
                                            accentColor = Color(0xFF4ADE80),
                                            icon = Icons.Default.Language,
                                            onClick = {
                                                playLobbySfx(android.media.ToneGenerator.TONE_PROP_BEEP)
                                                lobbySubScreen = "worlds"
                                            }
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        LobbyActionCard(
                                            title = "⚔ Battle Arena",
                                            subtitle = "Boss battles & tournament arenas",
                                            accentColor = Color(0xFFF87171),
                                            icon = Icons.Default.SportsEsports,
                                            onClick = {
                                                playLobbySfx(android.media.ToneGenerator.TONE_PROP_BEEP)
                                                viewModel.navigateTo("game_mode")
                                            }
                                        )
                                    }
                                    Box(modifier = Modifier.weight(1f)) {
                                        LobbyActionCard(
                                            title = "🎒 Inventory",
                                            subtitle = "Equip & unlock legendary weapons",
                                            accentColor = Color(0xFFFBBF24),
                                            icon = Icons.Default.Backpack,
                                            onClick = {
                                                playLobbySfx(android.media.ToneGenerator.TONE_PROP_BEEP)
                                                lobbySubScreen = "inventory"
                                            }
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        LobbyActionCard(
                                            title = "👤 Heroes",
                                            subtitle = "Upgrade skills & talent trees",
                                            accentColor = Color(0xFFC084FC),
                                            icon = Icons.Default.Person,
                                            onClick = {
                                                playLobbySfx(android.media.ToneGenerator.TONE_PROP_BEEP)
                                                lobbySubScreen = "heroes"
                                            }
                                        )
                                    }
                                    Box(modifier = Modifier.weight(1f)) {
                                        LobbyActionCard(
                                            title = "🤖 AI Tutor",
                                            subtitle = "Ask anything, explain summaries",
                                            accentColor = Color(0xFFF472B6),
                                            icon = Icons.Default.Psychology,
                                            onClick = {
                                                playLobbySfx(android.media.ToneGenerator.TONE_PROP_BEEP)
                                                viewModel.navigateTo("ai_tutor")
                                            }
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        LobbyActionCard(
                                            title = "🧠 Learning Paths",
                                            subtitle = "Syllabus, progress map, concepts",
                                            accentColor = Color(0xFF2DD4BF),
                                            icon = Icons.Default.AutoStories,
                                            onClick = {
                                                playLobbySfx(android.media.ToneGenerator.TONE_PROP_BEEP)
                                                lobbySubScreen = "journey"
                                            }
                                        )
                                    }
                                    Box(modifier = Modifier.weight(1f)) {
                                        LobbyActionCard(
                                            title = "🎯 Missions",
                                            subtitle = "Daily challenges & event rewards",
                                            accentColor = Color(0xFFFB923C),
                                            icon = Icons.Default.FilterCenterFocus,
                                            onClick = {
                                                playLobbySfx(android.media.ToneGenerator.TONE_PROP_BEEP)
                                                lobbySubScreen = "missions"
                                            }
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        LobbyActionCard(
                                            title = "🏆 Leaderboards",
                                            subtitle = "School, city, and global standings",
                                            accentColor = Color(0xFFFFD700),
                                            icon = Icons.Default.Leaderboard,
                                            onClick = {
                                                playLobbySfx(android.media.ToneGenerator.TONE_PROP_BEEP)
                                                lobbySubScreen = "leaderboard"
                                            }
                                        )
                                    }
                                    Box(modifier = Modifier.weight(1f)) {
                                        LobbyActionCard(
                                            title = "👥 Friends Guild",
                                            subtitle = "Form study alliances & chat live",
                                            accentColor = Color(0xFF60A5FA),
                                            icon = Icons.Default.Groups,
                                            onClick = {
                                                playLobbySfx(android.media.ToneGenerator.TONE_PROP_BEEP)
                                                lobbySubScreen = "friends"
                                            }
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        LobbyActionCard(
                                            title = "🎁 Lucky Spin",
                                            subtitle = "Spin wheel for coins & gems",
                                            accentColor = Color(0xFFE879F9),
                                            icon = Icons.Default.Stars,
                                            onClick = {
                                                playLobbySfx(android.media.ToneGenerator.TONE_PROP_BEEP)
                                                lobbySubScreen = "rewards"
                                            }
                                        )
                                    }
                                    Box(modifier = Modifier.weight(1f)) {
                                        LobbyActionCard(
                                            title = "🛒 Store",
                                            subtitle = "Epic visual cosmetic skins",
                                            accentColor = Color(0xFFFF70A6),
                                            icon = Icons.Default.ShoppingCart,
                                            onClick = {
                                                playLobbySfx(android.media.ToneGenerator.TONE_PROP_BEEP)
                                                lobbySubScreen = "store"
                                            }
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        LobbyActionCard(
                                            title = "🎨 Skins & Armory",
                                            subtitle = "Premium visual custom cosmetics",
                                            accentColor = Color(0xFF10B981),
                                            icon = Icons.Default.Palette,
                                            onClick = {
                                                playLobbySfx(android.media.ToneGenerator.TONE_PROP_BEEP)
                                                lobbySubScreen = "premium_customization"
                                            }
                                        )
                                    }
                                    Box(modifier = Modifier.weight(1f)) {
                                        LobbyActionCard(
                                            title = "⚙️ Game Settings",
                                            subtitle = "Control presets & graphics",
                                            accentColor = Color(0xFF38BDF8),
                                            icon = Icons.Default.Tune,
                                            onClick = {
                                                playLobbySfx(android.media.ToneGenerator.TONE_PROP_BEEP)
                                                lobbySubScreen = "premium_settings"
                                            }
                                        )
                                    }
                                }
                            }

                            // --- FOOTER PROFILE SUMMARY ---
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        playLobbySfx(android.media.ToneGenerator.TONE_PROP_BEEP)
                                        lobbySubScreen = "profile"
                                    },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                border = BorderStroke(1.dp, Color(0xFF334155))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.BarChart,
                                            contentDescription = "Stats",
                                            tint = Color(0xFF38BDF8)
                                        )
                                        Column {
                                            Text(
                                                "Syllabus Analytics",
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                "View study time, mastery levels & certificates",
                                                color = Color.Gray,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = "Open Stats",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // SUB-SCREEN 1: UPLOAD & AI WORLD GENERATOR
                // ==========================================
                "upload" -> {
                    val context = LocalContext.current
                    val genState by viewModel.pdfGenerationState.collectAsStateWithLifecycle()
                    val realms by viewModel.battleRealms.collectAsStateWithLifecycle()
                    val activeTopic by viewModel.selectedBattleTopic.collectAsStateWithLifecycle()
                    val generatedRealm = remember(realms, activeTopic) {
                        realms.firstOrNull { it.name == activeTopic }
                    }

                    val pdfLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                    ) { uri ->
                        if (uri != null) {
                            try {
                                val inputStream = context.contentResolver.openInputStream(uri)
                                val bytes = inputStream?.readBytes()
                                if (bytes != null) {
                                    var fileName = "syllabus.pdf"
                                    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                                        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                                        if (nameIndex != -1 && cursor.moveToFirst()) {
                                            fileName = cursor.getString(nameIndex)
                                        }
                                    }
                                    viewModel.generateWorldFromPdf(fileName, bytes)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LobbySubHeader(title = "📚 AI RPG World Builder", onBack = { viewModel.resetPdfGenerationState(); lobbySubScreen = "lobby" })

                        if (genState == null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                border = BorderStroke(1.5.dp, Color(0xFF38BDF8))
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF38BDF8).copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudUpload,
                                            contentDescription = "Upload",
                                            tint = Color(0xFF38BDF8),
                                            modifier = Modifier.size(40.dp)
                                        )
                                    }

                                    Text(
                                        "SELECT & UPLOAD STUDY PDF",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        color = Color.White
                                    )

                                    Text(
                                        "Upload any textbook chapter, study guide, worksheet, or lecture notes. VLEARN AI automatically extracts the contents, recognizes the key lessons, and synthesizes an entirely customized 2D adventure RPG with interactive NPCs, storyline quests, themed enemies, a final Boss, and dynamic barrier puzzles centered strictly on your material!",
                                        fontSize = 13.sp,
                                        color = Color.LightGray,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 20.sp
                                    )

                                    Button(
                                        onClick = {
                                            playLobbySfx(android.media.ToneGenerator.TONE_PROP_BEEP)
                                            pdfLauncher.launch("application/pdf")
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().height(50.dp)
                                    ) {
                                        Text("📂 BROWSE LOCAL PDF", fontWeight = FontWeight.Black, color = Color.Black)
                                    }
                                }
                            }

                            Text("ENGINE HIGHLIGHTS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray, letterSpacing = 1.sp)

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                    border = BorderStroke(1.dp, Color(0xFF334155))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.AutoAwesome, "AI", tint = Color(0xFFFFB300), modifier = Modifier.size(24.dp))
                                        Text("Zero Presets", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                        Text("Every uploaded document spins up a completely different thematic visual realm.", color = Color.LightGray, fontSize = 11.sp)
                                    }
                                }
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                    border = BorderStroke(1.dp, Color(0xFF334155))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.MilitaryTech, "Boss", tint = Color(0xFFF43F5E), modifier = Modifier.size(24.dp))
                                        Text("Boss Mechanics", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                        Text("Combats adapt to teach concepts naturally without random annoying popups.", color = Color.LightGray, fontSize = 11.sp)
                                    }
                                }
                            }
                        } else if (genState == "success") {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                                border = BorderStroke(2.dp, Color(0xFF10B981))
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Success",
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(44.dp)
                                        )
                                    }

                                    Text(
                                        "RPG SYNTHESIS COMPLETE!",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 20.sp,
                                        color = Color(0xFF10B981)
                                    )

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = generatedRealm?.worldName ?: "The Synthesis Dimension",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 22.sp,
                                            color = Color.White,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "Subject: ${generatedRealm?.name ?: "Extracted Lessons"}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF38BDF8)
                                        )
                                    }

                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2937)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Campaign, "Story", tint = Color(0xFFA855F7), modifier = Modifier.size(18.dp))
                                                Text("WORLD LORE & MISSION", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                                            }
                                            Text(
                                                text = generatedRealm?.description ?: "Study and defeat obstacles to clean the area.",
                                                color = Color.LightGray,
                                                fontSize = 12.sp,
                                                lineHeight = 18.sp
                                            )
                                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.SupportAgent, "Tutor", tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
                                                Text("WORLD GUARDIAN BOSS", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                                            }
                                            Text(
                                                text = generatedRealm?.bossName ?: "Concept Overlord",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = {
                                            playLobbySfx(android.media.ToneGenerator.TONE_PROP_BEEP2)
                                            viewModel.resetPdfGenerationState()
                                            viewModel.navigateTo("game_mode")
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth().height(56.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(Icons.Default.PlayArrow, "Play", tint = Color.Black, modifier = Modifier.size(24.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("⚔️ START GENERATED GAME", fontWeight = FontWeight.Black, color = Color.Black, fontSize = 15.sp)
                                        }
                                    }
                                }
                            }
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                                border = BorderStroke(1.5.dp, Color(0xFFA855F7))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(90.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFA855F7).copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            color = Color(0xFFA855F7),
                                            strokeWidth = 5.dp,
                                            modifier = Modifier.size(72.dp)
                                        )
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "Generating",
                                            tint = Color(0xFFFFD700),
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))

                                    Text(
                                        text = genState ?: "AI ENGINE WORKING...",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        color = Color.White,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        val isParsing = genState?.contains("Reading") == true || genState?.contains("Extracting") == true
                                        val isAnalyzing = genState?.contains("analyzing") == true
                                        val isDesigning = genState?.contains("designing") == true
                                        val isCrafting = genState?.contains("crafting") == true
                                        val isSyncing = genState?.contains("synchronization") == true

                                        GeneratorCheckItem(label = "Extracting raw text characters & tables", active = !isParsing)
                                        GeneratorCheckItem(label = "Cognitive mapping & semantic keyword clustering", active = !isParsing && !isAnalyzing)
                                        GeneratorCheckItem(label = "Story scripting & adaptive NPC dialog synthesis", active = !isParsing && !isAnalyzing && !isDesigning)
                                        GeneratorCheckItem(label = "Enemies, Barrier Quizzes, & Boss mechanic generation", active = isSyncing || genState?.contains("error") == true)
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // SUB-SCREEN 2: MY AI WORLDS
                // ==========================================
                "worlds" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LobbySubHeader(title = "🌍 My AI Worlds", onBack = { lobbySubScreen = "lobby" })

                        Text(
                            "GENERATED ACADEMIC EXPANSIONS",
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.2.sp
                        )

                        // Default worlds + user created ones
                        val worldsToShow = listOf(
                            Pair("Physics Kingdom", "Science"),
                            Pair("Chemistry Planet", "Science"),
                            Pair("Biology Forest", "Science"),
                            Pair("History Empire", "Social Science"),
                            Pair("Programming Cyber City", "Programming"),
                            Pair("Mathematics Dungeon", "Mathematics")
                        )

                        worldsToShow.forEach { (wName, subject) ->
                            val matchedDbCourse = allCourses.firstOrNull { it.title.contains(wName) || wName.contains(it.title) }
                            val isEnrolled = matchedDbCourse?.isEnrolled ?: true
                            val progress = matchedDbCourse?.progressPercent ?: 45

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                border = BorderStroke(1.dp, Color(0xFF334155))
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                subject.uppercase(),
                                                color = Color(0xFF38BDF8),
                                                fontWeight = FontWeight.Black,
                                                fontSize = 9.sp,
                                                letterSpacing = 1.sp
                                            )
                                            Text(
                                                wName,
                                                color = Color.White,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 17.sp
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF34D399).copy(alpha = 0.15f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                "Active",
                                                color = Color(0xFF34D399),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Boss: Concept Leviathan", color = Color.LightGray, fontSize = 12.sp)
                                        Text("Progress: $progress%", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    LinearProgressIndicator(
                                        progress = { progress / 100f },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = Color(0xFF10B981),
                                        trackColor = Color(0xFF334155)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                playLobbySfx(android.media.ToneGenerator.TONE_PROP_BEEP)
                                                // Preload topic
                                                viewModel.setBattleTopic(if (wName.contains("Physics")) "Physics" else "Science")
                                                viewModel.navigateTo("game_mode")
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, Color(0xFF475569))
                                        ) {
                                            Text("⚔️ BATTLE ARENA", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                        }

                                        Button(
                                            onClick = {
                                                playLobbySfx(android.media.ToneGenerator.TONE_PROP_BEEP2)
                                                viewModel.navigateTo("ai_tutor")
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("🤖 AI TUTOR SAGE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // SUB-SCREEN 3:🎒 INVENTORY SYSTEM
                // ==========================================
                "inventory" -> {
                    val activeWeapon by viewModel.selectedWeapon.collectAsStateWithLifecycle()
                    val unlockedWeapons by viewModel.unlockedWeapons.collectAsStateWithLifecycle()

                    val itemsDb = listOf(
                        Triple("Beginner Blaster", "Basic energy shooter. Standard academy gear.", 0),
                        Triple("Plasma Rifle", "Fires rapid charged ions. High DPS.", 1200),
                        Triple("Vortex Carbine", "Generates mini stellar blackholes.", 3500),
                        Triple("Celestial Wand", "Harnesses cosmic math equations. +50% Magic.", 5000),
                        Triple("Cosmic Greatsword", "Excalibur class logic blade.", 8000)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LobbySubHeader(title = "🎒 Scholar Inventory", onBack = { lobbySubScreen = "lobby" })

                        Text(
                            "WEAPON ARMORY",
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.2.sp
                        )

                        itemsDb.forEach { (wName, desc, cost) ->
                            val isUnlocked = unlockedWeapons.contains(wName)
                            val isEquipped = activeWeapon == wName

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                border = BorderStroke(1.5.dp, if (isEquipped) Color(0xFFFFB300) else Color(0xFF334155))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isEquipped) Color(0xFFFFB300).copy(alpha = 0.2f) else Color(0xFF0F172A)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            if (wName.contains("Blaster")) "🔫" else if (wName.contains("Rifle")) "⚡" else if (wName.contains("Wand")) "🪄" else "⚔️",
                                            fontSize = 24.sp
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            wName,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            desc,
                                            color = Color.LightGray,
                                            fontSize = 11.sp
                                        )
                                    }

                                    // Button Action
                                    userProfile?.let { profile ->
                                        if (isEquipped) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFFFFB300).copy(alpha = 0.2f))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text("Equipped", color = Color(0xFFFFB300), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        } else if (isUnlocked) {
                                            Button(
                                                onClick = {
                                                    playLobbySfx(android.media.ToneGenerator.TONE_PROP_BEEP2)
                                                    viewModel.selectWeapon(wName)
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Text("Equip", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        } else {
                                            Button(
                                                onClick = {
                                                    if (profile.coins >= cost) {
                                                        playLobbySfx(android.media.ToneGenerator.TONE_PROP_BEEP2)
                                                        viewModel.buyWeapon(wName, cost)
                                                    } else {
                                                        playLobbySfx(android.media.ToneGenerator.TONE_CDMA_PIP)
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFBBF24)),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text("🪙", fontSize = 11.sp)
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text("$cost", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // SUB-SCREEN 4: 👤 HERO CLASSES & TALENTS
                // ==========================================
                "heroes" -> {
                    val activeHeroClass by viewModel.selectedHeroClass.collectAsStateWithLifecycle()

                    val heroClasses = listOf(
                        Triple("Warrior", "High defense and explosive critical physical power.", Icons.Default.Shield),
                        Triple("Mage", "Infinite wisdom, mana, and academic spelling focus.", Icons.Default.AutoAwesome),
                        Triple("Tech Scholar", "Deploys companion AI drones and laser arrays.", Icons.Default.Memory)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LobbySubHeader(title = "👤 Academy Heroes", onBack = { lobbySubScreen = "lobby" })

                        Text(
                            "CHOOSE ACTIVE HERO CLASS",
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.2.sp
                        )

                        heroClasses.forEach { (hClass, desc, icon) ->
                            val isSelected = activeHeroClass == hClass

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                border = BorderStroke(2.dp, if (isSelected) Color(0xFFA855F7) else Color.Transparent)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Icon(imageVector = icon, contentDescription = "Hero", tint = if (isSelected) Color(0xFFA855F7) else Color.Gray)
                                            Text(hClass, fontWeight = FontWeight.Black, color = Color.White, fontSize = 16.sp)
                                        }
                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFFA855F7).copy(alpha = 0.2f))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text("ACTIVE", color = Color(0xFFC084FC), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        } else {
                                            Button(
                                                onClick = {
                                                    playLobbySfx(android.media.ToneGenerator.TONE_PROP_BEEP2)
                                                    viewModel.selectHeroClass(hClass)
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("Select", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    Text(desc, color = Color.LightGray, fontSize = 12.sp)
                                }
                            }
                        }

                        // TALENT TREE SPECS
                        Text(
                            "HERO TALENT UPGRADES (SPEND GEMS)",
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.2.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                            border = BorderStroke(1.dp, Color(0xFF4C1D95))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                TalentItemRow(title = "Intellectual Capacity (+20% Mana)", gemsCost = 15, onUpgrade = {
                                    userProfile?.let { p ->
                                        if (p.gems >= 15) {
                                            viewModel.updateProfile(p.copy(gems = p.gems - 15))
                                            playLobbySfx(android.media.ToneGenerator.TONE_PROP_BEEP2)
                                        } else {
                                            playLobbySfx(android.media.ToneGenerator.TONE_CDMA_PIP)
                                        }
                                    }
                                })
                                HorizontalDivider(color = Color(0xFF334155))
                                TalentItemRow(title = "Scientific Shielding (+15% HP)", gemsCost = 25, onUpgrade = {
                                    userProfile?.let { p ->
                                        if (p.gems >= 25) {
                                            viewModel.updateProfile(p.copy(gems = p.gems - 25))
                                            playLobbySfx(android.media.ToneGenerator.TONE_PROP_BEEP2)
                                        } else {
                                            playLobbySfx(android.media.ToneGenerator.TONE_CDMA_PIP)
                                        }
                                    }
                                })
                                HorizontalDivider(color = Color(0xFF334155))
                                TalentItemRow(title = "Derivative Speed Boost (+10% Crit)", gemsCost = 35, onUpgrade = {
                                    userProfile?.let { p ->
                                        if (p.gems >= 35) {
                                            viewModel.updateProfile(p.copy(gems = p.gems - 35))
                                            playLobbySfx(android.media.ToneGenerator.TONE_PROP_BEEP2)
                                        } else {
                                            playLobbySfx(android.media.ToneGenerator.TONE_CDMA_PIP)
                                        }
                                    }
                                })
                            }
                        }
                    }
                }

                // ==========================================
                // SUB-SCREEN 5: 🧠 MY LEARNING JOURNEY
                // ==========================================
                "journey" -> {
                    val nodes = listOf(
                        Pair("Node 1: Classical Mechanics", "Completed"),
                        Pair("Node 2: Thermodynamics Principles", "Completed"),
                        Pair("Node 3: Quantum Foundations", "Active"),
                        Pair("Node 4: Relativity Theory", "Locked"),
                        Pair("Node 5: Nuclear Forces", "Locked")
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LobbySubHeader(title = "🧠 Learning Map", onBack = { lobbySubScreen = "lobby" })

                        Text(
                            "SEQUENTIAL LESSON PROGRESSION MAP",
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.2.sp
                        )

                        nodes.forEachIndexed { idx, (title, status) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Draw simple vertical timeline connector
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.width(40.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (status) {
                                                    "Completed" -> Color(0xFF34D399)
                                                    "Active" -> Color(0xFFFFB300)
                                                    else -> Color(0xFF475569)
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("${idx + 1}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                    if (idx < nodes.size - 1) {
                                        Box(
                                            modifier = Modifier
                                                .width(3.dp)
                                                .height(50.dp)
                                                .background(Color(0xFF334155))
                                        )
                                    }
                                }

                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            if (status != "Locked") {
                                                playLobbySfx(android.media.ToneGenerator.TONE_PROP_BEEP2)
                                                // Take mock lesson quiz
                                                viewModel.awardPlayer(50, 0, 100)
                                            } else {
                                                playLobbySfx(android.media.ToneGenerator.TONE_CDMA_PIP)
                                            }
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("Status: $status", color = Color.LightGray, fontSize = 11.sp)
                                        }
                                        if (status != "Locked") {
                                            Icon(Icons.Default.ChevronRight, "Play", tint = Color.White)
                                        } else {
                                            Icon(Icons.Default.Lock, "Locked", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                // ==========================================
                // SUB-SCREEN 6: 🎯 MISSIONS LOGS
                // ==========================================
                "missions" -> {
                    var studyClaimed by remember { mutableStateOf(false) }
                    var bossClaimed by remember { mutableStateOf(false) }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LobbySubHeader(title = "🎯 Active Missions", onBack = { lobbySubScreen = "lobby" })

                        Text(
                            "DAILY ACADEMIC BOUNTIES",
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.2.sp
                        )

                        // Mission 1
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Ask Tutor 3 Questions Today", fontWeight = FontWeight.Black, color = Color.White, fontSize = 15.sp)
                                Text("Reward: 🪙 100, 💎 10", color = Color(0xFFFBBF24), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Progress: 3 / 3 completed", color = Color.LightGray, fontSize = 12.sp)
                                    Button(
                                        onClick = {
                                            if (!studyClaimed) {
                                                studyClaimed = true
                                                viewModel.awardPlayer(100, 10, 50)
                                                playLobbySfx(android.media.ToneGenerator.TONE_PROP_BEEP2)
                                            }
                                        },
                                        enabled = !studyClaimed,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                                    ) {
                                        Text(if (studyClaimed) "Claimed" else "Claim Reward", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Mission 2
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Defeat Quantum Gravity Titan Boss", fontWeight = FontWeight.Black, color = Color.White, fontSize = 15.sp)
                                Text("Reward: 🪙 500, 💎 50", color = Color(0xFFFBBF24), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Progress: 1 / 1 completed", color = Color.LightGray, fontSize = 12.sp)
                                    Button(
                                        onClick = {
                                            if (!bossClaimed) {
                                                bossClaimed = true
                                                viewModel.awardPlayer(500, 50, 200)
                                                playLobbySfx(android.media.ToneGenerator.TONE_PROP_BEEP2)
                                            }
                                        },
                                        enabled = !bossClaimed,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                                    ) {
                                        Text(if (bossClaimed) "Claimed" else "Claim Reward", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // SUB-SCREEN 7: 🏆 LEADERBOARD TABLES
                // ==========================================
                "leaderboard" -> {
                    var boardType by remember { mutableStateOf("School") }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LobbySubHeader(title = "🏆 Leaderboards", onBack = { lobbySubScreen = "lobby" })

                        // Scrollable Tab selector
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("School", "City", "Global").forEach { bType ->
                                Button(
                                    onClick = {
                                        boardType = bType
                                        playLobbySfx(android.media.ToneGenerator.TONE_PROP_BEEP)
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (boardType == bType) Color(0xFF3B82F6) else Color(0xFF1E293B)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(bType, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Rankings List
                        val ranks = listOf(
                            Triple("1", "VLearnSage", "2,490 XP"),
                            Triple("2", "PhysicsMaster", "2,100 XP"),
                            Triple("3", "AlphaMath", "1,980 XP"),
                            Triple("12", "You (Alex Mercer)", "1,250 XP")
                        )

                        ranks.forEach { (pos, rName, score) ->
                            val isMe = rName.contains("You")

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = if (isMe) Color(0xFF1E1B4B) else Color(0xFF1E293B)),
                                border = BorderStroke(1.dp, if (isMe) Color(0xFFA855F7) else Color(0xFF334155))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            pos,
                                            fontWeight = FontWeight.Black,
                                            color = when (pos) {
                                                "1" -> Color(0xFFFFD700)
                                                "2" -> Color(0xFFC0C0C0)
                                                "3" -> Color(0xFFCD7F32)
                                                else -> Color.White
                                            },
                                            fontSize = 16.sp,
                                            modifier = Modifier.width(24.dp)
                                        )
                                        Text(rName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                    Text(score, color = Color(0xFFFFB300), fontWeight = FontWeight.Black, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // SUB-SCREEN 8: 👥 STUDY GUILDS LIVE CHAT
                // ==========================================
                "friends" -> {
                    var chatTxt by remember { mutableStateOf("") }
                    var chatMessages by remember {
                        mutableStateOf(
                            listOf(
                                Pair("SaberMaster", "Ready for the Physics raid boss tonight?"),
                                Pair("ByteCrafter", "Calculated limit equations inside the arena!")
                            )
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LobbySubHeader(title = "👥 Friends Study Guild", onBack = { lobbySubScreen = "lobby" })

                        Text(
                            "LIVE GUILD CHAT: 'AI OVERLORDS'",
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.2.sp
                        )

                        // Chat box
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(chatMessages) { (sender, msg) ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            "$sender:",
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF38BDF8),
                                            fontSize = 12.sp
                                        )
                                        Text(msg, color = Color.White, fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        // Chat input
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = chatTxt,
                                onValueChange = { chatTxt = it },
                                placeholder = { Text("Send guild study note...", color = Color.Gray) },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF38BDF8),
                                    unfocusedBorderColor = Color(0xFF334155),
                                    focusedContainerColor = Color(0xFF1E293B),
                                    unfocusedContainerColor = Color(0xFF1E293B),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )

                            IconButton(
                                onClick = {
                                    if (chatTxt.isNotBlank()) {
                                        playLobbySfx(android.media.ToneGenerator.TONE_PROP_BEEP2)
                                        chatMessages = chatMessages + Pair("You", chatTxt)
                                        val typed = chatTxt
                                        chatTxt = ""
                                        // Simulate auto response
                                        scope.launch {
                                            delay(1200)
                                            chatMessages = chatMessages + Pair("Emma_Logic", "Exactly! Let's solve '$typed' together!")
                                            playLobbySfx(android.media.ToneGenerator.TONE_PROP_BEEP)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color(0xFF3B82F6))
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, "Send", tint = Color.White)
                            }
                        }

                        // Friends online list
                        Text(
                            "FRIENDS ONLINE",
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.2.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        listOf("Emma_Logic", "SaberMaster", "ByteCrafter").forEach { friName ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(friName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF10B981))
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Online", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // SUB-SCREEN 9: 🎁 LUCKY SPIN WHEEL & CHECK-IN
                // ==========================================
                "rewards" -> {
                    var checkinCompleted by remember { mutableStateOf(false) }

                    // Spinning wheel components
                    var isSpinningWheel by remember { mutableStateOf(false) }
                    var currentRotationAngle by remember { mutableStateOf(0f) }
                    var rewardSpinResult by remember { mutableStateOf<String?>(null) }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LobbySubHeader(title = "🎁 Lucky Spin Rewards", onBack = { lobbySubScreen = "lobby" })

                        // Daily Check-in card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            border = BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("DAILY STUDY CHECK-IN", fontWeight = FontWeight.Black, color = Color.White, fontSize = 15.sp)
                                Text("Claim your daily gold coins stack to unlock premium skins!", color = Color.LightGray, fontSize = 12.sp, textAlign = TextAlign.Center)
                                Spacer(modifier = Modifier.height(4.dp))
                                Button(
                                    onClick = {
                                        if (!checkinCompleted) {
                                            checkinCompleted = true
                                            viewModel.awardPlayer(150, 0, 0)
                                            playLobbySfx(android.media.ToneGenerator.TONE_PROP_BEEP2)
                                        }
                                    },
                                    enabled = !checkinCompleted,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(if (checkinCompleted) "Daily Claimed ✅" else "Claim 🪙 150 Coins", fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                                }
                            }
                        }

                        Text(
                            "THE WHEEL OF KNOWLEDGE",
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.2.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        // GORGEOUS ROTATING WHEEL CANVAS
                        val rotateAnim by animateFloatAsState(
                            targetValue = if (isSpinningWheel) currentRotationAngle else currentRotationAngle,
                            animationSpec = tween(2200, easing = FastOutSlowInEasing),
                            label = "spin_wheel",
                            finishedListener = {
                                isSpinningWheel = false
                                // Select random reward
                                val prizes = listOf("🪙 200 Coins", "💎 15 Gems", "🪙 500 Coins", "💎 5 Gems", "🪙 1,000 Jackpot!")
                                val picked = prizes.random()
                                rewardSpinResult = picked
                                when {
                                    picked.contains("200") -> viewModel.awardPlayer(200, 0, 0)
                                    picked.contains("15") -> viewModel.awardPlayer(0, 15, 0)
                                    picked.contains("500") -> viewModel.awardPlayer(500, 0, 0)
                                    picked.contains("5") -> viewModel.awardPlayer(0, 5, 0)
                                    else -> viewModel.awardPlayer(1000, 50, 0)
                                }
                                playLobbySfx(android.media.ToneGenerator.TONE_PROP_BEEP2)
                            }
                        )

                        Box(
                            modifier = Modifier
                                .size(220.dp)
                                .graphicsLayer(rotationZ = rotateAnim),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val radius = size.minDimension / 2f
                                val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
                                val colors = listOf(
                                    Color(0xFF3B82F6), Color(0xFF10B981), Color(0xFFEF4444),
                                    Color(0xFFF59E0B), Color(0xFF8B5CF6)
                                )
                                val sweep = 360f / 5f
                                for (i in 0 until 5) {
                                    drawArc(
                                        color = colors[i],
                                        startAngle = i * sweep,
                                        sweepAngle = sweep,
                                        useCenter = true,
                                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                                        topLeft = androidx.compose.ui.geometry.Offset(center.x - radius, center.y - radius)
                                    )
                                }
                                // Draw rim
                                drawCircle(
                                    color = Color.White,
                                    radius = radius,
                                    center = center,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f)
                                )
                                // Draw peg dividers
                                drawCircle(
                                    color = Color(0xFF0F172A),
                                    radius = 24f,
                                    center = center
                                )
                            }
                            Text("🔮", fontSize = 28.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                if (!isSpinningWheel) {
                                    isSpinningWheel = true
                                    rewardSpinResult = null
                                    currentRotationAngle += 1080f + (30..330).random().toFloat()
                                    playLobbySfx(android.media.ToneGenerator.TONE_PROP_BEEP)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("🔮 SPIN THE WHEEL", fontWeight = FontWeight.Black, color = Color.White)
                        }

                        rewardSpinResult?.let { result ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B))
                            ) {
                                Text(
                                    "CONGRATULATIONS! YOU WON:\n$result",
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFFD700),
                                    fontSize = 15.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                )
                            }
                        }
                    }
                }

                // ==========================================
                // SUB-SCREEN 10: 🛒 COSMETIC STORE
                // ==========================================
                "store" -> {
                    val storeSkins = listOf(
                        Triple("Golden Academic Armor", "Skins", 1500),
                        Triple("Quantum Hoverboard Mount", "Mounts", 3000),
                        Triple("AI Companion Robo-Pet", "Pets", 5000),
                        Triple("Nebula Cyber Lobby Background", "Themes", 6000)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LobbySubHeader(title = "🛒 Cosmetics Store", onBack = { lobbySubScreen = "lobby" })

                        Text(
                            "EPIC STUDY UPGRADE SHOP (NO PAY TO WIN!)",
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.2.sp
                        )

                        storeSkins.forEach { (item, category, cost) ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                border = BorderStroke(1.dp, Color(0xFF334155))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(category.uppercase(), color = Color(0xFFC084FC), fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                        Text(item, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }

                                    Button(
                                        onClick = {
                                            userProfile?.let { p ->
                                                if (p.coins >= cost) {
                                                    viewModel.updateProfile(p.copy(coins = p.coins - cost))
                                                    playLobbySfx(android.media.ToneGenerator.TONE_PROP_BEEP2)
                                                } else {
                                                    playLobbySfx(android.media.ToneGenerator.TONE_CDMA_PIP)
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFBBF24)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("🪙", fontSize = 11.sp)
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("$cost", color = Color(0xFF0F172A), fontWeight = FontWeight.Black, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ==========================================
                // SUB-SCREEN 11: 📊 SCHOLAR PROFILE STATS
                // ==========================================
                "profile" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        LobbySubHeader(title = "📊 Scholar Statistics", onBack = { lobbySubScreen = "lobby" })

                        userProfile?.let { profile ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                border = BorderStroke(1.dp, Color(0xFF334155))
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("ACADEMIC DOSSIER", fontWeight = FontWeight.Black, color = Color.White, fontSize = 15.sp)
                                    ProfileStatLine(label = "Student Username", value = "@${profile.username}")
                                    ProfileStatLine(label = "Study Region", value = profile.country)
                                    ProfileStatLine(label = "Current Class", value = profile.gradeClass)
                                    ProfileStatLine(label = "Active Disciplines", value = profile.subjects)
                                    ProfileStatLine(label = "Academic Streak", value = "${profile.streak} Days active")
                                }
                            }

                            Text(
                                "SUBJECT MASTERY CHART",
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.2.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    SubjectProgressBlock(subject = "Science & Physics", pct = 0.82f, color = Color(0xFFEF4444))
                                    SubjectProgressBlock(subject = "Math & Logic", pct = 0.68f, color = Color(0xFF3B82F6))
                                    SubjectProgressBlock(subject = "Computer Programming", pct = 0.95f, color = Color(0xFF10B981))
                                }
                            }
                        }
                    }
                }
                "premium_customization" -> {
                    PremiumCustomizationScreen(viewModel = viewModel, onBack = { lobbySubScreen = "lobby" })
                }
                "premium_settings" -> {
                    PremiumSettingsScreen(viewModel = viewModel, onBack = { lobbySubScreen = "lobby" })
                }
            }
        }
    }
}

@Composable
fun LobbySubHeader(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .clip(CircleShape)
                .background(Color(0xFF1E293B))
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
        }
        Text(
            title,
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 20.sp
        )
    }
}

@Composable
fun LobbyActionCard(
    title: String,
    subtitle: String,
    accentColor: Color,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.2.dp, accentColor.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Open",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    title,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    subtitle,
                    color = Color.Gray,
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun GeneratorCheckItem(label: String, active: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (active) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = "Check",
            tint = if (active) Color(0xFF10B981) else Color.Gray,
            modifier = Modifier.size(16.dp)
        )
        Text(
            label,
            color = if (active) Color.White else Color.Gray,
            fontSize = 12.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun TalentItemRow(title: String, gemsCost: Int, onUpgrade: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text("Permanent hero stats upgrade", color = Color.Gray, fontSize = 10.sp)
        }
        Button(
            onClick = onUpgrade,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22D3EE)),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text("💎 $gemsCost", color = Color(0xFF0F172A), fontWeight = FontWeight.Black, fontSize = 11.sp)
        }
    }
}

@Composable
fun ProfileStatLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.Gray, fontSize = 12.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
fun SubjectProgressBlock(subject: String, pct: Float, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(subject, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text("${(pct * 100).toInt()}% Mastery", color = color, fontWeight = FontWeight.Black, fontSize = 12.sp)
        }
        LinearProgressIndicator(
            progress = { pct },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = Color(0xFF334155)
        )
    }
}

@Composable
fun BadgeItem(name: String, icon: ImageVector, unlocked: Boolean, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (unlocked) color.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.1f))
                .border(2.dp, if (unlocked) color else Color.LightGray, CircleShape)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = if (unlocked) color else Color.LightGray,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            name,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (unlocked) MaterialTheme.colorScheme.onSurface else SlateGrey
        )
    }
}

// ==========================================
// AI TUTOR SCREEN
// ==========================================
@Composable
fun AiTutorScreen(viewModel: VLearnViewModel) {
    val chatHistory by viewModel.chatHistory.collectAsStateWithLifecycle()
    val aiState by viewModel.aiState.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val keyboardController = LocalSoftwareKeyboardController.current

    val prompts = listOf(
        "Explain limits in Calculus step-by-step.",
        "Create a 3-question quiz on Jetpack Compose.",
        "Help me write a Python program that checks for prime numbers.",
        "Summarize: Photosynthesis converts light energy into chemical energy."
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Chat History Scroll View
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
        ) {
            Text(
                "VLEARN Advanced AI Tutor",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Personalized step-by-step math, science, language, & programming assistance.",
                fontSize = 12.sp,
                color = SlateGrey,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Prompt suggestions
            if (chatHistory.size <= 1) {
                Text(
                    "Try asking me one of these topics:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    prompts.forEach { p ->
                        Card(
                            onClick = {
                                viewModel.askAiTutor(p)
                            },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Text(
                                p,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            chatHistory.forEach { message ->
                val isTutor = message.sender == "tutor"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = if (isTutor) Arrangement.Start else Arrangement.End
                ) {
                    if (isTutor) {
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp, top = 4.dp)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "AI",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isTutor) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isTutor) 0.dp else 16.dp,
                            bottomEnd = if (isTutor) 16.dp else 0.dp
                        ),
                        modifier = Modifier
                            .widthIn(max = 280.dp)
                            .border(
                                width = 1.dp,
                                color = if (isTutor) MaterialTheme.colorScheme.outlineVariant else Color.Transparent,
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isTutor) 0.dp else 16.dp,
                                    bottomEnd = if (isTutor) 16.dp else 0.dp
                                )
                            )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                message.content,
                                fontSize = 14.sp,
                                color = if (isTutor) MaterialTheme.colorScheme.onSurface else Color.White,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            if (aiState is AiTutorState.Loading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "AI",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                "AI Tutor is analyzing...",
                                fontSize = 13.sp,
                                color = SlateGrey,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            LaunchedEffect(chatHistory.size, aiState) {
                scrollState.animateScrollTo(scrollState.maxValue)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Chat Input field
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = { viewModel.clearChat() },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Clear Chat",
                    tint = CoralRed
                )
            }

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Ask your VLEARN Tutor...", fontSize = 14.sp) },
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_tutor_input"),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send,
                    keyboardType = KeyboardType.Text
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (inputText.isNotBlank()) {
                            viewModel.askAiTutor(inputText)
                            inputText = ""
                            keyboardController?.hide()
                        }
                    }
                )
            )

            FloatingActionButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.askAiTutor(inputText)
                        inputText = ""
                        keyboardController?.hide()
                    }
                },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("ai_tutor_send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send Message",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ==========================================
// COURSES SCREEN
// ==========================================
@Composable
fun CoursesScreen(viewModel: VLearnViewModel) {
    val allCourses by viewModel.allCourses.collectAsStateWithLifecycle()
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Programming", "Mathematics", "Science", "Commerce", "Languages")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "VLEARN Course Catalog",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            "Enroll in structured subject curricula with direct quiz evaluation.",
            fontSize = 12.sp,
            color = SlateGrey,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Categories chip row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = selectedCategory == cat
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        val filteredCourses = if (selectedCategory == "All") {
            allCourses
        } else {
            allCourses.filter { it.subject == selectedCategory }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredCourses) { course ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.selectCourse(course)
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val subjColor = when (course.subject) {
                                "Programming" -> Color(0xFF10B981)
                                "Mathematics" -> Color(0xFF3B82F6)
                                "Science" -> Color(0xFFEF4444)
                                "Languages" -> Color(0xFF8B5CF6)
                                else -> Color(0xFFF59E0B)
                            }
                            Text(
                                course.subject.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = subjColor,
                                letterSpacing = 1.sp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(subjColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                            if (course.isEnrolled) {
                                Text(
                                    "Enrolled (${course.progressPercent}%)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LightEmerald
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            course.title,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            course.description,
                            fontSize = 13.sp,
                            color = SlateGrey,
                            modifier = Modifier.padding(vertical = 6.dp),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Divider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Instructor",
                                    tint = SlateGrey,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    course.instructor,
                                    fontSize = 12.sp,
                                    color = SlateGrey,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (!course.isEnrolled) {
                                Button(
                                    onClick = { viewModel.enrollInCourse(course.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("Enroll Free", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.selectCourse(course) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text(
                                        "Open Study Path ➔",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// COURSE SYLLABUS DETAILED STUDY
// ==========================================
@Composable
fun CourseDetailScreen(viewModel: VLearnViewModel, course: Course) {
    val lessons = course.syllabus.split(";")
    val currentProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            IconButton(onClick = { viewModel.selectCourse(null) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                "Course Details",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    course.title,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Instructor: ${course.instructor}",
                    fontSize = 13.sp,
                    color = SlateGrey,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    course.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Course Syllabus (${lessons.size} Lessons)",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(lessons.size) { index ->
                val lesson = lessons[index]
                // Simulated check state for progress
                val requiredProgressForThisLesson = ((index + 1).toFloat() / lessons.size * 100).toInt()
                val isCompleted = course.progressPercent >= requiredProgressForThisLesson

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                    )
                            ) {
                                Text(
                                    "${index + 1}",
                                    color = if (isCompleted) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Column {
                                Text(
                                    lesson,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    if (isCompleted) "Completed (+100 XP)" else "Unlocks on sequential completion",
                                    fontSize = 11.sp,
                                    color = if (isCompleted) LightEmerald else SlateGrey,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Checkbox(
                            checked = isCompleted,
                            onCheckedChange = { checked ->
                                val newProgress = if (checked) {
                                    requiredProgressForThisLesson
                                } else {
                                    (index.toFloat() / lessons.size * 100).toInt()
                                }
                                viewModel.updateCourseProgress(course.id, newProgress)
                                // Trigger selection update
                                viewModel.selectCourse(course.copy(progressPercent = newProgress))
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Ask AI Tutor about this course button
        Button(
            onClick = {
                viewModel.navigateTo("ai_tutor")
                viewModel.askAiTutor("Hey! I'm studying the course '${course.title}' with VLEARN. Can you quiz me on one of the topics in the syllabus?")
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Psychology, contentDescription = "Tutor")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Quiz Me with AI Tutor", fontWeight = FontWeight.Bold)
        }
    }
}

// ==========================================
// PRODUCTIVITY HUB (STUDY PLANNER & FLASHCARDS)
// ==========================================
@Composable
fun ProductivityScreen(viewModel: VLearnViewModel) {
    var selectedTab by remember { mutableStateOf("Planner") } // Planner, Flashcards, Notes
    val studyPlans by viewModel.allStudyPlans.collectAsStateWithLifecycle()
    val flashcards by viewModel.allFlashcards.collectAsStateWithLifecycle()
    val notes by viewModel.allNotes.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "VLEARN Productivity Suite",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            "Centralize your notes, active flashcards, and personalized timers.",
            fontSize = 12.sp,
            color = SlateGrey,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        TabRow(
            selectedTabIndex = when (selectedTab) {
                "Planner" -> 0
                "Flashcards" -> 1
                "Notes" -> 2
                else -> 0
            },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Tab(
                selected = selectedTab == "Planner",
                onClick = { selectedTab = "Planner" },
                text = { Text("Study Planner", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
            Tab(
                selected = selectedTab == "Flashcards",
                onClick = { selectedTab = "Flashcards" },
                text = { Text("Flashcards", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
            Tab(
                selected = selectedTab == "Notes",
                onClick = { selectedTab = "Notes" },
                text = { Text("Study Notes", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
        }

        when (selectedTab) {
            "Planner" -> StudyPlannerTab(viewModel, studyPlans)
            "Flashcards" -> FlashcardsTab(viewModel, flashcards)
            "Notes" -> NotesTab(viewModel, notes)
        }
    }
}

@Composable
fun StudyPlannerTab(viewModel: VLearnViewModel, studyPlans: List<StudyPlan>) {
    var showDialog by remember { mutableStateOf(false) }
    var taskTitle by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("30") }
    var category by remember { mutableStateOf("Study") }
    val categories = listOf("Study", "Homework", "Exam", "Live Class")

    Box(modifier = Modifier.fillMaxSize()) {
        if (studyPlans.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = "Planner",
                    tint = SlateGrey,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Your Planner is Empty",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    "Organize your academic schedules and tasks easily with gamified rewards.",
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    color = SlateGrey
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(studyPlans) { plan ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val catColor = when (plan.category) {
                                    "Exam" -> CoralRed
                                    "Homework" -> Color(0xFFFFB300)
                                    "Live Class" -> IndigoDark
                                    else -> LightEmerald
                                }
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(catColor)
                                )
                                Column {
                                    Text(
                                        plan.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        textDecoration = if (plan.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                    )
                                    Text(
                                        "Due: ${plan.dueDate} • ${plan.estimatedTime} mins",
                                        fontSize = 12.sp,
                                        color = SlateGrey
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { viewModel.toggleStudyPlanCompleted(plan.id) }
                                ) {
                                    Icon(
                                        imageVector = if (plan.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = "Complete task",
                                        tint = if (plan.isCompleted) LightEmerald else MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(onClick = { viewModel.deleteStudyPlan(plan.id) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete task",
                                        tint = CoralRed.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add task floating action button
        FloatingActionButton(
            onClick = { showDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 8.dp)
                .testTag("add_task_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Task")
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Schedule New Study Session", fontWeight = FontWeight.Black) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = taskTitle,
                            onValueChange = { taskTitle = it },
                            label = { Text("Task / Goal Name") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = duration,
                            onValueChange = { duration = it },
                            label = { Text("Estimated Duration (mins)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Session Category:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categories.forEach { cat ->
                                FilterChip(
                                    selected = category == cat,
                                    onClick = { category = cat },
                                    label = { Text(cat) }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (taskTitle.isNotBlank()) {
                                viewModel.addStudyPlan(
                                    title = taskTitle,
                                    dueDate = "Today",
                                    estimatedTime = duration.toIntOrNull() ?: 30,
                                    category = category
                                )
                                taskTitle = ""
                                showDialog = false
                            }
                        }
                    ) {
                        Text("Add Session")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun FlashcardsTab(viewModel: VLearnViewModel, cards: List<Flashcard>) {
    var showAddDialog by remember { mutableStateOf(false) }
    var frontText by remember { mutableStateOf("") }
    var backText by remember { mutableStateOf("") }
    var cardSubject by remember { mutableStateOf("General") }

    Box(modifier = Modifier.fillMaxSize()) {
        if (cards.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.BookmarkBorder,
                    contentDescription = "Flashcards",
                    tint = SlateGrey,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "No Flashcards Created",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    "Add custom questions & answers to review before crucial exams.",
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    color = SlateGrey
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(cards) { card ->
                    var isFlipped by remember { mutableStateOf(false) }
                    // Custom interactive flip rotation animation
                    val rotation by animateFloatAsState(
                        targetValue = if (isFlipped) 180f else 0f,
                        animationSpec = tween(durationMillis = 400),
                        label = "CardFlipAnimation"
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .graphicsLayer {
                                rotationY = rotation
                                cameraDistance = 8 * density
                            }
                            .clickable { isFlipped = !isFlipped },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isFlipped) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isFlipped) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .graphicsLayer {
                                    // Reverse the flip text so it reads correctly
                                    rotationY = if (isFlipped) 180f else 0f
                                },
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    card.subject.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.sp
                                )
                                IconButton(
                                    onClick = { viewModel.deleteFlashcard(card.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Flashcard",
                                        tint = CoralRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isFlipped) card.backText else card.frontText,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    textAlign = TextAlign.Center,
                                    color = if (isFlipped) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    if (isFlipped) "Answer (Tap to Flip)" else "Question (Tap to Flip)",
                                    fontSize = 10.sp,
                                    color = SlateGrey,
                                    fontWeight = FontWeight.Bold
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        viewModel.toggleFlashcardLearned(card.id)
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (card.isLearned) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = "Learned",
                                        tint = if (card.isLearned) LightEmerald else SlateGrey,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Learned",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (card.isLearned) LightEmerald else SlateGrey
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 8.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Flashcard")
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Create New Flashcard", fontWeight = FontWeight.Black) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = frontText,
                            onValueChange = { frontText = it },
                            label = { Text("Front Side (Question)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = backText,
                            onValueChange = { backText = it },
                            label = { Text("Back Side (Answer)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = cardSubject,
                            onValueChange = { cardSubject = it },
                            label = { Text("Subject (e.g., Mathematics, Science)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (frontText.isNotBlank() && backText.isNotBlank()) {
                                viewModel.addFlashcard(
                                    front = frontText,
                                    back = backText,
                                    subject = cardSubject
                                )
                                frontText = ""
                                backText = ""
                                showAddDialog = false
                            }
                        }
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun NotesTab(viewModel: VLearnViewModel, notes: List<Note>) {
    var showAddDialog by remember { mutableStateOf(false) }
    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }
    var noteSubject by remember { mutableStateOf("Programming") }

    Box(modifier = Modifier.fillMaxSize()) {
        if (notes.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EditNote,
                    contentDescription = "Notes",
                    tint = SlateGrey,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "No Study Notes saved",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    "Capture summaries of lectures and AI suggestions instantly.",
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    color = SlateGrey
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(notes) { note ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    note.subject.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.sp
                                )
                                IconButton(
                                    onClick = { viewModel.deleteNote(note.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Note",
                                        tint = CoralRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                note.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                note.content,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 8.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Note")
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Compose New Note", fontWeight = FontWeight.Black) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = noteTitle,
                            onValueChange = { noteTitle = it },
                            label = { Text("Note Title") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = noteContent,
                            onValueChange = { noteContent = it },
                            label = { Text("Summary / Content") },
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = noteSubject,
                            onValueChange = { noteSubject = it },
                            label = { Text("Subject (e.g., Programming)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (noteTitle.isNotBlank() && noteContent.isNotBlank()) {
                                viewModel.addNote(
                                    title = noteTitle,
                                    content = noteContent,
                                    subject = noteSubject
                                )
                                noteTitle = ""
                                noteContent = ""
                                showAddDialog = false
                            }
                        }
                    ) {
                        Text("Save Note")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

// ==========================================
// SOCIAL DISCUSSION FORUMS
// ==========================================
@Composable
fun SocialScreen(viewModel: VLearnViewModel) {
    val forumMessages by viewModel.forumMessages.collectAsStateWithLifecycle()
    var selectedFilter by remember { mutableStateOf("All") }
    var typedMessage by remember { mutableStateOf("") }
    val subjects = listOf("All", "Programming", "Mathematics", "Science")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "VLEARN Community Forums",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            "Engage in academic discussions with peer students and verified teachers.",
            fontSize = 12.sp,
            color = SlateGrey,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            subjects.forEach { s ->
                val isSelected = selectedFilter == s
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = s },
                    label = { Text(s, fontWeight = FontWeight.Bold) }
                )
            }
        }

        val filteredMessages = if (selectedFilter == "All") {
            forumMessages
        } else {
            forumMessages.filter { it.subject == selectedFilter }
        }

        // Message Feed
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredMessages) { msg ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        msg.senderName.take(1).uppercase(),
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            msg.senderName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "(${msg.senderRole})",
                                            fontSize = 11.sp,
                                            color = if (msg.senderRole == "Teacher") VioletDark else SlateGrey,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }

                            Text(
                                msg.subject.uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            msg.messageContent,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Text field input
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = typedMessage,
                onValueChange = { typedMessage = it },
                placeholder = { Text("Ask your classmates a question...", fontSize = 13.sp) },
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.weight(1f)
            )

            FloatingActionButton(
                onClick = {
                    if (typedMessage.isNotBlank()) {
                        viewModel.sendForumMessage(
                            messageContent = typedMessage,
                            subject = if (selectedFilter == "All") "Programming" else selectedFilter
                        )
                        typedMessage = ""
                    }
                },
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Post message", modifier = Modifier.size(16.dp))
            }
        }
    }
}

// ==========================================
// PARENT HUB & DYNAMIC AI RECOMMENDATIONS
// ==========================================
@Composable
fun ParentPanelScreen(viewModel: VLearnViewModel) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "VLEARN Parent Dashboard",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "Keep track of academic attendance, metrics, strengths, & personalized recommendations.",
                fontSize = 12.sp,
                color = SlateGrey
            )
        }

        // Children summary card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Student Profile Assessed: Alex Mercer",
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        MetricBlock(label = "Class Attendance", value = "96%", desc = "Nominal Target Achieved", color = LightEmerald)
                        MetricBlock(label = "Course Progress", value = "2 Complete", desc = "5 active syllabus modules", color = IndigoDark)
                        MetricBlock(label = "XP Points Gained", value = "1,850 XP", desc = "Level 6 reached", color = GoldDark)
                    }
                }
            }
        }

        // Strength / Weakness section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Subject Strength Breakdown",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    SubjectBar(subj = "Programming (Jetpack Compose)", value = 0.85f, color = LightEmerald)
                    SubjectBar(subj = "Languages (Spanish Beginners)", value = 0.60f, color = VioletDark)
                    SubjectBar(subj = "Science (Organic Chemistry)", value = 0.45f, color = Color(0xFFFFB300))
                    SubjectBar(subj = "Mathematics (Calculus I)", value = 0.35f, color = CoralRed)
                }
            }
        }

        // AI Recommendations for parent
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "AI Recommender",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "AI TUTOR RECOMMENDATIONS",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "\"Alex is currently showing outstanding progress in modern Programming (Jetpack Compose), completing 40% of the syllabus. However, his performance in Calculus I shows a weak area in 'Derivative Rules' (35% proficiency). \n\n" +
                        "AI Tutor recommends that Alex practices with the 'Mathematics' Flashcards daily for 10 minutes and schedules a step-by-step limits walkthrough. Encouraging a 15-minute review today will unlock his streak bonus!\"",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MetricBlock(label: String, value: String, desc: String, color: Color) {
    Column {
        Text(label, fontSize = 11.sp, color = SlateGrey, fontWeight = FontWeight.Bold)
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = color)
        Text(desc, fontSize = 9.sp, color = SlateGrey)
    }
}

@Composable
fun SubjectBar(subj: String, value: Float, color: Color) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(subj, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("${(value * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { value },
            color = color,
            trackColor = color.copy(alpha = 0.15f),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        )
    }
}

// ==========================================
// TEACHER HUB (COURSE CREATOR)
// ==========================================
@Composable
fun TeacherPanelScreen(viewModel: VLearnViewModel) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Programming") }
    var description by remember { mutableStateOf("") }
    var instructor by remember { mutableStateOf("Helen Cho") }
    var lessonInput by remember { mutableStateOf("") }

    val subjects = listOf("Programming", "Mathematics", "Science", "Commerce", "Languages")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "VLEARN Teacher Dashboard",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "Create courses, manage interactive syllabus modules, and broadcast class instructions.",
                fontSize = 12.sp,
                color = SlateGrey
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Create New Subject Course",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Course Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Course Description") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = instructor,
                        onValueChange = { instructor = it },
                        label = { Text("Lead Instructor") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Curriculum Subject:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        subjects.forEach { s ->
                            FilterChip(
                                selected = subject == s,
                                onClick = { subject = s },
                                label = { Text(s) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = lessonInput,
                        onValueChange = { lessonInput = it },
                        label = { Text("Lessons (separate with semicolon ';')") },
                        placeholder = { Text("e.g. Lesson 1;Lesson 2;Lesson 3") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (title.isNotBlank() && description.isNotBlank()) {
                                val splitLessons = lessonInput.split(";").filter { it.isNotBlank() }
                                viewModel.createCourse(
                                    title = title,
                                    subject = subject,
                                    description = description,
                                    instructor = instructor,
                                    lessons = if (splitLessons.isEmpty()) listOf("Introduction to Subject") else splitLessons
                                )
                                title = ""
                                description = ""
                                lessonInput = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Publish Course Module", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// ADMIN DASHBOARD & SECURITY AUDITS
// ==========================================
@Composable
fun AdminPanelScreen(viewModel: VLearnViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "VLEARN Admin Console",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "Enterprise system controls, MFA status monitoring, and active API rate limiting shields.",
                fontSize = 12.sp,
                color = SlateGrey
            )
        }

        // Shield status block
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Shield Active",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Column {
                        Text(
                            "VLEARN SECURITY SHIELD: ACTIVE",
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "All API rate limiting, MFA tokens, data encryption, and global backups are fully operational.",
                            fontSize = 13.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // University metrics
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Registered Institutions",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        MetricBlock(label = "K-12 Schools", value = "1,245", desc = "Connected campuses", color = IndigoDark)
                        MetricBlock(label = "Universities", value = "412", desc = "Global higher ed", color = VioletDark)
                        MetricBlock(label = "Active Students", value = "2.4M+", desc = "Simulated users", color = LightEmerald)
                    }
                }
            }
        }

        // Security Logs
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Live Audit Log Streams",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LogLine(time = "02:30:14 UTC", log = "MFA Token verified for parent_auth_904", state = "OK")
                    LogLine(time = "02:29:45 UTC", log = "Database encryption check: 100% complete", state = "OK")
                    LogLine(time = "02:28:10 UTC", log = "Cloud backup executed & verified", state = "OK")
                    LogLine(time = "02:25:31 UTC", log = "Rate limiter shield successfully filtered 1.2k queries", state = "SHIELD")
                }
            }
        }
    }
}

@Composable
fun LogLine(time: String, log: String, state: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(log, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(time, fontSize = 10.sp, color = SlateGrey)
        }
        Text(
            state,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            color = if (state == "OK") LightEmerald else IndigoDark,
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(if (state == "OK") LightEmerald.copy(alpha = 0.15f) else IndigoDark.copy(alpha = 0.15f))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

// Simple FlowRow helper implementation for Compose 
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints) }
        val layoutWidth = constraints.maxWidth
        var layoutHeight = 0
        var currentX = 0
        var currentY = 0
        var rowHeight = 0

        val coordinates = ArrayList<Pair<Int, Int>>()

        placeables.forEach { placeable ->
            if (currentX + placeable.width > layoutWidth) {
                currentX = 0
                currentY += rowHeight + verticalArrangement.let { 8.dp.roundToPx() } // default vertical spacing
                rowHeight = 0
            }
            coordinates.add(Pair(currentX, currentY))
            currentX += placeable.width + horizontalArrangement.let { 8.dp.roundToPx() } // default horizontal spacing
            rowHeight = maxOf(rowHeight, placeable.height)
        }

        layoutHeight = currentY + rowHeight

        layout(layoutWidth, layoutHeight) {
            placeables.forEachIndexed { index, placeable ->
                val coords = coordinates[index]
                placeable.placeRelative(coords.first, coords.second)
            }
        }
    }
}
