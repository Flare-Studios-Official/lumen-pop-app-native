package com.togglestudios.lumenpop.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.togglestudios.lumenpop.R
import com.togglestudios.lumenpop.data.SavedProgress
import com.togglestudios.lumenpop.data.SupabaseManager
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AuthOverlayModal(
    progress: SavedProgress,
    supabaseManager: SupabaseManager,
    onClose: () -> Unit,
    onAuthSuccess: (String) -> Unit,
    onSignOut: () -> Unit
) {
    val session = supabaseManager.currentSession
    val isSignedIn = session != null

    var mode by remember { mutableStateOf<String>("menu") } // "menu", "sign-in", "sign-up"
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val totalStars = remember(progress.completed) {
        progress.completed.values.sumOf { it.stars }
    }
    val formattedShards = remember(progress.coins) {
        NumberFormat.getNumberInstance(Locale.US).format(progress.coins)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D032A).copy(alpha = 0.75f))
            .clickable { onClose() },
        contentAlignment = Alignment.Center
    ) {
        // Modal Card matching Image 3 (.modal relative w-full max-w-sm bg-[#130638])
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clickable(enabled = false) {} // Prevent click from dismissing
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0xFF130638))
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(32.dp))
                .shadow(32.dp, shape = RoundedCornerShape(32.dp))
                .padding(26.dp)
        ) {
            // Close 'X' button on top-right
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.TopEnd)
                    .clip(CircleShape)
                    .clickable { onClose() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✕",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSignedIn) {
                    // --- SIGNED-IN PROFILE VIEW ---
                    val userInitial = (session?.username?.take(1) ?: progress.username.take(1)).uppercase()

                    // Avatar Circle with Cyan Ring Glow
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .shadow(16.dp, CircleShape, spotColor = Color(0xFF00F0FF))
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF00F0FF), Color(0xFF9333EA))
                                )
                            )
                            .padding(2.5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color(0xFF130638)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userInitial,
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = session?.username ?: progress.username,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = session?.email ?: "Linked Account",
                        color = Color.White.copy(alpha = 0.55f),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Player Stats: 3 Cards (Progress, Stars, Shards)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Progress
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.06f))
                                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_crown),
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Lvl ${progress.highestUnlocked}",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "PROGRESS",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        // Stars
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.06f))
                                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_sparkles),
                                    contentDescription = null,
                                    tint = Color(0xFF00F0FF),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$totalStars",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "STARS",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        // Shards
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.06f))
                                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_gem),
                                    contentDescription = null,
                                    tint = Color(0xFFFF8CCC),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = formattedShards,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "SHARDS",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Sign Out Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFFF4500).copy(alpha = 0.12f))
                            .border(1.dp, Color(0xFFFF4500).copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .clickable {
                                supabaseManager.signOut()
                                onSignOut()
                                onClose()
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sign Out",
                            color = Color(0xFFFF8C8C),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    // --- SIGN-IN / SIGN-UP VIEW (Image 3) ---
                    // Circular Cyan User Icon
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00F0FF).copy(alpha = 0.12f))
                            .border(1.dp, Color(0xFF00F0FF).copy(alpha = 0.35f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_help),
                            contentDescription = null,
                            tint = Color(0xFF00F0FF),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Sign In to Lumen Pop",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Signing in is optional. Link an account to backup your progress across devices and keep your username secure.",
                        color = Color.White.copy(alpha = 0.62f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 17.sp,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Error notice if any
                    if (errorMessage != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFF3366).copy(alpha = 0.2f))
                                .border(1.dp, Color(0xFFFF3366).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                color = Color(0xFFFF99AA),
                                fontSize = 11.5.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    if (mode == "menu") {
                        // Button 1: "Continue with Google"
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(8.dp, RoundedCornerShape(15.dp))
                                .clip(RoundedCornerShape(15.dp))
                                .background(Color.White)
                                .clickable(enabled = !isLoading) {
                                    scope.launch {
                                        isLoading = true
                                        errorMessage = null
                                        val res = supabaseManager.signInAnonymousOrGuest(progress.username)
                                        isLoading = false
                                        if (res.isSuccess) {
                                            onAuthSuccess(progress.username)
                                            onClose()
                                        } else {
                                            errorMessage = res.exceptionOrNull()?.message ?: "Sign-in failed"
                                        }
                                    }
                                }
                                .padding(vertical = 13.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_google),
                                    contentDescription = "Google",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Continue with Google",
                                    color = Color(0xFF1F2937),
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Button 2: "Continue with Apple"
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(15.dp))
                                .background(Color(0xFF000000))
                                .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(15.dp))
                                .clickable(enabled = !isLoading) {
                                    scope.launch {
                                        isLoading = true
                                        errorMessage = null
                                        val res = supabaseManager.signInAnonymousOrGuest(progress.username)
                                        isLoading = false
                                        if (res.isSuccess) {
                                            onAuthSuccess(progress.username)
                                            onClose()
                                        } else {
                                            errorMessage = res.exceptionOrNull()?.message ?: "Apple Sign-in failed"
                                        }
                                    }
                                }
                                .padding(vertical = 13.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_apple),
                                    contentDescription = "Apple",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Continue with Apple",
                                    color = Color.White,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Divider: ─── OR ───
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                        ) {
                            HorizontalDivider(
                                color = Color.White.copy(alpha = 0.2f),
                                thickness = 1.dp,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "  OR  ",
                                color = Color.White.copy(alpha = 0.45f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                            HorizontalDivider(
                                color = Color.White.copy(alpha = 0.2f),
                                thickness = 1.dp,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Button 3: "Continue with Email"
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(15.dp))
                                .background(Color.White.copy(alpha = 0.12f))
                                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(15.dp))
                                .clickable { mode = "sign-in" }
                                .padding(vertical = 13.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_mail),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Continue with Email",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Link Afterwards (as requested in Phase 6 specification)
                        Text(
                            text = "Link Afterwards · Play as Stargazer",
                            color = Color(0xFF00F0FF).copy(alpha = 0.85f),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onClose()
                                }
                                .padding(vertical = 4.dp)
                        )
                    } else {
                        // Email Form (Sign In / Sign Up)
                        Column(modifier = Modifier.fillMaxWidth()) {
                            if (mode == "sign-up") {
                                OutlinedTextField(
                                    value = username,
                                    onValueChange = { username = it },
                                    placeholder = { Text("Stargazer username", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF00F0FF),
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                placeholder = { Text("Email address", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00F0FF),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                placeholder = { Text("Password (min 8 chars)", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                visualTransformation = PasswordVisualTransformation(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF00F0FF),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Submit Button
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0xFFFFE879), Color(0xFFFFBC43))
                                        )
                                    )
                                    .clickable(enabled = !isLoading) {
                                        focusManager.clearFocus()
                                        scope.launch {
                                            isLoading = true
                                            errorMessage = null
                                            if (mode == "sign-up") {
                                                val res = supabaseManager.signUp(email, password, username)
                                                isLoading = false
                                                if (res.isSuccess) {
                                                    onAuthSuccess(username)
                                                    onClose()
                                                } else {
                                                    errorMessage = res.exceptionOrNull()?.message ?: "Registration failed"
                                                }
                                            } else {
                                                val res = supabaseManager.signIn(email, password)
                                                isLoading = false
                                                if (res.isSuccess) {
                                                    val u = res.getOrNull()?.username ?: email
                                                    onAuthSuccess(u)
                                                    onClose()
                                                } else {
                                                    errorMessage = res.exceptionOrNull()?.message ?: "Sign-in failed"
                                                }
                                            }
                                        }
                                    }
                                    .padding(vertical = 13.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(color = Color(0xFF291063), modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Text(
                                        text = if (mode == "sign-up") "Create Account" else "Sign In",
                                        color = Color(0xFF291063),
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = if (mode == "sign-up") "Already have an account? Sign In" else "Need an account? Sign Up",
                                color = Color(0xFF00F0FF),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        mode = if (mode == "sign-up") "sign-in" else "sign-up"
                                        errorMessage = null
                                    }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "← Back to options",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        mode = "menu"
                                        errorMessage = null
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AuthScreen(
    supabaseManager: SupabaseManager,
    onAuthSuccess: (String) -> Unit
) {
    AuthOverlayModal(
        progress = SavedProgress(),
        supabaseManager = supabaseManager,
        onClose = { onAuthSuccess("Stargazer") },
        onAuthSuccess = onAuthSuccess,
        onSignOut = {}
    )
}
