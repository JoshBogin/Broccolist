package com.broccolist.checklist.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun PlaceholderItemRow(
    focusRequester: FocusRequester,
    onAddItem: (String) -> Unit,
    color: Color
) {
    var newItemName by remember { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .background(color)
            .clickable(
                interactionSource = null,
                indication = null
            ) {
                if (!isFocused) {
                    focusRequester.requestFocus()
                }
            }
    ) {
        TextField(
            value = newItemName,
            onValueChange = { newItemName = it },
            placeholder = {
                Text(
                    text = "Add Item",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    fontStyle = FontStyle.Italic
                )
            },
            singleLine = true,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
                .focusRequester(focusRequester)
                .onFocusChanged {
                    isFocused = it.isFocused
                },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                if (newItemName.isNotBlank()) {
                    onAddItem(newItemName)
                    newItemName = ""
//                    focusRequester.requestFocus()
                }
            }),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.onBackground,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground
            ),
        )
        Checkbox(
            checked = false,
            enabled = false,
            onCheckedChange = { /* Do nothing */ },
            colors = CheckboxDefaults.colors(
                uncheckedColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            ),
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(vertical = 16.dp)
                .size(16.dp)
        )
    }
}
