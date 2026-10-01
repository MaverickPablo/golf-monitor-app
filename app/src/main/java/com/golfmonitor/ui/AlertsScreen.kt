package com.golfmonitor.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.golfmonitor.alerts.AlertIngestor
import com.golfmonitor.alerts.GolfNowListenerService
import com.golfmonitor.data.db.DealDatabaseProvider
import com.golfmonitor.data.db.entity.CapturedAlertEntity
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val db = remember { DealDatabaseProvider.getDatabase(context) }
    val alertsFlow = remember { db.capturedAlertDao().observeRecent() }
    val alerts by alertsFlow.collectAsState(initial = emptyList())

    // Re-check access whenever we come back from the system settings screen.
    var captureOn by remember { mutableStateOf(GolfNowListenerService.isEnabled(context)) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) captureOn = GolfNowListenerService.isEnabled(context)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var testText by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("GolfNow alerts") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            item {
                Card(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            if (captureOn) "Alert capture is ON" else "Alert capture is OFF",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Reads notifications from the GolfNow app only and turns deals into tee times. " +
                                "Nothing else is read, and nothing leaves this phone.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                        }) {
                            Text(if (captureOn) "Notification access settings" else "Turn on")
                        }
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text("Test an alert", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Paste the text of a GolfNow notification or deal email to check it is read correctly.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = testText,
                        onValueChange = { testText = it },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        minLines = 2
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(enabled = testText.isNotBlank(), onClick = {
                            val text = testText
                            scope.launch {
                                val parsed = AlertIngestor.ingest(context, "manual", null, text, LocalDateTime.now())
                                snackbarHostState.showSnackbar(
                                    parsed?.let {
                                        "Added ${it.courseName}, ${it.date} ${it.time}, £${"%.2f".format(it.priceGbp)}"
                                    } ?: "Couldn't read that one - saved below for review"
                                )
                                if (parsed != null) testText = ""
                            }
                        }) { Text("Read it") }
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = { scope.launch { db.capturedAlertDao().deleteAll() } }) {
                            Text("Clear log")
                        }
                    }
                }
            }

            item {
                Text(
                    "Captured (${alerts.size})",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
                )
                if (alerts.isEmpty()) {
                    Text(
                        "No GolfNow alerts yet. Make sure deal alerts are switched on in the GolfNow app.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            items(alerts, key = { it.id }) { alert -> CapturedAlertRow(alert) }
        }
    }
}

@Composable
private fun CapturedAlertRow(alert: CapturedAlertEntity) {
    ListItem(
        overlineContent = {
            Text(alert.receivedAt.replace('T', ' ').take(16) + " • " + alert.source)
        },
        headlineContent = { Text(alert.title ?: alert.text.lineSequence().first(), maxLines = 1) },
        supportingContent = { Text(alert.text, maxLines = 4) },
        trailingContent = {
            AssistChip(
                onClick = {},
                label = { Text(if (alert.parsedDealId != null) "Read ✓" else "Not read") }
            )
        }
    )
}
