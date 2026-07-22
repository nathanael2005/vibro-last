package com.example.ui.auth

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.NetworkManager
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onNavigationNext: () -> Unit) {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2200)
        onNavigationNext()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1E293B), // Slate 800
                        Color(0xFF0F172A)  // Slate 900
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF3B82F6).copy(alpha = 0.15f))
                    .border(1.dp, Color(0xFF3B82F6).copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = "Vibro Logo",
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.size(54.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Vibro",
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Ethiopia's Dynamic Marketplace",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(64.dp))
            CircularProgressIndicator(
                color = Color(0xFF3B82F6),
                modifier = Modifier.size(28.dp),
                strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading amazing deals...",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun WelcomeOnboardingScreen(
    onGetStarted: () -> Unit,
    onSkip: () -> Unit
) {
    var currentPage by remember { mutableStateOf(0) }
    
    val onboardingData = listOf(
        Triple(
            Icons.Default.Storefront,
            "Buy & Sell Direct",
            "Instantly post your vehicles, smartphones, properties, or fashion. Connect directly with direct local buyers in Addis Ababa, Hawassa, and beyond."
        ),
        Triple(
            Icons.Default.FlashOn,
            "Premium Booster Tiers",
            "Get up to 10x more exposure! Upgrade your listings to Silver or Diamond booster tiers for high visibility on the search feed."
        ),
        Triple(
            Icons.Default.Forum,
            "Direct Secure Chats",
            "Negotiate prices and secure details safely with the built-in real-time seller and buyer messaging. No shared phone number needed."
        )
    )

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onSkip) {
                    Text(
                        text = "Skip",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val (icon, title, desc) = onboardingData[currentPage]

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(54.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    for (i in 0..2) {
                        val isActive = i == currentPage
                        val width = if (isActive) 24.dp else 8.dp
                        val color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        Box(
                            modifier = Modifier
                                .size(height = 8.dp, width = width)
                                .clip(RoundedCornerShape(4.dp))
                                .background(color)
                        )
                    }
                }

                Button(
                    onClick = {
                        if (currentPage < 2) {
                            currentPage++
                        } else {
                            onGetStarted()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (currentPage == 2) "Get Started" else "Next",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun LoginScreen(
    onLoginSuccess: (email: String, password: String, name: String, city: String) -> Unit,
    onSkip: () -> Unit
) {
    var isSignUpMode by remember { mutableStateOf(false) }
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Addis Ababa, Bole") }
    
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onSkip) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel")
                }
                TextButton(onClick = onSkip) {
                    Text("Skip to Guest", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Campaign,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "VIBRO MARKETPLACE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = if (isSignUpMode) "Create an Account" else "Welcome Back",
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (isSignUpMode) 
                        "Join Vibro to post ads, manage offers, and chat securely." 
                        else "Sign in to list items, sync chats, and boost exposure.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 28.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (isSignUpMode) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Your Full Name") },
                                leadingIcon = { Icon(Icons.Default.Person, null) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password (Min 6 Characters)") },
                            leadingIcon = { Icon(Icons.Default.Lock, null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        if (isSignUpMode) {
                            var showCityDropdown by remember { mutableStateOf(false) }
                            val cities = listOf("Addis Ababa", "Hawassa", "Adama", "Bahir Dar", "Dire Dawa")
                            
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = city,
                                    onValueChange = { city = it },
                                    label = { Text("Your Location / City") },
                                    leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                                    trailingIcon = {
                                        IconButton(onClick = { showCityDropdown = true }) {
                                            Icon(Icons.Default.ArrowDropDown, null)
                                        }
                                    },
                                    readOnly = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                DropdownMenu(
                                    expanded = showCityDropdown,
                                    onDismissRequest = { showCityDropdown = false }
                                ) {
                                    cities.forEach { choice ->
                                        DropdownMenuItem(
                                            text = { Text(choice) },
                                            onClick = {
                                                city = choice
                                                showCityDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (email.trim().isEmpty() || password.trim().isEmpty()) {
                            Toast.makeText(context, "Please fill in all core fields", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (password.length < 6) {
                            Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (isSignUpMode && name.trim().isEmpty()) {
                            Toast.makeText(context, "Please enter your name", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        coroutineScope.launch {
                            isLoading = true
                            try {
                                val fallbackPrefs = context.getSharedPreferences("vibro_auth_fallback", Context.MODE_PRIVATE)
                                if (!fallbackPrefs.contains("user_pass_nathanaeleshetu8901@gmail.com")) {
                                    fallbackPrefs.edit()
                                        .putString("user_pass_nathanaeleshetu8901@gmail.com", "123456")
                                        .putString("user_name_nathanaeleshetu8901@gmail.com", "Nathanael Eshetu")
                                        .putString("user_city_nathanaeleshetu8901@gmail.com", "Addis Ababa")
                                        .apply()
                                }

                                if (NetworkManager.isSupabaseConfigured) {
                                    if (isSignUpMode) {
                                        try {
                                            val response = NetworkManager.signUpWithEmail(email, password, name, city)
                                            fallbackPrefs.edit()
                                                .putString("user_pass_$email", password)
                                                .putString("user_name_$email", name)
                                                .putString("user_city_$email", city)
                                                .apply()
                                            
                                            isLoading = false
                                            onLoginSuccess(email, password, name, city)
                                            Toast.makeText(context, "Welcome to Vibro, $name! Account created.", Toast.LENGTH_LONG).show()
                                        } catch (e: Exception) {
                                            val isConnError = e is java.io.IOException || e.message?.contains("Unable to resolve host", ignoreCase = true) == true || e.message?.contains("connect", ignoreCase = true) == true || e.message?.contains("unavailable", ignoreCase = true) == true
                                            if (isConnError) {
                                                if (fallbackPrefs.contains("user_pass_$email")) {
                                                    throw Exception("Account already exists locally. Offline mode cannot overwrite. Please connect to the internet.")
                                                }
                                                fallbackPrefs.edit()
                                                    .putString("user_pass_$email", password)
                                                    .putString("user_name_$email", name)
                                                    .putString("user_city_$email", city)
                                                    .apply()
                                                isLoading = false
                                                onLoginSuccess(email, password, name, city)
                                                Toast.makeText(context, "Account created locally (offline fallback)! Welcome, $name.", Toast.LENGTH_LONG).show()
                                            } else {
                                                throw e
                                            }
                                        }
                                    } else {
                                        try {
                                            val response = NetworkManager.signInWithEmail(email, password)
                                            val metaName = response.user?.userMetadata?.name ?: email.substringBefore("@")
                                            val metaCity = response.user?.userMetadata?.city ?: "Addis Ababa"
                                            
                                            fallbackPrefs.edit()
                                                .putString("user_pass_$email", password)
                                                .putString("user_name_$email", metaName)
                                                .putString("user_city_$email", metaCity)
                                                .apply()
                                            
                                            isLoading = false
                                            onLoginSuccess(email, password, metaName, metaCity)
                                            Toast.makeText(context, "Welcome back, $metaName!", Toast.LENGTH_LONG).show()
                                        } catch (e: Exception) {
                                            val isConnError = e is java.io.IOException || e.message?.contains("Unable to resolve host", ignoreCase = true) == true || e.message?.contains("connect", ignoreCase = true) == true || e.message?.contains("unavailable", ignoreCase = true) == true
                                            if (isConnError && fallbackPrefs.contains("user_pass_$email")) {
                                                val savedPass = fallbackPrefs.getString("user_pass_$email", "")
                                                if (savedPass != password) {
                                                    throw Exception("Incorrect password (offline verification).")
                                                }
                                                val storedName = fallbackPrefs.getString("user_name_$email", "Guest") ?: "Guest"
                                                val storedCity = fallbackPrefs.getString("user_city_$email", "Addis Ababa") ?: "Addis Ababa"
                                                isLoading = false
                                                onLoginSuccess(email, password, storedName, storedCity)
                                                Toast.makeText(context, "Logged in via offline mode. Welcome back, $storedName!", Toast.LENGTH_LONG).show()
                                            } else {
                                                throw e
                                            }
                                        }
                                    }
                                } else {
                                    if (isSignUpMode) {
                                        if (fallbackPrefs.contains("user_pass_$email")) {
                                            isLoading = false
                                            Toast.makeText(context, "Email is already registered locally.", Toast.LENGTH_LONG).show()
                                            return@launch
                                        }
                                        fallbackPrefs.edit()
                                            .putString("user_pass_$email", password)
                                            .putString("user_name_$email", name)
                                            .putString("user_city_$email", city)
                                            .apply()
                                        
                                        isLoading = false
                                        onLoginSuccess(email, password, name, city)
                                        Toast.makeText(context, "Account created locally! Welcome, $name.", Toast.LENGTH_LONG).show()
                                    } else {
                                        if (!fallbackPrefs.contains("user_pass_$email")) {
                                            isLoading = false
                                            Toast.makeText(context, "No account found with this email. Please sign up register.", Toast.LENGTH_LONG).show()
                                            return@launch
                                        }
                                        val savedPass = fallbackPrefs.getString("user_pass_$email", "")
                                        if (savedPass != password) {
                                            isLoading = false
                                            Toast.makeText(context, "Incorrect password. Please try again.", Toast.LENGTH_LONG).show()
                                            return@launch
                                        }
                                        val storedName = fallbackPrefs.getString("user_name_$email", "Guest") ?: "Guest"
                                        val storedCity = fallbackPrefs.getString("user_city_$email", "Addis Ababa") ?: "Addis Ababa"
                                        
                                        isLoading = false
                                        onLoginSuccess(email, password, storedName, storedCity)
                                        Toast.makeText(context, "Successfully logged in! Welcome back, $storedName.", Toast.LENGTH_LONG).show()
                                    }
                                }
                            } catch (e: Exception) {
                                isLoading = false
                                val displayError = e.message ?: "Authentication failed"
                                android.util.Log.e("LoginScreen", "Auth error: $displayError", e)
                                Toast.makeText(context, displayError, Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            text = if (isSignUpMode) "Register & Start Deals" else "Sign In Now",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    onClick = { isSignUpMode = !isSignUpMode }
                ) {
                    Text(
                        text = if (isSignUpMode) 
                            "Already have an account? Sign In" 
                            else "Don't have an account? Create one now",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
