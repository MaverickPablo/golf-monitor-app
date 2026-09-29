package com.golfmonitor.ui

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.golfmonitor.config.AppConfig
import com.golfmonitor.data.preferences.FilterPreferences
import com.golfmonitor.model.CourseDetails
import com.golfmonitor.repository.DealRepository
import com.golfmonitor.work.WeekendMonitorWorker
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealListScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isRefreshing by remember { mutableStateOf(false) }
    var maxPrice by remember { mutableStateOf(AppConfig.MAX_GREEN_FEE_GBP.toFloat()) }
    var startTime by remember { mutableStateOf(AppConfig.TIME_WINDOW_START) }
    var endTime by remember { mutableStateOf(AppConfig.TIME_WINDOW_END) }
    var showDiscountsOnly by remember { mutableStateOf(false) }
    var lastSaved by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        FilterPreferences.getMaxPriceFlow(context).collect { maxPrice = it.toFloat() }
    }
    LaunchedEffect(Unit) {
        FilterPreferences.getStartTimeFlow(context).collect { startTime = it }
    }
    LaunchedEffect(Unit) {
        FilterPreferences.getEndTimeFlow(context).collect { endTime = it }
    }
    LaunchedEffect(Unit) {
        FilterPreferences.getShowDiscountsOnlyFlow(context).collect { showDiscountsOnly = it }
    }

    val repository = remember { DealRepository(context) }
    val dealsFlow = remember(maxPrice) { repository.getDealsFlow(maxPrice.toDouble()) }
    val deals by dealsFlow.collectAsState(initial = emptyList())

    val sampleDetails = remember {
        mapOf(
            "c1" to CourseDetails("c1", 72, 132, 4.5, null),
            "c2" to CourseDetails("c2", 71, 138, 4.7, null),
            "c3" to CourseDetails("c3", 70, 128, 4.3, null),
            "c4" to CourseDetails("c4", 72, 135, 4.6, null)
        )
    }

    val filteredDeals = remember(deals, showDiscountsOnly, startTime, endTime) {
        deals.filter { deal ->
            val discountOk = !showDiscountsOnly || (deal.discountPercent != null && deal.discountPercent!! >= 10.0)
            val timeOk = deal.time >= startTime && deal.time <= endTime
            discountOk && timeOk
        }
    }

    fun saveFilters() {
        scope.launch {
            FilterPreferences.saveFilters(context, maxPrice.toDouble(), startTime, endTime, showDiscountsOnly)
            lastSaved = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            snackbarHostState.showSnackbar("Filters saved")
        }
    }

    fun showTimePicker(current: String, onResult: (String) -> Unit) {
        val parts = current.split(":")
        val hour = parts[0].toIntOrNull() ?: 7
        val minute = parts[1].toIntOrNull() ?: 0
        TimePickerDialog(context, { _, h, m -> 
            val snappedMinute = (m / 5) * 5
            onResult("%02d:%02d".format(h, snappedMinute))
        }, hour, minute, true).show()
    }

    fun refreshDeals() {
        scope.launch {
            isRefreshing = true
            val workRequest = OneTimeWorkRequestBuilder<WeekendMonitorWorker>().build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                "weekend_monitor_manual",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
            kotlinx.coroutines.delay(1500)
            isRefreshing = false
            snackbarHostState.showSnackbar("Deals refreshed")
        }
    }

    val pullRefreshState = rememberPullToRefreshState(isRefreshing)

    Scaffold(
        topBar = { TopAppBar(title = { Text("Southeast Thames Tee Monitor") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        PullToRefreshBox(
            state = pullRefreshState,
            onRefresh = { refreshDeals() },
            modifier = Modifier.fillMaxSize()
        ) {
            Column(modifier = Modifier.padding(padding)) {
                Card(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Centre: ${AppConfig.CENTER_POSTCODE}")
                        Text("Max fee: £${String.format("%.0f", maxPrice)}")
                        Text("Radius: ${AppConfig.RADIUS_KM} km")
                        Text("Weekend only")
                        if (lastSaved.isNotEmpty()) {
                            Text("Last saved: $lastSaved", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Row(modifier = Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !showDiscountsOnly,
                        onClick = { showDiscountsOnly = false; saveFilters() },
                        label = { Text("All Deals") }
                    )
                    FilterChip(
                        selected = showDiscountsOnly,
                        onClick = { showDiscountsOnly = true; saveFilters() },
                        label = { Text("Only >10% Off") }
                    )
                    TextButton(
                        onClick = {
                            maxPrice = AppConfig.MAX_GREEN_FEE_GBP.toFloat()
                            startTime = AppConfig.TIME_WINDOW_START
                            endTime = AppConfig.TIME_WINDOW_END
                            showDiscountsOnly = false
                            saveFilters()
                        }
                    ) { Text("Reset") }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text("Max Price: £${"%.0f".format(maxPrice)}", style = MaterialTheme.typography.bodySmall)
                    Slider(
                        value = maxPrice,
                        onValueChange = { maxPrice = it },
                        onValueChangeFinished = { saveFilters() },
                        valueRange = 40f..100f,
                        steps = 11
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Time window:", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { showTimePicker(startTime) { startTime = it; saveFilters() } }) { Text(startTime) }
                    Text("–")
                    TextButton(onClick = { showTimePicker(endTime) { endTime = it; saveFilters() } }) { Text(endTime) }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn {
                    items(filteredDeals) { deal ->
                        DealCard(deal, sampleDetails[deal.courseId])
                    }
                }
            }
        }
    }
}
