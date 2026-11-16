package com.broccolist.checklist.presentation

import BroccolistTheme.BroccolistTheme
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import broccolist.composeapp.generated.resources.Res
import broccolist.composeapp.generated.resources.add_close_icon
import broccolist.composeapp.generated.resources.app_name
import broccolist.composeapp.generated.resources.broccolist_icon
import broccolist.composeapp.generated.resources.cd_add_item
import broccolist.composeapp.generated.resources.cd_add_section
import broccolist.composeapp.generated.resources.cd_drawer_menu
import broccolist.composeapp.generated.resources.cd_home_icon
import broccolist.composeapp.generated.resources.home
import broccolist.composeapp.generated.resources.item
import broccolist.composeapp.generated.resources.section
import broccolist.composeapp.generated.resources.sign_out
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import com.broccolist.checklist.presentation.components.ChecklistRow
import com.broccolist.checklist.presentation.components.DragHandle
import com.broccolist.checklist.presentation.components.ItemRow
import com.broccolist.checklist.presentation.components.PlaceholderItemRow
import com.broccolist.checklist.presentation.components.SwipeableBox
import com.broccolist.checklist.presentation.components.buildChecklistRows
import com.broccolist.checklist.presentation.components.resolveItemDrop
import com.broccolist.checklist.presentation.components.resolveSectionDrop
import com.broccolist.checklist.presentation.components.rowKey
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import com.broccolist.app.data.Route
import com.broccolist.checklist.domain.GrocerySection
import com.broccolist.checklist.domain.Section
import com.broccolist.checklist.domain.SectionItem
import com.broccolist.checklist.domain.has
import com.broccolist.checklist.presentation.components.ItemInserter
import com.broccolist.checklist.presentation.components.SectionSelector
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CheckListScreenRoute(
    viewModel: CheckListViewModel = koinViewModel(),
    listId: Int,
    navController: NavController,
    onLogout: () -> Unit,
) {
    val list by viewModel.checkLists.collectAsState()

    LaunchedEffect(listId) {
        // Phase 1 makes the screen list-id aware. Guest mode falls back to
        // the in-memory GuestListStore via the ViewModel; authenticated
        // users hit Supabase.
        viewModel.loadList(listId)
    }

    CheckListScreen(
        navController = navController,
        listId = list?.id ?: 0,
        sections = list?.sections ?: emptyList(),
        addSection = viewModel::addSection,
        removeSection = viewModel::removeSection,
        updateSection = viewModel::updateSection,
        addItemToSection = viewModel::addItemToSection,
        removeItemFromSection = viewModel::removeItemFromSection,
        updateItem = viewModel::updateItem,
        moveItem = viewModel::moveItem,
        moveSection = viewModel::moveSection,
        commitReorder = viewModel::commitReorder,
        signOut = viewModel::signOut,
        onLogout = onLogout
    )
}

@Composable
fun CheckListScreen(
    navController: NavController,
    listId: Int,
    sections: List<Section>,
    addSection: (Section) -> Unit,
    removeSection: (Section) -> Unit,
    updateSection: (Section) -> Unit,
    addItemToSection: (Section, SectionItem) -> Unit,
    removeItemFromSection: (Section, SectionItem) -> Unit,
    updateItem: (SectionItem) -> Unit,
    moveItem: (itemId: Int, toSectionId: Int, toIndex: Int) -> Unit,
    moveSection: (sectionId: Int, toIndex: Int) -> Unit,
    commitReorder: () -> Unit,
    signOut: () -> Unit,
    onLogout: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val lazyListState = rememberLazyListState()
    val haptic = LocalHapticFeedback.current
    val itemHeightPx = with(LocalDensity.current) { 56.dp.toPx() }
    val drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    var swipeInCooldown by remember { mutableStateOf(false) }
    var currentlySwipedId by remember { mutableStateOf<String?>(null) }
    var showSectionSelector by remember { mutableStateOf(false) }
    var showItemInserter by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme

    // Row keys of the sections the user has collapsed.
    var collapsedKeys by remember { mutableStateOf(emptySet<String>()) }
    var draggingItemKey by remember { mutableStateOf<String?>(null) }
    var sectionDragRequested by remember { mutableStateOf(false) }

    // Updated on every move so the reorder callback always reads the rows
    // that are actually on screen.
    var currentRows by remember { mutableStateOf(emptyList<ChecklistRow>()) }
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        if (sectionDragRequested) {
            resolveSectionDrop(currentRows, from.key, to.key)
                ?.let { moveSection(it.sectionId, it.index) }
        } else {
            resolveItemDrop(currentRows, from.key, to.key)
                ?.let { moveItem(it.itemId, it.sectionId, it.index) }
        }
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    // While a section is in the air every section folds down to its header,
    // so the whole list fits on screen for rearranging. The fold waits until
    // the library has picked the header up: it records the row's starting
    // offset at that point, and folding earlier would move the row out from
    // under the finger.
    val collapseAll = sectionDragRequested && reorderableState.isAnyItemDragging
    val rows = remember(sections, collapsedKeys, collapseAll, draggingItemKey) {
        buildChecklistRows(sections, collapsedKeys, collapseAll, draggingItemKey)
    }
    currentRows = rows

    val onSwipe: () -> Unit = {
        swipeInCooldown = true
        coroutineScope.launch {
            delay(500) // 500ms cooldown
            swipeInCooldown = false
        }
    }

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet(
                drawerShape = RoundedCornerShape(24.dp),
                drawerContainerColor = colorScheme.surfaceContainer,
                modifier = Modifier
                    .systemBarsPadding()
                    .padding(4.dp)
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
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        drawerState.close()
                                    }
                                },
                                modifier = Modifier
                                    .padding(16.dp)
                                    .size(32.dp)
                                    .align(Alignment.CenterStart)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Drawer",
                                    tint = colorScheme.primary,
                                    modifier = Modifier
                                        .fillMaxSize()
                                )
                            }
                            Text(
                                text = stringResource(Res.string.app_name),
                                fontSize = 24.sp,
                                color = colorScheme.primary,
                                modifier = Modifier
                                    .padding(16.dp)
                                    .align(Alignment.Center)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = colorScheme.tertiary,
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .clickable(
                                    interactionSource = null,
                                    indication = null,
                                    onClick = {
                                        navController.navigate(Route.Dashboard) {
                                            popUpTo(Route.Dashboard) { inclusive = false }
                                            launchSingleTop = true
                                        }
                                    }
                                )
                        ) {
                            IconButton(
                                onClick = {
                                    navController.navigate(Route.Dashboard) {
                                        // Pop everything above Dashboard so
                                        // we don't stack repeat instances.
                                        popUpTo(Route.Dashboard) { inclusive = false }
                                        launchSingleTop = true
                                    }
                                },
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
                        .background(colorScheme.surface),
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
                        shape = CircleShape
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
                state = lazyListState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = contentPadding.calculateTopPadding(),
                    // Room for the FAB so it never covers the last row.
                    bottom = contentPadding.calculateBottomPadding() + 72.dp,
                ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                items(rows, key = { it.key }, contentType = { it::class }) { row ->
                    when (row) {
                        is ChecklistRow.Header -> ReorderableItem(reorderableState, key = row.key) { isDragging ->
                            val canDrag = row.section.id != null
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(if (isDragging) 8.dp else 0.dp)
                                    .background(colorScheme.background),
                            ) {
                                HorizontalDivider(color = colorScheme.primary, thickness = 1.dp)
                                SwipeableBox(
                                    removeSection = removeSection,
                                    colorScheme = colorScheme,
                                    showItems = row.expanded,
                                    onClick = {
                                        collapsedKeys = if (row.key in collapsedKeys) {
                                            collapsedKeys - row.key
                                        } else {
                                            collapsedKeys + row.key
                                        }
                                    },
                                    section = row.section,
                                    onUpdateSection = updateSection,
                                    onDeleteSwipe = onSwipe,
                                    swipeInCooldown = swipeInCooldown,
                                    isCurrentlyBeingSwiped = currentlySwipedId == row.key,
                                    onSwipeStateChange = { currentlySwipedId = it },
                                    dragHandle = {
                                        DragHandle(
                                            enabled = canDrag,
                                            modifier = Modifier.draggableHandle(
                                                enabled = canDrag,
                                                onDragStarted = {
                                                    focusManager.clearFocus()
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    sectionDragRequested = true
                                                },
                                                onDragStopped = {
                                                    sectionDragRequested = false
                                                    commitReorder()
                                                },
                                            ),
                                        )
                                    },
                                )
                                HorizontalDivider(color = colorScheme.primary, thickness = 1.dp)
                            }
                        }

                        is ChecklistRow.Item -> ReorderableItem(reorderableState, key = row.key) { isDragging ->
                            val canDrag = row.item.id != null
                            ItemRow(
                                item = row.item,
                                color = if (row.indexInSection % 2 == 1) {
                                    colorScheme.secondaryContainer
                                } else {
                                    colorScheme.background
                                },
                                updateItem = updateItem,
                                removeItem = { item -> removeItemFromSection(row.section, item) },
                                onDeleteSwipe = onSwipe,
                                swipeInCooldown = swipeInCooldown,
                                isCurrentlyBeingSwiped = currentlySwipedId == row.key,
                                onSwipeStateChange = { currentlySwipedId = it },
                                modifier = Modifier.shadow(if (isDragging) 8.dp else 0.dp),
                                dragHandle = {
                                    DragHandle(
                                        enabled = canDrag,
                                        modifier = Modifier.draggableHandle(
                                            enabled = canDrag,
                                            onDragStarted = {
                                                focusManager.clearFocus()
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                draggingItemKey = row.key
                                            },
                                            onDragStopped = {
                                                // If the item was dropped into a collapsed
                                                // section, open it so the user can see
                                                // where it went.
                                                currentRows
                                                    .firstOrNull { it.key == draggingItemKey }
                                                    ?.let { it as? ChecklistRow.Item }
                                                    ?.let { collapsedKeys = collapsedKeys - it.section.rowKey }
                                                draggingItemKey = null
                                                commitReorder()
                                            },
                                        ),
                                    )
                                },
                            )
                        }

                        is ChecklistRow.AddItem -> Box(modifier = Modifier.animateItem()) {
                            val focusRequester = remember { FocusRequester() }
                            PlaceholderItemRow(
                                focusRequester = focusRequester,
                                onAddItem = { newItemName ->
                                    addItemToSection(
                                        row.section,
                                        SectionItem(
                                            name = newItemName,
                                            isChecked = false,
                                            sectionId = row.section.id,
                                        ),
                                    )
                                    // Keep the "Add Item" field in view as the
                                    // new row pushes it down.
                                    coroutineScope.launch {
                                        lazyListState.animateScrollBy(itemHeightPx)
                                    }
                                },
                                color = if (row.section.items.orEmpty().size % 2 == 1) {
                                    colorScheme.secondaryContainer
                                } else {
                                    colorScheme.background
                                },
                            )
                        }
                    }
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

@Preview
@Composable
fun CheckListScreenPreview() {
    BroccolistTheme {
        CheckListScreen(
            navController = rememberNavController(),
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
            moveItem = { _, _, _ -> },
            moveSection = { _, _ -> },
            commitReorder = { },
        )
    }
}

@Preview
@Composable
fun CheckListScreenDarkPreview() {
    BroccolistTheme(darkTheme = true) {
        CheckListScreen(
            navController = rememberNavController(),
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
            moveItem = { _, _, _ -> },
            moveSection = { _, _ -> },
            commitReorder = { },
        )
    }
}
