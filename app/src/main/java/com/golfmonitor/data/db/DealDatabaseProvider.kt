package com.golfmonitor.data.db

import android.content.Context
import androidx.room.Room

object DealDatabaseProvider {
    fun getDatabase(context: Context): DealDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            DealDatabase::class.java,
            "golf_monitor_db"
        ).build()
    }
}
