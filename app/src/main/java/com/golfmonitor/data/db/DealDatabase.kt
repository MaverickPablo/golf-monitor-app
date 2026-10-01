package com.golfmonitor.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.golfmonitor.data.db.entity.CapturedAlertEntity
import com.golfmonitor.data.db.entity.CourseEntity
import com.golfmonitor.data.db.entity.DealEntity
import com.golfmonitor.data.db.dao.CapturedAlertDao
import com.golfmonitor.data.db.dao.CourseDao
import com.golfmonitor.data.db.dao.DealDao

@Database(
    entities = [CourseEntity::class, DealEntity::class, CapturedAlertEntity::class],
    version = 4,
    exportSchema = false
)
abstract class DealDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun dealDao(): DealDao
    abstract fun capturedAlertDao(): CapturedAlertDao

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

        /** v4: raw GolfNow alert log. */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `captured_alerts` (" +
                        "`id` TEXT NOT NULL, `receivedAt` TEXT NOT NULL, `source` TEXT NOT NULL, " +
                        "`title` TEXT, `text` TEXT NOT NULL, `parsedDealId` TEXT, PRIMARY KEY(`id`))"
                )
            }
        }
    }
}
