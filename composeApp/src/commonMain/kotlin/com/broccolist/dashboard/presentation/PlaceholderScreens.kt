package com.broccolist.dashboard.presentation

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Stub for the Shared Lists tab. Phase 4 will wire this up to
 * `list_members` and Realtime; Phase 1 just communicates the upcoming
 * feature and respects guest-mode gating.
 */
@Composable
fun SharedListsScreen(
    isGuest: Boolean,
    onBack: () -> Unit,
) {
    PlaceholderScreen(
        title = "Shared Lists",
        icon = Icons.Outlined.Group,
        heading = if (isGuest) "Sign up to share lists" else "Coming soon",
        body = if (isGuest) {
            "Shared lists are part of the friends system — create an account to invite people and edit lists together in real time."
        } else {
            "We're building this next. Soon you'll be able to invite friends to a list and edit together in real time."
        },
        onBack = onBack,
    )
}

/**
 * Stub for the Recipes tab. Phase 5 will add full recipe CRUD + parsing.
 */
@Composable
fun RecipesScreen(onBack: () -> Unit) {
    PlaceholderScreen(
        title = "Recipes",
        icon = Icons.Outlined.RestaurantMenu,
        heading = "Coming soon",
        body = "Save recipes and add their ingredients to any list with one tap — auto-sorted into store sections.",
        onBack = onBack,
    )
}

/**
 * Stub for the Profile / Friends screen. Phase 4 will add the real
 * profile, friends list, and invites flow.
 */
@Composable
fun ProfileScreen(onBack: () -> Unit) {
    PlaceholderScreen(
        title = "Profile",
        icon = Icons.Default.Person,
        heading = "Coming soon",
        body = "Your profile, friends list, and invites will live here in a future update.",
        onBack = onBack,
    )
}

@Composable
private fun PlaceholderScreen(
    title: String,
    icon: ImageVector,
    heading: String,
    body: String,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background),
    ) {
        Surface(color = colorScheme.surfaceContainer) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(64.dp),
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .align(Alignment.CenterStart),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colorScheme.primary,
                    )
                }
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            HorizontalDivider(thickness = 1.dp, color = colorScheme.primary.copy(alpha = 0.2f))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(color = colorScheme.primaryContainer, shape = RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(48.dp),
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = heading,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = body,
                fontSize = 15.sp,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
