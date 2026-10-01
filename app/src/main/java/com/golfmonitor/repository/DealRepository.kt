package com.golfmonitor.repository

import android.content.Context
import com.golfmonitor.data.db.DealDatabaseProvider
import com.golfmonitor.data.db.entity.CourseEntity
import com.golfmonitor.data.db.entity.DealEntity
import com.golfmonitor.data.mapper.toModel
import com.golfmonitor.model.TeeTimeDeal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DealRepository(private val context: Context) {
    private val db = DealDatabaseProvider.getDatabase(context)
    
    fun getDealsFlow(maxPrice: Double): Flow<List<TeeTimeDeal>> {
        return db.dealDao().getDealsUnderPrice(maxPrice).map { entities ->
            entities.map { it.toModel() }
        }
    }
    
    fun getCoursesFlow(): Flow<List<CourseEntity>> = db.courseDao().observeAll()

    suspend fun refreshDeals() {
        // TODO: Trigger worker or fetch logic
    }
}
