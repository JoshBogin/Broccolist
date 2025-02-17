package com.broccolist.checklist.presentation

import BroccolistTheme.BroccolistTheme
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.broccolist.checklist.domain.has
import com.broccolist.checklist.presentation.components.ItemInserter
import com.broccolist.checklist.presentation.components.PlaceholderItemRow
import com.broccolist.checklist.presentation.components.SectionSelector
import io.github.jan.supabase.auth.user.UserSession
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
    userSession: UserSession,
    onLogout: () -> Unit,
) {
    val list by viewModel.checkLists.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getList(userSession)
    }

    CheckListScreen(
        listId = list?.id ?: 0,
        sections = list?.sections ?: emptyList(),
        addSection = viewModel::addSection,
        removeSection = viewModel::removeSection,
        updateSection = viewModel::updateSection,
        addItemToSection = viewModel::addItemToSection,
        removeItemFromSection = viewModel::removeItemFromSection,
        updateItem = viewModel::updateItem,
        signOut = viewModel::signOut,
        onLogout = onLogout
    )
}

@Composable
fun CheckListScreen(
    listId: Int,
    sections: List<Section>,
    addSection: (Section) -> Unit,
    removeSection: (Section) -> Unit,
    updateSection: (Section) -> Unit,
    addItemToSection: (Section, SectionItem) -> Unit,
    removeItemFromSection: (Section, SectionItem) -> Unit,
    updateItem: (SectionItem) -> Unit,
    signOut: () -> Unit,
    onLogout: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val scrollState = rememberLazyListState()
    val drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    var swipeInCooldown by remember { mutableStateOf(false) }
    var currentlySwipedId by remember { mutableStateOf<Int?>(null) }
    var showSectionSelector by remember { mutableStateOf(false) }
    var showItemInserter by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme

    println("CheckListScreen: listId=${listId}, sections=${sections}")

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet(
                drawerShape = RoundedCornerShape(24.dp),
                drawerContainerColor = colorScheme.surfaceContainer,
                modifier = Modifier
                    .safeDrawingPadding()
                    .padding(horizontal = 4.dp)
                    .padding(vertical = 16.dp)
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
                            signOut()
                            onLogout()
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
        modifier = Modifier.background(colorScheme.background)
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
                            focusManager.clearFocus()
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
                        onClick = {
                            focusManager.clearFocus()
                        },
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
                                onClick = {
                                    showSectionSelector = true
                                    showItems = false
                                    focusManager.clearFocus()
                                },
                                containerColor = colorScheme.background,
                                elevation = FloatingActionButtonDefaults.elevation(
                                    defaultElevation = 4.dp,
                                    pressedElevation = 8.dp
                                ),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
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
                                    showItemInserter = true
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
                                    imageVector = Icons.Default.Add,
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
            modifier = Modifier
                .statusBarsPadding()
                .safeDrawingPadding()
                .clickable(
                    onClick = { focusManager.clearFocus() },
                    indication = null,
                    interactionSource = null,
                )
        ) { contentPadding ->
            LazyColumn(
                state = scrollState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(sections) { section ->
                    ItemSection(
                        removeSection = removeSection,
                        onUpdateSection = updateSection,
                        addItemToSection = addItemToSection,
                        removeItemFromSection = removeItemFromSection,
                        updateItem = updateItem,
                        section = section,
                        onSwipe = {
                            swipeInCooldown = true
                            coroutineScope.launch {
                                delay(500) // 500ms cooldown
                                swipeInCooldown = false
                            }
                        },
                        swipeInCooldown = swipeInCooldown,
                        currentlySwipedId = currentlySwipedId,
                        onSwipeStateChange = { id ->
                            currentlySwipedId = id
                        },
                        scrollState = scrollState,
                    )
                }
            }

            if (showSectionSelector) {
                var selectedSection by remember {
                    mutableStateOf<GrocerySection?>(null)
                }

                AlertDialog(
                    onDismissRequest = { showSectionSelector = false },
                    title = { Text("Add Section") },
                    text = {
                        SectionSelector(
                            sections = sections,
                            onSectionSelected = { newSection ->
                                selectedSection = newSection
                            }
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val section = selectedSection ?: return@TextButton
                                addSection(
                                    Section(
                                        section = section,
                                        items = emptyList(),
                                        listId = listId
                                    ),
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

            if (showItemInserter) {
                var item by remember { mutableStateOf<SectionItem?>(null) }
                var selectedSection by remember { mutableStateOf<Section?>(null) }
                var isItemTextError by remember { mutableStateOf(false) }
                var isCustomSectionError by remember { mutableStateOf(false) }

                AlertDialog(
                    onDismissRequest = { showItemInserter = false },
                    title = { Text("Add Item") },
                    text = {
                        ItemInserter(
                            sections = sections,
                            onItemInserted = { newItem ->
                                if (newItem.name.isBlank()) {
                                    isItemTextError = true
                                } else {
                                    isItemTextError = false
                                    item = newItem
                                }
                            },
                            onSectionSelected = { newSection ->
                                selectedSection = if (newSection.section is GrocerySection.Custom &&
                                    newSection.section.displayName.isBlank()
                                ) {
                                    isCustomSectionError = true
                                    null
                                } else if(sections.has(newSection.section)) {
                                    isCustomSectionError = false
                                    sections.first { it.section == newSection.section }
                                } else {
                                    isCustomSectionError = false
                                    newSection
                                }
                            },
                            listId = listId,
                            isItemTextError = isItemTextError,
                            isCustomSectionError = isCustomSectionError,
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val section = selectedSection ?: return@TextButton
                                val newItem = item ?: return@TextButton
                                addItemToSection(
                                    section,
                                    newItem,
                                )
                                showItemInserter = false
                            }
                        ) {
                            Text("Add")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showItemInserter = false }
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
    onUpdateSection: (Section) -> Unit,
    addItemToSection: (Section, SectionItem) -> Unit,
    removeItemFromSection: (Section, SectionItem) -> Unit,
    updateItem: (SectionItem) -> Unit,
    section: Section,
    onSwipe: () -> Unit = {},
    swipeInCooldown: Boolean = false,
    currentlySwipedId: Int? = null,
    onSwipeStateChange: (Int) -> Unit,
    scrollState: LazyListState,
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
    val showEditOption = offsetX > deleteThreshold

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
                        .clickable {
                            removeSection(section)
                            onDeleteSwipe()
                        }
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


@Composable
private fun ItemRow(
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

@Preview
@Composable
fun CheckListScreenPreview() {
    BroccolistTheme {
        CheckListScreen(
            listId = 0,
            sections = listOf(
                Section(
                    id = 0,
                    section = GrocerySection.Custom("Section 1"),
                    items = listOf(
                        SectionItem(
                            id = 0,
                            name = "Item 1",
                            isChecked = false,
                            sectionId = 0,
                        ),
                        SectionItem(
                            id = 1,
                            name = "Item 2",
                            isChecked = true,
                            sectionId = 0,
                        ),
                        SectionItem(
                            id = 2,
                            name = "Item 3",
                            isChecked = false,
                            sectionId = 0,
                        ),
                    ),
                    listId = 0,
                ),
                Section(
                    id = 1,
                    section = GrocerySection.Custom("Section 2"),
                    items = listOf(
                        SectionItem(
                            id = 0,
                            name = "Item 1",
                            isChecked = false,
                            sectionId = 1,
                        ),
                        SectionItem(
                            id = 1,
                            name = "Item 2",
                            isChecked = true,
                            sectionId = 1,
                        ),
                        SectionItem(
                            id = 2,
                            name = "Item 3",
                            isChecked = false,
                            sectionId = 1,
                        ),
                    ),
                    listId = 0,
                ),
            ),
            addSection = { },
            removeSection = { },
            updateSection = { },
            signOut = { },
            onLogout = { },
            addItemToSection = { _, _ -> },
            removeItemFromSection = { _, _ -> },
            updateItem = { },
        )
    }
}

@Preview
@Composable
fun CheckListScreenDarkPreview() {
    BroccolistTheme(darkTheme = true) {
        CheckListScreen(
            listId = 1,
            sections = listOf(
                Section(
                    id = 0,
                    section = GrocerySection.Custom("Section 1"),
                    items = listOf(
                        SectionItem(
                            id = 0,
                            name = "Item 1",
                            isChecked = false,
                            sectionId = 0,
                        ),
                        SectionItem(
                            id = 1,
                            name = "Item 2",
                            isChecked = true,
                            sectionId = 0,
                        ),
                        SectionItem(
                            id = 2,
                            name = "Item 3",
                            isChecked = false,
                            sectionId = 0,
                        ),
                    ),
                    listId = 1,
                ),
                Section(
                    id = 1,
                    section = GrocerySection.Custom("Section 2"),
                    items = listOf(
                        SectionItem(
                            id = 0,
                            name = "Item 1",
                            isChecked = false,
                            sectionId = 1,
                        ),
                        SectionItem(
                            id = 1,
                            name = "Item 2",
                            isChecked = true,
                            sectionId = 1,
                        ),
                        SectionItem(
                            id = 2,
                            name = "Item 3",
                            isChecked = false,
                            sectionId = 1,
                        ),
                    ),
                    listId = 1,
                ),
            ),
            addSection = { },
            removeSection = { },
            updateSection = { },
            signOut = { },
            onLogout = { },
            addItemToSection = { _, _ -> },
            removeItemFromSection = { _, _ -> },
            updateItem = { },
        )
    }
}
