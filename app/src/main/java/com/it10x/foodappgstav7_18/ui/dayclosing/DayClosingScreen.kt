package com.it10x.foodappgstav7_18.ui.dayclosing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav7_18.utils.formatter.MoneyFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DayClosingScreen(
    viewModel: DayClosingViewModel,
    currencyCode: String,
    localeTag: String,
    onCloseDay: () -> Unit
) {

    val ui by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            "Business Day Closing",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE3F2FD)
            )
        ) {

            Column(
                Modifier.padding(16.dp)
            ) {

                Text(
                    "Business Information",
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(12.dp))

                InfoRow("Business Date", ui.businessDate)

                InfoRow("Opened By", ui.openedBy)

                InfoRow(
                    "Opened At",
                    formatTime(ui.openedAt)
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8F5E9)
            )
        ) {

            Column(
                Modifier.padding(16.dp)
            ) {

                Text(
                    "Sales Summary",
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(12.dp))

                MoneyRow(
                    "Total Sales",
                    ui.totalSales,
                    currencyCode,
                    localeTag
                )

                MoneyRow(
                    "Discount",
                    ui.totalDiscount,
                    currencyCode,
                    localeTag
                )

                MoneyRow(
                    "Tax",
                    ui.totalTax,
                    currencyCode,
                    localeTag
                )

                MoneyRow(
                    "Refund",
                    ui.totalRefund,
                    currencyCode,
                    localeTag
                )

                MoneyRow(
                    "Complimentary",
                    ui.complimentarySales,
                    currencyCode,
                    localeTag
                )

                Divider(
                    Modifier.padding(vertical = 8.dp)
                )

                InfoRow(
                    "Orders",
                    ui.totalOrders.toString()
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFF8E1)
            )
        ) {

            Column(
                Modifier.padding(16.dp)
            ) {

                Text(
                    "Payment Breakdown",
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(12.dp))

                MoneyRow("Cash", ui.cashSales, currencyCode, localeTag)

                MoneyRow("Card", ui.cardSales, currencyCode, localeTag)

                MoneyRow("UPI", ui.upiSales, currencyCode, localeTag)

                MoneyRow("Wallet", ui.walletSales, currencyCode, localeTag)

                MoneyRow("Credit", ui.creditSales, currencyCode, localeTag)
            }
        }

        Card {

            Column(
                Modifier.padding(16.dp)
            ) {

                Text(
                    "Cash Count",
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(12.dp))

                MoneyRow(
                    "Opening Cash",
                    ui.openingCash,
                    currencyCode,
                    localeTag
                )

                MoneyRow(
                    "Expected Cash",
                    ui.expectedCash,
                    currencyCode,
                    localeTag
                )

                OutlinedTextField(
                    value = ui.actualCash,
                    onValueChange = {
                        viewModel.updateActualCash(it)
                    },
                    label = {
                        Text("Actual Cash")
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = ui.notes,
                    onValueChange = {
                        viewModel.updateNotes(it)
                    },
                    label = {
                        Text("Notes")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onCloseDay
        ) {
            Text("Close Business Day")
        }
    }
}


@Composable
private fun InfoRow(
    label: String,
    value: String
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(label)

        Text(
            value,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun MoneyRow(
    label: String,
    amount: Double,
    currencyCode: String,
    localeTag: String
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(label)

        Text(
            MoneyFormatter.format(
                amount = amount,
                currencyCode = currencyCode,
                localeTag = localeTag
            ),
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun formatTime(time: Long): String {

    if (time == 0L) return "-"

    return SimpleDateFormat(
        "dd MMM yyyy  hh:mm a",
        Locale.getDefault()
    ).format(Date(time))
}