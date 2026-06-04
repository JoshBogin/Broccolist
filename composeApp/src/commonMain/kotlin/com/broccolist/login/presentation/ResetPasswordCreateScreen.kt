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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Step 3 of the reset flow: user enters a new password (and confirms it)
 * while the screen lints against three live requirements. Submit is only
 * enabled when all three pass AND the two fields match. Requires an
 * active recovery session created by the deep-link handler.
 */
@Composable
fun ResetPasswordCreateScreen(
    onBack: () -> Unit,
    onUpdatePassword: (password: String) -> Unit,
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val rules = PasswordRules.evaluate(password)
    val matches = password.isNotEmpty() && password == confirmPassword
    val canSubmit = rules.allMet && matches

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

        Spacer(Modifier.weight(0.4f))

        // Outlined Lock stands in for the shield-check shown in the design;
        // the extended Material icons set (which has `VerifiedUser`) would
        // require an extra dependency. Sticking to the core icon set keeps
        // the artifact small.
        IconBadgeRounded(icon = Icons.Outlined.Lock, contentDescription = null)

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Create a new password",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onBackground,
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Choose a strong password you haven't used before.",
            fontSize = 16.sp,
            color = colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            singleLine = true,
            label = { Text("New password") },
            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
            trailingIcon = {
                // Text-based show/hide toggle to avoid pulling in the
                // extended icons artifact for Visibility/VisibilityOff.
                TextButton(
                    onClick = { passwordVisible = !passwordVisible },
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp),
                ) {
                    Text(
                        text = if (passwordVisible) "Hide" else "Show",
                        color = colorScheme.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            singleLine = true,
            label = { Text("Confirm password") },
            leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            isError = confirmPassword.isNotEmpty() && !matches,
            supportingText = {
                if (confirmPassword.isNotEmpty() && !matches) {
                    Text("Passwords don't match", color = colorScheme.error)
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            RequirementRow(label = "At least 8 characters", met = rules.lengthOk)
            RequirementRow(label = "Upper & lowercase letters", met = rules.mixedCase)
            RequirementRow(label = "At least one number", met = rules.hasNumber)
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { onUpdatePassword(password) },
            enabled = canSubmit,
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
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(12.dp))
            Text(text = "Update password", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }

        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun RequirementRow(label: String, met: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (met) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        } else {
            // Empty circle outline for unmet requirements.
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(color = colorScheme.background, shape = CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(color = colorScheme.background, shape = CircleShape),
                )
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = colorScheme.outline.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            color = if (met) colorScheme.onSurface else colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
        )
    }
}

/**
 * Pure-data password rule evaluation. Kept separate from the composable so
 * it can be unit-tested without Compose dependencies (Phase-1 tests, when
 * we add them).
 */
internal data class PasswordRules(
    val lengthOk: Boolean,
    val mixedCase: Boolean,
    val hasNumber: Boolean,
) {
    val allMet: Boolean get() = lengthOk && mixedCase && hasNumber

    companion object {
        fun evaluate(password: String): PasswordRules = PasswordRules(
            lengthOk = password.length >= 8,
            mixedCase = password.any { it.isUpperCase() } && password.any { it.isLowerCase() },
            hasNumber = password.any { it.isDigit() },
        )
    }
}
