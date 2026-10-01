package com.golfmonitor.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.golfmonitor.offers.Scheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.checkDataStore by preferencesDataStore(name = "check_prefs")

/** Remembers which courses have been checked for a given Sunday, and Paul's schemes. */
object CheckPreferences {
    private val NEAR_ONLY = booleanPreferencesKey("near_only")
    private val SCHEMES = stringSetPreferencesKey("member_schemes")

    private fun checkedKey(sunday: LocalDate) = stringSetPreferencesKey("checked_$sunday")

    fun checkedFlow(context: Context, sunday: LocalDate): Flow<Set<String>> =
        context.checkDataStore.data.map { it[checkedKey(sunday)] ?: emptySet() }

    suspend fun setChecked(context: Context, sunday: LocalDate, courseId: String, checked: Boolean) {
        context.checkDataStore.edit { prefs ->
            val current = prefs[checkedKey(sunday)] ?: emptySet()
            prefs[checkedKey(sunday)] = if (checked) current + courseId else current - courseId
        }
    }

    suspend fun clear(context: Context, sunday: LocalDate) {
        context.checkDataStore.edit { it.remove(checkedKey(sunday)) }
    }

    fun nearOnlyFlow(context: Context): Flow<Boolean> =
        context.checkDataStore.data.map { it[NEAR_ONLY] ?: true }

    suspend fun setNearOnly(context: Context, nearOnly: Boolean) {
        context.checkDataStore.edit { it[NEAR_ONLY] = nearOnly }
    }

    fun schemesFlow(context: Context): Flow<Set<Scheme>> =
        context.checkDataStore.data.map { prefs ->
            (prefs[SCHEMES] ?: emptySet()).mapNotNull { name -> Scheme.values().firstOrNull { it.name == name } }.toSet()
        }

    suspend fun setMember(context: Context, scheme: Scheme, member: Boolean) {
        context.checkDataStore.edit { prefs ->
            val current = prefs[SCHEMES] ?: emptySet()
            prefs[SCHEMES] = if (member) current + scheme.name else current - scheme.name
        }
    }
}
