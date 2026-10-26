package com.zmoee.shiguang.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.shiguangDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "shiguang_contacts_store",
)

class ShiguangRepository(context: Context) {
    private val appContext = context.applicationContext

    val contacts: Flow<List<Contact>> = appContext.shiguangDataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences -> decodeContacts(preferences[CONTACTS_KEY]) }
        .distinctUntilChanged()

    val settings: Flow<UserSettings> = appContext.shiguangDataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { preferences ->
            UserSettings(
                sortOrder = when (preferences[SORT_ORDER_KEY]) {
                    SortOrder.NAME.name -> SortOrder.NAME
                    else -> SortOrder.RECENT
                },
                autoFavorite = preferences[AUTO_FAVORITE_KEY] ?: false,
            )
        }
        .distinctUntilChanged()

    suspend fun upsert(contact: Contact) {
        appContext.shiguangDataStore.edit { preferences ->
            val current = decodeContacts(preferences[CONTACTS_KEY])
            val updated = current.toMutableList()
            val index = updated.indexOfFirst { it.id == contact.id }
            if (index >= 0) {
                updated[index] = contact
            } else {
                updated.add(contact)
            }
            preferences[CONTACTS_KEY] = encodeContacts(updated)
        }
    }

    suspend fun delete(contactId: String): Contact? {
        var deleted: Contact? = null
        appContext.shiguangDataStore.edit { preferences ->
            val current = decodeContacts(preferences[CONTACTS_KEY])
            deleted = current.firstOrNull { it.id == contactId }
            if (deleted != null) {
                preferences[CONTACTS_KEY] = encodeContacts(current.filterNot { it.id == contactId })
            }
        }
        return deleted
    }

    suspend fun clearContacts() {
        appContext.shiguangDataStore.edit { preferences ->
            preferences.remove(CONTACTS_KEY)
        }
    }

    suspend fun setSortOrder(sortOrder: SortOrder) {
        appContext.shiguangDataStore.edit { preferences ->
            preferences[SORT_ORDER_KEY] = sortOrder.name
        }
    }

    suspend fun setAutoFavorite(enabled: Boolean) {
        appContext.shiguangDataStore.edit { preferences ->
            preferences[AUTO_FAVORITE_KEY] = enabled
        }
    }

    private fun decodeContacts(raw: String?): List<Contact> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val json = array.optJSONObject(index) ?: continue
                    val id = json.optString("id")
                    val name = json.optString("name")
                    if (id.isBlank() || name.isBlank()) continue
                    add(
                        Contact(
                            id = id,
                            name = name,
                            relation = json.optString("relation"),
                            phone = json.optString("phone"),
                            note = json.optString("note"),
                            photoPath = json.optString("photoPath").takeIf { it.isNotBlank() },
                            photoTag = json.optString("photoTag"),
                            isFavorite = json.optBoolean("isFavorite", false),
                            updatedAt = json.optLong("updatedAt", 0L),
                        ),
                    )
                }
            }
        }.getOrElse { emptyList() }
    }

    private fun encodeContacts(contacts: List<Contact>): String {
        val array = JSONArray()
        contacts.forEach { contact ->
            array.put(
                JSONObject().apply {
                    put("id", contact.id)
                    put("name", contact.name)
                    put("relation", contact.relation)
                    put("phone", contact.phone)
                    put("note", contact.note)
                    put("photoPath", contact.photoPath ?: "")
                    put("photoTag", contact.photoTag)
                    put("isFavorite", contact.isFavorite)
                    put("updatedAt", contact.updatedAt)
                },
            )
        }
        return array.toString()
    }

    private companion object {
        val CONTACTS_KEY = stringPreferencesKey("contacts_json")
        val SORT_ORDER_KEY = stringPreferencesKey("sort_order")
        val AUTO_FAVORITE_KEY = booleanPreferencesKey("auto_favorite")
    }
}
