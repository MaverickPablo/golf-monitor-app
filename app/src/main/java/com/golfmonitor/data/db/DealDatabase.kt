package com.golfmonitor.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.golfmonitor.data.db.entity.CourseEntity
import com.golfmonitor.data.db.entity.DealEntity
import com.golfmonitor.data.db.dao.CourseDao
import com.golfmonitor.data.db.dao.DealDao

@Database(entities = [CourseEntity::class, DealEntity::class], version = 2)
abstract class DealDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun dealDao(): DealDao
}
