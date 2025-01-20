package com.broccolist.checklist.presentation

import BroccolistTheme.BroccolistTheme
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import broccolist.composeapp.generated.resources.Res
import broccolist.composeapp.generated.resources.add_close_icon
import broccolist.composeapp.generated.resources.app_name
import broccolist.composeapp.generated.resources.broccolist_icon
import broccolist.composeapp.generated.resources.cd_add_item
import broccolist.composeapp.generated.resources.cd_add_section
import broccolist.composeapp.generated.resources.cd_chevron_icon
import broccolist.composeapp.generated.resources.cd_delete_icon
import broccolist.composeapp.generated.resources.cd_drawer_menu
import broccolist.composeapp.generated.resources.cd_edit_icon
import broccolist.composeapp.generated.resources.cd_home_icon
import broccolist.composeapp.generated.resources.home
import broccolist.composeapp.generated.resources.item
import broccolist.composeapp.generated.resources.section
import broccolist.composeapp.generated.resources.sign_out
import com.broccolist.checklist.domain.GrocerySection
import com.broccolist.checklist.domain.Section
import com.broccolist.checklist.domain.SectionItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

@Composable
fun CheckListScreenRoute(
    viewModel: CheckListViewModel = koinViewModel(),
    onLogout: () -> Unit,
) {
    CheckListScreen(
        sections = viewModel.sections.collectAsStateWithLifecycle().value,
        addSection = viewModel::addSection,
        removeSection = viewModel::removeSection,
        updateSection = viewModel::updateSection,
        signOut = viewModel::signOut,
        onLogout = onLogout
    )
}

@Composable
fun CheckListScreen(
    sections: List<Section>,
    addSection: (Section) -> Unit,
    removeSection: (Section) -> Unit,
    updateSection: (Section) -> Unit,
    signOut: suspend () -> Unit,
    onLogout: () -> Unit,
) {
    val drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    var swipeInCooldown by remember { mutableStateOf(false) }
    var showSectionSelector by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet(
                drawerShape = RoundedCornerShape(24.dp),
                drawerContainerColor = colorScheme.surfaceContainer,
                modifier = Modifier
                    .padding(vertical = 64.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(24.dp)
                    )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(Res.string.app_name),
                            fontSize = 24.sp,
                            color = colorScheme.primary,
                            modifier = Modifier
                                .padding(16.dp)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = colorScheme.tertiary,
                                    shape = RoundedCornerShape(24.dp)
                                )
                        ) {
                            IconButton(
                                onClick = { },
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = stringResource(Res.string.cd_home_icon),
                                    tint = colorScheme.primary,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Text(
                                text = stringResource(Res.string.home),
                                fontSize = 20.sp,
                                color = colorScheme.primary,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                signOut()
                                onLogout()
                            }
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.primary
                        ),
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.BottomCenter)
                    ) {
                        Text(
                            text = stringResource(Res.string.sign_out),
                            fontSize = 16.sp,
                            color = colorScheme.onPrimary
                        )
                    }
                }

            }
        },
        drawerState = drawerState,
        scrimColor = Color.Transparent,
        modifier = Modifier.safeDrawingPadding()
    ) {
        Scaffold(
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorScheme.primaryContainer),
                ) {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                drawerState.open()
                            }
                        },
                        modifier = Modifier
                            .padding(16.dp)
                            .size(32.dp)
                            .align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = stringResource(Res.string.cd_drawer_menu),
                            tint = colorScheme.primary,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Icon(
                        painter = painterResource(Res.drawable.broccolist_icon),
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier
                            .padding(16.dp)
                            .size(32.dp)
                            .align(Alignment.Center)
                    )
                    IconButton(
                        onClick = {},
                        modifier = Modifier
                            .padding(16.dp)
                            .size(32.dp)
                            .align(Alignment.CenterEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = colorScheme.primary,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            },
            floatingActionButton = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.End,
                ) {
                    var showItems by remember { mutableStateOf(false) }
                    AnimatedVisibility(
                        visible = showItems,
                        enter = expandVertically(
                            clip = false
                        ),
                        exit = shrinkVertically(
                            clip = false
                        ),
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.End,
                        ) {
                            ExtendedFloatingActionButton(
                                onClick = { showSectionSelector = true },
                                containerColor = colorScheme.background,
                                elevation = FloatingActionButtonDefaults.elevation(
                                    defaultElevation = 4.dp,
                                    pressedElevation = 8.dp
                                ),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = stringResource(Res.string.cd_add_section),
                                    tint = colorScheme.primary,
                                    modifier = Modifier
                                        .size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(Res.string.section),
                                    fontSize = 16.sp,
                                    color = colorScheme.primary,
                                )
                            }
                            ExtendedFloatingActionButton(
                                onClick = {
                                    addSection(
                                        Section(
                                            id =  (0..1000000).random().toString(),
                                            section = GrocerySection.Custom("New Section"),
                                            items = listOf(
                                                SectionItem(
                                                    name = "New Item",
                                                    isChecked = false
                                                )
                                            )
                                        )
                                    )
                                    showItems = false
                                },
                                containerColor = colorScheme.background,
                                elevation = FloatingActionButtonDefaults.elevation(
                                    defaultElevation = 4.dp,
                                    pressedElevation = 8.dp
                                ),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = stringResource(Res.string.cd_add_item),
                                    tint = colorScheme.primary,
                                    modifier = Modifier
                                        .size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(Res.string.item),
                                    fontSize = 16.sp,
                                    color = colorScheme.primary,
                                )
                            }
                        }
                    }
                    FloatingActionButton(
                        onClick = { showItems = !showItems },
                        containerColor = colorScheme.tertiary,
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(Res.string.add_close_icon),
                            tint = colorScheme.primary,
                            modifier = Modifier
                                .size(32.dp)
                                .padding(horizontal = 4.dp)
                                .rotate(
                                    if (showItems) 45f else 0f
                                )
                        )
                    }
                }
            },
        ) { contentPadding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(sections) { section ->
                    ItemSection(
                        removeSection = removeSection,
                        onSectionUpdate = updateSection,
                        section = section,
                        onSwipe = {
                            swipeInCooldown = true
                            coroutineScope.launch {
                                delay(500) // 500ms cooldown
                                swipeInCooldown = false
                            }
                        },
                        swipeInCooldown = swipeInCooldown
                    )
                }
            }

            if (showSectionSelector) {
                var selectedSection by remember {
                    mutableStateOf<GrocerySection>(GrocerySection.Custom(""))
                }

                AlertDialog(
                    onDismissRequest = { showSectionSelector = false },
                    title = { Text("Add Section") },
                    text = {
                        SectionSelector(
                            onSectionSelected = { newSection ->
                                selectedSection = newSection
                            }
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (selectedSection == GrocerySection.Custom("")) {
                                    return@TextButton
                                }
                                addSection(
                                    Section(
                                        section = selectedSection,
                                        items = emptyList(),
                                        id = (0..1000000).random().toString()
                                    )
                                )
                                showSectionSelector = false
                            }
                        ) {
                            Text("Add")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showSectionSelector = false }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ItemSection(
    removeSection: (Section) -> Unit,
    onSectionUpdate: (Section) -> Unit,
    section: Section,
    onSwipe: () -> Unit = {},
    swipeInCooldown: Boolean = false,
) {
    var showItems by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SwipeableBox(
            removeSection = removeSection,
            colorScheme = colorScheme,
            showItems = showItems,
            onClick = { showItems = !showItems },
            section = section,
            onSectionUpdate = { onSectionUpdate(it) },
            onDeleteSwipe = onSwipe,
            swipeInCooldown = swipeInCooldown,
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
                section.items?.forEachIndexed { index, item ->
                    val color = if (index % 2 == 1) {
                        colorScheme.secondaryContainer
                    } else {
                        colorScheme.background
                    }
                    ItemRow(
                        itemName = item.name,
                        color = color
                    )
                }
            }
        }
    }
}

@Composable
private fun SwipeableBox(
    removeSection: (Section) -> Unit,
    colorScheme: ColorScheme,
    showItems: Boolean,
    onClick: () -> Unit,
    section: Section,
    onSectionUpdate: (Section) -> Unit,
    onDeleteSwipe: () -> Unit = {},
    swipeInCooldown: Boolean = false,
) {
    val density = LocalDensity.current
    var offsetX by remember { mutableFloatStateOf(0f) }
    val showOptionsStartThreshold = with(density) { -80.dp.toPx() }
    val showOptionsThreshold = with(density) { -180.dp.toPx() }
    val deleteThreshold = with(density) { -240.dp.toPx() }
    val fullDeleteThreshold = with(density) { -300.dp.toPx() }

    var showEditDialog by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    var hasTriggeredHaptic by remember { mutableStateOf(false) }
    val showEditOption = offsetX > deleteThreshold

    Box {
        // Background layer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Split background for edit and delete
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Edit background
                AnimatedVisibility(showEditOption) {
                    Box(
                        modifier = Modifier
                            .height(IntrinsicSize.Min)
                            .background(colorScheme.tertiaryContainer)
                            .clickable { showEditDialog = true }
                            .padding(horizontal = 32.dp, vertical = 8.dp),
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
                        .height(IntrinsicSize.Min)
                        .then(deleteWidth)
                        .background(colorScheme.error)
                        .clickable {
                            removeSection(section)
                            onDeleteSwipe()
                        }
                        .padding(horizontal = 32.dp, vertical = 8.dp),
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
                    .fillMaxWidth()
                    .offset { IntOffset(offsetX.roundToInt(), 0) }
                    .background(colorScheme.background)
                    .clickable(
                        interactionSource = null,
                        indication = null
                    ) { onClick() }
                    .draggable(
                        orientation = Orientation.Horizontal,
                        state = rememberDraggableState { delta ->
                            if (swipeInCooldown || delta > 0 && offsetX >= 0f) {
                                return@rememberDraggableState
                            }
                            offsetX += delta

                            // Trigger delete if swiped past fullDeleteThreshold
                            if (offsetX < fullDeleteThreshold) {
                                removeSection(section)
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
                                removeSection(section)
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
                    text = section.section.displayName,
                    fontSize = 16.sp,
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
                            onSectionUpdate(section.copy(section = GrocerySection.Custom(newName)))
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


@Composable
private fun ItemRow(
    itemName: String,
    color: Color,
) {
    var isChecked by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .background(color)
            .clickable(
                interactionSource = null,
                indication = null
            ) { isChecked = !isChecked }
    ) {
        Text(
            text = itemName,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(vertical = 12.dp)
        )
        Checkbox(
            checked = isChecked,
            onCheckedChange = { isChecked = it },
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(vertical = 12.dp)
                .size(16.dp)
        )
    }
}

@Preview
@Composable
fun CheckListScreenPreview() {
    BroccolistTheme {
        CheckListScreen(
            sections = listOf(
                Section(
                    id = "id",
                    section = GrocerySection.Custom("Section 1"),
                    items = listOf(
                        SectionItem(
                            name = "Item 1",
                            isChecked = false
                        ),
                        SectionItem(
                            name = "Item 2",
                            isChecked = true
                        ),
                        SectionItem(
                            name = "Item 3",
                            isChecked = false
                        ),
                    )
                ),
                Section(
                    id = "id",
                    section = GrocerySection.Custom("Section 2"),
                    items = listOf(
                        SectionItem(
                            name = "Item 1",
                            isChecked = false
                        ),
                        SectionItem(
                            name = "Item 2",
                            isChecked = true
                        ),
                        SectionItem(
                            name = "Item 3",
                            isChecked = false
                        ),
                    )
                ),
            ),
            addSection = { },
            removeSection = { },
            updateSection = { },
            signOut = { },
            onLogout = { },
        )
    }
}

@Preview
@Composable
fun CheckListScreenDarkPreview() {
    BroccolistTheme(darkTheme = true) {
        CheckListScreen(
            sections = listOf(
                Section(
                    id = "id",
                    section = GrocerySection.Custom("Section 1"),
                    items = listOf(
                        SectionItem(
                            name = "Item 1",
                            isChecked = false
                        ),
                        SectionItem(
                            name = "Item 2",
                            isChecked = true
                        ),
                        SectionItem(
                            name = "Item 3",
                            isChecked = false
                        ),
                    )
                ),
                Section(
                    id = "id",
                    section = GrocerySection.Custom("Section 2"),
                    items = listOf(
                        SectionItem(
                            name = "Item 1",
                            isChecked = false
                        ),
                        SectionItem(
                            name = "Item 2",
                            isChecked = true
                        ),
                        SectionItem(
                            name = "Item 3",
                            isChecked = false
                        ),
                    )
                ),
            ),
            addSection = { },
            removeSection = { },
            updateSection = { },
            signOut = { },
            onLogout = { },
        )
    }
}
