package com.example.sanosysalvosv2.data.session

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val TOKEN_KEY = stringPreferencesKey("auth_token")
private val USER_ID_KEY = stringPreferencesKey("auth_user_id")
private val USER_ROLE_KEY = stringPreferencesKey("auth_user_role")

private val Context.sessionDataStore by preferencesDataStore(name = "session_store")

class SessionStore(context: Context) {
    private val dataStore = context.sessionDataStore

    val tokenFlow: Flow<String?> = dataStore.data
        .catch {
            if (it is IOException) emit(emptyPreferences()) else throw it
        }
        .map { preferences: Preferences -> preferences[TOKEN_KEY] }

    val roleFlow: Flow<String?> = dataStore.data
        .catch {
            if (it is IOException) emit(emptyPreferences()) else throw it
        }
        .map { preferences: Preferences -> preferences[USER_ROLE_KEY] }

    suspend fun saveSession(token: String, userId: String, role: String) {
        dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[USER_ID_KEY] = userId
            preferences[USER_ROLE_KEY] = role.uppercase()
        }
    }

    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(USER_ID_KEY)
            preferences.remove(USER_ROLE_KEY)
        }
    }
}
