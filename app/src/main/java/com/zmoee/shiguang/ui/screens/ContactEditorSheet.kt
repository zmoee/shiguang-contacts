package com.zmoee.shiguang.ui.screens

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.zmoee.shiguang.data.Contact
import com.zmoee.shiguang.data.ContactDraft
import com.zmoee.shiguang.ui.components.Sym
import com.zmoee.shiguang.ui.components.Symbol
import com.zmoee.shiguang.ui.components.pressScale
import kotlinx.coroutines.launch

@Composable
fun ContactEditorSheet(
    contact: Contact?,
    onDismiss: () -> Unit,
    onSave: (ContactDraft) -> Unit,
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    )
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    var name by rememberSaveable(contact?.id) { mutableStateOf(contact?.name ?: "") }
    var relation by rememberSaveable(contact?.id) { mutableStateOf(contact?.relation ?: "") }
    var phone by rememberSaveable(contact?.id) { mutableStateOf(contact?.phone ?: "") }
    var note by rememberSaveable(contact?.id) { mutableStateOf(contact?.note ?: "") }
    var showNameError by rememberSaveable { mutableStateOf(false) }
    var showPhoneError by rememberSaveable { mutableStateOf(false) }
    var saving by rememberSaveable { mutableStateOf(false) }
    val phonePattern = remember { Regex("^[0-9+()\\-\\s]{3,30}$") }
    val interactionSource = remember { MutableInteractionSource() }

    fun closeSheet() {
        focusManager.clearFocus()
        scope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) onDismiss()
        }
    }

    fun submit() {
        val trimmedName = name.trim()
        val trimmedPhone = phone.trim()
        showNameError = trimmedName.isEmpty()
        showPhoneError = trimmedPhone.isNotEmpty() && !phonePattern.matches(trimmedPhone)
        if (showNameError || showPhoneError) return
        focusManager.clearFocus()
        saving = true
        scope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                onSave(
                    ContactDraft(
                        name = trimmedName,
                        relation = relation,
                        phone = trimmedPhone,
                        note = note,
                    ),
                )
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    modifier = Modifier.size(width = 32.dp, height = 4.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                ) {}
            }
        },
        contentWindowInsets = { androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (contact == null) "新建联系人" else "编辑资料",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "只保存在这台设备上",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                val closeInteraction = remember { MutableInteractionSource() }
                IconButton(
                    onClick = { closeSheet() },
                    modifier = Modifier.pressScale(closeInteraction, pressedScale = 0.9f),
                    interactionSource = closeInteraction,
                ) {
                    Symbol(
                        symbol = Sym.Close,
                        contentDescription = "关闭",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it.take(40)
                    if (showNameError) showNameError = false
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("姓名") },
                placeholder = { Text("例如：林姨") },
                singleLine = true,
                isError = showNameError,
                supportingText = if (showNameError) {
                    { Text("请输入姓名") }
                } else {
                    null
                },
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next,
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                ),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = relation,
                onValueChange = { relation = it.take(24) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("关系或标签") },
                placeholder = { Text("例如：老同学、邻居") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                ),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = {
                    phone = it.take(30)
                    if (showPhoneError) showPhoneError = false
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("电话") },
                placeholder = { Text("选填") },
                singleLine = true,
                isError = showPhoneError,
                supportingText = if (showPhoneError) {
                    { Text("请输入有效的电话号码") }
                } else {
                    null
                },
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next,
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                ),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it.take(240) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("备注") },
                placeholder = { Text("记录一件想记住的小事") },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Default,
                ),
                supportingText = {
                    Text("${note.length}/240")
                },
            )
            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = { closeSheet() }) {
                    Text("取消")
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { submit() },
                    modifier = Modifier
                        .height(56.dp)
                        .pressScale(interactionSource, pressedScale = 0.97f),
                    enabled = !saving,
                    shape = CircleShape,
                    contentPadding = ButtonDefaults.ContentPadding,
                    interactionSource = interactionSource,
                ) {
                    Symbol(symbol = Sym.Check, size = 18.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(if (saving) "保存中…" else "保存")
                }
            }
        }
    }
}
