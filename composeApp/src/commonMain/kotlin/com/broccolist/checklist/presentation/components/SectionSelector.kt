package com.broccolist.checklist.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.broccolist.checklist.domain.GrocerySection
import com.broccolist.checklist.domain.Section
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionSelector(
    sections: List<Section>,
    onSectionSelected: (GrocerySection) -> Unit
) {
    var expandedDropdown by remember { mutableStateOf(false) }
    var showCustomTextField by remember { mutableStateOf(false) }
    var customSectionName by remember { mutableStateOf("") }
    var selectedSection by remember { mutableStateOf("Select a section") }

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Dropdown for predefined sections
        ExposedDropdownMenuBox(
            expanded = expandedDropdown,
            onExpandedChange = { expandedDropdown = it }
        ) {
            OutlinedTextField(
                value = selectedSection,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown)
                },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable)
            )

            ExposedDropdownMenu(
                expanded = expandedDropdown,
                onDismissRequest = { expandedDropdown = false }
            ) {
                // Predefined sections
                GrocerySection.getPredefinedSections().forEach { section ->
                    if (sections.any { it.section == section }) return@forEach

                    DropdownMenuItem(
                        text = { Text(section.displayName) },
                        onClick = {
                            selectedSection = section.displayName
                            onSectionSelected(section)
                            expandedDropdown = false
                        }
                    )
                }

                // Custom section option
                DropdownMenuItem(
                    text = { Text("Add Custom Section") },
                    onClick = {
                        selectedSection = "Add Custom Section"
                        showCustomTextField = true
                        expandedDropdown = false
                    }
                )
            }
        }

        // Text field for custom section
        if (showCustomTextField) {
            OutlinedTextField(
                value = customSectionName,
                onValueChange = {
                    customSectionName = it
                    onSectionSelected(GrocerySection.Custom(it))
                },
                label = { Text("Section Name") },
                singleLine = true
            )
        }
    }
}

@Preview
@Composable
fun SectionSelectorPreview() {
    SectionSelector(
        sections = emptyList(),
        onSectionSelected = {}
    )
}
