/**
 * MainScreen — fullscreen camera viewfinder with floating overlay controls.
 *
 * Layout:
 *   Layer 0: fullscreen viewfinder (UVC preview or phone camera or black screen)
 *   Layer 1: StandbyPlaceholder (when no source available)
 *   Layer 2: Live status indicator (top-left when streaming)
 *   Layer 3: FloatingRadialMenu (bottom-right FAB + radial actions)
 *   Layer 4: StreamPlatformsOverlay (modal, shown on demand)
 *
 * Source priority (Q1 answer) is managed by DeviceManager.
 * Related: UsbViewModel, StreamViewModel, DeviceManager, FloatingRadialMenu
 */

package com.kriniks.kcam.ui.screens

import android.view.TextureView
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kriniks.kcam.feature.capture.DeviceManager
import com.kriniks.kcam.feature.capture.model.VideoSource
import com.kriniks.kcam.feature.streaming.model.StreamState
import com.kriniks.kcam.feature.streaming.model.isLive
import com.kriniks.kcam.feature.streaming.ui.StreamPlatformsOverlay
import com.kriniks.kcam.feature.streaming.ui.StreamViewModel
import com.kriniks.kcam.feature.usb.ui.NoSourceView
import com.kriniks.kcam.feature.usb.ui.UsbViewModel
import com.kriniks.kcam.feature.usb.ui.UvcPreviewView
import com.kriniks.kcam.ui.overlay.FloatingRadialMenu
import com.kriniks.kcam.ui.overlay.StandbyPlaceholder

private val AcidPink = Color(0xFFFF1A8C)
private val LiveRed = Color(0xFFFF1A1A)

@Composable
fun MainScreen(
    onNavigateToSettings: () -> Unit,
    deviceManager: DeviceManager,
    usbViewModel: UsbViewModel = hiltViewModel(),
    streamViewModel: StreamViewModel = hiltViewModel(),
) {
    val usbState by usbViewModel.uiState.collectAsStateWithLifecycle()
    val streamState by streamViewModel.streamState.collectAsStateWithLifecycle()
    val profiles by streamViewModel.profiles.collectAsStateWithLifecycle()
    val activeProfile by streamViewModel.activeProfile.collectAsStateWithLifecycle()
    val activeSource by deviceManager.activeVideoSource.collectAsStateWithLifecycle()

    var showPlatformsOverlay by remember { mutableStateOf(false) }

    // Bridge USB events → DeviceManager (keeps :feature:usb decoupled from :feature:capture)
    LaunchedEffect(usbState.activeCameraId) {
        val id = usbState.activeCameraId
        if (id != null) {
            val device = usbState.connectedDevices.firstOrNull { it.deviceId == id }
            if (device != null) {
                deviceManager.notifyUvcConnected(
                    com.kriniks.kcam.feature.capture.model.VideoSource.UvcCamera(
                        id          = id.toString(),
                        displayName = device.productName ?: "USB Camera",
                        vendorId    = device.vendorId,
                        productId   = device.productId,
                    )
                )
            }
        }
    }
    LaunchedEffect(usbState.connectedDevices.size) {
        // Detect disconnection: if activeCameraId is no longer in connectedDevices
        val active = usbState.activeCameraId ?: return@LaunchedEffect
        if (usbState.connectedDevices.none { it.deviceId == active }) {
            deviceManager.notifyUvcDisconnected(active.toString())
        }
    }

    // TextureView reference from UVC preview — shared with RtmpStreamer
    var previewTexture by remember { mutableStateOf<TextureView?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        // ── Layer 0: Viewfinder ──────────────────────────────────────
        when (val source = activeSource) {
            is VideoSource.UvcCamera -> {
                val camera = usbState.activeCamera
                if (camera != null) {
                    UvcPreviewView(
                        camera = camera,
                        modifier = Modifier.fillMaxSize(),
                        onSurfaceReady = { tv ->
                            previewTexture = tv
                            streamViewModel.attachPreviewSurface(tv)
                        },
                    )
                } else {
                    // Camera source registered but not yet opened
                    StandbyPlaceholder(modifier = Modifier.fillMaxSize())
                }
            }
            is VideoSource.PhoneCamera -> {
                // TODO Phase 1: phone camera preview via Camera2 / CameraX
                // For now show standby with a hint
                StandbyPlaceholder(
                    message = "Phone camera preview coming soon",
                    modifier = Modifier.fillMaxSize(),
                )
            }
            VideoSource.None -> {
                StandbyPlaceholder(
                    message = "Connect a USB webcam via OTG,\nor check Settings for help",
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        // ── Layer 1: Live indicator (top-left) ───────────────────────
        AnimatedVisibility(
            visible = streamState.isLive,
            enter = fadeIn() + slideInHorizontally(),
            exit = fadeOut() + slideOutHorizontally(),
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp),
        ) {
            LiveBadge(streamState)
        }

        // ── Layer 2: Snackbar for stream errors / warnings ───────────
        val snackbarHostState = remember { SnackbarHostState() }
        LaunchedEffect(Unit) {
            streamViewModel.snackbar.collect { msg ->
                snackbarHostState.showSnackbar(msg, duration = SnackbarDuration.Short)
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp),
        )

        // ── Layer 3: Radial FAB menu (bottom-right) ──────────────────
        FloatingRadialMenu(
            streamState = streamState,
            onStartStream = { streamViewModel.startStream() },
            onStopStream = { streamViewModel.stopStream() },
            onOpenPlatforms = { showPlatformsOverlay = true },
            onOpenSettings = onNavigateToSettings,
            modifier = Modifier.fillMaxSize(),
        )
    }

    // ── Layer 4: Platforms modal overlay ────────────────────────────
    if (showPlatformsOverlay) {
        StreamPlatformsOverlay(
            profiles = profiles,
            activeProfileId = activeProfile?.id,
            onDismiss = { showPlatformsOverlay = false },
            onSelectProfile = { streamViewModel.selectProfile(it) },
            onSaveProfile = { streamViewModel.saveProfile(it) },
            onDeleteProfile = { streamViewModel.deleteProfile(it) },
            onStartStream = { streamViewModel.startStream(); showPlatformsOverlay = false },
        )
    }
}

@Composable
private fun LiveBadge(state: StreamState) {
    val bitrateText = if (state is StreamState.Live && state.bitrateKbps > 0)
        "  ${state.bitrateKbps} kbps" else ""

    Surface(
        color = LiveRed,
        shape = RoundedCornerShape(6.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                Modifier
                    .size(8.dp)
                    .background(Color.White, shape = androidx.compose.foundation.shape.CircleShape),
            )
            Text(
                text = "LIVE$bitrateText",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
