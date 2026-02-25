package com.phantomcrowd.ui.components

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.phantomcrowd.ui.theme.DesignSystem
import kotlinx.coroutines.launch

/**
 * Onboarding overlay shown on first launch. 
 * Explains SafeHer AR's core value propositions and privacy guarantees.
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun OnboardingOverlay(
    onDismiss: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            emoji = "🛡️",
            title = "Welcome to SafeHer AR",
            body = "An AR-powered safety companion that helps you identify and report unsafe areas in real-time.",
            accent = DesignSystem.Colors.primary
        ),
        OnboardingPage(
            emoji = "👁️‍🗨️",
            title = "See Safety Through AR",
            body = "Point your camera to reveal safety reports, risk levels, and community alerts pinned to real-world locations.",
            accent = DesignSystem.Colors.secondary
        ),
        OnboardingPage(
            emoji = "🔒",
            title = "100% Anonymous",
            body = "Your reports are anonymous. No personal data is collected or shared. All content moderation happens on-device using MediaPipe.",
            accent = DesignSystem.Colors.success
        ),
        OnboardingPage(
            emoji = "🆘",
            title = "Emergency SOS",
            body = "Tap the SOS button anytime for instant emergency vibration alerts. Your safety comes first — always.",
            accent = DesignSystem.Colors.sos
        ),
        OnboardingPage(
            emoji = "📋",
            title = "Data & Privacy",
            body = "• Reports are stored in Firebase (encrypted in transit)\n• Location is used only for nearby reports\n• No login or account required\n• Data auto-expires after 90 days\n• Open-source codebase",
            accent = DesignSystem.Colors.primary
        )
    )
    
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(0.1f))
            
            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(0.65f)
            ) { page ->
                val p = pages[page]
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(p.emoji, fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        p.title,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        p.body,
                        fontSize = 15.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }
            }
            
            // Page indicators
            Row(
                modifier = Modifier.padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(pages.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (pagerState.currentPage == index) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == index)
                                    pages[pagerState.currentPage].accent
                                else
                                    Color.White.copy(alpha = 0.3f)
                            )
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(0.05f))
            
            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Skip
                TextButton(onClick = onDismiss) {
                    Text(
                        "Skip",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                }
                
                // Next / Get Started
                val isLast = pagerState.currentPage == pages.size - 1
                Button(
                    onClick = {
                        if (isLast) {
                            onDismiss()
                        } else {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = pages[pagerState.currentPage].accent
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        if (isLast) "Get Started" else "Next",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private data class OnboardingPage(
    val emoji: String,
    val title: String,
    val body: String,
    val accent: Color
)
