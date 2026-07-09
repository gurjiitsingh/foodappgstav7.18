package com.it10x.foodappgstav7_18.ui.kot.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.it10x.foodappgstav7_18.data.pos.entities.PosKotHistoryEntity

import java.text.SimpleDateFormat
import java.util.*

@Composable

fun KotHistoryScreen(
    viewModel: KotHistoryViewModel,
    navController: NavController
) {

    val history by viewModel.history.collectAsState()




    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {

        Text(
            text = "KOT History",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        if (history.isEmpty()) {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No KOT History")
            }

        } else {

            KotHistoryHeader()

            LazyColumn {

                items(
                    history,
                    key = { it.id }
                ) { item ->

                    KotHistoryRow(
                        item = item,
                        onClick = {
                            // TODO Open Batch Detail
                        }
                    )

                }

            }

        }

    }

}

@Composable
private fun KotHistoryHeader() {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF64748B))
            .padding(8.dp)
    ) {

        HeaderCell("Table", .15f)
        HeaderCell("Item", .35f)
        HeaderCell("Qty", .10f)
        HeaderCell("Source", .15f)
        HeaderCell("Status", .15f)
        HeaderCell("Time", .20f)

    }

}

@Composable
private fun RowScope.HeaderCell(
    text: String,
    weight: Float
) {



    Text(
        text = text,
        modifier = Modifier.weight(weight),
        fontWeight = FontWeight.Bold
    )

}

@Composable
private fun KotHistoryRow(
    item: PosKotHistoryEntity,
    onClick: () -> Unit
) {
    val statusColor = when (item.status) {
        "ACTIVE" -> Color(0xFF2E7D32)
        "PAID" -> Color(0xFF1565C0)
        "DELETED" -> Color.Red
        else -> Color.Gray
    }


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {


        Text(
            item.tableNo,
            modifier = Modifier.weight(.15f)
        )

        Text(
            item.name,
            modifier = Modifier.weight(.35f)
        )

        Text(
            item.quantity.toString(),
            modifier = Modifier.weight(.10f)
        )

        Text(
            item.source,
            modifier = Modifier.weight(.15f)
        )

        Text(
            item.status,
            color = statusColor,
            modifier = Modifier.weight(.15f)
        )

        Text(
            formatTime(item.createdAt),
            modifier = Modifier.weight(.20f)
        )

    }

    Divider()

}

private fun formatTime(
    millis: Long
): String {

    return SimpleDateFormat(
        "dd MMM  hh:mm a",
        Locale.getDefault()
    ).format(Date(millis))

}