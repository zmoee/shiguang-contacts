package com.zmoee.shiguang.ui.screens

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.zmoee.shiguang.data.Contact
import com.zmoee.shiguang.data.UserSettings
import com.zmoee.shiguang.data.filteredBy
import com.zmoee.shiguang.data.sortedFor
import com.zmoee.shiguang.ui.components.ContactList
import com.zmoee.shiguang.ui.components.EmptyState
import com.zmoee.shiguang.ui.components.SectionLabel
import com.zmoee.shiguang.ui.components.ScreenTitle
import com.zmoee.shiguang.ui.components.Sym
import com.zmoee.shiguang.ui.components.Symbol
import com.zmoee.shiguang.ui.components.pressScale

@Composable
fun SearchScreen(
    contacts: List<Contact>,
    settings: UserSettings,
    query: String,
    onQueryChange: (String) -> Unit,
    onContactClick: (Contact) -> Unit,
    modifier: Modifier = Modifier,
) {
    val filtered = remember(contacts, query, settings.sortOrder) {
        contacts.filteredBy(query).sortedFor(settings.sortOrder)
    }
    val focusManager = LocalFocusManager.current
    val clearInteraction = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Top + WindowInsetsSides.End + WindowInsetsSides.Bottom,
                ),
            )
            .padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(16.dp))
        ScreenTitle(
            title = "搜索",
            subtitle = "按姓名、关系、电话或照片标签查找",
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            placeholder = {
                Text(
                    text = "搜索联系人",
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            leadingIcon = {
                Symbol(
                    symbol = Sym.Search,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            trailingIcon = if (query.isNotEmpty()) {
                {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier.pressScale(clearInteraction, pressedScale = 0.9f),
                        interactionSource = clearInteraction,
                    ) {
                        Symbol(
                            symbol = Sym.Close,
                            contentDescription = "清空搜索",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                null
            },
            singleLine = true,
            shape = CircleShape,
            textStyle = MaterialTheme.typography.bodyLarge,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )
        Spacer(Modifier.height(20.dp))

        when {
            contacts.isEmpty() -> {
                EmptyState(
                    symbol = Sym.PersonAdd,
                    title = "还没有联系人",
                    body = "创建联系人后，就能在这里按姓名、电话或照片标签快速找到。",
                    modifier = Modifier.weight(1f),
                )
            }
            filtered.isEmpty() -> {
                EmptyState(
                    symbol = Sym.Search,
                    title = "没有找到结果",
                    body = "换一个关键词试试，或检查输入是否有误。",
                    modifier = Modifier.weight(1f),
                )
            }
            else -> {
                SectionLabel(
                    text = if (query.isBlank()) "全部联系人" else "搜索结果",
                    trailing = "${filtered.size} 位",
                )
                Spacer(Modifier.height(8.dp))
                ContactList(
                    contacts = filtered,
                    onContactClick = onContactClick,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
