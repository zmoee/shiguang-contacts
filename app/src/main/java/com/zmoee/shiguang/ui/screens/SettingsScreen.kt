package com.zmoee.shiguang.ui.screens

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zmoee.shiguang.data.SortOrder
import com.zmoee.shiguang.data.UserSettings
import com.zmoee.shiguang.ui.components.IconCircle
import com.zmoee.shiguang.ui.components.ScreenTitle
import com.zmoee.shiguang.ui.components.SectionLabel
import com.zmoee.shiguang.ui.components.Sym
import com.zmoee.shiguang.ui.components.Symbol
import com.zmoee.shiguang.ui.components.connectedItemShape
import com.zmoee.shiguang.ui.components.pressScale

@Composable
fun SettingsScreen(
    settings: UserSettings,
    contactCount: Int,
    onSortOrderChange: (SortOrder) -> Unit,
    onAutoFavoriteChange: (Boolean) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showClearDialog by rememberSaveable { mutableStateOf(false) }

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
        Spacer(Modifier.height(16.dp))
        ScreenTitle(
            title = "设置",
            subtitle = "让这份小通讯录更合你的习惯",
        )
        Spacer(Modifier.height(24.dp))

        SectionLabel(text = "排序")
        Spacer(Modifier.height(8.dp))
        SortOrder.entries.forEachIndexed { index, order ->
            SettingsRow(
                title = order.label,
                subtitle = when (order) {
                    SortOrder.RECENT -> "最近添加或修改的人排在前面"
                    SortOrder.NAME -> "按姓名顺序排列"
                },
                leading = Sym.Sort,
                shape = connectedItemShape(index, SortOrder.entries.size),
                selected = settings.sortOrder == order,
                onClick = { onSortOrderChange(order) },
                trailing = {
                    RadioButton(
                        selected = settings.sortOrder == order,
                        onClick = null,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary,
                            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                },
            )
        }

        Spacer(Modifier.height(24.dp))
        SectionLabel(text = "偏好")
        Spacer(Modifier.height(8.dp))
        SettingsRow(
            title = "新建时自动收藏",
            subtitle = "把新写下的联系人直接放进收藏",
            leading = Sym.Star,
            shape = RoundedCornerShape(28.dp),
            onClick = { onAutoFavoriteChange(!settings.autoFavorite) },
            trailing = {
                Switch(
                    checked = settings.autoFavorite,
                    onCheckedChange = onAutoFavoriteChange,
                )
            },
        )

        Spacer(Modifier.height(24.dp))
        SectionLabel(text = "数据")
        Spacer(Modifier.height(8.dp))
        SettingsRow(
            title = "所有数据只在本机",
            subtitle = "共有 $contactCount 位联系人。没有账号，也不会上传任何内容。",
            leading = Sym.Info,
            shape = RoundedCornerShape(
                topStart = 28.dp,
                topEnd = 28.dp,
                bottomStart = 8.dp,
                bottomEnd = 8.dp,
            ),
            onClick = null,
        )
        Spacer(Modifier.height(3.dp))
        SettingsRow(
            title = "清空所有联系人",
            subtitle = "同时移除已保存的联系人资料和照片",
            leading = Sym.DeleteForever,
            shape = RoundedCornerShape(
                topStart = 8.dp,
                topEnd = 8.dp,
                bottomStart = 28.dp,
                bottomEnd = 28.dp,
            ),
            onClick = { showClearDialog = true },
            isDestructive = true,
        )

        Spacer(Modifier.height(24.dp))
        SectionLabel(text = "关于")
        Spacer(Modifier.height(8.dp))
        SettingsRow(
            title = "拾光簿",
            subtitle = "Version 1.0.0 · Material 3 Expressive",
            leading = Sym.Contacts,
            shape = RoundedCornerShape(28.dp),
            onClick = null,
        )

        Spacer(Modifier.height(32.dp))
        Text(
            text = "拾光簿把联系人当作一段段值得留下的记忆。所有内容由你掌控。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
        )
        Spacer(Modifier.height(16.dp))
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            icon = {
                Symbol(
                    symbol = Sym.DeleteForever,
                    tint = MaterialTheme.colorScheme.error,
                    size = 28.dp,
                )
            },
            title = { Text("清空所有联系人？") },
            text = {
                Text(
                    "这会删除 $contactCount 位联系人的资料和照片，且无法恢复。操作完成后会提供一次撤销机会。",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDialog = false
                        onClearAll()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text("清空")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("取消")
                }
            },
        )
    }
}

@Composable
private fun SettingsRow(
    title: String,
    subtitle: String? = null,
    leading: Sym,
    shape: RoundedCornerShape,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    isDestructive: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
) {
    val containerColor = when {
        selected -> MaterialTheme.colorScheme.secondaryContainer
        isDestructive -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }
    val contentColor = when {
        selected -> MaterialTheme.colorScheme.onSecondaryContainer
        isDestructive -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
    val interactionSource = remember { MutableInteractionSource() }

    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 68.dp)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconCircle(
                symbol = leading,
                diameter = 40.dp,
                iconSize = 21.dp,
                containerColor = if (isDestructive) {
                    MaterialTheme.colorScheme.errorContainer
                } else if (selected) {
                    MaterialTheme.colorScheme.surfaceContainer
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                },
                contentColor = if (isDestructive) {
                    MaterialTheme.colorScheme.error
                } else if (selected) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                },
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (selected) {
                            MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        } else if (isDestructive) {
                            MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
            if (trailing != null) {
                Spacer(Modifier.width(8.dp))
                trailing()
            }
        }
    }

    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier
                .fillMaxWidth()
                .pressScale(interactionSource, pressedScale = 0.985f),
            shape = shape,
            color = containerColor,
            contentColor = contentColor,
            interactionSource = interactionSource,
            content = content,
        )
    } else {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            color = containerColor,
            contentColor = contentColor,
            content = content,
        )
    }
}
