package com.broccolist.checklist.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import broccolist.composeapp.generated.resources.Res
import broccolist.composeapp.generated.resources.cd_chevron_icon
import broccolist.composeapp.generated.resources.cd_delete_icon
import broccolist.composeapp.generated.resources.cd_edit_icon
import com.broccolist.checklist.domain.GrocerySection
import com.broccolist.checklist.domain.Section
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@Composable
internal fun SwipeableBox(
    removeSection: (Section) -> Unit,
    colorScheme: ColorScheme,
    showItems: Boolean,
    onClick: () -> Unit,
    section: Section,
    onUpdateSection: (Section) -> Unit,
    onDeleteSwipe: () -> Unit = {},
    swipeInCooldown: Boolean = false,
    isCurrentlyBeingSwiped: Boolean,
    onSwipeStateChange: (Int) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current
    var offsetX by remember { mutableFloatStateOf(0f) }
    val showOptionsStartThreshold = with(density) { -80.dp.toPx() }
    val showOptionsThreshold = with(density) { -180.dp.toPx() }
    val deleteThreshold = with(density) { -240.dp.toPx() }
    val fullDeleteThreshold = with(density) { -300.dp.toPx() }

    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val showEditOption = offsetX > deleteThreshold

    val haptic = LocalHapticFeedback.current
    var hasTriggeredHaptic by remember { mutableStateOf(false) }

    // Centralized "are we deleting this section?" dispatch — called from
    // every delete path (tapping the swiped-out red background, swiping
    // past the full-delete threshold, or releasing past the trigger
    // threshold). Each path used to call `removeSection` directly; now
    // they all flip this flag so the AlertDialog below gets the final say.
    fun askToDeleteSection() {
        showDeleteConfirmation = true
    }

    // Reset offset when another section is being swiped
    LaunchedEffect(isCurrentlyBeingSwiped) {
        if (!isCurrentlyBeingSwiped) {
            offsetX = 0f
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        // Background layer
        Box(
            modifier = Modifier
                .matchParentSize()
        ) {
            // Split background for edit and delete
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxSize()
            ) {
                // Edit background
                AnimatedVisibility(showEditOption) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .background(colorScheme.tertiaryContainer)
                            .clickable { showEditDialog = true }
                            .padding(horizontal = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(Res.string.cd_edit_icon),
                            tint = colorScheme.onTertiaryContainer
                        )
                    }
                }

                val deleteWidth = if (showEditOption)
                    Modifier.width(IntrinsicSize.Min)
                else
                    Modifier.fillMaxWidth()

                // Delete background
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .then(deleteWidth)
                        .background(colorScheme.error)
                        .clickable { askToDeleteSection() }
                        .padding(horizontal = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(Res.string.cd_delete_icon),
                        tint = colorScheme.onError
                    )
                }
            }

            // Foreground content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset { IntOffset(offsetX.roundToInt(), 0) }
                    .background(colorScheme.background)
                    .clickable(
                        interactionSource = null,
                        indication = null
                    ) {
                        onClick()
                        focusManager.clearFocus()
                    }
                    .draggable(
                        orientation = Orientation.Horizontal,
                        state = rememberDraggableState { delta ->
                            if (swipeInCooldown || delta > 0 && offsetX >= 0f) {
                                return@rememberDraggableState
                            }
                            // Clear focus when swiping
                            focusManager.clearFocus()
                            // Notify parent that this section is being swiped
                            onSwipeStateChange(section.id ?: 0)
                            offsetX += delta

                            // Past the full-delete threshold — ask for
                            // confirmation, snap the row back, and let the
                            // AlertDialog handle the actual removal. Used
                            // to delete unconditionally here, but section
                            // delete is now confirmation-gated.
                            if (offsetX < fullDeleteThreshold) {
                                askToDeleteSection()
                                offsetX = 0f
                            }

                            // Haptic feedback
                            if (offsetX < fullDeleteThreshold * 0.8f && !hasTriggeredHaptic) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                hasTriggeredHaptic = true
                            } else if (offsetX > fullDeleteThreshold * 0.8f) {
                                hasTriggeredHaptic = false
                            }
                        },
                        onDragStopped = {
                            // Released past deleteThreshold — confirm before
                            // removing. Snap to 0 so the row's resting
                            // state matches "no pending action".
                            if (offsetX <= deleteThreshold) {
                                askToDeleteSection()
                                offsetX = 0f
                            } else if (offsetX <= showOptionsStartThreshold && offsetX > deleteThreshold) {
                                offsetX = showOptionsThreshold
                            } else if (offsetX > showOptionsStartThreshold) {
                                offsetX = 0f
                            }
                        }
                    ),
            ) {
                Text(
                    text = section.section.displayName,
                    fontSize = 18.sp,
                    color = colorScheme.primary,
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.Center)
                )
                IconButton(
                    onClick = onClick,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(vertical = 8.dp)
                        .size(16.dp)
                        .align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = stringResource(Res.string.cd_chevron_icon),
                        tint = colorScheme.primary,
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(if (showItems) 180f else 0f)
                    )
                }
            }
        }

        // Delete confirmation. All three section-delete entry points
        // (the swipe-revealed red button, a full-swipe past the trigger,
        // and a release past the deleteThreshold) funnel into this one
        // dialog so the user always sees a confirm step before a section
        // and all its items are gone.
        if (showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteConfirmation = false
                    offsetX = 0f
                },
                title = { Text("Delete section?") },
                text = {
                    Text(
                        "\"${section.section.displayName}\" and all of its items will be permanently deleted.",
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            removeSection(section)
                            onDeleteSwipe()
                            showDeleteConfirmation = false
                            offsetX = 0f
                        },
                    ) {
                        Text("Delete", color = colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDeleteConfirmation = false
                            offsetX = 0f
                        },
                    ) {
                        Text("Cancel")
                    }
                },
            )
        }

        // Edit Dialog
        if (showEditDialog) {
            var newName by remember { mutableStateOf(section.section.displayName) }

            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = { Text("Edit Section Name") },
                text = {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Section Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onUpdateSection(section.copy(section = GrocerySection.Custom(newName)))
                            showEditDialog = false
                            offsetX = 0f
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showEditDialog = false }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
