package com.it10x.foodappgstav7_18.ui.dayclosing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
    )
    {

        Text(
            text = "Business Day Closing",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        //==========================================================
        // ROW 1
        //==========================================================

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE3F2FD)
                )
            ) {

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    Text(
                        "Business Information",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Divider()

                    InfoRow("Business Date", ui.businessDate)

                    InfoRow("Opened By", ui.openedBy)

                    InfoRow(
                        "Opened At",
                        formatTime(ui.openedAt)
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE8F5E9)
                )
            ) {

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    Text(
                        "Sales Summary",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Divider()

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

                    Divider()

                    InfoRow(
                        "Orders",
                        ui.totalOrders.toString()
                    )
                }
            }
        }

        //==========================================================
        // ROW 2 - PAYMENT
        //==========================================================

        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFF8E1)
            )
        )
        {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    "Payment Breakdown",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    PaymentCard(
                        "Cash",
                        ui.cashSales,
                        currencyCode,
                        localeTag,
                        Modifier.weight(1f)
                    )

                    PaymentCard(
                        "Card",
                        ui.cardSales,
                        currencyCode,
                        localeTag,
                        Modifier.weight(1f)
                    )

                    PaymentCard(
                        "UPI",
                        ui.upiSales,
                        currencyCode,
                        localeTag,
                        Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    PaymentCard(
                        "Wallet",
                        ui.walletSales,
                        currencyCode,
                        localeTag,
                        Modifier.weight(1f)
                    )

                    PaymentCard(
                        "Credit",
                        ui.creditSales,
                        currencyCode,
                        localeTag,
                        Modifier.weight(1f)
                    )

                    Spacer(
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        //==========================================================
        // ROW 3
        //==========================================================

        Card {

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            )
            {

                Text(
                    text = "Cash Count",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                //====================================
                // Opening / Expected
                //====================================

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    SmallMoneyCard(
                        modifier = Modifier.weight(1f),
                        title = "Opening",
                        amount = ui.openingCash,
                        currencyCode = currencyCode,
                        localeTag = localeTag
                    )

                    SmallMoneyCard(
                        modifier = Modifier.weight(1f),
                        title = "Expected",
                        amount = ui.expectedCash,
                        currencyCode = currencyCode,
                        localeTag = localeTag
                    )
                }

                //====================================
                // Actual Cash
                //====================================

                OutlinedTextField(
                    value = ui.actualCash,
                    onValueChange = viewModel::updateActualCash,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = {
                        Text("Actual Cash Counted")
                    }
                )

                //====================================
                // Notes
                //====================================

                OutlinedTextField(
                    value = ui.notes,
                    onValueChange = viewModel::updateNotes,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp),
                    label = {
                        Text("Notes (Optional)")
                    }
                )

                //====================================
                // Action
                //====================================

                Button(
                    modifier = Modifier
                        .align(Alignment.End)
                        .height(44.dp),
                    onClick = onCloseDay
                ) {

                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null
                    )

                    Spacer(Modifier.width(8.dp))

                    Text("Close Day")
                }
            }
        }
    }
}


@Composable
private fun SmallMoneyCard(
    modifier: Modifier = Modifier,
    title: String,
    amount: Double,
    currencyCode: String,
    localeTag: String
) {

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {

        Column(
            modifier = Modifier.padding(12.dp)
        ) {

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = MoneyFormatter.format(
                    amount,
                    currencyCode,
                    localeTag
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
@Composable
private fun PaymentCard(
    title: String,
    amount: Double,
    currencyCode: String,
    localeTag: String,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {

        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = MoneyFormatter.format(
                    amount,
                    currencyCode,
                    localeTag
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
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