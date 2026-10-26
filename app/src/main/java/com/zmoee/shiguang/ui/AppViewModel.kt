package com.zmoee.shiguang.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zmoee.shiguang.data.Contact
import com.zmoee.shiguang.data.ContactDraft
import com.zmoee.shiguang.data.ShiguangRepository
import com.zmoee.shiguang.data.SortOrder
import com.zmoee.shiguang.data.UserSettings
import java.io.File
import java.util.UUID
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ShiguangUiState(
    val contacts: List<Contact> = emptyList(),
    val settings: UserSettings = UserSettings(),
)

sealed interface AppEvent {
    data class Message(
        val text: String,
        val actionLabel: String? = null,
        val undo: (() -> Unit)? = null,
        val isError: Boolean = false,
    ) : AppEvent
}

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ShiguangRepository(application)

    private val _events = MutableSharedFlow<AppEvent>(extraBufferCapacity = 16)
    val events = _events.asSharedFlow()

    val uiState: StateFlow<ShiguangUiState> = combine(
        repository.contacts,
        repository.settings,
    ) { contacts, settings ->
        ShiguangUiState(contacts = contacts, settings = settings)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ShiguangUiState(),
    )

    fun saveContact(
        existing: Contact?,
        draft: ContactDraft,
        onSaved: (Contact) -> Unit,
    ) {
        val now = System.currentTimeMillis()
        val saved = if (existing == null) {
            Contact(
                id = UUID.randomUUID().toString(),
                name = draft.trimmedName,
                relation = draft.trimmedRelation,
                phone = draft.trimmedPhone,
                note = draft.trimmedNote,
                isFavorite = uiState.value.settings.autoFavorite,
                updatedAt = now,
            )
        } else {
            existing.copy(
                name = draft.trimmedName,
                relation = draft.trimmedRelation,
                phone = draft.trimmedPhone,
                note = draft.trimmedNote,
                updatedAt = now,
            )
        }

        viewModelScope.launch {
            runCatching {
                repository.upsert(saved)
            }.onSuccess {
                onSaved(saved)
                _events.emit(
                    AppEvent.Message(
                        text = "已保存",
                        actionLabel = "撤销",
                        undo = {
                            viewModelScope.launch {
                                if (existing == null) {
                                    repository.delete(saved.id)
                                } else {
                                    repository.upsert(existing)
                                }
                            }
                        },
                    ),
                )
            }.onFailure {
                _events.emit(AppEvent.Message("保存失败，请重试", isError = true))
            }
        }
    }

    fun toggleFavorite(contact: Contact) {
        viewModelScope.launch {
            runCatching {
                repository.upsert(
                    contact.copy(
                        isFavorite = !contact.isFavorite,
                        updatedAt = System.currentTimeMillis(),
                    ),
                )
            }.onFailure {
                _events.emit(AppEvent.Message("操作失败，请重试", isError = true))
            }
        }
    }

    fun deleteContact(contact: Contact) {
        viewModelScope.launch {
            runCatching {
                repository.delete(contact.id)
            }.onSuccess {
                _events.emit(
                    AppEvent.Message(
                        text = "已删除",
                        actionLabel = "撤销",
                        undo = {
                            viewModelScope.launch {
                                repository.upsert(contact)
                            }
                        },
                    ),
                )
            }.onFailure {
                _events.emit(AppEvent.Message("删除失败，请重试", isError = true))
            }
        }
    }

    fun savePhoto(contact: Contact, filePath: String, tag: String) {
        val updated = contact.copy(
            photoPath = filePath,
            photoTag = tag.trim(),
            updatedAt = System.currentTimeMillis(),
        )
        viewModelScope.launch {
            runCatching {
                repository.upsert(updated)
            }.onSuccess {
                _events.emit(
                    AppEvent.Message(
                        text = "照片已保存",
                        actionLabel = "撤销",
                        undo = {
                            viewModelScope.launch {
                                repository.upsert(contact)
                                File(filePath).delete()
                            }
                        },
                    ),
                )
            }.onFailure {
                File(filePath).delete()
                _events.emit(AppEvent.Message("照片保存失败，请重试", isError = true))
            }
        }
    }

    fun clearAllContacts() {
        val snapshot = uiState.value.contacts
        viewModelScope.launch {
            runCatching {
                repository.clearContacts()
            }.onSuccess {
                _events.emit(
                    AppEvent.Message(
                        text = "联系人已清空",
                        actionLabel = if (snapshot.isEmpty()) null else "撤销",
                        undo = if (snapshot.isEmpty()) null else {
                            {
                                viewModelScope.launch {
                                    snapshot.forEach { repository.upsert(it) }
                                }
                            }
                        },
                    ),
                )
            }.onFailure {
                _events.emit(AppEvent.Message("清空失败，请重试", isError = true))
            }
        }
    }

    fun setSortOrder(order: SortOrder) {
        viewModelScope.launch {
            runCatching { repository.setSortOrder(order) }
                .onFailure { _events.emit(AppEvent.Message("设置保存失败", isError = true)) }
        }
    }

    fun setAutoFavorite(enabled: Boolean) {
        viewModelScope.launch {
            runCatching { repository.setAutoFavorite(enabled) }
                .onFailure { _events.emit(AppEvent.Message("设置保存失败", isError = true)) }
        }
    }

    fun reportError(message: String) {
        viewModelScope.launch {
            _events.emit(AppEvent.Message(message, isError = true))
        }
    }
}
