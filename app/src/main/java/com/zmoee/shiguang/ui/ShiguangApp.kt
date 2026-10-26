package com.zmoee.shiguang.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.WideNavigationRail
import androidx.compose.material3.WideNavigationRailDefaults
import androidx.compose.material3.WideNavigationRailItem
import androidx.compose.material3.WideNavigationRailItemDefaults
import androidx.compose.material3.WideNavigationRailValue
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.zmoee.shiguang.data.Contact
import com.zmoee.shiguang.ui.components.Sym
import com.zmoee.shiguang.ui.components.Symbol
import com.zmoee.shiguang.ui.components.ShiguangSnackbar
import com.zmoee.shiguang.ui.components.pressScale
import com.zmoee.shiguang.ui.screens.ContactDetailScreen
import com.zmoee.shiguang.ui.screens.ContactEditorSheet
import com.zmoee.shiguang.ui.screens.FavoritesScreen
import com.zmoee.shiguang.ui.screens.HomeScreen
import com.zmoee.shiguang.ui.screens.PhotoCaptureSheet
import com.zmoee.shiguang.ui.screens.SearchScreen
import com.zmoee.shiguang.ui.screens.SettingsScreen
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class RootDestination(
    val route: String,
    val label: String,
    val symbol: Sym,
) {
    HOME("home", "首页", Sym.Home),
    SEARCH("search", "搜索", Sym.Search),
    FAVORITES("favorites", "收藏", Sym.Favorite),
    SETTINGS("settings", "设置", Sym.Settings),
}

private data class SnackbarItem(
    val event: AppEvent.Message,
    val token: Long,
)

@Composable
fun ShiguangApp() {
    val viewModel: AppViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    var showEditor by rememberSaveable { mutableStateOf(false) }
    var editorContactId by rememberSaveable { mutableStateOf<String?>(null) }
    var photoContactId by rememberSaveable { mutableStateOf<String?>(null) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var pendingOpenContactId by rememberSaveable { mutableStateOf<String?>(null) }
    var snackbar by remember { mutableStateOf<SnackbarItem?>(null) }

    fun resolveContact(id: String?): Contact? {
        if (id == null) return null
        return uiState.contacts.firstOrNull { it.id == id }
    }

    fun navigateRoot(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            if (event is AppEvent.Message) {
                snackbar = SnackbarItem(event = event, token = System.nanoTime())
            }
        }
    }

    LaunchedEffect(snackbar?.token) {
        val shown = snackbar ?: return@LaunchedEffect
        delay(5_000)
        if (snackbar?.token == shown.token) {
            snackbar = null
        }
    }

    LaunchedEffect(pendingOpenContactId, uiState.contacts) {
        val id = pendingOpenContactId ?: return@LaunchedEffect
        if (uiState.contacts.any { it.id == id }) {
            navController.navigate("detail/$id") {
                launchSingleTop = true
            }
            pendingOpenContactId = null
        }
    }

    val selectedDestination = when {
        currentRoute == RootDestination.SEARCH.route -> RootDestination.SEARCH
        currentRoute == RootDestination.FAVORITES.route -> RootDestination.FAVORITES
        currentRoute == RootDestination.SETTINGS.route -> RootDestination.SETTINGS
        else -> RootDestination.HOME
    }

    val spatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()
    val effectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            AppNavigationRail(
                selected = selectedDestination,
                onSelect = { destination ->
                    when (destination) {
                        RootDestination.HOME -> navigateRoot(RootDestination.HOME.route)
                        RootDestination.SEARCH -> navigateRoot(RootDestination.SEARCH.route)
                        RootDestination.FAVORITES -> navigateRoot(RootDestination.FAVORITES.route)
                        RootDestination.SETTINGS -> navigateRoot(RootDestination.SETTINGS.route)
                    }
                },
            )

            NavHost(
                navController = navController,
                startDestination = RootDestination.HOME.route,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                enterTransition = {
                    slideInHorizontally(
                        animationSpec = spatialSpec,
                        initialOffsetX = { it },
                    ) + fadeIn(animationSpec = effectsSpec)
                },
                exitTransition = {
                    slideOutHorizontally(
                        animationSpec = spatialSpec,
                        targetOffsetX = { -it / 4 },
                    ) + fadeOut(animationSpec = effectsSpec)
                },
                popEnterTransition = {
                    slideInHorizontally(
                        animationSpec = spatialSpec,
                        initialOffsetX = { -it / 4 },
                    ) + fadeIn(animationSpec = effectsSpec)
                },
                popExitTransition = {
                    slideOutHorizontally(
                        animationSpec = spatialSpec,
                        targetOffsetX = { it },
                    ) + fadeOut(animationSpec = effectsSpec)
                },
            ) {
                composable(RootDestination.HOME.route) {
                    HomeScreen(
                        contacts = uiState.contacts,
                        settings = uiState.settings,
                        onOpenSearch = {
                            searchQuery = ""
                            navigateRoot(RootDestination.SEARCH.route)
                        },
                        onVoiceSearch = { spoken ->
                            searchQuery = spoken
                            navigateRoot(RootDestination.SEARCH.route)
                        },
                        onCreateContact = {
                            editorContactId = null
                            showEditor = true
                        },
                        onContactClick = { contact ->
                            navController.navigate("detail/${contact.id}")
                        },
                        onError = viewModel::reportError,
                    )
                }

                composable(RootDestination.SEARCH.route) {
                    SearchScreen(
                        contacts = uiState.contacts,
                        settings = uiState.settings,
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onContactClick = { contact ->
                            navController.navigate("detail/${contact.id}")
                        },
                    )
                }

                composable(RootDestination.FAVORITES.route) {
                    FavoritesScreen(
                        contacts = uiState.contacts,
                        settings = uiState.settings,
                        onContactClick = { contact ->
                            navController.navigate("detail/${contact.id}")
                        },
                        onBrowseContacts = {
                            navigateRoot(RootDestination.HOME.route)
                        },
                    )
                }

                composable(RootDestination.SETTINGS.route) {
                    SettingsScreen(
                        settings = uiState.settings,
                        contactCount = uiState.contacts.size,
                        onSortOrderChange = viewModel::setSortOrder,
                        onAutoFavoriteChange = viewModel::setAutoFavorite,
                        onClearAll = viewModel::clearAllContacts,
                    )
                }

                composable(
                    route = "detail/{contactId}",
                    arguments = listOf(
                        navArgument("contactId") { type = NavType.StringType },
                    ),
                ) { entry ->
                    val contactId = entry.arguments?.getString("contactId")
                    ContactDetailScreen(
                        contact = resolveContact(contactId),
                        onBack = { navController.popBackStack() },
                        onEdit = { contact ->
                            editorContactId = contact.id
                            showEditor = true
                        },
                        onDelete = { contact ->
                            viewModel.deleteContact(contact)
                            navController.popBackStack()
                        },
                        onToggleFavorite = viewModel::toggleFavorite,
                        onAddPhoto = { contact ->
                            photoContactId = contact.id
                        },
                    )
                }
            }
        }

        AnimatedContent(
            targetState = snackbar,
            transitionSpec = {
                (fadeIn(animationSpec = effectsSpec) + slideInVertically(
                    animationSpec = spatialSpec,
                    initialOffsetY = { it / 2 },
                )).togetherWith(fadeOut(animationSpec = effectsSpec))
            },
            label = "appSnackbar",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        ) { item ->
            if (item != null) {
                ShiguangSnackbar(
                    message = item.event.text,
                    actionLabel = item.event.actionLabel,
                    isError = item.event.isError,
                    onAction = {
                        val action = item.event.undo
                        snackbar = null
                        action?.invoke()
                    },
                )
            }
        }
    }

    if (showEditor) {
        val editingContact = resolveContact(editorContactId)
        if (editorContactId == null || editingContact != null) {
            ContactEditorSheet(
                contact = editingContact,
                onDismiss = {
                    showEditor = false
                    editorContactId = null
                },
                onSave = { draft ->
                    val existing = editingContact
                    showEditor = false
                    editorContactId = null
                    viewModel.saveContact(existing = existing, draft = draft) { saved ->
                        if (existing == null) {
                            pendingOpenContactId = saved.id
                        }
                    }
                },
            )
        }
    }

    val photoContact = resolveContact(photoContactId)
    if (photoContact != null) {
        PhotoCaptureSheet(
            contact = photoContact,
            onDismiss = { photoContactId = null },
            onSave = { filePath, tag ->
                photoContactId = null
                viewModel.savePhoto(photoContact, filePath, tag)
            },
        )
    }
}

@Composable
private fun AppNavigationRail(
    selected: RootDestination,
    onSelect: (RootDestination) -> Unit,
) {
    val railState = rememberWideNavigationRailState()
    val scope = rememberCoroutineScope()
    val railExpanded = railState.targetValue == WideNavigationRailValue.Expanded
    val colors = MaterialTheme.colorScheme
    val selectedTextCollapsed = accessibleColor(
        preferred = colors.secondary,
        background = colors.surfaceContainer,
        fallback = colors.onSurface,
    )
    val selectedTextExpanded = accessibleColor(
        preferred = colors.secondary,
        background = colors.secondaryContainer,
        fallback = colors.onSecondaryContainer,
    )
    val itemColors = WideNavigationRailItemDefaults.colors(
        selectedIconColor = colors.onSecondaryContainer,
        selectedTextColorTopIconPosition = if (railExpanded) {
            selectedTextExpanded
        } else {
            selectedTextCollapsed
        },
        selectedTextColorStartIconPosition = selectedTextExpanded,
        selectedIndicatorColor = colors.secondaryContainer,
        unselectedIconColor = colors.onSurfaceVariant,
        unselectedTextColor = colors.onSurfaceVariant,
    )
    val railColors = WideNavigationRailDefaults.colors(
        containerColor = colors.surfaceContainer,
        contentColor = colors.onSurface,
        modalContainerColor = colors.surfaceContainer,
        modalContentColor = colors.onSurface,
    )
    val menuInteraction = remember { MutableInteractionSource() }

    WideNavigationRail(
        modifier = Modifier
            .fillMaxHeight()
            .widthIn(max = 220.dp),
        state = railState,
        colors = railColors,
        contentPadding = PaddingValues(top = 8.dp, bottom = 12.dp),
        header = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                IconButton(
                    onClick = { scope.launch { railState.toggle() } },
                    modifier = Modifier.pressScale(menuInteraction, pressedScale = 0.9f),
                    interactionSource = menuInteraction,
                ) {
                    Symbol(
                        symbol = Sym.Menu,
                        contentDescription = if (railExpanded) "收起导航栏" else "展开导航栏",
                    )
                }
            }
        },
    ) {
        RootDestination.entries.forEach { destination ->
            val interactionSource = remember(destination) { MutableInteractionSource() }
            WideNavigationRailItem(
                selected = selected == destination,
                onClick = { onSelect(destination) },
                icon = {
                    Symbol(symbol = destination.symbol, size = 24.dp)
                },
                label = {
                    Text(
                        text = destination.label,
                        maxLines = 1,
                    )
                },
                railExpanded = railExpanded,
                modifier = Modifier.pressScale(interactionSource, pressedScale = 0.96f),
                colors = itemColors,
                interactionSource = interactionSource,
            )
        }
    }
}

private fun accessibleColor(preferred: Color, background: Color, fallback: Color): Color {
    return if (contrastRatio(preferred, background) >= 4.5f) preferred else fallback
}

private fun contrastRatio(first: Color, second: Color): Float {
    val firstLuminance = relativeLuminance(first)
    val secondLuminance = relativeLuminance(second)
    val lighter = max(firstLuminance, secondLuminance)
    val darker = min(firstLuminance, secondLuminance)
    return (lighter + 0.05f) / (darker + 0.05f)
}

private fun relativeLuminance(color: Color): Float {
    fun linear(channel: Float): Float =
        if (channel <= 0.03928f) channel / 12.92f else ((channel + 0.055f) / 1.055f).pow(2.4f)

    return 0.2126f * linear(color.red) +
        0.7152f * linear(color.green) +
        0.0722f * linear(color.blue)
}
