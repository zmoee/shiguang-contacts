package com.zmoee.shiguang.data

import java.text.Collator
import java.util.Locale

/**
 * A locally stored person in the contact journal.
 *
 * [photoPath] is an absolute path inside the app's private files directory. Photos never leave
 * the device unless the user backs the app up through the operating system.
 */
data class Contact(
    val id: String,
    val name: String,
    val relation: String = "",
    val phone: String = "",
    val note: String = "",
    val photoPath: String? = null,
    val photoTag: String = "",
    val isFavorite: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
) {
    val displaySubtitle: String
        get() = listOf(relation, phone).filter { it.isNotBlank() }.joinToString(" · ")

    val searchableText: String
        get() = listOf(name, relation, phone, note, photoTag)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .lowercase()
}

enum class SortOrder(val label: String) {
    RECENT("最近更新"),
    NAME("姓名排序"),
}

data class UserSettings(
    val sortOrder: SortOrder = SortOrder.RECENT,
    val autoFavorite: Boolean = false,
)

data class ContactDraft(
    val name: String = "",
    val relation: String = "",
    val phone: String = "",
    val note: String = "",
) {
    val trimmedName: String get() = name.trim()
    val trimmedRelation: String get() = relation.trim()
    val trimmedPhone: String get() = phone.trim()
    val trimmedNote: String get() = note.trim()
}

fun List<Contact>.filteredBy(query: String): List<Contact> {
    val needle = query.trim().lowercase()
    if (needle.isEmpty()) return this
    return filter { it.searchableText.contains(needle) }
}

fun List<Contact>.sortedFor(order: SortOrder): List<Contact> =
    when (order) {
        SortOrder.RECENT -> sortedByDescending { it.updatedAt }
        SortOrder.NAME -> {
            val collator = Collator.getInstance(Locale.CHINA)
            sortedWith(
                Comparator { first, second ->
                    collator.compare(first.name, second.name)
                },
            )
        }
    }
