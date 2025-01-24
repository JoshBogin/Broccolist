package com.broccolist.checklist.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.broccolist.checklist.domain.GrocerySection
import com.broccolist.checklist.domain.Section
import com.broccolist.checklist.domain.SectionItem
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemInserter(
    sections: List<Section>,
    onItemInserted: (SectionItem) -> Unit,
    onSectionSelected: (Section) -> Unit // need to use this to check if section exists or not
) {
    var expandedDropdown by remember { mutableStateOf(false) }
    var showCustomTextField by remember { mutableStateOf(false) }
    var itemName by remember { mutableStateOf("") }
    var customSectionName by remember { mutableStateOf("") }
    var selectedSection by remember { mutableStateOf("Select a section") }

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Text field for item name
        OutlinedTextField(
            value = itemName,
            onValueChange = {
                itemName = it
                onItemInserted(SectionItem(
                    id = (0..1000000).random().toString(),
                    name = it,
                    isChecked = false
                ))
            },
            label = { Text("Item Name") },
            singleLine = true
        )

        // Dropdown for predefined sections
        ExposedDropdownMenuBox(
            expanded = expandedDropdown,
            onExpandedChange = { expandedDropdown = it },
            modifier = Modifier.padding(top = 8.dp) // Add padding to balance the spacing
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
                // Existing sections
                if (sections.isNotEmpty()) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "-- Current Sections --",
                                fontWeight = FontWeight.Bold,
                            )
                        },
                        onClick = { /* do nothing */ },
                    )
                    sections.forEach { section ->
                        DropdownMenuItem(
                            text = { Text(section.section.displayName) },
                            onClick = {
                                selectedSection = section.section.displayName
                                onSectionSelected(section)
                                showCustomTextField = false
                                expandedDropdown = false
                            }
                        )
                    }
                }

                // Unused predefined sections
                val unUsedSections = GrocerySection.getPredefinedSections().filter { section ->
                    section !in sections.map { it.section }
                }
                if (unUsedSections.isNotEmpty()) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "-- Unused Sections --",
                                fontWeight = FontWeight.Bold,
                            )
                        },
                        onClick = { /* do nothing */ },
                    )
                    GrocerySection.getPredefinedSections().filter { section ->
                        section !in sections.map { it.section }
                    }.forEach { section ->
                        DropdownMenuItem(
                            text = { Text(section.displayName) },
                            onClick = {
                                selectedSection = section.displayName
                                onSectionSelected(
                                    Section(
                                        id = (0..1000000).random().toString(),
                                        section = section,
                                        items = emptyList()
                                    )
                                )
                                showCustomTextField = false
                                expandedDropdown = false
                            }
                        )
                    }
                }

                // Custom section option
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Add Custom Section",
                            fontStyle = FontStyle.Italic,
                        )
                   },
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
                    onSectionSelected(
                        Section(
                            id = (0..1000000).random().toString(),
                            section = GrocerySection.Custom(it),
                            items = emptyList()
                        )
                    )
                },
                label = { Text("Section Name") },
                singleLine = true,
            )
        }
    }
}

@Preview
@Composable
fun ItemInserterPreview() {
    ItemInserter(
        sections = emptyList(),
        onItemInserted = {},
        onSectionSelected = {},
    )
}
