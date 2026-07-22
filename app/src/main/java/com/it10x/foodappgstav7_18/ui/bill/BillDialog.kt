package com.it10x.foodappgstav7_18.ui.bill

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.it10x.foodappgstav7_18.ui.payment.PaymentInput
import com.it10x.foodappgstav7_18.ui.components.NumPad
import com.it10x.foodappgstav7_18.utils.MoneyUtils
import java.util.Locale
import com.it10x.foodappgstav7_18.utils.formatter.MoneyFormatter


@Composable
fun BillDialog(
    showBill: Boolean,
    onDismiss: () -> Unit,
    sessionId: String?,
    tableId: String?,
    orderType: String,
    localeTag: String,
    currencyCode: String,
    selectedTableName: String
) {
    if (!showBill || sessionId == null) return

    val context = LocalContext.current

    //--------------- PHONE ---------------if (isProcessing) return@Buttonif (isProcessing) return@Buttonif (isProcessing) return@Button

    var activeInput by remember { mutableStateOf<String?>(null) }
    val discountFlat = remember { mutableStateOf("") }
    val discountPercent = remember { mutableStateOf("") }
    val creditAmount = remember { mutableStateOf("") }
    var showRemainingOptions by remember { mutableStateOf(false) }
    var showDiscount by remember { mutableStateOf(false) }
    var showDelivery by remember { mutableStateOf(false) }
    val deliveryFee = remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }

    val usedPaymentModes = remember { mutableStateListOf<String>() }
    var isCreditSelected by remember { mutableStateOf(false) }
   // val paymentList = remember { mutableStateListOf<PaymentInput>() }   // ✅ ADD THIS LINE


    // NEW PRINT WINDOW
    var isBillPrinted by remember { mutableStateOf(false) }
    var isPrinted by remember { mutableStateOf(false) }



    val billViewModel: BillViewModel = viewModel(

        key = "BillVM_${sessionId}_${orderType}",
        factory = BillViewModelFactory(
            application = (LocalContext.current.applicationContext as? Application)
                ?: throw IllegalStateException("Application not found"),
            tableId = tableId ?: orderType,
            tableName = selectedTableName,
            orderType = orderType
        )
    )

    LaunchedEffect(Unit) {
        billViewModel.toastEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    val uiState = billViewModel.uiState.collectAsState()

    var hasLoadedItems by remember { mutableStateOf(false) }
    var showMoreOptions by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.value.items) {

        if (uiState.value.items.isNotEmpty()) {
            hasLoadedItems = true
        }

        if (hasLoadedItems && uiState.value.items.isEmpty()) {
            onDismiss()   // ✅ CLOSE DIALOG HERE
        }
    }





    val suggestions = billViewModel.customerSuggestions.collectAsState()

    val remainingPaise by billViewModel.remainingPaise.collectAsState()






    LaunchedEffect(showBill) {
        if (showBill) {

            if (uiState.value.discountFlat > 0) {
                discountFlat.value = uiState.value.discountFlat.toString()
                discountPercent.value = ""
                showDiscount = true
            }
            else if (uiState.value.discountPercent > 0) {
                discountPercent.value = uiState.value.discountPercent.toString()
                discountFlat.value = ""
                showDiscount = true
            }
            else {
                discountFlat.value = ""
                discountPercent.value = ""
            }
            // ✅ ADD THIS BLOCK
            if (uiState.value.deliveryFee > 0) {
                deliveryFee.value = uiState.value.deliveryFee.toString()
                showDelivery = true
            } else {
                deliveryFee.value = ""
                showDelivery = false
            }
        }
    }


    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    )
    {

        Surface(
            modifier = Modifier
                .fillMaxWidth(1f)
                .fillMaxHeight(1f)
                .padding(8.dp),
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 8.dp
        )
        {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // ========= LEFT COLUMN (Bill List + Totals) =========
                Column(
                    modifier = Modifier
                        .weight(2.2f)
                        .padding(8.dp)
                        .fillMaxHeight()
                ) {


                    Text(
                        "Final Bill ${ selectedTableName}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )


                    Divider(thickness = 1.dp, color = Color.Gray.copy(alpha = 0.4f))
                    BillScreen(
                        viewModel = billViewModel,
                        onPayClick = { paymentType ->

                            val totalAmount = billViewModel.totalPaise


                            billViewModel.payBill(
                                payments = listOf(
                                    PaymentInput(
                                        mode = paymentType.name,
                                        amount = totalAmount
                                    )
                                ),
                                name = "Customer",
                                phone = uiState.value.customerPhone
                            )

                           // onDismiss()
                        },
                        currencyCode = currencyCode,
                        localeTag = localeTag,
                    )


                }

                // ========= RIGHT COLUMN (Discount + Payment Buttons) =========
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp, horizontal = 6.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                )
                {

                    // ---------------- DISCOUNT SECTION ----------------

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
//                        Text(
//                            "Actions",
//                            style = MaterialTheme.typography.titleSmall,
//                            color = Color.White
//                        )

                        if (isPrinted) {

                            Button(
                                onClick = {
                                    isPrinted = false
                                },
                                modifier = Modifier
                                    .height(28.dp)
                                    .width(90.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1976D2),
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(vertical = 0.dp)
                            ) {
                                Text("Print Again", fontSize = 12.sp)
                            }

                        } else {

                            Button(
                                onClick = {
                                    isPrinted = true
                                },
                                modifier = Modifier
                                    .height(28.dp)
                                    .width(90.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2E7D32),
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(vertical = 0.dp)
                            ) {
                                Text("No Print", fontSize = 12.sp)
                            }
                        }

                        // ✅ Compact Close button (top-right)
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .height(28.dp)
                                .width(70.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFB71C1C), // POS red
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(vertical = 0.dp)
                        ) {
                            Text("Close", fontSize = 12.sp)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { activeInput = "PHONE" }
                    ) {
                        OutlinedTextField(
                            value = uiState.value.customerPhone,
                            onValueChange = {},
                            label = { Text("Customer Phone") },
                            enabled = false,
                            readOnly = true,
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledContainerColor =
                                    if (activeInput == "PHONE") Color(0xFF1E2A22)  // darker green tone
                                    else Color(0xFF2A2A2A),

                                disabledBorderColor =
                                    if (activeInput == "PHONE") Color(0xFF4CAF50)
                                    else Color.Gray,

                                disabledTextColor = Color.White,
                                disabledLabelColor = Color.LightGray
                            ),
                            textStyle = LocalTextStyle.current.copy(fontSize = 15.sp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)   // 👈 keeps safe height
                        )
                    }
                    if (suggestions.value.isNotEmpty() && activeInput == "PHONE") {

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A))
                        ) {
                            Column {
                                suggestions.value.forEach { customer ->

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                billViewModel.setCustomerPhone(customer.phone)
                                                billViewModel.clearCustomerSuggestions()
                                                activeInput = null
                                            }
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = "${customer.phone}  (${customer.name})",
                                            color = Color.White,
                                            fontSize = 14.sp
                                        )
                                    }

                                    Divider(color = Color.DarkGray)
                                }
                            }
                        }
                    }


// DISCOUNT TOGGLE

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),   // 🔹 reduced top & bottom spacing
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        Text(
                            text = "Enable Discount",
                            fontSize = 14.sp,          // 🔹 smaller title
                            color = Color.LightGray,
                            fontWeight = FontWeight.Medium
                        )

                        Switch(
                            checked = showDiscount,
                            onCheckedChange = { showDiscount = it },
                            colors = SwitchDefaults.colors(

                                // 🔹 Enabled (ON) - Dark Orange Thumb
                                checkedThumbColor = Color(0xFFE65100),      // dark orange
                                checkedTrackColor = Color(0xFFFFCCBC),      // pastel orange track

                                // 🔹 Disabled (OFF) - Soft Pastel Gray
                                uncheckedThumbColor = Color(0xFFBDBDBD),
                                uncheckedTrackColor = Color(0xFFE0E0E0),

                                // optional: subtle border
                                uncheckedBorderColor = Color(0xFFBDBDBD)
                            )
                        )
                    }





                    Log.d("ISPIRINTED", "is print done = ${isPrinted}")

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
//                        Text("Discount", style = MaterialTheme.typography.titleSmall)
                        // -------- FLAT --------
                        if (showDiscount) {


                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        activeInput = "FLAT"
                                        discountPercent.value = ""
                                        billViewModel.setPercentDiscount(0.0)
                                    }
                            ) {
                                OutlinedTextField(
                                    value = discountFlat.value,
                                    onValueChange = {},
                                    label = { Text("Flat") },
                                    readOnly = true,
                                    enabled = isPrinted,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledContainerColor =
                                            if (activeInput == "FLAT") Color(0xFF1E2A22)
                                            else Color(0xFF2A2A2A),

                                        disabledBorderColor =
                                            if (activeInput == "FLAT") Color(0xFF4CAF50)
                                            else Color.Gray,

                                        disabledTextColor = Color.White,
                                        disabledLabelColor = Color.LightGray
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // -------- PERCENT --------
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        activeInput = "PERCENT"
                                        discountFlat.value = ""
                                        billViewModel.setFlatDiscount(0.0)
                                    }
                            ) {
                                OutlinedTextField(
                                    value = discountPercent.value,
                                    onValueChange = {},
                                    label = { Text("%") },
                                    readOnly = true,
                                   enabled = isPrinted,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledContainerColor =
                                            if (activeInput == "PERCENT") Color(0xFF1E2A22)
                                            else Color(0xFF2A2A2A),

                                        disabledBorderColor =
                                            if (activeInput == "PERCENT") Color(0xFF4CAF50)
                                            else Color.Gray,

                                        disabledTextColor = Color.White,
                                        disabledLabelColor = Color.LightGray
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            TextButton(
                                onClick = {
                                    if (!isPrinted) {   // ✅ prevent action after print
                                        discountFlat.value = ""
                                        discountPercent.value = ""
                                        billViewModel.setFlatDiscount(0.0)
                                        billViewModel.setPercentDiscount(0.0)
                                        activeInput = null
                                    }
                                },
                                enabled = !isPrinted   // ✅ disable button UI also
                            ) {
                                Text("❌")
                            }
                        }

                    }

// ---------------- DELIVERY TOGGLE ----------------

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        Text(
                            text = "Enable Delivery Charges",
                            fontSize = 14.sp,
                            color = Color.LightGray,
                            fontWeight = FontWeight.Medium
                        )

                        Switch(
                            checked = showDelivery,
                            onCheckedChange = {
                                showDelivery = it
                            },
                        )
                    }


// ---------------- DELIVERY INPUT ----------------

                    if (showDelivery) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        activeInput = "DELIVERY"
                                    }
                            ) {
                                OutlinedTextField(
                                    value = deliveryFee.value,
                                    onValueChange = {},
                                    label = { Text("Delivery") },
                                    readOnly = true,
                                    enabled = isPrinted,
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledContainerColor =
                                            if (activeInput == "DELIVERY") Color(0xFF1E2A22)
                                            else Color(0xFF2A2A2A),

                                        disabledBorderColor =
                                            if (activeInput == "DELIVERY") Color(0xFF0288D1)
                                            else Color.Gray,

                                        disabledTextColor = Color.White,
                                        disabledLabelColor = Color.LightGray
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            TextButton(
                                onClick = {
                                    if (!isPrinted) {
                                        deliveryFee.value = ""
                                        activeInput = null
                                        billViewModel.setDeliveryFee(0.0)
                                    }
                                },
                                enabled = !isPrinted   // ✅ IMPORTANT
                            ) {
                                Text("❌")
                            }
                        }
                    }






                    // ---------- PAYMENT BUTTONS (Compact, Pastel Colors) ----------
//                    Text("Select Payment", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(4.dp))



                    val showPrintButton = !isPrinted

                    if (showPrintButton) {

                        PrintButton(
                            onPrint = {

                                billViewModel.printCurrentBill()

                                // ✅ THIS WILL NOW TRIGGER RECOMPOSITION
                                isPrinted = true
                            }
                        )

                    } else {
                        PaymentButtonsSection(
                            remainingPaise = remainingPaise,

                            onPay = { mode, amount ->
                                val finalPayments = listOf(
                                    PaymentInput(
                                        mode = mode,
                                        amount = amount
                                    )
                                )

                                billViewModel.payBill(
                                    payments = finalPayments,
                                    name = "Customer",
                                    phone = uiState.value.customerPhone
                                )
                            },

                            onMoreClick = {
                                showMoreOptions = true
                            },

                            // ✅ NEW PARAMS
                            currencyCode = currencyCode,
                            localeTag = localeTag,

                            isCreditSelected = isCreditSelected,
                            showRemainingOptions = showRemainingOptions,

                            onCreditClick = {
                                billViewModel.clearCredit()

                                val paise = remainingPaise
                                val rupees = paise / 100
                                val paisaPart = paise % 100

                                creditAmount.value = if (paisaPart == 0L) {
                                    rupees.toString()
                                } else {
                                    "$rupees.${paisaPart.toString().padStart(2, '0')}"
                                }

                                activeInput = "CREDIT"
                                isCreditSelected = true
                                showRemainingOptions = false
                            },

                            onPayLaterClick = {
                                val phone = uiState.value.customerPhone.trim()

                                if (phone.length != 10) {
                                    Toast.makeText(context, "Enter valid 10 digit phone number", Toast.LENGTH_SHORT).show()
                                    return@PaymentButtonsSection
                                }

                                billViewModel.payBill(
                                    payments = listOf(
                                        PaymentInput("DELIVERY_PENDING", remainingPaise)
                                    ),
                                    name = "Customer",
                                    phone = phone
                                )
                            },

                            // ✅ CREDIT UI PASSED HERE
                            creditContent = {

                                Column {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {

                                        OutlinedTextField(
                                            value = creditAmount.value,
                                            onValueChange = {},
                                            label = { Text("Credit") },
                                            readOnly = true,
                                            modifier = Modifier.weight(1f)
                                        )

                                        // ❌ Cancel
                                        IconButton(onClick = {
                                            creditAmount.value = ""
                                            activeInput = null
                                            isCreditSelected = false
                                        }) {
                                            Text("✕")
                                        }

                                        // ✔ Confirm
                                        IconButton(onClick = {

                                            val input = creditAmount.value.trim()
                                            val parts = input.split(".")

                                            val rupees = parts.getOrNull(0)?.toLongOrNull() ?: 0L
                                            val paise = parts.getOrNull(1)?.padEnd(2, '0')?.take(2)?.toLongOrNull() ?: 0L
                                            val enteredPaise = rupees * 100 + paise

                                            if (enteredPaise <= 0 || enteredPaise > remainingPaise) return@IconButton

                                            billViewModel.setCreditAmountRaw(input)

                                            val totalPaise = billViewModel.totalPaise

                                            if (enteredPaise == totalPaise) {
                                                billViewModel.payBill(
                                                    payments = emptyList(),
                                                    name = "Customer",
                                                    phone = uiState.value.customerPhone
                                                )
                                            } else {
                                                showRemainingOptions = true
                                            }

                                            creditAmount.value = ""
                                            activeInput = null
                                            isCreditSelected = false

                                        }) {
                                            Text("✔")
                                        }
                                    }
                                }
                            }
                        )

                }

// ===============================
// GLOBAL NUMPAD (Single Keyboard)
// ===============================



                        Spacer(modifier = Modifier.height(12.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(8.dp))

                    NumPad { label ->
                        handleInput(
                            label = label,
                            activeInput = activeInput,
                            uiState = uiState.value,
                            discountFlat = discountFlat,
                            discountPercent = discountPercent,
                            creditAmount = creditAmount,
                            deliveryFee = deliveryFee,
                            billViewModel = billViewModel,
                            isPrinted = isPrinted
                        )
                    }











                }

            }
        }
    }


    if (showMoreOptions) {

        Dialog(
            onDismissRequest = {
                showMoreOptions = false
            }
        ) {

            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.88f),
                shape = RoundedCornerShape(28.dp),
                tonalElevation = 12.dp,
                color = Color.White
            ) {

                Column(
                    modifier = Modifier
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {


                    Text(
                        text = "🎁",
                        fontSize = 42.sp
                    )


                    Text(
                        text = "Complimentary Order",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )


                    Text(
                        text = "Select reason for free order",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )


                    Spacer(
                        modifier = Modifier.height(8.dp)
                    )


                    // DRIVER
                    Button(
                        onClick = {

                            billViewModel.complimentaryOrder(
                                payments = listOf(
                                    PaymentInput(
                                        mode = "FREE",
                                        amount = 0
                                    )
                                ),
                                name = "Driver",
                                phone = uiState.value.customerPhone,
                                reason = "DRIVER"
                            )

                            showMoreOptions = false
                            onDismiss()
                        },

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),

                        shape = RoundedCornerShape(14.dp),

                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1976D2)
                        )

                    ) {
                        Text(
                            "🚚  Driver",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }



                    // FAMILY
                    Button(
                        onClick = {

                            billViewModel.complimentaryOrder(
                                payments = listOf(
                                    PaymentInput(
                                        mode = "FREE",
                                        amount = 0
                                    )
                                ),
                                name = "Family",
                                phone = uiState.value.customerPhone,
                                reason = "FAMILY"
                            )

                            showMoreOptions = false
                            onDismiss()
                        },

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),

                        shape = RoundedCornerShape(14.dp),

                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF9C27B0)
                        )

                    ) {

                        Text(
                            "👨‍👩‍👧  Family",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                    }



                    // FRIEND
                    Button(
                        onClick = {

                            billViewModel.complimentaryOrder(
                                payments = listOf(
                                    PaymentInput(
                                        mode = "FREE",
                                        amount = 0
                                    )
                                ),
                                name = "Friend",
                                phone = uiState.value.customerPhone,
                                reason = "FRIEND"
                            )

                            showMoreOptions = false
                            onDismiss()
                        },

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),

                        shape = RoundedCornerShape(14.dp),

                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )

                    ) {

                        Text(
                            "🤝  Friend",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                    }



                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )


                    // CANCEL
                    OutlinedButton(
                        onClick = {
                            showMoreOptions = false
                        },

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),

                        shape = RoundedCornerShape(14.dp)

                    ) {

                        Text(
                            "Cancel",
                            color = Color.Gray,
                            fontSize = 15.sp
                        )

                    }

                }
            }
        }
    }


}//end of main funciton



fun handleInput(
    label: String,
    activeInput: String?,
    uiState: BillUiState,
    discountFlat: MutableState<String>,
    discountPercent: MutableState<String>,
    creditAmount: MutableState<String>,
    deliveryFee: MutableState<String>,
    billViewModel: BillViewModel,
    isPrinted: Boolean   // ✅ NEW PARAM
) {

    // 🛑 GLOBAL LOCK (after print)
    if (isPrinted) {
        // ✅ Allow ONLY phone editing (optional)
        if (activeInput != "PHONE") return
    }

    when (activeInput) {

        // =========================
        // 📱 PHONE INPUT
        // =========================
        "PHONE" -> {
            when (label) {

                "←" -> {
                    if (uiState.customerPhone.isNotEmpty()) {

                        val newPhone = uiState.customerPhone.dropLast(1)
                        billViewModel.setCustomerPhone(newPhone)

                        if (newPhone.length in 3..9) {
                            billViewModel.observeCustomerSuggestions(newPhone)
                        } else {
                            billViewModel.clearCustomerSuggestions()
                        }
                    }
                }

                "." -> {
                    // ignore dot
                }

                else -> {
                    if (uiState.customerPhone.length < 10) {

                        val newPhone = uiState.customerPhone + label
                        billViewModel.setCustomerPhone(newPhone)

                        if (newPhone.length in 3..9) {
                            billViewModel.observeCustomerSuggestions(newPhone)
                        } else {
                            billViewModel.clearCustomerSuggestions()
                        }
                    }
                }
            }
        }

        // =========================
        // 💰 FLAT DISCOUNT
        // =========================
        "FLAT" -> {
            discountFlat.value = handleNumberInput(discountFlat.value, label)

            billViewModel.setFlatDiscount(
                discountFlat.value.toDoubleOrNull() ?: 0.0
            )
        }

        // =========================
        // 📊 PERCENT DISCOUNT
        // =========================
        "PERCENT" -> {
            discountPercent.value = handleNumberInput(
                current = discountPercent.value,
                label = label,
                maxValue = 100.0
            )

            billViewModel.setPercentDiscount(
                discountPercent.value.toDoubleOrNull() ?: 0.0
            )
        }

        // =========================
        // 💳 CREDIT
        // =========================
        "CREDIT" -> {
            creditAmount.value = handleNumberInput(creditAmount.value, label)
        }

        // =========================
        // 🚚 DELIVERY
        // =========================
        "DELIVERY" -> {
            deliveryFee.value = handleNumberInput(deliveryFee.value, label)

            billViewModel.setDeliveryFee(
                deliveryFee.value.toDoubleOrNull() ?: 0.0
            )
        }
    }
}



fun handleNumberInput(
    current: String,
    label: String,
    allowDecimal: Boolean = true,
    maxDecimals: Int = 2,
    maxValue: Double? = null
): String {

    var value = current

    when (label) {

        "←" -> {
            if (value.isNotEmpty()) {
                value = value.dropLast(1)
            }
        }

        "." -> {
            if (!allowDecimal) return value

            if (!value.contains(".")) {
                value = if (value.isEmpty()) "0." else value + "."
            }
        }

        else -> {
            if (!label.all { it.isDigit() }) return value

            // limit decimals
            if (value.contains(".")) {
                val parts = value.split(".")
                if (parts.size == 2 && parts[1].length >= maxDecimals) {
                    return value
                }
            }

            val newValue = value + label

            // limit max value if needed
            if (maxValue != null) {
                val num = newValue.toDoubleOrNull() ?: return value
                if (num > maxValue) return value
            }

            value = newValue
        }
    }

    return value
}