package com.golfmonitor.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.golfmonitor.data.db.entity.CourseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(courses: List<CourseEntity>)

    @Query("SELECT * FROM courses")
    suspend fun getAll(): List<CourseEntity>

    @Query("SELECT * FROM courses ORDER BY driveMinutes")
    fun observeAll(): Flow<List<CourseEntity>>
}
