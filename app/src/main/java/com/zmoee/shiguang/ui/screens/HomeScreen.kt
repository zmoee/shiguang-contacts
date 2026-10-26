package com.zmoee.shiguang.ui.screens

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zmoee.shiguang.data.Contact
import com.zmoee.shiguang.data.SortOrder
import com.zmoee.shiguang.data.UserSettings
import com.zmoee.shiguang.data.sortedFor
import com.zmoee.shiguang.ui.components.ContactList
import com.zmoee.shiguang.ui.components.EmptyState
import com.zmoee.shiguang.ui.components.SectionLabel
import com.zmoee.shiguang.ui.components.ScreenTitle
import com.zmoee.shiguang.ui.components.Sym
import com.zmoee.shiguang.ui.components.Symbol
import com.zmoee.shiguang.ui.components.pressScale
import java.util.Locale

@Composable
fun HomeScreen(
    contacts: List<Contact>,
    settings: UserSettings,
    onOpenSearch: () -> Unit,
    onVoiceSearch: (String) -> Unit,
    onCreateContact: () -> Unit,
    onContactClick: (Contact) -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sortedContacts = contacts.sortedFor(settings.sortOrder)

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
        HomeSearchBar(
            onOpenSearch = onOpenSearch,
            onVoiceSearch = onVoiceSearch,
            onError = onError,
        )
        Spacer(Modifier.height(20.dp))
        ScreenTitle(
            title = "联系人",
            subtitle = "本机共 ${contacts.size} 位 · 只属于你的通讯录",
            trailing = {
                FilledTonalButton(
                    onClick = onCreateContact,
                    modifier = Modifier.height(40.dp),
                    shape = CircleShape,
                    contentPadding = ButtonDefaults.ContentPadding,
                ) {
                    Symbol(symbol = Sym.Add, size = 18.dp)
                    Spacer(Modifier.size(6.dp))
                    Text(text = "新建")
                }
            },
        )
        Spacer(Modifier.height(16.dp))

        if (contacts.isEmpty()) {
            EmptyState(
                symbol = Sym.PersonAdd,
                title = "从第一位联系人开始",
                body = "姓名、关系、电话和照片都可以留在这里。数据只保存在本机。",
                actionLabel = "创建联系人",
                onAction = onCreateContact,
                modifier = Modifier.weight(1f),
            )
        } else {
            SectionLabel(
                text = if (settings.sortOrder == SortOrder.RECENT) "最近更新" else "按姓名",
                trailing = "${contacts.size} 位",
            )
            Spacer(Modifier.height(8.dp))
            ContactList(
                contacts = sortedContacts,
                onContactClick = onContactClick,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun HomeSearchBar(
    onOpenSearch: () -> Unit,
    onVoiceSearch: (String) -> Unit,
    onError: (String) -> Unit,
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val micInteractionSource = remember { MutableInteractionSource() }
    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val resultText = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
                ?.trim()
            if (resultText.isNullOrBlank()) {
                onError("没有听清，请再试一次")
            } else {
                onVoiceSearch(resultText)
            }
        }
    }

    fun startVoiceSearch() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "说出你想找的人")
        }
        try {
            voiceLauncher.launch(intent)
        } catch (_: ActivityNotFoundException) {
            onError("此设备未安装语音识别服务")
        }
    }

    Surface(
        onClick = onOpenSearch,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .pressScale(interactionSource, pressedScale = 0.985f),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        interactionSource = interactionSource,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Symbol(
                symbol = Sym.Search,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.size(12.dp))
            Text(
                text = "搜索",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            IconButton(
                onClick = { startVoiceSearch() },
                modifier = Modifier.pressScale(micInteractionSource, pressedScale = 0.9f),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                interactionSource = micInteractionSource,
            ) {
                Symbol(
                    symbol = Sym.Mic,
                    contentDescription = "语音搜索",
                )
            }
        }
    }
}
