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
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
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
    
    // Collect state
    val anchors by viewModel.anchors.collectAsState()
    val userLocation by viewModel.currentLocation.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    // First-time AR tutorial
    val prefs = remember { context.getSharedPreferences("safeher_ar_prefs", Context.MODE_PRIVATE) }
    var showTutorial by remember { mutableStateOf(!prefs.getBoolean("ar_tutorial_seen", false)) }
    
    // TTS for HIGH risk spoken alerts
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    val spokenAlertIds = remember { mutableSetOf<String>() }
    
    DisposableEffect(Unit) {
        val engine = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }
        tts = engine
        onDispose { engine.shutdown() }
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
        
        // Issue Labels Overlay - Uses pre-calculated data
        if (anchorData.isNotEmpty()) {
            val visibleAnchors = remember(deviceHeading, anchorData) {
                anchorData.mapNotNull { data ->
                    // Calculate angle difference from current heading
                    var angleDiff = data.bearing - deviceHeading
                    if (angleDiff > 180) angleDiff -= 360
                    if (angleDiff < -180) angleDiff += 360
                    
                    // Only show issues within ±60° field of view
                    if (abs(angleDiff) <= 60) {
                        VisibleAnchor(data.anchor, angleDiff, data.distance)
                    } else null
                }
            }
            
            // Draw labels - max 5
            visibleAnchors.take(5).forEachIndexed { index, visible ->
                // Position label based on angle (center = 0, left = -60, right = +60)
                val xOffset = visible.angleDiff / 60f // -1 to +1
                
                // Size based on distance (closer = bigger)
                val scale = when {
                    visible.distance < 20 -> 1.3f
                    visible.distance < 50 -> 1.1f
                    visible.distance < 100 -> 1.0f
                    visible.distance < 200 -> 0.9f
                    else -> 0.8f
                }
                
                // Vertical position based on index (stack labels)
                val yPosition = 0.3f + (index * 0.12f)
                
                IssueLabel(
                    anchor = visible.anchor,
                    distance = visible.distance,
                    xOffset = xOffset,
                    yPosition = yPosition,
                    scale = scale,
                    modifier = Modifier.align(Alignment.TopCenter)
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
                    Text("🛡️", fontSize = 48.sp)
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
        
        // Haptic + TTS alert for HIGH risk anchors entering view
        LaunchedEffect(anchorData) {
            anchorData.filter { data ->
                val ageDays = (System.currentTimeMillis() - data.anchor.timestamp).toDouble() / (1000 * 60 * 60 * 24)
                val level = RiskScoring.computeRiskLevel(data.anchor.severity, data.anchor.upvotes, ageDays, data.distance.toDouble())
                level == RiskLevel.HIGH && data.anchor.id !in spokenAlertIds && data.distance < 200
            }.take(1).forEach { data ->
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                spokenAlertIds.add(data.anchor.id)
                tts?.speak(
                    "Caution. High risk safety alert ${data.distance.toInt()} meters ahead.",
                    TextToSpeech.QUEUE_ADD,
                    null,
                    data.anchor.id
                )
            }
        }
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
 * Floating issue label composable — Dark glassmorphic card with severity accent.
 *
 * Design:
 *  ┌──────────────────────────┐
 *  │ ● 🚨 Issue message text  │  ← severity-colored dot + icon
 *  │      12m • URGENT        │  ← distance + severity label
 *  └──────────────────────────┘
 *  Left 3dp border = severity color
 *  Background = dark translucent (#0D1117 @ 88%)
 *  Subtle bottom glow for URGENT/HIGH
 */
@Composable
private fun IssueLabel(
    anchor: AnchorData,
    distance: Float,
    xOffset: Float,
    yPosition: Float,
    scale: Float,
    modifier: Modifier = Modifier
) {
    val severityColor = getSeverityColor(anchor.severity)
    val isUrgent = anchor.severity.uppercase() in listOf("URGENT", "HIGH")

    // Pulsing animation for URGENT/HIGH severity
    val infiniteTransition = rememberInfiniteTransition(label = "severity_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val dotAlpha = if (isUrgent) pulseAlpha else 1f
    val categoryIcon = getCategoryIcon(anchor.category, anchor.severity)
    val distanceText = if (distance < 1000) "${distance.toInt()}m" else "${String.format("%.1f", distance / 1000)}km"
    val severityLabel = anchor.severity.ifEmpty { "MEDIUM" }.uppercase()

    // Frosted indigo card with subtle severity tint
    // — not pure black (too harsh), not white (invisible)
    // — deep indigo base picks up a whisper of the severity hue
    val cardBase = Color(0xFF1A1A2E)        // Deep indigo
    val cardBg = androidx.compose.ui.graphics.lerp(cardBase, severityColor, 0.08f)
        .copy(alpha = 0.82f)
    val borderShape = RoundedCornerShape(
        topStart = 0.dp, bottomStart = 0.dp,
        topEnd = 12.dp, bottomEnd = 12.dp
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(x = (xOffset * 150).dp, y = (yPosition * 1000).dp)
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .widthIn(min = 140.dp, max = 200.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Left severity accent bar ──────────────────────
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                    .background(severityColor.copy(alpha = dotAlpha))
            )

            // ── Card body ────────────────────────────────────
            Column(
                modifier = Modifier
                    .clip(borderShape)
                    .background(cardBg)
                    .padding(start = 10.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                // Top row: severity dot + icon + text
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Severity dot (pulsing for URGENT/HIGH)
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                severityColor.copy(alpha = dotAlpha),
                                RoundedCornerShape(50)
                            )
                    )
                    // Category icon
                    Text(categoryIcon, fontSize = 14.sp)
                    // Message
                    Text(
                        text = anchor.messageText.ifEmpty { anchor.category },
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Bottom row: distance + risk level + severity badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = distanceText,
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )

                    // Risk level pill
                    val ageDays = (System.currentTimeMillis() - anchor.timestamp).toDouble() / (1000 * 60 * 60 * 24)
                    val riskLevel = RiskScoring.computeRiskLevel(anchor.severity, anchor.upvotes, ageDays, distance.toDouble())
                    val riskColor = when (riskLevel) {
                        RiskLevel.HIGH -> Color(0xFFEF4444)
                        RiskLevel.MEDIUM -> Color(0xFFFBBF24)
                        RiskLevel.LOW -> Color(0xFF22C55E)
                    }
                    Text(
                        text = riskLevel.shortLabel,
                        color = riskColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(riskColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )

                    // Severity badge
                    Text(
                        text = severityLabel,
                        color = severityColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier
                            .background(
                                severityColor.copy(alpha = 0.15f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    )

                    // Report count
                    if (anchor.upvotes > 0) {
                        Text(
                            text = "${anchor.upvotes}✓",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 9.sp
                        )
                    }
                }
            }
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
