package com.golfmonitor.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.golfmonitor.data.db.entity.DealEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DealDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(deals: List<DealEntity>)

    @Query("SELECT * FROM deals WHERE priceGbp <= :maxPrice ORDER BY date, time")
    fun getDealsUnderPrice(maxPrice: Double): Flow<List<DealEntity>>

    @Query("SELECT COUNT(*) FROM deals")
    suspend fun count(): Int
}
