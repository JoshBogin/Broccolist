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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
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
    onMoveItem: (SectionItem, Section, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showItems by remember { mutableStateOf(true) }
    val colorScheme = MaterialTheme.colorScheme
    val sectionFocusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val itemHeightPx = with(density) { 56.dp.toPx() }  // Convert dp to pixels

    // TODO will need to further hoist these up to the parent
      // that way drags can be tracked across sections and the z-index can be set when a
      // section item is being dragged
    // State for drag and drop
    var isDragging by remember { mutableStateOf(false) }
    var draggedItem by remember { mutableStateOf<SectionItem?>(null) }
    var draggedItemStartPosition by remember { mutableStateOf<Offset?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var currentDropSection by remember { mutableStateOf<Section?>(null) }
    var dropIndex by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = modifier
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
                        isDragging = isDragging && draggedItem == sectionItem,
                        onDragStart = { offset ->
                            isDragging = true
                            draggedItem = sectionItem
                            draggedItemStartPosition = offset
                            currentDropSection = section
                        },
                        onDrag = { offset ->
                            // Calculate potential drop position
                            dragOffset += offset
                            val newIndex = calculateDropIndex(dragOffset, section.items)
                            println("New Index: $newIndex")
                            dropIndex = newIndex
                        },
                        onDragEnd = {
                            if (dropIndex != null && currentDropSection != null) {
                                    onMoveItem(sectionItem, currentDropSection ?: section, dropIndex ?: index)
                            }
                            isDragging = false
                            draggedItem = null
                            draggedItemStartPosition = null
                            currentDropSection = null
                            dropIndex = null
                            dragOffset = Offset.Zero
                        },
                        modifier = if (isDragging && draggedItem == sectionItem) {
                            Modifier
                                .zIndex(1f)
                                .graphicsLayer {
                                    translationY = dragOffset.y
                                }
                        } else {
                            Modifier
                        }
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

private fun calculateDropIndex(offset: Offset, items: List<SectionItem>): Int {
    // Calculate the index based on the Y position
    val itemHeight = 56.dp
    val index = (offset.y / itemHeight.value).toInt()
//    println("New Index: $index")
    return index.coerceIn(0, items.size)
}
