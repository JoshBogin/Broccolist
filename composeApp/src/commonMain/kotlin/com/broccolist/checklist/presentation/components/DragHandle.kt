package com.broccolist.checklist.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import broccolist.composeapp.generated.resources.Res
import broccolist.composeapp.generated.resources.cd_drag_handle
import org.jetbrains.compose.resources.stringResource

/**
 * Grip shown at the start of item rows and section headers. The caller
 * supplies the drag gesture through [modifier] (the reorderable library's
 * `draggableHandle`), so this stays a plain piece of UI.
 */
@Composable
internal fun DragHandle(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxHeight()
            .width(48.dp),
    ) {
        Icon(
            imageVector = Icons.Default.DragHandle,
            contentDescription = stringResource(Res.string.cd_drag_handle),
            // A row that is still being saved can't be moved yet.
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = if (enabled) 0.45f else 0.15f),
            modifier = Modifier.size(20.dp),
        )
    }
}
