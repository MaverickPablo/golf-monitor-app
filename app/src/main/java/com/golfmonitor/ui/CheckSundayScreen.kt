package com.golfmonitor.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.golfmonitor.data.db.entity.CourseEntity
import com.golfmonitor.data.preferences.CheckPreferences
import com.golfmonitor.offers.Offer
import com.golfmonitor.offers.OfferCatalog
import com.golfmonitor.offers.Scheme
import com.golfmonitor.planner.CheckSundayPlanner
import com.golfmonitor.repository.DealRepository
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * One-tap list of club visitor-booking pages for the target Sunday, in rotation /
 * drive-time order. Paul opens each page himself; the app never fetches them.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckSundayScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { DealRepository(context) }

    val sunday = remember { CheckSundayPlanner.targetSunday(LocalDateTime.now()) }
    val coursesFlow = remember { repository.getCoursesFlow() }
    val courses by coursesFlow.collectAsState(initial = emptyList())
    val checkedFlow = remember(sunday) { CheckPreferences.checkedFlow(context, sunday) }
    val checked by checkedFlow.collectAsState(initial = emptySet())
    val nearOnlyFlow = remember { CheckPreferences.nearOnlyFlow(context) }
    val nearOnly by nearOnlyFlow.collectAsState(initial = true)
    val schemesFlow = remember { CheckPreferences.schemesFlow(context) }
    val schemes by schemesFlow.collectAsState(initial = emptySet())

    val allOffers = remember {
        OfferCatalog.parse(context.assets.open(OfferCatalog.ASSET).bufferedReader().use { it.readText() })
    }
    val offersByCourse = remember(allOffers, schemes) { OfferCatalog.usableByCourse(allOffers, schemes) }

    val ordered = remember(courses, sunday, nearOnly, offersByCourse) {
        CheckSundayPlanner.orderForChecking(courses, sunday, nearOnly, offersByCourse.keys)
    }
    val checkedCount = ordered.count { it.id in checked }

    fun setChecked(course: CourseEntity, value: Boolean) {
        scope.launch { CheckPreferences.setChecked(context, sunday, course.id, value) }
    }

    fun open(course: CourseEntity) {
        val url = CheckSundayPlanner.checkUrl(course) ?: return
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        setChecked(course, true)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text("Check " + sunday.format(DateTimeFormatter.ofPattern("EEE d MMM", Locale.UK)))
            })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "$checkedCount of ${ordered.size} checked",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = nearOnly,
                    onClick = { scope.launch { CheckPreferences.setNearOnly(context, !nearOnly) } },
                    label = { Text("≤${CheckSundayPlanner.NEAR_DRIVE_MINUTES} min") }
                )
                TextButton(onClick = { scope.launch { CheckPreferences.clear(context, sunday) } }) {
                    Text("Reset")
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("My schemes", style = MaterialTheme.typography.labelLarge)
                Scheme.values().forEach { scheme ->
                    val member = scheme in schemes
                    FilterChip(
                        selected = member,
                        onClick = { scope.launch { CheckPreferences.setMember(context, scheme, !member) } },
                        label = { Text(scheme.label) }
                    )
                }
            }
            Text(
                "Tap Check to open the club's visitor booking page. Not played recently comes first, then scheme offers, then nearest.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                items(ordered, key = { it.id }) { course ->
                    CheckCourseRow(
                        course = course,
                        isChecked = course.id in checked,
                        playedRecently = CheckSundayPlanner.playedRecently(course, sunday),
                        offers = offersByCourse[course.id].orEmpty(),
                        onCheckedChange = { setChecked(course, it) },
                        onOpen = { open(course) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CheckCourseRow(
    course: CourseEntity,
    isChecked: Boolean,
    playedRecently: Boolean,
    offers: List<Offer>,
    onCheckedChange: (Boolean) -> Unit,
    onOpen: () -> Unit
) {
    val hasLink = CheckSundayPlanner.checkUrl(course) != null
    ListItem(
        leadingContent = {
            AsyncImage(
                model = course.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp))
            )
        },
        headlineContent = { Text(course.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = {
            val parts = listOfNotNull(
                course.driveMinutes?.let { "$it min" },
                course.greenFeeBaseline?.let { "usual £%.0f".format(it) },
                course.googleRating?.let { "★%.1f".format(it) },
                course.bookingSystem?.takeIf { it != "unknown/own" } ?: "Club site",
                course.lastPlayed?.let { if (playedRecently) "played $it (recent)" else "played $it" }
            )
            Column {
                Text(parts.joinToString(" • "), maxLines = 2)
                offers.forEach {
                    Text(
                        it.summary(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isChecked, onCheckedChange = onCheckedChange)
                Button(onClick = onOpen, enabled = hasLink) { Text("Check") }
            }
        }
    )
}
