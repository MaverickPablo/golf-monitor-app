package com.golfmonitor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.golfmonitor.data.db.SeedData
import com.golfmonitor.ui.DealListScreen
import com.golfmonitor.work.scheduleWeekendMonitor
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            SeedData.seedIfEmpty(this@MainActivity)
        }
        scheduleWeekendMonitor(this)
        setContent {
            DealListScreen()
        }
    }
}
