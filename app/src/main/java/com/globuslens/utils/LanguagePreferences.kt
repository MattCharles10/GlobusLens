package com.globuslens.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "language_prefs")

@Singleton
class LanguagePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TARGET_LANGUAGE_KEY = stringPreferencesKey("target_language")

    val targetLanguageFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[TARGET_LANGUAGE_KEY] ?: Constants.DEFAULT_TARGET_LANG
        }

    suspend fun setTargetLanguage(languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[TARGET_LANGUAGE_KEY] = languageCode
        }
    }
}