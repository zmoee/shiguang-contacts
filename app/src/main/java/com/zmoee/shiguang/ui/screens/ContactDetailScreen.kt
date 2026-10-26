package com.zmoee.shiguang.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.zmoee.shiguang.data.Contact
import com.zmoee.shiguang.ui.components.IconCircle
import com.zmoee.shiguang.ui.components.Sym
import com.zmoee.shiguang.ui.components.Symbol
import com.zmoee.shiguang.ui.components.pressScale
import java.io.File

@Composable
fun ContactDetailScreen(
    contact: Contact?,
    onBack: () -> Unit,
    onEdit: (Contact) -> Unit,
    onDelete: (Contact) -> Unit,
    onToggleFavorite: (Contact) -> Unit,
    onAddPhoto: (Contact) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    if (contact == null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(16.dp),
        ) {
            DetailTopBar(
                isFavorite = false,
                onBack = onBack,
                onToggleFavorite = {},
                onEdit = {},
                onDelete = {},
                hasContact = false,
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = "找不到这位联系人",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "它可能已经被删除。返回列表后再试一次。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(20.dp))
            Button(onClick = onBack, shape = CircleShape) {
                Text("返回")
            }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Top + WindowInsetsSides.End + WindowInsetsSides.Bottom,
                ),
            )
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        DetailTopBar(
            isFavorite = contact.isFavorite,
            onBack = onBack,
            onToggleFavorite = { onToggleFavorite(contact) },
            onEdit = { onEdit(contact) },
            onDelete = { showDeleteDialog = true },
            hasContact = true,
        )
        Spacer(Modifier.height(12.dp))
        HeroPanel(contact = contact)
        Spacer(Modifier.height(16.dp))
        ProfileCard(contact = contact)
        Spacer(Modifier.height(12.dp))
        AdditionalDetails(contact = contact)
        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            val interactionSource = remember { MutableInteractionSource() }
            Button(
                onClick = { onAddPhoto(contact) },
                modifier = Modifier
                    .height(56.dp)
                    .pressScale(interactionSource),
                shape = CircleShape,
                contentPadding = ButtonDefaults.ContentPadding,
                interactionSource = interactionSource,
            ) {
                Symbol(symbol = Sym.Add, size = 18.dp)
                Spacer(Modifier.width(8.dp))
                Text(if (contact.photoPath == null) "添加照片" else "更换照片")
            }
        }
        Spacer(Modifier.height(96.dp))
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Symbol(
                    symbol = Sym.Delete,
                    tint = MaterialTheme.colorScheme.error,
                    size = 28.dp,
                )
            },
            title = { Text("删除这位联系人？") },
            text = {
                Text("“${contact.name}”的资料会从本机移除。删除后仍可在底部提示里撤销。")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete(contact)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            },
        )
    }
}

@Composable
private fun DetailTopBar(
    isFavorite: Boolean,
    hasContact: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuExpanded by rememberSaveable { mutableStateOf(false) }
    val backInteraction = remember { MutableInteractionSource() }
    val favoriteInteraction = remember { MutableInteractionSource() }
    val moreInteraction = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.pressScale(backInteraction, pressedScale = 0.9f),
            interactionSource = backInteraction,
        ) {
            Symbol(
                symbol = Sym.ArrowBack,
                contentDescription = "返回",
            )
        }
        Spacer(Modifier.width(4.dp))
        Text(
            text = "联系人详情",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (hasContact) {
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.pressScale(favoriteInteraction, pressedScale = 0.9f),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = if (isFavorite) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                ),
                interactionSource = favoriteInteraction,
            ) {
                Symbol(
                    symbol = Sym.Favorite,
                    contentDescription = if (isFavorite) "取消收藏" else "收藏",
                )
            }
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.pressScale(moreInteraction, pressedScale = 0.9f),
                    interactionSource = moreInteraction,
                ) {
                    Symbol(
                        symbol = Sym.MoreVert,
                        contentDescription = "更多操作",
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ) {
                    DropdownMenuItem(
                        text = { Text("编辑资料") },
                        onClick = {
                            menuExpanded = false
                            onEdit()
                        },
                        leadingIcon = {
                            Symbol(symbol = Sym.Edit, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "删除联系人",
                                color = MaterialTheme.colorScheme.error,
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        },
                        leadingIcon = {
                            Symbol(
                                symbol = Sym.Delete,
                                tint = MaterialTheme.colorScheme.error,
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroPanel(contact: Contact) {
    val shape = RoundedCornerShape(28.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .offset(x = (-86).dp, y = (-82).dp)
                .size(190.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    shape = CircleShape,
                ),
        )
        Box(
            modifier = Modifier
                .offset(x = 112.dp, y = 88.dp)
                .size(124.dp)
                .background(
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.9f),
                    shape = CircleShape,
                ),
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(128.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .border(
                        border = BorderStroke(4.dp, MaterialTheme.colorScheme.surface),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (contact.photoPath != null) {
                    AsyncImage(
                        model = File(contact.photoPath),
                        contentDescription = "${contact.name}的照片",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Symbol(
                        symbol = Sym.Person,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        size = 58.dp,
                    )
                }
            }
            if (contact.relation.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ) {
                    Text(
                        text = contact.relation,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        if (contact.photoTag.isNotBlank()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Symbol(symbol = Sym.Image, size = 16.dp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = contact.photoTag,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileCard(contact: Contact) {
    val bodyText = when {
        contact.note.isNotBlank() -> contact.note
        contact.displaySubtitle.isNotBlank() -> contact.displaySubtitle
        else -> "还没有备注。点击右上角编辑资料，补上一句想记住的话。"
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(78.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center,
            ) {
                if (contact.photoPath != null) {
                    AsyncImage(
                        model = File(contact.photoPath),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Symbol(
                        symbol = Sym.Image,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        size = 30.dp,
                    )
                }
                if (contact.photoPath != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.scrim.copy(alpha = 0f),
                                        MaterialTheme.colorScheme.scrim.copy(alpha = 0.24f),
                                    ),
                                ),
                            ),
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (contact.isFavorite) {
                    Symbol(
                        symbol = Sym.Star,
                        tint = MaterialTheme.colorScheme.primary,
                        size = 18.dp,
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = bodyText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun AdditionalDetails(contact: Contact) {
    val rows = buildList {
        if (contact.phone.isNotBlank()) add(Triple(Sym.Contacts, "电话", contact.phone))
        if (contact.relation.isNotBlank()) add(Triple(Sym.Person, "关系", contact.relation))
        if (contact.photoTag.isNotBlank()) add(Triple(Sym.Image, "照片标签", contact.photoTag))
    }
    if (rows.isEmpty()) return

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            rows.forEachIndexed { index, row ->
                if (index > 0) {
                    Spacer(Modifier.height(12.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconCircle(
                        symbol = row.first,
                        diameter = 36.dp,
                        iconSize = 19.dp,
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = row.second,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = row.third,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}
