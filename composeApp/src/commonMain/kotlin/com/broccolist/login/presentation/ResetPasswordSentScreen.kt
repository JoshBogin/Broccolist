package com.broccolist.login.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Step 2 of the reset flow: "Check your email." The user taps the link in
 * the email (which silently creates a recovery session via the deep link
 * handler in `handleSupabaseDeeplink`), then taps [onContinue] to advance
 * to [com.broccolist.app.data.Route.ResetPasswordCreate].
 */
@Composable
fun ResetPasswordSentScreen(
    email: String,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onResend: () -> Unit,
    onBackToLogin: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 24.dp),
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.padding(vertical = 8.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = colorScheme.primary,
            )
        }

        Spacer(Modifier.weight(0.8f))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            IconBadgeCircle(icon = Icons.Outlined.MailOutline, contentDescription = null)

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Check your email",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(12.dp))

            // Email shown bold inline with the rest of the body copy.
            val body = buildAnnotatedString {
                append("We sent a password reset link to ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(email) }
                append(". Tap the link in that email to choose a new password.")
            }
            Text(
                text = body,
                fontSize = 16.sp,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = onContinue,
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
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "I've got the link — continue",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                )
            }

            Spacer(Modifier.height(8.dp))

            TextButton(onClick = onResend) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(text = "Resend email", color = colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }

            TextButton(onClick = onBackToLogin) {
                Text("Back to login", color = colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.weight(1.2f))
    }
}

// Vertical-space arrangement reused by the create/success screens too.
@Suppress("unused")
private val ResetSpacing = Arrangement.spacedBy(16.dp)
