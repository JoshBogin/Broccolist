package com.broccolist.checklist.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.broccolist.checklist.domain.Section
import com.broccolist.checklist.domain.SectionItem
import kotlinx.coroutines.launch

@Composable
internal fun ItemSection(
    removeSection: (Section) -> Unit,
    onUpdateSection: (Section) -> Unit,
    addItemToSection: (Section, SectionItem) -> Unit,
    removeItemFromSection: (Section, SectionItem) -> Unit,
    updateItem: (SectionItem) -> Unit,
    section: Section,
    onSwipe: () -> Unit = {},
    swipeInCooldown: Boolean = false,
    currentlySwipedId: Int? = null,
    onSwipeStateChange: (Int) -> Unit,
    scrollState: ScrollState,
    isLastSection: Boolean,
) {
    var showItems by remember { mutableStateOf(true) }
    val colorScheme = MaterialTheme.colorScheme
    val sectionFocusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val itemHeightPx = with(density) { 56.dp.toPx() }  // Convert dp to pixels

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalDivider(
            color = colorScheme.primary,
            thickness = 1.dp,
        )

        SwipeableBox(
            removeSection = removeSection,
            colorScheme = colorScheme,
            showItems = showItems,
            onClick = { showItems = !showItems },
            section = section,
            onUpdateSection = onUpdateSection,
            onDeleteSwipe = onSwipe,
            swipeInCooldown = swipeInCooldown,
            isCurrentlyBeingSwiped = currentlySwipedId == section.id,
            onSwipeStateChange = onSwipeStateChange,
        )

        HorizontalDivider(
            color = colorScheme.primary,
            thickness = 1.dp,
        )

        AnimatedVisibility(showItems) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                section.items?.forEachIndexed { index, sectionItem ->
                    val color = if (index % 2 == 1) {
                        colorScheme.secondaryContainer
                    } else {
                        colorScheme.background
                    }
                    ItemRow(
                        item = sectionItem,
                        color = color,
                        updateItem = updateItem,
                        removeItem = { item ->
                            removeItemFromSection(section, item)
                        },
                        onDeleteSwipe = onSwipe,
                        swipeInCooldown = swipeInCooldown,
                        isCurrentlyBeingSwiped = currentlySwipedId == sectionItem.id,
                        onSwipeStateChange = onSwipeStateChange,
                    )
                }

                // Additional placeholder row for adding new items
                PlaceholderItemRow(
                    focusRequester = sectionFocusRequester,
                    onAddItem = { newItemName ->
                        val newItem = SectionItem(
                            name = newItemName,
                            isChecked = false,
                            sectionId = section.id
                        )
                        addItemToSection(section, newItem)

                        // Scroll after adding the item
                        coroutineScope.launch {
                            // Scroll by the height of one item
                            scrollState.animateScrollBy(itemHeightPx)
                        }
                    },
                    color = if ((section.items?.size ?: 0) % 2 == 1) {
                        colorScheme.secondaryContainer
                    } else {
                        colorScheme.background
                    }
                )
                if (isLastSection) {
                    // Add padding to the bottom of the last section
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
        }
    }
}
