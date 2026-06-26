package com.broccolist.checklist.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import broccolist.composeapp.generated.resources.Res
import broccolist.composeapp.generated.resources.cd_delete_icon
import com.broccolist.checklist.domain.SectionItem
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

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

    // Always-rendered TextField approach. We sidestep the "tap is eaten by
    // the draggable parent" gesture conflict by letting BasicTextField own
    // tap-to-focus from the start — it visually matches plain Text when
    // unfocused, and the cursor + keyboard appear when focused. No separate
    // Text/TextField swap, no extra clickable competing with draggable.
    val itemKey = item.id ?: 0
    var draftName by remember(itemKey) { mutableStateOf(item.name) }
    val interactionSource = remember(itemKey) { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // Sync the draft buffer with external changes to the item's name (e.g.
    // a server-side update from another device) — but only when the user
    // isn't currently editing, so we never clobber an in-progress edit.
    LaunchedEffect(item.name, isFocused) {
        if (!isFocused) draftName = item.name
    }

    // Commits the current draft. Empty / whitespace-only entries revert to
    // the previous name (per PRD's "empty names revert" rule). Called from
    // the Done key handler AND from onFocusChanged so blur also commits.
    fun commitRename() {
        val trimmed = draftName.trim()
        when {
            trimmed.isEmpty() -> draftName = item.name
            trimmed != item.name -> updateItem(item.copy(name = trimmed))
        }
    }

    // Reset offset when another section is being swiped.
    LaunchedEffect(isCurrentlyBeingSwiped) {
        if (!isCurrentlyBeingSwiped) {
            offsetX = 0f
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
    ) {
        // Background layer (the red Delete pane revealed during a swipe).
        Box(modifier = Modifier.matchParentSize()) {
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxSize(),
            ) {
                val deleteWidth = if (offsetX > deleteThreshold)
                    Modifier.width(IntrinsicSize.Min)
                else
                    Modifier.fillMaxWidth()

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
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(Res.string.cd_delete_icon),
                        tint = MaterialTheme.colorScheme.onError,
                    )
                }
            }

            // Foreground content
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    // fillMaxSize (not just width) so the foreground fully
                    // covers the 56dp outer Box. With just fillMaxWidth,
                    // the row sized itself to the BasicTextField's
                    // intrinsic height — a few dp shorter than 56dp — and
                    // the red Delete background underneath would peek out
                    // along the top/bottom edges between rows.
                    .fillMaxSize()
                    .offset { IntOffset(offsetX.roundToInt(), 0) }
                    .background(color)
                    .draggable(
                        // While editing, hand drag gestures off so the
                        // TextField can do cursor / selection drags. The
                        // user has to commit (press Done or tap away)
                        // before swipe-to-delete is available again.
                        enabled = !isFocused,
                        orientation = Orientation.Horizontal,
                        state = rememberDraggableState { delta ->
                            if (swipeInCooldown || delta > 0 && offsetX >= 0f) {
                                return@rememberDraggableState
                            }
                            focusManager.clearFocus()
                            onSwipeStateChange(item.id ?: 0)
                            offsetX += delta

                            if (offsetX < fullDeleteThreshold) {
                                removeItem(item)
                                onDeleteSwipe()
                                offsetX = 0f
                            }

                            if (offsetX < fullDeleteThreshold * 0.8f && !hasTriggeredHaptic) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                hasTriggeredHaptic = true
                            } else if (offsetX > fullDeleteThreshold * 0.8f) {
                                hasTriggeredHaptic = false
                            }
                        },
                        onDragStopped = {
                            if (offsetX <= deleteThreshold) {
                                removeItem(item)
                                onDeleteSwipe()
                                offsetX = 0f
                            } else if (offsetX <= showOptionsStartThreshold && offsetX > deleteThreshold) {
                                offsetX = showOptionsThreshold
                            } else if (offsetX > showOptionsStartThreshold) {
                                offsetX = 0f
                            }
                        },
                    ),
            ) {
                // Always-rendered editable field. Looks identical to a
                // static Text when unfocused (no border / cursor); pressing
                // Done or tapping away commits whatever the user typed.
                BasicTextField(
                    value = draftName,
                    onValueChange = { draftName = it },
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        commitRename()
                        focusManager.clearFocus()
                    }),
                    interactionSource = interactionSource,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .onFocusChanged { state ->
                            // Lost focus (tapped away, scrolled, swiped)
                            // → commit any pending edit so it doesn't
                            // silently disappear.
                            if (!state.isFocused) commitRename()
                        },
                )
                Checkbox(
                    checked = item.isChecked,
                    onCheckedChange = {
                        // Checkbox doesn't take focus on tap, so the
                        // TextField above stays focused unless we clear
                        // focus ourselves. Clearing here trips the
                        // TextField's onFocusChanged → commitRename, so
                        // any in-progress edit is saved before toggling.
                        focusManager.clearFocus()
                        updateItem(item.copy(isChecked = it))
                    },
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .padding(vertical = 16.dp)
                        .size(16.dp),
                )
            }
        }
    }
}
