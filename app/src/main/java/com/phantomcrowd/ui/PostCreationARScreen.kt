package com.phantomcrowd.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phantomcrowd.data.*
import com.phantomcrowd.ui.theme.DesignSystem
import com.phantomcrowd.utils.Logger
import com.phantomcrowd.ai.ContentModerationHelper
import com.phantomcrowd.ai.ModerationResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Form state for the 5-step wizard.
 */
data class PostFormState(
    val currentStep: Int = 1,
    val selectedUseCase: UseCase? = UseCase.WOMENS_SAFETY,
    val selectedCategory: Category? = null,
    val description: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val locationName: String = "",
    val severity: Severity = Severity.MEDIUM,
    val nearbyIssueCount: Int = 0,
    val confirmAccurate: Boolean = false,
    val confirmAnonymous: Boolean = false,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false
)

/**
 * 3-Step Wizard for Women's Safety Issue Reporting.
 * Step 1: Select Tag (subcategory)
 * Step 2: Enter Details + Privacy
 * Step 3: Optional AR Placement
 * Final: Success Confirmation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCreationARScreen(
    viewModel: MainViewModel,
    onPostCreated: () -> Unit,
    onCancel: () -> Unit,
    onOpenARPlacement: ((messageText: String, category: String, severity: String, useCase: String) -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    
    // Form state
    var formState by remember { mutableStateOf(PostFormState()) }
    
    // Location from ViewModel
    val currentLocation by viewModel.currentLocation.collectAsState()
    
    // Update form state with location when available
    LaunchedEffect(currentLocation) {
        currentLocation?.let { loc ->
            formState = formState.copy(
                latitude = loc.latitude,
                longitude = loc.longitude,
                locationName = "Lat: ${String.format("%.4f", loc.latitude)}, Lon: ${String.format("%.4f", loc.longitude)}"
            )
        }
    }
    
    // Request location on mount
    LaunchedEffect(Unit) {
        viewModel.updateLocation()
    }
    
    // Nearby count query (for Step 3 impact metric)
    var nearbyCount by remember { mutableIntStateOf(0) }
    var isLoadingNearbyCount by remember { mutableStateOf(false) }
    
    LaunchedEffect(formState.selectedUseCase, currentLocation) {
        if (formState.currentStep == 2 && currentLocation != null && formState.selectedUseCase != null) {
            isLoadingNearbyCount = true
            try {
                // Query nearby issues with same use case
                val count = viewModel.getNearbyIssueCountForUseCase(
                    currentLocation!!.latitude,
                    currentLocation!!.longitude,
                    formState.selectedUseCase!!.name
                )
                nearbyCount = count
                formState = formState.copy(nearbyIssueCount = count)
            } catch (e: Exception) {
                Logger.e(Logger.Category.DATA, "Failed to get nearby count", e)
                nearbyCount = 0
            } finally {
                isLoadingNearbyCount = false
            }
        }
    }
    
    // AI Content Moderation
    val moderationHelper = remember { ContentModerationHelper(context) }
    var moderationResult by remember { mutableStateOf<ModerationResult>(ModerationResult.Empty) }
    var isAnalyzing by remember { mutableStateOf(false) }
    
    // Debounced AI analysis - triggers 500ms after user stops typing
    LaunchedEffect(formState.description) {
        if (formState.description.length >= 10) {
            isAnalyzing = true
            delay(500) // Debounce - wait for user to stop typing
            moderationResult = moderationHelper.moderateContent(formState.description)
            isAnalyzing = false
        } else {
            moderationResult = ModerationResult.Empty
            isAnalyzing = false
        }
    }
    
    // Cleanup AI resources when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            moderationHelper.close()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        when (formState.currentStep) {
                            1 -> "Step 1/3 — What Happened"
                            2 -> "Step 2/3 — Details"
                            3 -> "Step 3/3 — AR Placement"
                            4 -> "Report Submitted"
                            else -> "Report Issue"
                        },
                        style = DesignSystem.Typography.titleLarge
                    )
                },
                navigationIcon = {
                    if (formState.currentStep > 1 && formState.currentStep < 4) {
                        IconButton(onClick = {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            formState = formState.copy(currentStep = formState.currentStep - 1)
                        }) {
                            Icon(Icons.Filled.ArrowBack, "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DesignSystem.Colors.surface,
                    titleContentColor = DesignSystem.Colors.onSurface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedContent(
                targetState = formState.currentStep,
                transitionSpec = {
                    fadeIn() + slideInHorizontally { it } togetherWith
                    fadeOut() + slideOutHorizontally { -it }
                },
                label = "StepTransition"
            ) { step ->
                when (step) {
                    // Step 1: Tag selection (Women's Safety subcategories)
                    1 -> Step2CategorySelection(
                        formState = formState,
                        onCategorySelected = { category ->
                            formState = formState.copy(
                                selectedCategory = category,
                                severity = category.defaultSeverity
                            )
                        },
                        onNext = {
                            if (formState.selectedCategory != null) {
                                formState = formState.copy(currentStep = 2)
                            } else {
                                Toast.makeText(context, "Please select what happened", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onBack = { onCancel() }
                    )
                    
                    // Step 2: Details and Privacy
                    2 -> Step3DetailsAndPrivacy(
                        formState = formState,
                        nearbyCount = nearbyCount,
                        isLoadingNearbyCount = isLoadingNearbyCount,
                        moderationResult = moderationResult,
                        isAnalyzing = isAnalyzing,
                        onDescriptionChange = { desc ->
                            formState = formState.copy(description = desc)
                        },
                        onConfirmAccurateChange = { checked ->
                            formState = formState.copy(confirmAccurate = checked)
                        },
                        onConfirmAnonymousChange = { checked ->
                            formState = formState.copy(confirmAnonymous = checked)
                        },
                        onNext = {
                            when {
                                moderationResult is ModerationResult.Blocked -> {
                                    Toast.makeText(context, "Please revise your content before posting", Toast.LENGTH_LONG).show()
                                }
                                formState.description.isBlank() -> {
                                    Toast.makeText(context, "Please describe what happened", Toast.LENGTH_SHORT).show()
                                }
                                !formState.confirmAccurate -> {
                                    Toast.makeText(context, "Please confirm the information is accurate", Toast.LENGTH_SHORT).show()
                                }
                                !formState.confirmAnonymous -> {
                                    Toast.makeText(context, "Please confirm you understand the anonymous submission", Toast.LENGTH_SHORT).show()
                                }
                                else -> {
                                    formState = formState.copy(currentStep = 3)
                                }
                            }
                        },
                        onBack = { formState = formState.copy(currentStep = 1) }
                    )
                    
                    // Step 3: AR Placement
                    3 -> Step4ARPlacement(
                        formState = formState,
                        onPlaceOnSurface = {
                            onOpenARPlacement?.invoke(
                                formState.description,
                                formState.selectedCategory?.id ?: "general",
                                formState.severity.name,
                                formState.selectedUseCase?.name ?: ""
                            )
                            formState = formState.copy(currentStep = 4, saveSuccess = true)
                        },
                        onSkipAR = {
                            scope.launch {
                                formState = formState.copy(isSaving = true)
                                saveIssue(viewModel, formState, context) { success ->
                                    formState = formState.copy(isSaving = false)
                                    if (success) {
                                        formState = formState.copy(currentStep = 4, saveSuccess = true)
                                    }
                                }
                            }
                        },
                        isSaving = formState.isSaving,
                        onBack = { formState = formState.copy(currentStep = 2) }
                    )
                    
                    // Success confirmation
                    4 -> Step5SuccessConfirmation(
                        formState = formState,
                        nearbyCount = nearbyCount + 1,
                        onShare = {
                            val shareText = buildString {
                                append("🚨 ${formState.selectedUseCase?.icon ?: ""} ${formState.selectedUseCase?.label ?: "Issue"} Alert\n\n")
                                append("${formState.description}\n\n")
                                append("📍 Location: ${formState.locationName}\n")
                                append("Report anonymously at SafeHer AR app")
                            }
                            val sendIntent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            context.startActivity(android.content.Intent.createChooser(sendIntent, "Share Alert"))
                        },
                        onViewOnMap = { onPostCreated() },
                        onDone = { onPostCreated() }
                    )
                }
            }
        }
    }
}

/**
 * Step 1: Use Case Selection Grid
 */
@Composable
private fun Step1UseCaseSelection(
    formState: PostFormState,
    onUseCaseSelected: (UseCase) -> Unit,
    onNext: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // SafeHer AR Banner
        Card(
            colors = CardDefaults.cardColors(
                containerColor = DesignSystem.Colors.secondary.copy(alpha = 0.1f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🛡️", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "SafeHer AR — Women Safety Mode",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = DesignSystem.Colors.secondary
                    )
                    Text(
                        "Report anonymously. Your identity is protected.",
                        style = MaterialTheme.typography.bodySmall,
                        color = DesignSystem.Colors.neutralMuted
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Title
        Text(
            "What kind of issue are you reporting?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Your report helps protect women in this area",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        // Use Case Grid (2 columns)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(UseCase.entries) { useCase ->
                UseCaseButton(
                    useCase = useCase,
                    isSelected = formState.selectedUseCase == useCase,
                    onClick = { onUseCaseSelected(useCase) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }
            Button(
                onClick = onNext,
                enabled = formState.selectedUseCase != null,
                modifier = Modifier.weight(1f)
            ) {
                Text("Next")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

/**
 * Use Case selection button component
 */
@Composable
private fun UseCaseButton(
    useCase: UseCase,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        useCase.color.copy(alpha = 0.2f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    val borderColor = if (isSelected) useCase.color else Color.Transparent
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    useCase.icon,
                    fontSize = 40.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    useCase.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }
            
            // Checkmark overlay when selected
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(useCase.color)
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Step 2: Category Selection
 */
@Composable
private fun Step2CategorySelection(
    formState: PostFormState,
    onCategorySelected: (Category) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val categories = formState.selectedUseCase?.let { UseCaseCategories.getCategories(it) } ?: emptyList()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Title with selected use case
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                formState.selectedUseCase?.icon ?: "",
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    "You selected:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    formState.selectedUseCase?.label ?: "",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "What happened?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Category list
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories, key = { it.id }) { category ->
                CategoryButton(
                    category = category,
                    isSelected = formState.selectedCategory?.id == category.id,
                    useCase = formState.selectedUseCase!!,
                    onClick = { onCategorySelected(category) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Back")
            }
            Button(
                onClick = onNext,
                enabled = formState.selectedCategory != null,
                modifier = Modifier.weight(1f)
            ) {
                Text("Next")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

/**
 * Category selection button — soft filled card, no thick borders
 */
@Composable
private fun CategoryButton(
    category: Category,
    isSelected: Boolean,
    useCase: UseCase,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        DesignSystem.Colors.selectedCard  // Gentle pink #F9E4EF
    } else {
        DesignSystem.Colors.surface       // White #FFFFFF
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = DesignSystem.Shapes.card,  // 14dp radius
        elevation = CardDefaults.cardElevation(
            defaultElevation = DesignSystem.Elevation.smallCard  // 1dp soft
        )
        // No border — premium clean look
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DesignSystem.Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(category.icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(DesignSystem.Spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    category.label,
                    style = DesignSystem.Typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = DesignSystem.Colors.onSurface
                )
                Text(
                    category.description,
                    style = DesignSystem.Typography.bodyMedium,
                    color = DesignSystem.Colors.neutralMuted
                )
            }
            // Severity badge — soft pill
            Box(
                modifier = Modifier
                    .clip(DesignSystem.Shapes.chip)
                    .background(category.defaultSeverity.color.copy(alpha = 0.15f))
                    .padding(horizontal = DesignSystem.Spacing.xs, vertical = DesignSystem.Spacing.xxs)
            ) {
                Text(
                    category.defaultSeverity.label,
                    style = DesignSystem.Typography.labelLarge,
                    color = category.defaultSeverity.color
                )
            }
            if (isSelected) {
                Spacer(modifier = Modifier.width(DesignSystem.Spacing.xs))
                Icon(
                    Icons.Filled.Check,
                    contentDescription = "Selected",
                    tint = DesignSystem.Colors.primary
                )
            }
        }
    }
}

/**
 * Step 3: Details and Privacy with AI Content Moderation
 */
@Composable
private fun Step3DetailsAndPrivacy(
    formState: PostFormState,
    nearbyCount: Int,
    isLoadingNearbyCount: Boolean,
    moderationResult: ModerationResult,
    isAnalyzing: Boolean,
    onDescriptionChange: (String) -> Unit,
    onConfirmAccurateChange: (Boolean) -> Unit,
    onConfirmAnonymousChange: (Boolean) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val currentTime = timeFormat.format(Date())
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Report Details",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Location section
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📍", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Location (Auto-filled)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            formState.locationName.ifEmpty { "Getting location..." },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
        
        // Time section
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⏰", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Time (Auto-captured)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            currentTime,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
        
        // Description input
        item {
            OutlinedTextField(
                value = formState.description,
                onValueChange = { if (it.length <= 300) onDescriptionChange(it) },
                label = { Text("Describe what happened") },
                placeholder = { Text("Be specific about the issue, location details, and any safety concerns...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                maxLines = 6,
                supportingText = {
                    Text("${formState.description.length}/300 characters")
                },
                isError = formState.description.isEmpty()
            )
        }
        
        // AI Content Moderation Feedback
        item {
            AnimatedVisibility(
                visible = formState.description.length >= 10 || isAnalyzing,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                val (backgroundColor, icon, message, textColor) = when {
                    isAnalyzing -> listOf(
                        Color(0xFFE3F2FD), "🔍", "Analyzing content...", Color(0xFF1565C0)
                    )
                    moderationResult is ModerationResult.Safe -> listOf(
                        Color(0xFFE8F5E9), "✅", "Your report looks appropriate", Color(0xFF2E7D32)
                    )
                    moderationResult is ModerationResult.Warning -> listOf(
                        Color(0xFFFFF8E1), "⚠️", "Please ensure your report is factual and respectful", Color(0xFFF57C00)
                    )
                    moderationResult is ModerationResult.Blocked -> listOf(
                        Color(0xFFFFEBEE), "🚫", "This content may violate community guidelines. Please revise.", Color(0xFFC62828)
                    )
                    moderationResult is ModerationResult.Error -> listOf(
                        Color(0xFFECEFF1), "❓", "Could not analyze content", Color(0xFF546E7A)
                    )
                    else -> listOf(Color.Transparent, "", "", Color.Transparent)
                }
                
                if (backgroundColor != Color.Transparent) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = backgroundColor as Color),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isAnalyzing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = textColor as Color
                                )
                            } else {
                                Text(icon as String, fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    message as String,
                                    color = textColor as Color,
                                    fontWeight = FontWeight.Medium,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (moderationResult is ModerationResult.Blocked) {
                                    Text(
                                        "Modify your text to continue",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = (textColor as Color).copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Why This Matters
        item {
            formState.selectedUseCase?.let { useCase ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = useCase.color.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("💡", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Why This Matters",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                ImpactMessages.getWhyThisMatters(useCase),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
        
        // Impact Metric (Live Query)
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📊", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    if (isLoadingNearbyCount) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Loading nearby reports...")
                    } else {
                        formState.selectedUseCase?.let { useCase ->
                            Text(
                                ImpactMessages.getImpactMetricLabel(useCase, nearbyCount),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
        
        // Privacy Banner
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A237E)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔒", fontSize = 32.sp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            "Completely Anonymous",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val privacyPoints = listOf(
                        "✓ No name collected",
                        "✓ No phone ID stored",
                        "✓ No email required",
                        "✓ No location history",
                        "✓ No identification possible",
                        "✓ You are never tracked"
                    )
                    
                    privacyPoints.forEach { point ->
                        Text(
                            point,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Police & admins see patterns, not people.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF90CAF9)
                    )
                }
            }
        }
        
        // Confirmation Checkboxes
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onConfirmAccurateChange(!formState.confirmAccurate) }
                            .padding(vertical = 8.dp)
                    ) {
                        Checkbox(
                            checked = formState.confirmAccurate,
                            onCheckedChange = onConfirmAccurateChange
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "I confirm this information is accurate",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onConfirmAnonymousChange(!formState.confirmAnonymous) }
                            .padding(vertical = 8.dp)
                    ) {
                        Checkbox(
                            checked = formState.confirmAnonymous,
                            onCheckedChange = onConfirmAnonymousChange
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "I understand this is anonymous and I agree to submit",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
        
        // Navigation buttons
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Back")
                }
                Button(
                    onClick = onNext,
                    enabled = formState.description.isNotBlank() && 
                              formState.confirmAccurate && 
                              formState.confirmAnonymous,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Next")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Step 4: Optional AR Placement
 */
@Composable
private fun Step4ARPlacement(
    formState: PostFormState,
    onPlaceOnSurface: () -> Unit,
    onSkipAR: () -> Unit,
    isSaving: Boolean,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🎯", fontSize = 72.sp)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "Place on Physical Location?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "Would you like to anchor this issue to the physical location? Others can see it in AR when they visit this spot.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        if (isSaving) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Saving your report...")
        } else {
            Button(
                onClick = onPlaceOnSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text(
                    "🧱 Yes, Place on Surface",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = onSkipAR,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    "📤 No, Just Post",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            TextButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Go Back")
            }
        }
    }
}

/**
 * Step 5: Success Confirmation
 */
@Composable
private fun Step5SuccessConfirmation(
    formState: PostFormState,
    nearbyCount: Int,
    onShare: () -> Unit,
    onViewOnMap: () -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        // Success icon with animation
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFF4CAF50))
        ) {
            Icon(
                Icons.Filled.Check,
                contentDescription = "Success",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(60.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "✅ Report Submitted Successfully!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            "Your anonymous report has been posted",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // What Happens Next
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "What Happens Next",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                val happeningPoints = listOf(
                    "👥 $nearbyCount people have now reported this area",
                    "${formState.severity.icon} This zone is flagged ${formState.severity.label.uppercase()}",
                    "🚨 Authorities have been alerted",
                    "⏳ Expect action within 48 hours"
                )
                
                happeningPoints.forEach { point ->
                    Text(
                        point,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Impact message
        formState.selectedUseCase?.let { useCase ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = useCase.color.copy(alpha = 0.1f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    ImpactMessages.getSuccessMessage(useCase),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Action buttons
        Button(
            onClick = onShare,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Icon(Icons.Filled.Share, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Share This Alert")
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = onViewOnMap,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("🗺️ View on Map")
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Done")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Save issue to Firestore
 */
private suspend fun saveIssue(
    viewModel: MainViewModel,
    formState: PostFormState,
    context: android.content.Context,
    onComplete: (Boolean) -> Unit
) {
    try {
        val anchorData = AnchorData(
            id = UUID.randomUUID().toString(),
            latitude = formState.latitude,
            longitude = formState.longitude,
            altitude = 0.0,
            messageText = formState.description,
            category = formState.selectedCategory?.id?.lowercase() ?: "general",
            timestamp = System.currentTimeMillis(),
            useCase = formState.selectedUseCase?.name ?: "",
            useCaseCategory = formState.selectedCategory?.id ?: "",
            severity = formState.severity.name,
            locationName = formState.locationName,
            nearbyIssueCount = formState.nearbyIssueCount,
            status = "PENDING",
            wallAnchorId = "wall-${UUID.randomUUID()}"
        )
        
        viewModel.uploadIssueSafely(anchorData)
        
        // Small delay for visual feedback
        delay(500)
        
        Toast.makeText(context, "Issue reported! 🎉", Toast.LENGTH_SHORT).show()
        onComplete(true)
        
    } catch (e: Exception) {
        Logger.e(Logger.Category.DATA, "Failed to save issue", e)
        Toast.makeText(context, "Failed to save: ${e.message}", Toast.LENGTH_LONG).show()
        onComplete(false)
    }
}
