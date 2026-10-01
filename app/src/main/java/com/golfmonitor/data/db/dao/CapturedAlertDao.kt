package com.golfmonitor.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.golfmonitor.data.db.entity.CapturedAlertEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CapturedAlertDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alert: CapturedAlertEntity)

    @Query("SELECT * FROM captured_alerts ORDER BY receivedAt DESC LIMIT 100")
    fun observeRecent(): Flow<List<CapturedAlertEntity>>

    @Query("DELETE FROM captured_alerts")
    suspend fun deleteAll()
}
