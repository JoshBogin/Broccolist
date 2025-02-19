package com.broccolist.checklist.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import broccolist.composeapp.generated.resources.Res
import broccolist.composeapp.generated.resources.cd_delete_icon
import com.broccolist.checklist.domain.SectionItem
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ItemRow(
    item: SectionItem,
    color: Color,
    updateItem: (SectionItem) -> Unit,
    removeItem: (SectionItem) -> Unit = {},
    onDeleteSwipe: () -> Unit = {},
    swipeInCooldown: Boolean = false,
    isCurrentlyBeingSwiped: Boolean,
    onSwipeStateChange: (Int) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current
    var offsetX by remember { mutableFloatStateOf(0f) }
    val showOptionsStartThreshold = with(density) { -40.dp.toPx() }
    val showOptionsThreshold = with(density) { -90.dp.toPx() }
    val deleteThreshold = with(density) { -200.dp.toPx() }
    val fullDeleteThreshold = with(density) { -300.dp.toPx() }

    val haptic = LocalHapticFeedback.current
    var hasTriggeredHaptic by remember { mutableStateOf(false) }

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
            .dragAndDropSource(
                drawDragDecoration = { } // TODO add drag decoration
            ) {
                detectTapGestures(
                    onLongPress = {
                        focusManager.clearFocus()
                        onSwipeStateChange(item.id ?: 0) // maybe use this as a common function to
                        // ensure only one item is swiped or dragged at a time
                        offsetX = showOptionsThreshold
                    }
                )
            }
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
                val deleteWidth = if (offsetX > deleteThreshold)
                    Modifier.width(IntrinsicSize.Min)
                else
                    Modifier.fillMaxWidth()

                // Delete background
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .then(deleteWidth)
                        .background(MaterialTheme.colorScheme.error)
                        .clickable {
                            removeItem(item)
                            onDeleteSwipe()
                        }
                        .padding(horizontal = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(Res.string.cd_delete_icon),
                        tint = MaterialTheme.colorScheme.onError
                    )
                }
            }

            // Foreground content
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(offsetX.roundToInt(), 0) }
                    .background(color)
                    .clickable(
                        interactionSource = null,
                        indication = null
                    ) {
                        updateItem(item.copy(isChecked = !item.isChecked))
                        focusManager.clearFocus()
                    }
                    .draggable(
                        orientation = Orientation.Horizontal,
                        state = rememberDraggableState { delta ->
                            if (swipeInCooldown || delta > 0 && offsetX >= 0f) {
                                return@rememberDraggableState
                            }
                            // clear focus from text field when swiping
                            focusManager.clearFocus()
                            // Notify parent that this section is being swiped
                            onSwipeStateChange(item.id ?: 0)
                            offsetX += delta

                            // Trigger delete if swiped past fullDeleteThreshold
                            if (offsetX < fullDeleteThreshold) {
                                removeItem(item)
                                onDeleteSwipe()
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
                            // Delete row if swiped past deleteThreshold
                            if (offsetX <= deleteThreshold) {
                                removeItem(item)
                                onDeleteSwipe()
                                offsetX = 0f
                                // Otherwise, snap to edit/delete position or reset
                            } else if (offsetX <= showOptionsStartThreshold && offsetX > deleteThreshold) {
                                offsetX = showOptionsThreshold
                            } else if (offsetX > showOptionsStartThreshold) {
                                offsetX = 0f
                            }
                        }
                    ),
            ) {
                Text(
                    text = item.name,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .padding(vertical = 16.dp)
                )
                Checkbox(
                    checked = item.isChecked,
                    onCheckedChange = { updateItem(item.copy(isChecked = it)) },
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .padding(vertical = 16.dp)
                        .size(16.dp)
                )
            }
        }
    }
}
