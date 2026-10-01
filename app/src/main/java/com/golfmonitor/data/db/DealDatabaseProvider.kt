package com.golfmonitor.data.db

import android.content.Context
import androidx.room.Room

object DealDatabaseProvider {
    @Volatile
    private var instance: DealDatabase? = null

    fun getDatabase(context: Context): DealDatabase =
        instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                DealDatabase::class.java,
                "golf_monitor_db"
            )
                .addMigrations(DealDatabase.MIGRATION_2_3, DealDatabase.MIGRATION_3_4, DealDatabase.MIGRATION_4_5)
                .fallbackToDestructiveMigration()
                .build()
                .also { instance = it }
        }
}
