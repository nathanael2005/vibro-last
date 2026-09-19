package com.nate.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.nate.app.data.local.PreferencesManager
import com.nate.app.ui.theme.DarkBackground
import com.nate.app.ui.theme.DarkSurfaceVariant
import com.nate.app.ui.theme.TextPrimary
import com.nate.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SettingsScreen(
    preferencesManager: PreferencesManager,
    modifier: Modifier = Modifier
) {
    val apiKey by preferencesManager.apiKeyFlow.collectAsState(initial = "")

    var inputKey by remember(apiKey) { mutableStateOf(apiKey ?: "") }
    var isTextFieldFocused by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    TvLazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Main Title Header
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.displayMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Configure your streaming credentials and preferences locally.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )
        }

        // Section 1: TMDB API Key settings
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "TMDB (The Movie Database) API Key",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Enter your free TMDB developer key to request metadata. Keys are stored strictly on your device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // D-pad focused Input box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isTextFieldFocused) Color.White.copy(alpha = 0.15f) else DarkSurfaceVariant)
                            .onFocusChanged { isTextFieldFocused = it.isFocused }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = inputKey,
                            onValueChange = { inputKey = it },
                            textStyle = TextStyle(
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            cursorBrush = SolidColor(TextPrimary),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                    scope.launch {
                                        preferencesManager.setApiKey(inputKey.trim())
                                    }
                                }
                            ),
                            decorationBox = { innerTextField ->
                                if (inputKey.isEmpty()) {
                                    Text(
                                        text = "Paste or type your TMDB API Key...",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = TextSecondary
                                    )
                                }
                                innerTextField()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Save Button
                    Button(
                        onClick = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            scope.launch {
                                preferencesManager.setApiKey(inputKey.trim())
                            }
                        },
                        scale = ButtonDefaults.scale(focusedScale = 1.08f),
                        colors = ButtonDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.secondary,
                            focusedContentColor = TextPrimary
                        ),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text("Save Key", fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.weight(1f))

                Column(
                    modifier = Modifier.padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "This product uses the TMDB API but is not endorsed or certified by TMDB.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "Version 1.2.0 (Stable)",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
