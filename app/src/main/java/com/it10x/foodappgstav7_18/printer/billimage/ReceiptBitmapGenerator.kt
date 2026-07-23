package com.it10x.foodappgstav7_18.printer.billimage



import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.it10x.foodappgstav7_18.data.print.OutletInfo
import com.it10x.foodappgstav7_18.printer.PrintOrder

object ReceiptBitmapGenerator {

    //================================================
    // Printer Constants (58mm Printer)
    //================================================

    private const val RECEIPT_WIDTH = 384

    private const val LEFT = 12f
    private const val RIGHT = 372f

    private const val START_Y = 25f

    //================================================
    // Public API
    //================================================

    fun billing48Image(
        context: Context,
        order: PrintOrder,
        outletInfo: OutletInfo
    ): Bitmap {

        // Temporary height (will become dynamic later)
        val bitmap = Bitmap.createBitmap(
            RECEIPT_WIDTH,
            2200,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)

        // White Background
        canvas.drawColor(Color.WHITE)

        // Create Drawer
        val drawer = ReceiptDrawer(canvas)

        //==============================
        // Header
        //==============================

        drawer.drawHeader(
            order,
            outletInfo
        )

        //==============================
        // Order Information
        //==============================

        drawer.drawOrderInfo(order)

        //==============================
        // Item Table Header
        //==============================

        drawer.drawItemsHeader()

        //==============================
        // Part 3
        // Draw Items
        //==============================

        drawer.drawItems(order)

        //==============================
        // Part 4
        // Totals
        //==============================

        // drawer.drawTotals(order)

        //==============================
        // Part 5
        // Footer
        //==============================

        // drawer.drawFooter()

        return bitmap
    }

    //================================================
    // Receipt Drawer
    //================================================

    private class ReceiptDrawer(
        private val canvas: Canvas
    ) {

        var y = START_Y

        //================================================
        // Column Positions
        //================================================

        private val xItem = 12f
        private val xRate = 220f
        private val xQty = 275f
        private val xAmount = 372f

        //================================================
        // Paints
        //================================================

        private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 30f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        }

        private val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
        }

        private val normalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 21f
            typeface = Typeface.DEFAULT
        }

        private val smallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 18f
            textAlign = Paint.Align.CENTER
        }

        private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            strokeWidth = 2f
        }

        //================================================
        // Header
        //================================================

        fun drawHeader(
            order: PrintOrder,
            outletInfo: OutletInfo
        ) {

            drawCenter(
                outletInfo.outletName.uppercase(),
                titlePaint
            )

            if (!outletInfo.addressLine1.isNullOrBlank()) {
                drawCenter(
                    outletInfo.addressLine1,
                    smallPaint
                )
            }

            if (!outletInfo.phone.isNullOrBlank()) {
                drawCenter(
                    "☎ ${outletInfo.phone}",
                    smallPaint
                )
            }

            if (!outletInfo.gstVatNumber.isNullOrBlank()) {
                drawCenter(
                    "GSTIN : ${outletInfo.gstVatNumber}",
                    smallPaint
                )
            }

            drawDivider()

            drawCenter(
                "TAX INVOICE",
                boldPaint
            )

            drawDivider()
        }

        //================================================
        // Order Details
        //================================================

        fun drawOrderInfo(
            order: PrintOrder
        ) {

            drawLeft(
                "Bill : ${order.orderNo}",
                LEFT,
                normalPaint
            )

            drawRight(
                order.dateTime,
                RIGHT,
                normalPaint
            )

            y += 30f

            drawLeft(
                "Table : ${order.tableNo ?: "-"}",
                LEFT,
                normalPaint
            )

            drawRight(
                "Token : ${order.tableNo ?: "-"}",
                RIGHT,
                normalPaint
            )

            y += 35f
        }

        //================================================
        // Item Header
        //================================================

        fun drawItemsHeader() {

            // Top Border

            canvas.drawLine(
                LEFT,
                y,
                RIGHT,
                y,
                linePaint
            )

            y += 28f

            drawLeft(
                "ITEM",
                xItem,
                boldPaint
            )

            drawLeft(
                "RATE",
                xRate,
                boldPaint
            )

            drawLeft(
                "QTY",
                xQty,
                boldPaint
            )

            drawRight(
                "AMOUNT",
                RIGHT,
                boldPaint
            )

            // Vertical Lines

            canvas.drawLine(
                200f,
                y - 20,
                200f,
                y + 8,
                linePaint
            )

            canvas.drawLine(
                260f,
                y - 20,
                260f,
                y + 8,
                linePaint
            )

            canvas.drawLine(
                315f,
                y - 20,
                315f,
                y + 8,
                linePaint
            )

            y += 15f

            canvas.drawLine(
                LEFT,
                y,
                RIGHT,
                y,
                linePaint
            )

            y += 25f
        }

        //================================================
// Draw All Items
//================================================

        fun drawItems(
            order: PrintOrder
        ) {

            order.items.forEach { item ->

                drawSingleItem(
                    itemName = item.name.uppercase(),
                    rate = item.price,
                    qty = item.quantity.toDouble(),
                    amount = item.subtotal
                )

                //--------------- Modifiers ----------------

                if (!item.modifiersJson.isNullOrBlank()) {

                    try {

                        val modifiers = item.modifiersJson
                            .removePrefix("[")
                            .removeSuffix("]")
                            .split(",")
                            .map {
                                it.trim().replace("\"", "")
                            }
                            .filter {
                                it.isNotBlank()
                            }

                        modifiers.forEach {

                            drawModifier(it)

                        }

                    } catch (_: Exception) {

                        drawModifier(item.modifiersJson)

                    }

                }

                //---------------- Notes -------------------

                if (!item.note.isNullOrBlank()) {

                    drawNote(item.note)

                }

                y += 12f

            }

            // Bottom Line

            canvas.drawLine(
                LEFT,
                y,
                RIGHT,
                y,
                linePaint
            )

            y += 20f
        }

        //================================================
        // Helpers
        //================================================

        private fun drawCenter(
            text: String,
            paint: Paint
        ) {

            canvas.drawText(
                text,
                RECEIPT_WIDTH / 2f,
                y,
                paint
            )

            y += paint.textSize + 8f
        }

        private fun drawLeft(
            text: String,
            x: Float,
            paint: Paint
        ) {

            canvas.drawText(
                text,
                x,
                y,
                paint
            )
        }

        private fun drawRight(
            text: String,
            x: Float,
            paint: Paint
        ) {

            val width = paint.measureText(text)

            canvas.drawText(
                text,
                x - width,
                y,
                paint
            )
        }

        private fun drawDivider() {

            canvas.drawLine(
                LEFT,
                y,
                RIGHT,
                y,
                linePaint
            )

            y += 15f
        }

    }

}