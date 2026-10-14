package com.togglestudios.lumenpop.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.togglestudios.lumenpop.data.SupabaseManager
import kotlinx.coroutines.launch

enum class AuthStage {
    CLAIM_USERNAME,
    CHOOSE_METHOD,
    WELCOME_BACK
}

@Composable
fun AuthScreen(
    supabaseManager: SupabaseManager,
    onAuthSuccess: (String) -> Unit
) {
    var stage by remember { mutableStateOf(AuthStage.CLAIM_USERNAME) }

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    var loginIdentifier by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var showLoginPassword by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoDialogText by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0D0422), Color(0xFF1B0B3B), Color(0xFF281155))
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0xFF12053C).copy(alpha = 0.92f))
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(32.dp))
                .padding(26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Glow Icon Header
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00F0FF).copy(alpha = 0.12f))
                    .border(1.dp, Color(0xFF00F0FF).copy(alpha = 0.35f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (stage == AuthStage.WELCOME_BACK) "✨" else "🪐",
                    fontSize = 28.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "LUMEN POP",
                color = Color(0xFF00F0FF),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = when (stage) {
                    AuthStage.CLAIM_USERNAME -> "Claim Your Username"
                    AuthStage.CHOOSE_METHOD -> "Choose Sign-In Method"
                    AuthStage.WELCOME_BACK -> "Welcome Back"
                },
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = when (stage) {
                    AuthStage.CLAIM_USERNAME -> "Pick a unique stargazer username and a secure password."
                    AuthStage.CHOOSE_METHOD -> "Connect an identity to save your cosmic progress across devices."
                    AuthStage.WELCOME_BACK -> "Sign in to recover your levels, shards, and stars."
                },
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Error Message
            AnimatedVisibility(visible = errorMessage != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFF1493).copy(alpha = 0.15f))
                        .border(1.dp, Color(0xFFFF1493).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFFF69B4),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            when (stage) {
                AuthStage.CLAIM_USERNAME -> {
                    // Username Field
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it.lowercase().trim()
                            errorMessage = null
                        },
                        label = { Text("Username (e.g. nova_7)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00F0FF),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = Color(0xFF00F0FF),
                            unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        label = { Text("Password (min 8 chars)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            Text(
                                text = if (showPassword) "👁" else "🔒",
                                modifier = Modifier
                                    .clickable { showPassword = !showPassword }
                                    .padding(8.dp),
                                fontSize = 16.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00F0FF),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = Color(0xFF00F0FF),
                            unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Next Button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            val clean = username.trim().lowercase()
                            if (clean.length < 3 || clean.length > 12) {
                                errorMessage = "Username must be between 3 and 12 characters"
                                return@Button
                            }
                            if (!clean.matches(Regex("^[a-z0-9_-]+$"))) {
                                errorMessage = "Only lowercase letters, numbers, _, and - allowed"
                                return@Button
                            }
                            if (password.length < 8) {
                                errorMessage = "Password must be at least 8 characters long"
                                return@Button
                            }

                            isLoading = true
                            errorMessage = null
                            scope.launch {
                                val availRes = supabaseManager.checkUsernameAvailable(clean)
                                isLoading = false
                                if (availRes.isSuccess && availRes.getOrNull() == true) {
                                    stage = AuthStage.CHOOSE_METHOD
                                } else {
                                    errorMessage = "Username is already claimed. Try another!"
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color(0xFF0D0422),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Next →",
                                color = Color(0xFF0D0422),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sign In Link
                    TextButton(
                        onClick = {
                            errorMessage = null
                            stage = AuthStage.WELCOME_BACK
                        }
                    ) {
                        Text(
                            text = "Sign in to existing account",
                            color = Color(0xFF00F0FF),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                AuthStage.CHOOSE_METHOD -> {
                    // Option 1: Continue with Google
                    Button(
                        onClick = {
                            infoDialogText = "Google Sign-In is configured on the backend. For quick play, tap 'Link Afterwards' below to begin immediately."
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        enabled = !isLoading
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🌐", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Continue with Google",
                                color = Color(0xFF1F1F1F),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Option 2: Continue with Apple
                    Button(
                        onClick = {
                            infoDialogText = "Apple Sign-In is supported for cross-platform iOS accounts. Use 'Link Afterwards' to proceed with your unique credentials."
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                        border = ButtonDefaults.outlinedButtonBorder,
                        enabled = !isLoading
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "", fontSize = 20.sp, color = Color.White)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Continue with Apple",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Option 3: Continue with Email
                    Button(
                        onClick = {
                            infoDialogText = "Your username '$username' is already mapped to your persistent account. Tap 'Link Afterwards' to enter the cosmos."
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.12f)),
                        enabled = !isLoading
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "✉", fontSize = 18.sp, color = Color.White)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Continue with Email",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Link Afterwards
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(26.dp),
                            color = Color(0xFF00F0FF),
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            text = "Link Afterwards",
                            color = Color(0xFFFFD700),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    isLoading = true
                                    errorMessage = null
                                    scope.launch {
                                        val regRes = supabaseManager.signUpWithUsername(username, password)
                                        isLoading = false
                                        if (regRes.isSuccess) {
                                            onAuthSuccess(username)
                                        } else {
                                            errorMessage = regRes.exceptionOrNull()?.message ?: "Account creation failed"
                                        }
                                    }
                                }
                                .padding(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    TextButton(
                        onClick = {
                            errorMessage = null
                            stage = AuthStage.CLAIM_USERNAME
                        }
                    ) {
                        Text(text = "← Back to Username", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                    }
                }

                AuthStage.WELCOME_BACK -> {
                    // Login identifier field
                    OutlinedTextField(
                        value = loginIdentifier,
                        onValueChange = {
                            loginIdentifier = it
                            errorMessage = null
                        },
                        label = { Text("Username or Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00F0FF),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = Color(0xFF00F0FF),
                            unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Login password field
                    OutlinedTextField(
                        value = loginPassword,
                        onValueChange = {
                            loginPassword = it
                            errorMessage = null
                        },
                        label = { Text("Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        visualTransformation = if (showLoginPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            Text(
                                text = if (showLoginPassword) "👁" else "🔒",
                                modifier = Modifier
                                    .clickable { showLoginPassword = !showLoginPassword }
                                    .padding(8.dp),
                                fontSize = 16.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00F0FF),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = Color(0xFF00F0FF),
                            unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Sign In Button
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            val clean = loginIdentifier.trim()
                            if (clean.isEmpty() || loginPassword.isEmpty()) {
                                errorMessage = "Please enter both your username and password"
                                return@Button
                            }

                            isLoading = true
                            errorMessage = null
                            scope.launch {
                                val loginRes = supabaseManager.signInWithUsername(clean, loginPassword)
                                isLoading = false
                                if (loginRes.isSuccess) {
                                    val session = loginRes.getOrNull()
                                    onAuthSuccess(session?.username ?: clean)
                                } else {
                                    errorMessage = "Invalid username or password. Please verify your credentials."
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color(0xFF0D0422),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Sign In",
                                color = Color(0xFF0D0422),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Forgot Password?
                    TextButton(
                        onClick = {
                            infoDialogText = "To reset your password, contact Flare Studios support or log in via your linked recovery email."
                        }
                    ) {
                        Text(
                            text = "Forgot Password?",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Switch to Claim Username
                    TextButton(
                        onClick = {
                            errorMessage = null
                            stage = AuthStage.CLAIM_USERNAME
                        }
                    ) {
                        Text(
                            text = "Need an account? Claim your username",
                            color = Color(0xFF00F0FF),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Info popup
        if (infoDialogText != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable { infoDialogText = null },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF1B0B3B))
                        .border(1.dp, Color(0xFF00F0FF).copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                        .padding(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "✨", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Account Information",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = infoDialogText ?: "",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = { infoDialogText = null },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = "Got it", color = Color(0xFF0D0422), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
