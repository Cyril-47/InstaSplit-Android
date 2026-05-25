package com.instasplit.app.presentation.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * A beautiful, premium phone mockup container. Clips its children inside
 * the simulated "screen" of a mobile device.
 */
@Composable
fun PhoneMockup(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    // Phone shell: dark bezel with rounded corners and subtle metallic border
    Box(
        modifier = modifier
            .fillMaxHeight(0.7f)
            .aspectRatio(0.6f) // Standard phone aspect ratio
            .clip(RoundedCornerShape(36.dp))
            .background(Color(0xFF0F0F14))
            .border(4.dp, Color(0xFF2C2C35), RoundedCornerShape(36.dp))
            .border(12.dp, Color(0xFF08080C), RoundedCornerShape(36.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        // Phone screen area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            content()

            // Dynamic Island / Notch Pill Cutout
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
                    .width(72.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black)
            )
        }
    }
}

