package com.golfmonitor.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.golfmonitor.data.db.entity.CourseEntity
import com.golfmonitor.data.db.entity.DealEntity
import com.golfmonitor.data.db.dao.CourseDao
import com.golfmonitor.data.db.dao.DealDao

@Database(entities = [CourseEntity::class, DealEntity::class], version = 3, exportSchema = false)
abstract class DealDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun dealDao(): DealDao

    companion object {
        /** v3: Sunday Caddie course fields; drops the old Surrey demo courses and their deals. */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE courses ADD COLUMN driveMinutes INTEGER")
                db.execSQL("ALTER TABLE courses ADD COLUMN bookingSystem TEXT")
                db.execSQL("ALTER TABLE courses ADD COLUMN bookingUrl TEXT")
                db.execSQL("ALTER TABLE courses ADD COLUMN websiteUrl TEXT")
                db.execSQL("ALTER TABLE courses ADD COLUMN imageUrl TEXT")
                db.execSQL("ALTER TABLE courses ADD COLUMN lastPlayed TEXT")
                db.execSQL("DELETE FROM deals WHERE courseId IN ('c1','c2','c3','c4')")
                db.execSQL("DELETE FROM courses WHERE id IN ('c1','c2','c3','c4')")
            }
        }
    }
}
