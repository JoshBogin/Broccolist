package com.broccolist.login.presentation

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Step 1 of the reset flow: capture the user's email and trigger
 * `auth.resetPasswordForEmail` via [onSendResetLink]. Calling code is
 * expected to navigate to [com.broccolist.app.data.Route.ResetPasswordSent]
 * after invoking the callback.
 */
@Composable
fun ResetPasswordRequestScreen(
    onBack: () -> Unit,
    onBackToLogin: () -> Unit,
    onSendResetLink: (email: String) -> Unit,
) {
    var email by remember { mutableStateOf("") }

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

        Spacer(Modifier.weight(0.6f))

        IconBadgeRounded(icon = Icons.Outlined.Lock, contentDescription = null)

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Reset your password",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onBackground,
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Enter the email associated with your account and we'll send you a link to reset your password.",
            fontSize = 16.sp,
            color = colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            singleLine = true,
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { onSendResetLink(email.trim()) },
            enabled = email.isNotBlank(),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.primaryContainer,
                contentColor = colorScheme.primary,
                disabledContainerColor = colorScheme.primaryContainer.copy(alpha = 0.5f),
                disabledContentColor = colorScheme.primary.copy(alpha = 0.5f),
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(12.dp))
            Text(text = "Send reset link", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            TextButton(onClick = onBackToLogin) {
                Text("Back to login", color = colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.weight(1f))
    }
}

/**
 * Shared rounded-square icon badge used by every step of the reset flow.
 * Light primaryContainer fill, primary-tint icon.
 */
@Composable
internal fun IconBadgeRounded(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String?,
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .background(
                color = colorScheme.primaryContainer,
                shape = RoundedCornerShape(20.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = colorScheme.primary,
            modifier = Modifier.size(36.dp),
        )
    }
}

/** Circular variant used by the "Check your email" and success states. */
@Composable
internal fun IconBadgeCircle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String?,
) {
    Box(
        modifier = Modifier
            .size(96.dp)
            .background(
                color = colorScheme.primaryContainer,
                shape = androidx.compose.foundation.shape.CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = colorScheme.primary,
            modifier = Modifier.size(48.dp),
        )
    }
}
