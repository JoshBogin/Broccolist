package com.broccolist.app.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shown when Supabase reports `SessionStatus.RefreshFailure` — the
 * stored refresh token couldn't get a new access token (expired, revoked,
 * or the network dropped mid-refresh).
 *
 * - **Try again** asks the auth API to refresh the current session. If it
 *   succeeds, the underlying `sessionStatus` flow flips back to
 *   `Authenticated` and `AuthenticatedContent` will swap this screen for
 *   the real content automatically.
 * - **Sign in again** signs out (drops the bad refresh token) and routes
 *   to Login.
 */
@Composable
fun SessionRefreshFailureScreen(
    onTryAgain: () -> Unit,
    onSignInAgain: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(0.8f))

        SessionFailureIllustration()

        Spacer(Modifier.height(40.dp))

        Text(
            text = "We couldn't refresh\nyour session",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onBackground,
            textAlign = TextAlign.Center,
            lineHeight = 36.sp,
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Your secure session expired or the connection dropped. Your place is saved — let's reconnect.",
            fontSize = 15.sp,
            color = colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
        )

        Spacer(Modifier.weight(1.2f))

        Button(
            onClick = onTryAgain,
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.primary,
                contentColor = colorScheme.onPrimary,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(12.dp))
            Text(text = "Try again", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }

        TextButton(
            onClick = onSignInAgain,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
        ) {
            Text(
                text = "Sign in again",
                color = colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
            )
        }
    }
}

/**
 * Decorative scene above the heading: a soft light-green disc with a
 * dashed outline ring, scattered "ambient" dots and small shapes, and a
 * white warning-badge tucked into the lower-right of the disc. Drawn with
 * Compose's [Canvas] rather than vector assets so it picks up the theme
 * colors and survives a dark-mode switch without re-exporting artwork.
 */
@Composable
private fun SessionFailureIllustration() {
    val discRadius = 110.dp
    val totalSize = 280.dp

    Box(
        modifier = Modifier.size(totalSize),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val discPx = discRadius.toPx()

            // Filled disc — light-green primaryContainer.
            drawCircle(
                color = androidx.compose.ui.graphics.Color(0xFFC8E6C9).copy(alpha = 0.5f),
                radius = discPx,
                center = center,
            )

            // Dashed outline ring just inside the edge.
            drawCircle(
                color = androidx.compose.ui.graphics.Color(0xFF378A49).copy(alpha = 0.45f),
                radius = discPx - 2.dp.toPx(),
                center = center,
                style = Stroke(
                    width = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f),
                ),
            )

            val accent = androidx.compose.ui.graphics.Color(0xFF378A49).copy(alpha = 0.45f)

            // Scattered dots around the disc.
            val dotPositions = listOf(
                Offset(0.18f, 0.30f) to 4.dp,
                Offset(0.30f, 0.55f) to 5.dp,
                Offset(0.78f, 0.40f) to 4.dp,
                Offset(0.62f, 0.22f) to 3.dp,
                Offset(0.84f, 0.62f) to 5.dp,
                Offset(0.22f, 0.78f) to 3.dp,
            )
            dotPositions.forEach { (relative, radiusDp) ->
                drawCircle(
                    color = accent,
                    radius = radiusDp.toPx(),
                    center = Offset(size.width * relative.x, size.height * relative.y),
                )
            }

            // Outlined small circle (top-left vibe).
            drawCircle(
                color = accent,
                radius = 8.dp.toPx(),
                center = Offset(size.width * 0.20f, size.height * 0.66f),
                style = Stroke(width = 1.5.dp.toPx()),
            )

            // Outlined small square (lower-right outside the disc).
            val squareSide = 12.dp.toPx()
            drawRect(
                color = accent,
                topLeft = Offset(
                    x = size.width * 0.74f,
                    y = size.height * 0.78f,
                ),
                size = Size(squareSide, squareSide),
                style = Stroke(width = 1.5.dp.toPx()),
            )

            // Short tick marks for ambient decoration.
            drawLine(
                color = accent,
                start = Offset(size.width * 0.08f, size.height * 0.50f),
                end = Offset(size.width * 0.14f, size.height * 0.50f),
                strokeWidth = 1.5.dp.toPx(),
            )
            drawLine(
                color = accent,
                start = Offset(size.width * 0.92f, size.height * 0.30f),
                end = Offset(size.width * 0.92f, size.height * 0.36f),
                strokeWidth = 1.5.dp.toPx(),
            )
        }

        // White warning badge tucked into the lower-right corner of the disc.
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(start = 96.dp, top = 76.dp)
                .size(52.dp)
                .background(color = colorScheme.surface, shape = CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.WarningAmber,
                contentDescription = null,
                tint = colorScheme.onSurface,
                modifier = Modifier.size(26.dp),
            )
        }
    }
}
