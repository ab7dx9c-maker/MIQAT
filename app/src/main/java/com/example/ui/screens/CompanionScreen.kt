package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.AiChatMessage
import com.example.ui.CompanionUiState
import com.example.ui.components.LuxuryGlassCard
import com.example.ui.theme.LocalMeeqatPalette
import com.example.ui.theme.LocalMeeqatStrings
import com.example.ui.theme.StatusCrimsonBright

@Composable
fun CompanionScreen(
    messages: List<AiChatMessage>,
    companionState: CompanionUiState,
    onSendQuery: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalMeeqatPalette.current
    val s = LocalMeeqatStrings.current
    var inputText by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()

    val quickPrompts = s.companionQuickPrompts

    LaunchedEffect(messages.size, messages.lastOrNull()?.text?.length) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .padding(bottom = 84.dp)
        ) {
            // Quick suggestion chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickPrompts.forEachIndexed { idx, prompt ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = palette.badgeBg,
                        border = BorderStroke(0.8.dp, palette.goldBorder),
                        modifier = Modifier
                            .height(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSendQuery(prompt) }
                            .testTag("companion_quick_prompt_$idx")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = palette.goldPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = prompt,
                                style = MaterialTheme.typography.labelMedium,
                                color = palette.textPrimary
                            )
                        }
                    }
                }
            }

            // Streaming chat messages
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (msg.isUser) Arrangement.Start else Arrangement.End
                    ) {
                        LuxuryGlassCard(
                            highlighted = msg.isUser,
                            cornerRadius = 18.dp,
                            modifier = Modifier.widthIn(max = 310.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = msg.text.ifBlank { s.companionThinking },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = palette.textPrimary
                                )
                                if (msg.isStreaming) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = palette.goldPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                if (companionState is CompanionUiState.Error) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = StatusCrimsonBright.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, StatusCrimsonBright),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = companionState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = StatusCrimsonBright,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }

            // Input Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = s.companionPlaceholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.textSecondary
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(22.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = palette.goldAccent,
                        unfocusedBorderColor = palette.goldBorder,
                        focusedContainerColor = palette.glassElevated,
                        unfocusedContainerColor = palette.glassSurface,
                        focusedTextColor = palette.textPrimary,
                        unfocusedTextColor = palette.textPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("companion_input_field")
                )

                val isBusy = companionState == CompanionUiState.Loading ||
                    companionState == CompanionUiState.Streaming
                Surface(
                    shape = CircleShape,
                    color = palette.activeChipBg,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .clickable(enabled = !isBusy && inputText.isNotBlank()) {
                            val q = inputText
                            inputText = ""
                            onSendQuery(q)
                        }
                        .testTag("companion_send_button")
                ) {
                    Box(
                        modifier = Modifier.background(palette.activeChipBg),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isBusy) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = palette.activeChipText
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = s.companionSendDesc,
                                tint = palette.activeChipText
                            )
                        }
                    }
                }
            }
        }
    }
}
