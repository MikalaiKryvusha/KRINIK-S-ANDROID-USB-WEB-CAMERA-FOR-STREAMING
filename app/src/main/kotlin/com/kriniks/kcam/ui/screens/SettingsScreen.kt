/**
 * SettingsScreen — app-wide settings panel.
 *
 * Phase 1 sections:
 *   1. Streaming Platforms — same data as StreamPlatformsOverlay (Q3 answer),
 *      modifications are instantly reflected in the overlay and vice versa.
 *   2. Debug — share latest log file button.
 *   3. About — version, GitHub link, author.
 *
 * Related: StreamViewModel (profiles sync), FileLogger (:core:logging), NavGraph
 */

package com.kriniks.kcam.ui.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kriniks.kcam.core.logging.FileLogger
import com.kriniks.kcam.feature.streaming.ui.StreamPlatformsOverlay
import com.kriniks.kcam.feature.streaming.ui.StreamViewModel

private val AcidPink = Color(0xFFFF1A8C)
private val DarkSurface = Color(0xFF1A1A1A)
private val DarkBg = Color(0xFF0D0D0D)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    fileLogger: FileLogger,
    streamViewModel: StreamViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val profiles by streamViewModel.profiles.collectAsStateWithLifecycle()
    val activeProfile by streamViewModel.activeProfile.collectAsStateWithLifecycle()
    var showPlatforms by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg),
            )
        },
        containerColor = DarkBg,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Streaming Platforms ───────────────────────────────────
            SettingsSection(title = "Streaming") {
                SettingsRow(
                    icon = Icons.Default.Wifi,
                    title = "Platforms",
                    subtitle = if (profiles.isEmpty()) "No platforms configured"
                               else "${profiles.size} platform${if (profiles.size > 1) "s" else ""} · tap to manage",
                    onClick = { showPlatforms = true },
                )
                if (activeProfile != null) {
                    SettingsRow(
                        icon = Icons.Default.PlayArrow,
                        title = "Active profile",
                        subtitle = activeProfile!!.name,
                        onClick = { showPlatforms = true },
                    )
                }
            }

            // ── Debug / Logging ───────────────────────────────────────
            SettingsSection(title = "Debug") {
                SettingsRow(
                    icon = Icons.Default.BugReport,
                    title = "Share log file",
                    subtitle = "Send today's debug log for analysis",
                    onClick = {
                        val intent = fileLogger.shareIntent()
                        context.startActivity(Intent.createChooser(intent, "Share KrinikCam log"))
                    },
                )
            }

            // ── About ─────────────────────────────────────────────────
            SettingsSection(title = "About") {
                SettingsRow(
                    icon = Icons.Default.Info,
                    title = "KrinikCam",
                    subtitle = "Open-source USB webcam streamer · MIT License",
                )
                SettingsRow(
                    icon = Icons.Default.Person,
                    title = "Author",
                    subtitle = "Mikalai Kryvusha aka KOT KRINIK",
                )
            }
        }
    }

    // Platforms overlay — same component as FloatingRadialMenu opens (Q3 answer)
    if (showPlatforms) {
        StreamPlatformsOverlay(
            profiles = profiles,
            activeProfileId = activeProfile?.id,
            onDismiss = { showPlatforms = false },
            onSelectProfile = { streamViewModel.selectProfile(it) },
            onSaveProfile = { streamViewModel.saveProfile(it) },
            onDeleteProfile = { streamViewModel.deleteProfile(it) },
            onStartStream = {},
        )
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title.uppercase(),
            color = AcidPink,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp, top = 4.dp),
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
        ) {
            Column { content() }
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(icon, contentDescription = null, tint = AcidPink, modifier = Modifier.size(22.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Text(subtitle, color = Color(0xFF888888), fontSize = 12.sp)
            }
        }
        if (onClick != null) {
            Icon(Icons.Default.ChevronRight, null, tint = Color(0xFF555555))
        }
    }
}
