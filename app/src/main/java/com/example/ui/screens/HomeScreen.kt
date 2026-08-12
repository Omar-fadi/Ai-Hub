package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VideoCameraBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CoinBadge
import com.example.ui.components.LevelBar
import com.example.ui.components.StreakBanner
import com.example.ui.components.ToolCard
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CoinGold
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PrimaryViolet
import com.example.ui.theme.PrimaryVioletLight
import com.example.ui.theme.SecondaryCyan
import com.example.ui.theme.TertiaryPink
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToChat: () -> Unit,
    onNavigateToCreateTool: (Int) -> Unit, // 0 = Image, 1 = Vision, 2 = Speech, 3 = Video
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val wallet by viewModel.walletState.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    var quickPromptText by remember { mutableStateOf("") }

    val quickPrompts = listOf(
        "💡 Explain Quantum Physics simply",
        "🎨 Generate Algiers 2050 art",
        "💻 Write a Kotlin Compose snippet",
        "📊 Summarize latest tech trends"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Top App Bar & Profile / Coins Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AI HUB",
                        color = PrimaryVioletLight,
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Good day, ${wallet?.username ?: "Creator"}",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    CoinBadge(
                        coins = wallet?.coins ?: 0,
                        onClick = onNavigateToProfile
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant)
                            .border(1.dp, PrimaryViolet, CircleShape)
                            .clickable { onNavigateToProfile() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = wallet?.username?.take(1)?.uppercase() ?: "A",
                            color = PrimaryViolet,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        // Level & XP Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                LevelBar(
                    level = wallet?.level ?: 1,
                    xp = wallet?.xp ?: 0,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // Daily Streak Banner
        item {
            StreakBanner(
                streakDays = wallet?.streakDays ?: 1,
                onClaimCheckIn = { viewModel.claimDailyCheckIn() }
            )
        }

        // Central Ask Box
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Assistant",
                            tint = PrimaryViolet,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "What can I help with today?",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = quickPromptText,
                        onValueChange = { quickPromptText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_ask_input"),
                        placeholder = { Text("Ask anything or choose prompt below...", color = TextMuted, fontSize = 13.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurfaceVariant,
                            unfocusedContainerColor = DarkSurfaceVariant,
                            focusedBorderColor = PrimaryViolet,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (quickPromptText.isNotBlank()) {
                                        viewModel.sendChatMessage(quickPromptText)
                                        quickPromptText = ""
                                        onNavigateToChat()
                                    }
                                },
                                modifier = Modifier.testTag("home_ask_submit")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send",
                                    tint = PrimaryViolet
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(quickPrompts) { prompt ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = DarkSurfaceVariant,
                                modifier = Modifier.clickable {
                                    quickPromptText = prompt.substringAfter(" ")
                                }
                            ) {
                                Text(
                                    text = prompt,
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section Title: AI Tools
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AI CREATIVE TOOLS",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "See All",
                    color = PrimaryViolet,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToCreateTool(0) }
                )
            }
        }

        // 2x2 Grid for AI Tools
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        ToolCard(
                            title = "AI Chat",
                            subtitle = "Multi-turn assistant",
                            costBadge = "3 Coins",
                            icon = Icons.Default.AutoAwesome,
                            accentColor = PrimaryViolet,
                            onClick = onNavigateToChat
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        ToolCard(
                            title = "AI Image",
                            subtitle = "Text to image studio",
                            costBadge = "20 Coins",
                            icon = Icons.Default.Palette,
                            accentColor = SecondaryCyan,
                            onClick = { onNavigateToCreateTool(0) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        ToolCard(
                            title = "AI Vision",
                            subtitle = "Photo & Doc analyzer",
                            costBadge = "8 Coins",
                            icon = Icons.Default.CameraAlt,
                            accentColor = TertiaryPink,
                            onClick = { onNavigateToCreateTool(1) }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        ToolCard(
                            title = "Speech → Text",
                            subtitle = "Voice transcription",
                            costBadge = "10 Coins",
                            icon = Icons.Default.Mic,
                            accentColor = CoinGold,
                            onClick = { onNavigateToCreateTool(2) }
                        )
                    }
                }
            }
        }

        // Recent Activity
        item {
            Text(
                text = "RECENT TRANSACTIONS",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 1.sp
            )
        }

        items(transactions.take(3)) { tx ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = tx.title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = tx.type, color = TextMuted, fontSize = 11.sp)
                    }
                    Text(
                        text = if (tx.amount > 0) "+${tx.amount} Coins" else "${tx.amount} Coins",
                        color = if (tx.amount > 0) SecondaryCyan else CoinGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}
