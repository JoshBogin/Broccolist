package com.broccolist.dashboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.RestaurantMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import broccolist.composeapp.generated.resources.Res
import broccolist.composeapp.generated.resources.broccolist_icon
import com.broccolist.checklist.domain.CheckList
import io.github.jan.supabase.auth.user.UserSession
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DashboardScreenRoute(
    viewModel: DashboardViewModel = koinViewModel(),
    userSession: UserSession?,
    onOpenMenu: () -> Unit,
    onOpenProfile: () -> Unit,
    onPersonalLists: () -> Unit,
    onSharedLists: () -> Unit,
    onRecipes: () -> Unit,
    onOpenList: (listId: Int) -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(userSession?.user?.id) {
        viewModel.refresh(userSession)
    }

    DashboardScreen(
        state = state,
        onOpenMenu = onOpenMenu,
        onOpenProfile = onOpenProfile,
        onPersonalLists = onPersonalLists,
        onSharedLists = onSharedLists,
        onRecipes = onRecipes,
        onOpenList = onOpenList,
    )
}

@Composable
fun DashboardScreen(
    state: DashboardState,
    onOpenMenu: () -> Unit,
    onOpenProfile: () -> Unit,
    onPersonalLists: () -> Unit,
    onSharedLists: () -> Unit,
    onRecipes: () -> Unit,
    onOpenList: (listId: Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background),
    ) {
        DashboardTopBar(onOpenMenu = onOpenMenu, onOpenProfile = onOpenProfile)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(16.dp))

            Greeting(displayName = state.displayName)

            Spacer(Modifier.height(20.dp))

            DashboardTile(
                icon = Icons.AutoMirrored.Outlined.List,
                title = "Personal Lists",
                subtitle = "Lists only you can see",
                count = state.personalListCount,
                onClick = onPersonalLists,
            )
            Spacer(Modifier.height(12.dp))
            DashboardTile(
                icon = Icons.Outlined.Group,
                title = "Shared Lists",
                subtitle = if (state.isGuest) {
                    "Sign up to share lists with friends"
                } else "Plan together in real time",
                count = state.sharedListCount,
                onClick = onSharedLists,
            )
            Spacer(Modifier.height(12.dp))
            DashboardTile(
                icon = Icons.Outlined.RestaurantMenu,
                title = "Recipes",
                subtitle = "Save meals, add to any list",
                count = state.recipeCount,
                onClick = onRecipes,
            )

            if (state.recentLists.isNotEmpty()) {
                Spacer(Modifier.height(28.dp))
                Text(
                    text = "JUMP BACK IN",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp,
                    color = colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                state.recentLists.forEachIndexed { index, list ->
                    RecentListCard(list = list, onClick = { list.id?.let(onOpenList) })
                    if (index < state.recentLists.lastIndex) Spacer(Modifier.height(12.dp))
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DashboardTopBar(
    onOpenMenu: () -> Unit,
    onOpenProfile: () -> Unit,
) {
    Surface(color = colorScheme.surfaceContainer) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(64.dp),
        ) {
            IconButton(
                onClick = onOpenMenu,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .align(Alignment.CenterStart),
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open menu",
                    tint = colorScheme.primary,
                )
            }
            Icon(
                painter = painterResource(Res.drawable.broccolist_icon),
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.Center),
            )
            // Profile avatar — filled green circle with white person icon.
            Box(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .size(40.dp)
                    .clickable(onClick = onOpenProfile)
                    .background(color = colorScheme.primary, shape = CircleShape)
                    .align(Alignment.CenterEnd),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Open profile",
                    tint = colorScheme.onPrimary,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
        HorizontalDivider(thickness = 1.dp, color = colorScheme.primary.copy(alpha = 0.2f))
    }
}

@Composable
private fun Greeting(displayName: String) {
    val timeOfDay = currentTimeOfDay()
    Column {
        Text(
            text = "Good $timeOfDay, $displayName",
            fontSize = 14.sp,
            color = colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "What are we shopping for?",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onBackground,
        )
    }
}

@Composable
private fun DashboardTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    count: Int,
    onClick: () -> Unit,
) {
    Surface(
        color = colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
        shadowElevation = 1.dp,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Light-green rounded-square icon badge.
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(color = colorScheme.primaryContainer, shape = RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(28.dp),
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = colorScheme.onSurfaceVariant,
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = count.toString(),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary,
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun RecentListCard(
    list: CheckList,
    onClick: () -> Unit,
) {
    val totals = list.itemTotals()

    Surface(
        color = colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
        shadowElevation = 1.dp,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color = colorScheme.primaryContainer, shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = list.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                )
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { if (totals.total > 0) totals.checked.toFloat() / totals.total else 0f },
                    color = colorScheme.primary,
                    trackColor = colorScheme.primaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                )
            }

            Spacer(Modifier.width(12.dp))

            Text(
                text = "${totals.checked}/${totals.total}",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurface,
            )
        }
    }
}

private data class ItemTotals(val checked: Int, val total: Int)

private fun CheckList.itemTotals(): ItemTotals {
    var checked = 0
    var total = 0
    sections.orEmpty().forEach { section ->
        section.items.orEmpty().forEach { item ->
            total++
            if (item.isChecked) checked++
        }
    }
    return ItemTotals(checked = checked, total = total)
}

/**
 * Lightweight platform-agnostic time-of-day bucket. Avoids pulling kotlinx
 * datetime tz handling into common code; just uses the device's clock via
 * Clock.System.now() and bucketing by UTC hour. Good enough for greeting
 * copy — exact timezone correctness can come later.
 */
@Composable
private fun currentTimeOfDay(): String {
    val now = kotlinx.datetime.Clock.System.now()
    val hour = now.toEpochMilliseconds() / 3_600_000L % 24L
    return when (hour) {
        in 5..11 -> "morning"
        in 12..16 -> "afternoon"
        in 17..21 -> "evening"
        else -> "evening"
    }
}
