package com.golfmonitor.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.golfmonitor.model.CourseDetails
import com.golfmonitor.model.TeeTimeDeal

@Composable
fun DealCard(
    deal: TeeTimeDeal,
    details: CourseDetails? = null,
    imageUrl: String? = null,
    driveMinutes: Int? = null,
    googleRating: Double? = null
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = deal.courseName,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                deal.discountPercent?.let { discount ->
                    if (discount >= 10.0) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = "%.0f%% OFF".format(discount),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                listOfNotNull(
                    deal.date,
                    deal.time,
                    driveMinutes?.let { "$it min drive" },
                    googleRating?.let { "★%.1f".format(it) }
                )
                    .joinToString(" • ")
            )
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "£${"%.2f".format(deal.priceGbp)}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                deal.baselinePriceGbp?.let { baseline ->
                    if (baseline > deal.priceGbp) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "£${"%.2f".format(baseline)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                if (deal.players > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("• ${deal.players} players", style = MaterialTheme.typography.bodyMedium)
                }
            }

            details?.let {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Par ${it.par} • Slope ${it.slope} • Rating ${it.rating} ★",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            (imageUrl ?: details?.imageUrl)?.let { url ->
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = url,
                    contentDescription = deal.courseName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Via: ${deal.source}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Button(enabled = deal.bookingUrl != null, onClick = {
                    deal.bookingUrl?.let { url ->
                        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))
                        context.startActivity(intent)
                    }
                }) {
                    Text("Book Slot")
                }
            }
        }
    }
}

