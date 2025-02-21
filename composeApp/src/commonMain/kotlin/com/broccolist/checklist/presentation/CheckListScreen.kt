package com.broccolist.checklist.presentation

import BroccolistTheme.BroccolistTheme
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
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
import com.broccolist.app.data.Route
import com.broccolist.checklist.domain.GrocerySection
import com.broccolist.checklist.domain.Section
import com.broccolist.checklist.domain.SectionItem
import com.broccolist.checklist.domain.has
import com.broccolist.checklist.presentation.components.ItemInserter
import com.broccolist.checklist.presentation.components.ItemSection
import com.broccolist.checklist.presentation.components.SectionSelector
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CheckListScreenRoute(
    viewModel: CheckListViewModel = koinViewModel(),
    userSession: UserSession,
    navController: NavController,
    onLogout: () -> Unit,
) {
    val list by viewModel.checkLists.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getList(userSession)
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
        signOut = viewModel::signOut,
        onLogout = onLogout,
        onMoveItem = viewModel::moveItem,
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
    signOut: () -> Unit,
    onLogout: () -> Unit,
    onMoveItem: (SectionItem, Section, Int) -> Unit = { _, _, _ -> },
) {
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
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
                                        navController.navigate(Route.CheckList)
                                    }
                                )
                        ) {
                            IconButton(
                                onClick = {
                                    navController.navigate(Route.CheckList)
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
                                shape = CircleShape
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
                                shape = CircleShape
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
            var columnHeights by remember { mutableStateOf(List(sections.size) { 0f }) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(
                        state = scrollState,
                    )
                    .padding(contentPadding)
                    .pointerInput(key1 = scrollState) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { offset ->
                                // factor in scroll position to determine which section the drag is in
                                println("Dragging section: $offset")
                                val y = offset.y + scrollState.value
                                val heights = columnHeights
                                val sectionIndex = heights.indexOfFirst { y < it }
                                if (sectionIndex != -1) {
                                    val section = sections[sectionIndex]
                                    currentlySwipedId = section.id ?: 0
                                }
                            },
                            onDragEnd = {},
                            onDragCancel = {},
                            onDrag = { change, dragAmount ->
//                                change.consume()
                            },
                        )
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val lastIndex = sections.size - 1
                sections.forEachIndexed { index, section ->
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
                        isLastSection = index == lastIndex,
                        onMoveItem = onMoveItem,
                        modifier = Modifier
                            .onGloballyPositioned { coordinates ->
                                if (columnHeights.isEmpty()) return@onGloballyPositioned

                                val height = coordinates.size.height.toFloat()
                                columnHeights = columnHeights.toMutableList().also {
                                    it[index] = height
                                }
                            },
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
        )
    }
}
