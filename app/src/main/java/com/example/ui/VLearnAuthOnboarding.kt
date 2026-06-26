package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.UserProfile
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class AuthScreen {
    WELCOME,
    LOGIN_EMAIL,
    CREATE_ACCOUNT,
    OTP_VERIFY,
    PASSWORD_RESET
}

enum class OnboardingStep {
    ROLE_SELECT,
    NAME_DETAILS,
    ACADEMIC_SUBJECTS,
    CHOOSE_AVATAR,
    AI_CREATING
}

@Composable
fun VLearnAuthAndOnboardingContainer(
    viewModel: VLearnViewModel,
    onComplete: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val syncStatus by viewModel.cloudSyncStatus.collectAsStateWithLifecycle()

    var authScreen by remember { mutableStateOf(AuthScreen.WELCOME) }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var usernameInput by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        if (userProfile == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = IndigoDark)
            }
        } else {
            val profile = userProfile!!
            if (profile.isLoggedIn) {
                if (profile.isOnboarded) {
                    LaunchedEffect(Unit) {
                        onComplete()
                    }
                } else {
                    // Render onboarding flow
                    VLearnOnboardingFlow(
                        viewModel = viewModel,
                        onComplete = onComplete
                    )
                }
            } else {
                // Render Authentication Screens
                VLearnAuthFlow(
                    authScreen = authScreen,
                    onNavigate = { authScreen = it },
                    email = emailInput,
                    onEmailChange = { emailInput = it },
                    password = passwordInput,
                    onPasswordChange = { passwordInput = it },
                    name = nameInput,
                    onNameChange = { nameInput = it },
                    username = usernameInput,
                    onUsernameChange = { usernameInput = it },
                    onLoginClick = { authType ->
                        viewModel.loginUser(authType) { onboarded ->
                            if (onboarded) {
                                onComplete()
                            }
                        }
                    }
                )
            }
        }

        // Google Cloud Sync Overlay
        syncStatus?.let { status ->
            CloudSyncProgressOverlay(statusMessage = status)
        }
    }
}

@Composable
fun VLearnAuthFlow(
    authScreen: AuthScreen,
    onNavigate: (AuthScreen) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    name: String,
    onNameChange: (String) -> Unit,
    username: String,
    onUsernameChange: (String) -> Unit,
    onLoginClick: (String) -> Unit
) {
    AnimatedContent(
        targetState = authScreen,
        transitionSpec = {
            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
        },
        label = "auth_screens"
    ) { screen ->
        when (screen) {
            AuthScreen.WELCOME -> WelcomeLandingScreen(
                onNavigate = onNavigate,
                onLoginClick = onLoginClick
            )
            AuthScreen.LOGIN_EMAIL -> LoginEmailScreen(
                email = email,
                onEmailChange = onEmailChange,
                password = password,
                onPasswordChange = onPasswordChange,
                onBack = { onNavigate(AuthScreen.WELCOME) },
                onNavigate = onNavigate,
                onLoginClick = { onNavigate(AuthScreen.OTP_VERIFY) }
            )
            AuthScreen.CREATE_ACCOUNT -> CreateAccountScreen(
                name = name,
                onNameChange = onNameChange,
                email = email,
                onEmailChange = onEmailChange,
                password = password,
                onPasswordChange = onPasswordChange,
                username = username,
                onUsernameChange = onUsernameChange,
                onBack = { onNavigate(AuthScreen.WELCOME) },
                onRegisterClick = { onNavigate(AuthScreen.OTP_VERIFY) }
            )
            AuthScreen.OTP_VERIFY -> OtpVerificationScreen(
                email = email,
                onBack = { onNavigate(AuthScreen.LOGIN_EMAIL) },
                onVerifyClick = { onLoginClick("Email/OTP") }
            )
            AuthScreen.PASSWORD_RESET -> PasswordResetScreen(
                email = email,
                onEmailChange = onEmailChange,
                onBack = { onNavigate(AuthScreen.LOGIN_EMAIL) }
            )
        }
    }
}

@Composable
fun WelcomeLandingScreen(
    onNavigate: (AuthScreen) -> Unit,
    onLoginClick: (String) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Center visual icon / logo with a glowing radial background
        Box(
            modifier = Modifier
                .size(140.dp)
                .graphicsLayer(scaleX = logoScale, scaleY = logoScale),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                IndigoDark.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Image(
                painter = painterResource(id = R.drawable.vlearn_logo),
                contentDescription = "VLEARN Logo",
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(2.dp, IndigoDark, RoundedCornerShape(24.dp))
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Welcome to VLEARN AI",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            textAlign = TextAlign.Center,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Learn Through Adventure",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = GoldDark,
            textAlign = TextAlign.Center,
            letterSpacing = 1.2.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Actions List
        Button(
            onClick = { onLoginClick("Google") },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("continue_google_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Cloud,
                    contentDescription = "Google Icon",
                    tint = IndigoDark,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Continue with Google",
                    color = Color(0xFF0F172A),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onNavigate(AuthScreen.LOGIN_EMAIL) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("continue_email_button"),
            colors = ButtonDefaults.buttonColors(containerColor = IndigoDark),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email Icon",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Continue with Email",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { onNavigate(AuthScreen.CREATE_ACCOUNT) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("create_account_button"),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, SlateGrey)
        ) {
            Text(
                "Create New Account",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        TextButton(
            onClick = { onLoginClick("Guest") },
            modifier = Modifier.testTag("guest_mode_button")
        ) {
            Text(
                "Guest Mode",
                color = SlateGrey,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Terms & Privacy Policy",
            color = SlateGrey.copy(alpha = 0.7f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginEmailScreen(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    onBack: () -> Unit,
    onNavigate: (AuthScreen) -> Unit,
    onLoginClick: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(top = 8.dp)
                .background(SurfaceDark, CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Welcome Back",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Enter your credentials to access your cloud classroom.",
            fontSize = 14.sp,
            color = SlateGrey,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(36.dp))

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email Address", color = SlateGrey) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("email_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = IndigoDark,
                unfocusedBorderColor = SurfaceDark,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = SurfaceDark,
                unfocusedContainerColor = SurfaceDark
            ),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Password", color = SlateGrey) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("password_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = IndigoDark,
                unfocusedBorderColor = SurfaceDark,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = SurfaceDark,
                unfocusedContainerColor = SurfaceDark
            ),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle password visibility",
                        tint = SlateGrey
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = { onNavigate(AuthScreen.PASSWORD_RESET) },
                modifier = Modifier.testTag("forgot_password_button")
            ) {
                Text(
                    "Forgot Password?",
                    color = GoldDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    onLoginClick()
                }
            },
            enabled = email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("login_submit_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = IndigoDark,
                disabledContainerColor = SurfaceDark
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                "Sign In",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (email.isNotBlank() && password.isNotBlank()) Color.White else SlateGrey
            )
        }
    }
}

@Composable
fun CreateAccountScreen(
    name: String,
    onNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    username: String,
    onUsernameChange: (String) -> Unit,
    onBack: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(top = 8.dp)
                .background(SurfaceDark, CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Create Account",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Begin your learning quest with automated cloud backups.",
            fontSize = 14.sp,
            color = SlateGrey,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(28.dp))

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Full Name", color = SlateGrey) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("create_name_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = IndigoDark,
                unfocusedBorderColor = SurfaceDark,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = SurfaceDark,
                unfocusedContainerColor = SurfaceDark
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = { Text("Desired Username", color = SlateGrey) },
            singleLine = true,
            prefix = { Text("@", color = IndigoDark, fontWeight = FontWeight.Bold) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("create_username_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = IndigoDark,
                unfocusedBorderColor = SurfaceDark,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = SurfaceDark,
                unfocusedContainerColor = SurfaceDark
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email Address", color = SlateGrey) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("create_email_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = IndigoDark,
                unfocusedBorderColor = SurfaceDark,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = SurfaceDark,
                unfocusedContainerColor = SurfaceDark
            ),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Password (Min 6 Characters)", color = SlateGrey) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("create_password_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = IndigoDark,
                unfocusedBorderColor = SurfaceDark,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = SurfaceDark,
                unfocusedContainerColor = SurfaceDark
            ),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = {
                if (name.isNotBlank() && email.isNotBlank() && password.length >= 6) {
                    onRegisterClick()
                }
            },
            enabled = name.isNotBlank() && email.isNotBlank() && password.length >= 6,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("register_submit_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = IndigoDark,
                disabledContainerColor = SurfaceDark
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                "Register & Verify",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (name.isNotBlank() && email.isNotBlank() && password.length >= 6) Color.White else SlateGrey
            )
        }
    }
}

@Composable
fun OtpVerificationScreen(
    email: String,
    onBack: () -> Unit,
    onVerifyClick: () -> Unit
) {
    var otp1 by remember { mutableStateOf("") }
    var otp2 by remember { mutableStateOf("") }
    var otp3 by remember { mutableStateOf("") }
    var otp4 by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .background(SurfaceDark, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "OTP Verification",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "A secure verification code has been dispatched to:",
            fontSize = 14.sp,
            color = SlateGrey,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = email.ifEmpty { "your-email@vlearn.ai" },
            fontSize = 14.sp,
            color = GoldDark,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(36.dp))

        // OTP inputs row
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                Pair(otp1) { s: String -> otp1 = s },
                Pair(otp2) { s: String -> otp2 = s },
                Pair(otp3) { s: String -> otp3 = s },
                Pair(otp4) { s: String -> otp4 = s }
            ).forEachIndexed { index, pair ->
                OutlinedTextField(
                    value = pair.first,
                    onValueChange = { input ->
                        if (input.length <= 1) {
                            pair.second(input)
                        }
                    },
                    modifier = Modifier
                        .width(60.dp)
                        .height(60.dp)
                        .testTag("otp_input_$index"),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IndigoDark,
                        unfocusedBorderColor = SurfaceDark,
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                onVerifyClick()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("verify_otp_button"),
            colors = ButtonDefaults.buttonColors(containerColor = LightEmerald),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                "Verify & Cloud Sync",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = {
            otp1 = "1"
            otp2 = "2"
            otp3 = "3"
            otp4 = "4"
        }) {
            Text(
                "Simulate OTP Auto-fill (Code: 1234)",
                color = SlateGrey,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun PasswordResetScreen(
    email: String,
    onEmailChange: (String) -> Unit,
    onBack: () -> Unit
) {
    var emailSent by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(top = 8.dp)
                .background(SurfaceDark, CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Reset Password",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "A secure cloud reset link will be sent to recover your account configuration.",
            fontSize = 14.sp,
            color = SlateGrey,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(36.dp))

        if (emailSent) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(LightEmerald.copy(alpha = 0.15f))
                    .border(1.5.dp, LightEmerald, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Sent",
                            tint = LightEmerald,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Link Dispatched!",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Check your inbox at $email for instructions.",
                        color = SlateGrey,
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email Address", color = SlateGrey) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("reset_email_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = IndigoDark,
                unfocusedBorderColor = SurfaceDark,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = SurfaceDark,
                unfocusedContainerColor = SurfaceDark
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (email.isNotBlank()) {
                    emailSent = true
                }
            },
            enabled = email.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("send_reset_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = IndigoDark,
                disabledContainerColor = SurfaceDark
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                "Send Recovery Link",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (email.isNotBlank()) Color.White else SlateGrey
            )
        }
    }
}

@Composable
fun CloudSyncProgressOverlay(statusMessage: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(1.dp, IndigoDark),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Spinning gradient circle loader
                val infiniteTransition = rememberInfiniteTransition(label = "rotation")
                val rotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "rotate"
                )

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .graphicsLayer { rotationZ = rotation }
                        .border(
                            width = 4.dp,
                            brush = Brush.sweepGradient(listOf(IndigoDark, VioletDark, GoldDark, IndigoDark)),
                            shape = CircleShape
                        )
                )

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "Syncing with Cloud",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = statusMessage,
                    fontSize = 14.sp,
                    color = GoldDark,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Everything is stored in Google Cloud. No local data loss.",
                    fontSize = 11.sp,
                    color = SlateGrey,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun VLearnOnboardingFlow(
    viewModel: VLearnViewModel,
    onComplete: () -> Unit
) {
    var step by remember { mutableStateOf(OnboardingStep.ROLE_SELECT) }
    var chosenRole by remember { mutableStateOf("Student") }
    var nameInput by remember { mutableStateOf("") }
    var countryInput by remember { mutableStateOf("United States") }
    var gradeInput by remember { mutableStateOf("Grade 10") }
    var selectedSubjects by remember { mutableStateOf(setOf("Science", "Math", "Programming")) }
    var chosenAvatar by remember { mutableStateOf("avatar_1") }
    var usernameInput by remember { mutableStateOf("") }

    AnimatedContent(
        targetState = step,
        transitionSpec = {
            slideInHorizontally { width -> width } + fadeIn() togetherWith
                    slideOutHorizontally { width -> -width } + fadeOut()
        },
        label = "onboarding_steps"
    ) { currentStep ->
        when (currentStep) {
            OnboardingStep.ROLE_SELECT -> OnboardingRoleSelectScreen(
                selectedRole = chosenRole,
                onRoleSelected = { chosenRole = it },
                onNext = { step = OnboardingStep.NAME_DETAILS }
            )
            OnboardingStep.NAME_DETAILS -> OnboardingNameDetailsScreen(
                name = nameInput,
                onNameChange = { nameInput = it },
                country = countryInput,
                onCountryChange = { countryInput = it },
                grade = gradeInput,
                onGradeChange = { gradeInput = it },
                onBack = { step = OnboardingStep.ROLE_SELECT },
                onNext = { step = OnboardingStep.ACADEMIC_SUBJECTS }
            )
            OnboardingStep.ACADEMIC_SUBJECTS -> OnboardingSubjectsScreen(
                selectedSubjects = selectedSubjects,
                onToggleSubject = { sub ->
                    selectedSubjects = if (selectedSubjects.contains(sub)) {
                        selectedSubjects - sub
                    } else {
                        selectedSubjects + sub
                    }
                },
                onBack = { step = OnboardingStep.NAME_DETAILS },
                onNext = { step = OnboardingStep.CHOOSE_AVATAR }
            )
            OnboardingStep.CHOOSE_AVATAR -> OnboardingAvatarScreen(
                selectedAvatar = chosenAvatar,
                onAvatarSelected = { chosenAvatar = it },
                username = usernameInput,
                onUsernameChange = { usernameInput = it },
                onBack = { step = OnboardingStep.ACADEMIC_SUBJECTS },
                onNext = {
                    step = OnboardingStep.AI_CREATING
                }
            )
            OnboardingStep.AI_CREATING -> OnboardingAiCreationScreen(
                role = chosenRole,
                name = nameInput,
                country = countryInput,
                grade = gradeInput,
                subjects = selectedSubjects.toList(),
                avatar = chosenAvatar,
                username = usernameInput,
                viewModel = viewModel,
                onComplete = onComplete
            )
        }
    }
}

@Composable
fun OnboardingRoleSelectScreen(
    selectedRole: String,
    onRoleSelected: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Progress bar
        LinearProgressIndicator(
            progress = { 0.2f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = IndigoDark,
            trackColor = SurfaceDark
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "Choose Your Role",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Are you ready to learn, or ready to guide?",
            fontSize = 14.sp,
            color = SlateGrey,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Student Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onRoleSelected("Student") }
                .border(
                    width = if (selectedRole == "Student") 2.dp else 1.dp,
                    color = if (selectedRole == "Student") GoldDark else SurfaceDark,
                    shape = RoundedCornerShape(16.dp)
                ),
            colors = CardDefaults.cardColors(
                containerColor = if (selectedRole == "Student") SurfaceDark else SurfaceDark.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            if (selectedRole == "Student") GoldDark.copy(alpha = 0.2f) else SlateGrey.copy(
                                alpha = 0.1f
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Student Icon",
                        tint = if (selectedRole == "Student") GoldDark else SlateGrey,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        "Student Warrior",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Unlock quests, combat math monsters, study with your AI buddy, and climb the leaderboards.",
                        fontSize = 13.sp,
                        color = SlateGrey
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Teacher Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onRoleSelected("Teacher") }
                .border(
                    width = if (selectedRole == "Teacher") 2.dp else 1.dp,
                    color = if (selectedRole == "Teacher") GoldDark else SurfaceDark,
                    shape = RoundedCornerShape(16.dp)
                ),
            colors = CardDefaults.cardColors(
                containerColor = if (selectedRole == "Teacher") SurfaceDark else SurfaceDark.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            if (selectedRole == "Teacher") IndigoDark.copy(alpha = 0.2f) else SlateGrey.copy(
                                alpha = 0.1f
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CoPresent,
                        contentDescription = "Teacher Icon",
                        tint = if (selectedRole == "Teacher") IndigoDark else SlateGrey,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        "Instructor / Guide",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Create customized syllabus targets, upload textbooks to prompt AI generators, and review student progress.",
                        fontSize = 13.sp,
                        color = SlateGrey
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("onboarding_role_next"),
            colors = ButtonDefaults.buttonColors(containerColor = IndigoDark),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next")
            }
        }
    }
}

@Composable
fun OnboardingNameDetailsScreen(
    name: String,
    onNameChange: (String) -> Unit,
    country: String,
    onCountryChange: (String) -> Unit,
    grade: String,
    onGradeChange: (String) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val countries = listOf("United States", "United Kingdom", "Canada", "Australia", "Singapore", "India", "Brazil", "Japan", "Germany")
    val grades = listOf("Grade 9", "Grade 10", "Grade 11", "Grade 12", "College Freshman", "College Sophomore", "Undergrad")

    var countryExpanded by remember { mutableStateOf(false) }
    var gradeExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.background(SurfaceDark, CircleShape)
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            LinearProgressIndicator(
                progress = { 0.4f },
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(CircleShape),
                color = IndigoDark,
                trackColor = SurfaceDark
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "About Yourself",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Help VLEARN configure your classroom experience.",
            fontSize = 14.sp,
            color = SlateGrey
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Your Name", color = SlateGrey) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("onboarding_name_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = IndigoDark,
                unfocusedBorderColor = SurfaceDark,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = SurfaceDark,
                unfocusedContainerColor = SurfaceDark
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Country Selection Box
        Text("Country", color = SlateGrey, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Box {
            OutlinedButton(
                onClick = { countryExpanded = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("onboarding_country_dropdown"),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, SurfaceDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(country, color = Color.White, fontSize = 16.sp)
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = SlateGrey)
                }
            }
            DropdownMenu(
                expanded = countryExpanded,
                onDismissRequest = { countryExpanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .background(SurfaceDark)
            ) {
                countries.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item, color = Color.White) },
                        onClick = {
                            onCountryChange(item)
                            countryExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Grade selection
        Text("Grade / Class", color = SlateGrey, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Box {
            OutlinedButton(
                onClick = { gradeExpanded = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("onboarding_grade_dropdown"),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, SurfaceDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(grade, color = Color.White, fontSize = 16.sp)
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = SlateGrey)
                }
            }
            DropdownMenu(
                expanded = gradeExpanded,
                onDismissRequest = { gradeExpanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .background(SurfaceDark)
            ) {
                grades.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item, color = Color.White) },
                        onClick = {
                            onGradeChange(item)
                            gradeExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onNext,
            enabled = name.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("onboarding_details_next"),
            colors = ButtonDefaults.buttonColors(
                containerColor = IndigoDark,
                disabledContainerColor = SurfaceDark
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Next Step",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (name.isNotBlank()) Color.White else SlateGrey
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next",
                    tint = if (name.isNotBlank()) Color.White else SlateGrey
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingSubjectsScreen(
    selectedSubjects: Set<String>,
    onToggleSubject: (String) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val subjectsList = listOf(
        Pair("Science", "🧪"),
        Pair("Math", "🧮"),
        Pair("Programming", "💻"),
        Pair("Geography", "🗺️"),
        Pair("History", "🏛️"),
        Pair("Literature", "📚"),
        Pair("Languages", "🗣️"),
        Pair("Commerce", "📈")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.background(SurfaceDark, CircleShape)
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            LinearProgressIndicator(
                progress = { 0.6f },
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(CircleShape),
                color = IndigoDark,
                trackColor = SurfaceDark
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "Select Your Subjects",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "VLEARN AI will auto-create digital dungeon worlds and challenges based on these study subjects.",
            fontSize = 14.sp,
            color = SlateGrey
        )

        Spacer(modifier = Modifier.height(32.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            subjectsList.forEach { pair ->
                val isSelected = selectedSubjects.contains(pair.first)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) IndigoDark else SurfaceDark)
                        .border(
                            width = 1.5.dp,
                            color = if (isSelected) GoldDark else Color.Transparent,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onToggleSubject(pair.first) }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(pair.second, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            pair.first,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onNext,
            enabled = selectedSubjects.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("onboarding_subjects_next"),
            colors = ButtonDefaults.buttonColors(
                containerColor = IndigoDark,
                disabledContainerColor = SurfaceDark
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Next Step",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selectedSubjects.isNotEmpty()) Color.White else SlateGrey
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next",
                    tint = if (selectedSubjects.isNotEmpty()) Color.White else SlateGrey
                )
            }
        }
    }
}

@Composable
fun OnboardingAvatarScreen(
    selectedAvatar: String,
    onAvatarSelected: (String) -> Unit,
    username: String,
    onUsernameChange: (String) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val avatars = listOf(
        Triple("avatar_1", "🌌", "Astro Cadet"),
        Triple("avatar_2", "🧬", "Bio Chemist"),
        Triple("avatar_3", "🧙", "Quantum Wizard"),
        Triple("avatar_4", "🦖", "Dino Historian"),
        Triple("avatar_5", "⚙️", "Cyber Mechanic")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.background(SurfaceDark, CircleShape)
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            LinearProgressIndicator(
                progress = { 0.8f },
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(CircleShape),
                color = IndigoDark,
                trackColor = SurfaceDark
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "Astro Character",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Select an digital avatar and register your universal nickname.",
            fontSize = 14.sp,
            color = SlateGrey
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Username
        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = { Text("Choose Username", color = SlateGrey) },
            singleLine = true,
            prefix = { Text("@", color = IndigoDark, fontWeight = FontWeight.Bold) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("onboarding_username_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = IndigoDark,
                unfocusedBorderColor = SurfaceDark,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = SurfaceDark,
                unfocusedContainerColor = SurfaceDark
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text("Select Avatar Character", color = SlateGrey, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        // Avatars grid row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            avatars.forEach { tri ->
                val isSelected = selectedAvatar == tri.first
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) IndigoDark else SurfaceDark)
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) GoldDark else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { onAvatarSelected(tri.first) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(tri.second, fontSize = 32.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selected avatar subtitle
        avatars.find { it.first == selectedAvatar }?.let { active ->
            Text(
                "Class: ${active.third}",
                color = GoldDark,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onNext,
            enabled = username.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("onboarding_avatar_next"),
            colors = ButtonDefaults.buttonColors(
                containerColor = IndigoDark,
                disabledContainerColor = SurfaceDark
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Launch Quest",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (username.isNotBlank()) Color.White else SlateGrey
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.RocketLaunch,
                    contentDescription = "Rocket",
                    tint = if (username.isNotBlank()) Color.White else SlateGrey
                )
            }
        }
    }
}

@Composable
fun OnboardingAiCreationScreen(
    role: String,
    name: String,
    country: String,
    grade: String,
    subjects: List<String>,
    avatar: String,
    username: String,
    viewModel: VLearnViewModel,
    onComplete: () -> Unit
) {
    var stepIndex by remember { mutableIntStateOf(0) }
    val steps = listOf(
        "Scanning Profile Options...",
        "Forging AI Academic Core...",
        "Structuring custom subject worlds...",
        "GCP Firebase storage registration...",
        "Sync complete! Launching VLEARN AI..."
    )

    LaunchedEffect(Unit) {
        while (stepIndex < steps.size - 1) {
            delay(1200)
            stepIndex++
        }
        delay(1000)
        viewModel.completeOnboarding(
            role = role,
            name = name,
            country = country,
            gradeClass = grade,
            subjects = subjects,
            avatar = avatar,
            username = username,
            onComplete = onComplete
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Glowing spinning celestial core
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = LinearOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )

        Box(
            modifier = Modifier
                .size(120.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .border(
                    width = 4.dp,
                    brush = Brush.sweepGradient(listOf(GoldDark, IndigoDark, LightEmerald, GoldDark)),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "AI Creating",
                tint = GoldDark,
                modifier = Modifier.size(56.dp)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            "AI Creating Cloud Profile",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = steps[stepIndex],
            fontSize = 15.sp,
            color = GoldDark,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Progress bar indicator
        LinearProgressIndicator(
            progress = { (stepIndex + 1).toFloat() / steps.size },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(6.dp)
                .clip(CircleShape),
            color = LightEmerald,
            trackColor = SurfaceDark
        )
    }
}
