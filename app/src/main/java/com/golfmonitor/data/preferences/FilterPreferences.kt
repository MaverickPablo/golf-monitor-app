package com.golfmonitor.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "filter_prefs")

object FilterPreferences {
    private val MAX_PRICE = doublePreferencesKey("max_price")
    private val START_TIME = stringPreferencesKey("start_time")
    private val END_TIME = stringPreferencesKey("end_time")
    private val SHOW_DISCOUNTS_ONLY = booleanPreferencesKey("show_discounts_only")

    suspend fun saveFilters(context: Context, maxPrice: Double, startTime: String, endTime: String, showDiscountsOnly: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[MAX_PRICE] = maxPrice
            prefs[START_TIME] = startTime
            prefs[END_TIME] = endTime
            prefs[SHOW_DISCOUNTS_ONLY] = showDiscountsOnly
        }
    }

    fun getMaxPriceFlow(context: Context): Flow<Double> = context.dataStore.data.map { it[MAX_PRICE] ?: 75.0 }
    fun getStartTimeFlow(context: Context): Flow<String> = context.dataStore.data.map { it[START_TIME] ?: "07:00" }
    fun getEndTimeFlow(context: Context): Flow<String> = context.dataStore.data.map { it[END_TIME] ?: "18:00" }
    fun getShowDiscountsOnlyFlow(context: Context): Flow<Boolean> = context.dataStore.data.map { it[SHOW_DISCOUNTS_ONLY] ?: false }
}
