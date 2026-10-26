package com.zmoee.shiguang.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zmoee.shiguang.data.Contact
import com.zmoee.shiguang.data.UserSettings
import com.zmoee.shiguang.data.sortedFor
import com.zmoee.shiguang.ui.components.ContactList
import com.zmoee.shiguang.ui.components.EmptyState
import com.zmoee.shiguang.ui.components.SectionLabel
import com.zmoee.shiguang.ui.components.ScreenTitle
import com.zmoee.shiguang.ui.components.Sym

@Composable
fun FavoritesScreen(
    contacts: List<Contact>,
    settings: UserSettings,
    onContactClick: (Contact) -> Unit,
    onBrowseContacts: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val favorites = remember(contacts, settings.sortOrder) {
        contacts.filter { it.isFavorite }.sortedFor(settings.sortOrder)
    }

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
            title = "收藏",
            subtitle = "把最重要的人留在手边",
        )
        Spacer(Modifier.height(20.dp))

        if (favorites.isEmpty()) {
            EmptyState(
                symbol = Sym.Star,
                title = "还没有收藏",
                body = "打开联系人详情，点一下星标就能收藏。",
                actionLabel = "浏览联系人",
                onAction = onBrowseContacts,
                modifier = Modifier.weight(1f),
            )
        } else {
            SectionLabel(
                text = "已收藏",
                trailing = "${favorites.size} 位",
            )
            Spacer(Modifier.height(8.dp))
            ContactList(
                contacts = favorites,
                onContactClick = onContactClick,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "小提示：收藏的人也会出现在「搜索」结果里。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
    }
}
