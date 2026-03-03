package com.phantomcrowd.ui

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.phantomcrowd.data.AnchorData
import com.phantomcrowd.data.RiskLevel
import com.phantomcrowd.data.RiskScoring
import com.phantomcrowd.ui.theme.DesignSystem
import com.phantomcrowd.utils.BearingCalculator
import com.phantomcrowd.utils.Logger
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.speech.tts.TextToSpeech
import java.util.Locale
import kotlin.math.abs
import androidx.compose.animation.core.*

/**
 * ARViewScreen - Simplified AR View with CameraX and Overlay Labels
 * 
 * Features:
 * - CameraX camera preview (no ARCore conflicts!)
 * - Floating labels for nearby issues based on GPS bearing
 * - Real-time compass heading for label positioning
 * - Distance-based label sizing
 * - OPTIMIZED: Throttled sensor updates to prevent performance issues
 */
@Composable
fun ARViewScreen(
    viewModel: MainViewModel,
    onClose: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptic = LocalHapticFeedback.current
    val config = LocalConfiguration.current
    val screenWidthDp = config.screenWidthDp.dp
    
    // Collect state
    val anchors by viewModel.anchors.collectAsState()
    val userLocation by viewModel.currentLocation.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    // First-time AR tutorial
    val prefs = remember { context.getSharedPreferences("safeher_ar_prefs", Context.MODE_PRIVATE) }
    var showTutorial by remember { mutableStateOf(!prefs.getBoolean("ar_tutorial_seen", false)) }
    
    // Detail sheet state
    var selectedAnchor by remember { mutableStateOf<AnchorData?>(null) }
    var selectedDistance by remember { mutableFloatStateOf(0f) }
    var isReadingAloud by remember { mutableStateOf(false) }
    
    // TTS toggle (default ON for demo)
    var ttsEnabled by remember { mutableStateOf(prefs.getBoolean("ar_tts_enabled", true)) }
    
    // TTS for HIGH risk spoken alerts
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var ttsReady by remember { mutableStateOf(false) }
    val spokenAlertIds = remember { mutableSetOf<String>() }
    
    DisposableEffect(Unit) {
        val engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                ttsReady = true
            }
        }
        tts = engine
        onDispose {
            engine.stop()
            engine.shutdown()
        }
    }
    
    // Compass heading - THROTTLED to prevent excessive recompositions
    var deviceHeading by remember { mutableFloatStateOf(0f) }
    var lastUpdateTime by remember { mutableLongStateOf(0L) }
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    
    // Camera executor
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var cameraReady by remember { mutableStateOf(false) }
    var cameraError by remember { mutableStateOf<String?>(null) }
    
    // Pre-calculate anchor bearings and distances (only when location or anchors change)
    val anchorData = remember(userLocation, anchors) {
        if (userLocation == null) emptyList()
        else anchors.map { anchor ->
            val bearing = BearingCalculator.calculateBearing(
                userLocation!!.latitude, userLocation!!.longitude,
                anchor.latitude, anchor.longitude
            ).toFloat()
            
            val distance = FloatArray(1)
            Location.distanceBetween(
                userLocation!!.latitude, userLocation!!.longitude,
                anchor.latitude, anchor.longitude,
                distance
            )
            
            AnchorDisplayData(anchor, bearing, distance[0])
        }.sortedBy { it.distance }
    }
    
    // Compass sensor listener - THROTTLED
    DisposableEffect(sensorManager) {
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        var lastHeading = 0f
        
        val sensorListener = object : SensorEventListener {
            private val rotationMatrix = FloatArray(9)
            private val orientationAngles = FloatArray(3)
            
            override fun onSensorChanged(event: SensorEvent) {
                val currentTime = System.currentTimeMillis()
                
                // Throttle to max 10 updates per second (100ms)
                if (currentTime - lastUpdateTime < 100) return
                
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                
                // Convert to degrees (0-360)
                val azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                val newHeading = (azimuth + 360) % 360
                
                // Only update if heading changed by more than 3 degrees
                val headingDiff = abs(newHeading - lastHeading)
                if (headingDiff > 3 || headingDiff > 357) { // Handle wrap-around
                    deviceHeading = newHeading
                    lastHeading = newHeading
                    lastUpdateTime = currentTime
                }
            }
            
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        
        if (rotationSensor != null) {
            sensorManager.registerListener(
                sensorListener,
                rotationSensor,
                SensorManager.SENSOR_DELAY_GAME // Faster updates, but we throttle manually
            )
        }
        
        onDispose {
            sensorManager.unregisterListener(sensorListener)
            cameraExecutor.shutdown()
            // CRITICAL: Explicitly unbind CameraX to release camera for ARCore
            try {
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    try {
                        val cameraProvider = cameraProviderFuture.get()
                        cameraProvider.unbindAll()
                        Logger.d(Logger.Category.AR, "CameraX fully unbound - camera released for ARCore")
                    } catch (e: Exception) {
                        Logger.e(Logger.Category.AR, "Failed to unbind CameraX", e)
                    }
                }, ContextCompat.getMainExecutor(context))
            } catch (e: Exception) {
                Logger.e(Logger.Category.AR, "Failed to get CameraProvider for cleanup", e)
            }
            Logger.d(Logger.Category.AR, "ARViewScreen disposed - camera released")
        }
    }
    
    // Refresh anchors on start
    LaunchedEffect(Unit) {
        viewModel.updateLocation()
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    startCamera(ctx, this, lifecycleOwner, cameraExecutor, 
                        onReady = { cameraReady = true },
                        onError = { error -> cameraError = error }
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // ─── Issue Labels Overlay — collision-aware ────────────────
        if (anchorData.isNotEmpty()) {
            val visibleAnchors = remember(deviceHeading, anchorData) {
                anchorData.mapNotNull { data ->
                    var angleDiff = data.bearing - deviceHeading
                    if (angleDiff > 180) angleDiff -= 360
                    if (angleDiff < -180) angleDiff += 360
                    if (abs(angleDiff) <= 60) {
                        VisibleAnchor(data.anchor, angleDiff, data.distance)
                    } else null
                }.sortedWith(
                    compareByDescending<VisibleAnchor> {
                        // Priority: risk > proximity > confirmations
                        val ageDays = (System.currentTimeMillis() - it.anchor.timestamp).toDouble() / (1000 * 60 * 60 * 24)
                        val risk = RiskScoring.computeRiskLevel(it.anchor.severity, it.anchor.upvotes, ageDays, it.distance.toDouble())
                        when (risk) { RiskLevel.HIGH -> 3; RiskLevel.MEDIUM -> 2; RiskLevel.LOW -> 1 }
                    }.thenBy { it.distance }
                     .thenByDescending { it.anchor.upvotes }
                )
            }

            // Off-screen indicators for labels beyond FOV
            anchorData.forEach { data ->
                var angleDiff = data.bearing - deviceHeading
                if (angleDiff > 180) angleDiff -= 360
                if (angleDiff < -180) angleDiff += 360
                if (abs(angleDiff) > 60 && data.distance < 200) {
                    val isLeft = angleDiff < 0
                    val ageDays = (System.currentTimeMillis() - data.anchor.timestamp).toDouble() / (1000 * 60 * 60 * 24)
                    val risk = RiskScoring.computeRiskLevel(data.anchor.severity, data.anchor.upvotes, ageDays, data.distance.toDouble())
                    EdgeIndicator(
                        isLeft = isLeft,
                        riskLevel = risk,
                        distance = data.distance,
                        modifier = Modifier
                            .align(if (isLeft) Alignment.CenterStart else Alignment.CenterEnd)
                            .padding(vertical = 4.dp)
                    )
                }
            }

            // Collision-aware label positioning
            val placedPositions = mutableListOf<Pair<Float, Float>>() // (x, y)
            val clustered = mutableListOf<VisibleAnchor>() // overflow

            visibleAnchors.take(8).forEach { visible ->
                val xOffset = visible.angleDiff / 60f
                var yPosition = 0.25f + (placedPositions.size * 0.14f)

                // Collision detection: radial push
                var collisionAttempts = 0
                while (collisionAttempts < 3 && placedPositions.any { existing ->
                    abs(existing.first - xOffset) < 0.25f && abs(existing.second - yPosition) < 0.12f
                }) {
                    yPosition += 0.12f
                    collisionAttempts++
                }

                if (placedPositions.size < 5 && yPosition < 0.85f) {
                    placedPositions.add(xOffset to yPosition)
                    val scale = when {
                        visible.distance < 20 -> 1.2f
                        visible.distance < 50 -> 1.05f
                        visible.distance < 100 -> 1.0f
                        visible.distance < 200 -> 0.9f
                        else -> 0.85f
                    }
                    ARInlineLabel(
                        anchor = visible.anchor,
                        distance = visible.distance,
                        xOffset = xOffset,
                        yPosition = yPosition,
                        scale = scale,
                        screenWidth = screenWidthDp,
                        onTap = {
                            selectedAnchor = visible.anchor
                            selectedDistance = visible.distance
                        },
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                } else {
                    clustered.add(visible)
                }
            }

            // Cluster marker for overflow labels (>3 clustered)
            if (clustered.size >= 2) {
                ClusterMarker(
                    count = clustered.size,
                    onTap = {
                        // Show highest-priority clustered anchor
                        clustered.firstOrNull()?.let {
                            selectedAnchor = it.anchor
                            selectedDistance = it.distance
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 120.dp)
                )
            }
        }
        
        // Top status bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(16.dp)
        ) {
            // Status chip
            Surface(
                color = if (cameraReady) DesignSystem.Colors.success else DesignSystem.Colors.warning,
                shape = DesignSystem.Shapes.pill
            ) {
                Text(
                    text = if (cameraReady) "AR Active" else "Initializing…",
                    color = Color.White,
                    style = DesignSystem.Typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            
            // Camera error
            cameraError?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "📷 $error",
                    color = Color(0xFFFF5252),
                    fontSize = 12.sp,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            
            // Compass heading
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "🧭 ${deviceHeading.toInt()}° ${getCardinalDirection(deviceHeading)}",
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        
        // Bottom info panel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🛡️ ${anchors.size} safety reports nearby",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            if (userLocation != null) {
                Text(
                    text = "${String.format("%.5f", userLocation!!.latitude)}, ${String.format("%.5f", userLocation!!.longitude)}",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "👆 Point camera at your surroundings to reveal safety labels",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            FilledTonalButton(
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    viewModel.updateLocation()
                }
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Refresh")
            }
        }
        
        // Loading overlay
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
        
        // First-time AR tutorial overlay
        if (showTutorial) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .padding(32.dp)
                        .background(DesignSystem.Colors.surface, RoundedCornerShape(20.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.phantomcrowd.R.drawable.ic_app_logo),
                        contentDescription = "SafeHer AR logo",
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        "SafeHer AR View",
                        style = DesignSystem.Typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = DesignSystem.Colors.onSurface
                    )
                    Text(
                        "• Point your camera to see safety reports around you\n" +
                        "• \u200B🔴 Red labels = urgent safety alerts\n" +
                        "• \u200B🟡 Yellow labels = medium risk\n" +
                        "• \u200B🟢 Green labels = low risk\n" +
                        "• Haptic vibration alerts for HIGH risk zones",
                        style = DesignSystem.Typography.bodyMedium,
                        color = DesignSystem.Colors.neutralMuted
                    )
                    Button(
                        onClick = {
                            showTutorial = false
                            prefs.edit().putBoolean("ar_tutorial_seen", true).apply()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DesignSystem.Colors.primary
                        )
                    ) {
                        Text("Got it!", color = DesignSystem.Colors.onPrimary)
                    }
                }
            }
        }
        
        // ─── Auto-TTS + Haptic for HIGH risk (once per label per session) ──
        LaunchedEffect(anchorData, ttsEnabled) {
            if (!ttsEnabled || !ttsReady) return@LaunchedEffect
            anchorData.filter { data ->
                val ageDays = (System.currentTimeMillis() - data.anchor.timestamp).toDouble() / (1000 * 60 * 60 * 24)
                val level = RiskScoring.computeRiskLevel(data.anchor.severity, data.anchor.upvotes, ageDays, data.distance.toDouble())
                level == RiskLevel.HIGH && data.anchor.id !in spokenAlertIds && data.distance < 200
            }.take(1).forEach { data ->
                // Haptic pulse first
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                spokenAlertIds.add(data.anchor.id)
                // Speak headline + distance
                val headline = data.anchor.messageText.ifEmpty { data.anchor.category }
                    .take(80) // Keep TTS short
                tts?.speak(
                    "Caution. $headline. ${data.distance.toInt()} meters ahead.",
                    TextToSpeech.QUEUE_ADD,
                    null,
                    data.anchor.id
                )
            }
        }
        
        // ─── Detail Sheet Overlay ─────────────────────────────────
        com.phantomcrowd.ui.components.ARDetailSheet(
            anchor = selectedAnchor ?: AnchorData(),
            distance = selectedDistance,
            isVisible = selectedAnchor != null,
            onDismiss = {
                selectedAnchor = null
                isReadingAloud = false
                tts?.stop()
            },
            onConfirm = { /* TODO: Increment upvote */ },
            onSOS = { /* TODO: Trigger SOS */ },
            onNavigate = { /* TODO: Navigate to anchor */ },
            onReadAloud = {
                if (isReadingAloud) {
                    tts?.stop()
                    isReadingAloud = false
                } else {
                    val text = selectedAnchor?.messageText ?: ""
                    if (text.isNotEmpty() && ttsReady) {
                        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "detail_read")
                        isReadingAloud = true
                    }
                }
            },
            isReadingAloud = isReadingAloud
        )
    }
}

// Data classes for caching calculations
private data class AnchorDisplayData(
    val anchor: AnchorData,
    val bearing: Float,
    val distance: Float
)

private data class VisibleAnchor(
    val anchor: AnchorData,
    val angleDiff: Float,
    val distance: Float
)

/**
 * Get severity color from the anchor's severity field.
 */
private fun getSeverityColor(severity: String): Color {
    return when (severity.uppercase()) {
        "URGENT"  -> Color(0xFFFF1744)   // Vivid red
        "HIGH"    -> DesignSystem.Colors.severityHigh    // #D6453D
        "MEDIUM"  -> DesignSystem.Colors.severityMedium  // #E67A00
        "LOW"     -> DesignSystem.Colors.severityLow     // #2E9B5D
        else      -> DesignSystem.Colors.severityMedium
    }
}

/**
 * Get category icon.
 */
private fun getCategoryIcon(category: String, severity: String): String {
    return when (severity.uppercase()) {
        "URGENT" -> "🚨"
        "HIGH"   -> "⚠\uFE0F"
        else -> when (category.lowercase()) {
            "safety", "assault", "harassment", "stalking", "unsafe_area" -> "🛡\uFE0F"
            "facility", "infrastructure", "broken_light", "pothole"     -> "🏗\uFE0F"
            "traffic", "accident", "signal", "road"                     -> "🚦"
            "sanitation", "garbage", "waste", "pollution"               -> "♻\uFE0F"
            "noise", "disturbance"                                      -> "🔊"
            else                                                        -> "📍"
        }
    }
}

/**
 * AR Inline Label — Light translucent card, production-ready.
 *
 * Design:
 *  ┌────────────────────────────────────────┐
 *  │ ● 🚨 Issue headline (1 line)       ⋯ │
 *  │   12m • HIGH • URGENT • 2✓            │
 *  └────────────────────────────────────────┘
 *  Background: rgba(255,255,255,0.92)
 *  Width: 220–360dp  Height: max 120dp
 *  800ms pulse glow for HIGH (≤6% scale)
 */
@Composable
private fun ARInlineLabel(
    anchor: AnchorData,
    distance: Float,
    xOffset: Float,
    yPosition: Float,
    scale: Float,
    screenWidth: androidx.compose.ui.unit.Dp,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val severityColor = getSeverityColor(anchor.severity)
    val isHighRisk = anchor.severity.uppercase() in listOf("URGENT", "HIGH")

    // 800ms pulse glow for HIGH (≤6% scale change)
    val infiniteTransition = rememberInfiniteTransition(label = "ar_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isHighRisk) 1.06f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = if (isHighRisk) 1f else 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val categoryIcon = getCategoryIcon(anchor.category, anchor.severity)
    val distanceText = if (distance < 1000) "${distance.toInt()}m" else "${String.format("%.1f", distance / 1000)}km"
    val severityLabel = anchor.severity.ifEmpty { "MEDIUM" }.uppercase()
    val ageDays = (System.currentTimeMillis() - anchor.timestamp).toDouble() / (1000 * 60 * 60 * 24)
    val riskLevel = RiskScoring.computeRiskLevel(anchor.severity, anchor.upvotes, ageDays, distance.toDouble())

    // Light translucent background
    val cardBg = Color.White.copy(alpha = pulseAlpha)
    val cardShape = DesignSystem.Shapes.card // 14dp radius

    // Preferred width: 45% of screen, clamped 220–360dp
    val preferredWidth = screenWidth * 0.45f

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(x = (xOffset * 150).dp, y = (yPosition * 1000).dp)
                .graphicsLayer(
                    scaleX = scale * pulseScale,
                    scaleY = scale * pulseScale
                )
                .widthIn(min = 220.dp, max = 360.dp)
                .heightIn(max = 120.dp) // Compact height limit
                .clip(cardShape)
                .background(cardBg)
                .clickable(onClick = onTap)
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .semantics {
                    contentDescription = "${anchor.messageText.ifEmpty { anchor.category }}, $distanceText away, $severityLabel risk. Tap to open details."
                }
        ) {
            // ── Top row: dot + icon + headline + chevron ──
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Risk dot
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(severityColor)
                )
                // Category icon (28dp)
                Text(categoryIcon, fontSize = 20.sp)
                // Headline — Poppins SemiBold 16sp, 1-line ellipsis
                Text(
                    text = anchor.messageText.ifEmpty { anchor.category },
                    style = DesignSystem.Typography.titleLarge.copy(fontSize = 16.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = DesignSystem.Colors.onSurface, // #0F1724
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                // Chevron affordance
                Text(
                    "⋯",
                    color = DesignSystem.Colors.neutralMuted,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── Bottom row: distance + risk pill + severity pill + confirm count ──
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Distance
                Text(
                    text = distanceText,
                    style = DesignSystem.Typography.bodyMedium,
                    color = DesignSystem.Colors.neutralMuted,
                    fontSize = 12.sp
                )

                // Risk pill (24dp height, 12dp radius)
                val riskColor = when (riskLevel) {
                    RiskLevel.HIGH -> DesignSystem.Colors.error
                    RiskLevel.MEDIUM -> DesignSystem.Colors.warning
                    RiskLevel.LOW -> DesignSystem.Colors.success
                }
                Text(
                    text = riskLevel.shortLabel,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .height(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(riskColor)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )

                // Severity badge
                Text(
                    text = severityLabel,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .height(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(severityColor)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )

                // Confirmations count
                if (anchor.upvotes > 0) {
                    Text(
                        text = "${anchor.upvotes}✓",
                        color = DesignSystem.Colors.success,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Edge indicator arrow for off-screen labels within 200m.
 */
@Composable
private fun EdgeIndicator(
    isLeft: Boolean,
    riskLevel: RiskLevel,
    distance: Float,
    modifier: Modifier = Modifier
) {
    val color = when (riskLevel) {
        RiskLevel.HIGH -> DesignSystem.Colors.error
        RiskLevel.MEDIUM -> DesignSystem.Colors.warning
        RiskLevel.LOW -> DesignSystem.Colors.success
    }
    val arrow = if (isLeft) "◀" else "▶"
    val distText = if (distance < 1000) "${distance.toInt()}m" else "${String.format("%.1f", distance / 1000)}km"

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.85f))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(arrow, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(distText, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}

/**
 * Cluster marker badge for >3 overlapping labels that didn't fit on screen.
 */
@Composable
private fun ClusterMarker(
    count: Int,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .size(48.dp)
            .clickable(onClick = onTap),
        shape = CircleShape,
        color = DesignSystem.Colors.primary.copy(alpha = 0.9f),
        contentColor = Color.White
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "+$count",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Start CameraX preview
 */
private fun startCamera(
    context: Context,
    previewView: PreviewView,
    lifecycleOwner: LifecycleOwner,
    executor: ExecutorService,
    onReady: () -> Unit,
    onError: (String) -> Unit
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    
    cameraProviderFuture.addListener({
        try {
            val cameraProvider = cameraProviderFuture.get()
            
            // Unbind all before rebinding
            cameraProvider.unbindAll()
            
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
            
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview
            )
            
            onReady()
            Logger.d(Logger.Category.AR, "CameraX started successfully")
            
        } catch (e: Exception) {
            Logger.e(Logger.Category.AR, "CameraX init failed", e)
            onError(e.message ?: "Camera init failed")
        }
    }, ContextCompat.getMainExecutor(context))
}

/**
 * Get cardinal direction from heading
 */
private fun getCardinalDirection(heading: Float): String {
    return when {
        heading < 22.5 || heading >= 337.5 -> "N"
        heading < 67.5 -> "NE"
        heading < 112.5 -> "E"
        heading < 157.5 -> "SE"
        heading < 202.5 -> "S"
        heading < 247.5 -> "SW"
        heading < 292.5 -> "W"
        else -> "NW"
    }
}
