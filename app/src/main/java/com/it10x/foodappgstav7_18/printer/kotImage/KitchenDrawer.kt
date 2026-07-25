package com.it10x.foodappgstav7_18.printer.kotImage

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.it10x.foodappgstav7_18.data.pos.entities.PosKotItemEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class KitchenDrawer(private val canvas: Canvas) {

    var y = 30f

    // ✅ AUTO WIDTH DETECTION (KEY CHANGE)
    private val receiptWidth = canvas.width.toFloat()

    // ✅ AUTO SCALE (32mm / 48mm handled automatically)
    private val scale = receiptWidth / 384f

    private val padding = 12f * scale
    private val lineHeight = 22f * scale

    private val paint = Paint().apply {
        color = Color.BLACK
        textSize = 18f * scale
        isAntiAlias = true
        textAlign = Paint.Align.LEFT
    }

    private val boldPaint = Paint(paint).apply {
        textSize = 20f * scale
        typeface = Typeface.DEFAULT_BOLD
    }

    private val rightPaint = Paint(paint).apply {
        textAlign = Paint.Align.RIGHT
    }

    private val boxPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 1f * scale
    }

    // =========================
    // HEADER
    // =========================
    fun drawHeader(kotNumber: String, orderType: String) {

        val boxHeight = 60f * scale

        canvas.drawRect(
            padding,
            y,
            receiptWidth - padding,
            y + boxHeight,
            boxPaint
        )

        boldPaint.textSize = 22f * scale
        paint.textSize = 18f * scale

        canvas.drawText("KOT:", padding + 8f, y + (25f * scale), boldPaint)
        canvas.drawText(kotNumber, padding + (80f * scale), y + (25f * scale), paint)

        val time = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Date: $time", padding + 8f, y + (45f * scale), paint)

        y += boxHeight + (10f * scale)

        canvas.drawText("Table: $orderType", padding, y, boldPaint)
        y += lineHeight

        canvas.drawLine(padding, y, receiptWidth - padding, y, boxPaint)
        y += 10f * scale

        canvas.drawText("Item", padding, y, boldPaint)
        canvas.drawText("Qty", receiptWidth - padding, y, rightPaint)

        y += 10f * scale
        canvas.drawLine(padding, y, receiptWidth - padding, y, boxPaint)
        y += lineHeight
    }

    // =========================
    // ITEMS
    // =========================
    fun drawItems(items: List<PosKotItemEntity>) {

        val compactLineHeight = 26f * scale

        val nameX = padding
        val qtyX = receiptWidth - padding
        val maxNameWidth = receiptWidth - (80f * scale)

        if (items.isEmpty()) {
            canvas.drawText("No items", nameX, y, paint)
            y += compactLineHeight
            return
        }

        items.forEach { item ->

            val name = item.name?.trim().takeUnless { it.isNullOrEmpty() } ?: "Item"
            val qty = item.quantity ?: 0.0
            val note = item.note

            val words = name.split(" ")
            var line = ""

            // 🔹 TEXT WRAP
            for (word in words) {

                val testLine = if (line.isEmpty()) word else "$line $word"

                if (paint.measureText(testLine) > maxNameWidth) {

                    if (line.isNotEmpty()) {
                        canvas.drawText(line, nameX, y, paint)
                        y += compactLineHeight
                    }

                    line = word
                } else {
                    line = testLine
                }
            }

            // 🔹 LAST LINE + QTY
            if (line.isNotEmpty()) {

                canvas.drawText(line, nameX, y, paint)

                val qtyText = try {
                    String.format(Locale.US, "%.0f", qty)
                } catch (e: Exception) {
                    "0"
                }

                canvas.drawText(qtyText, qtyX, y, rightPaint)

                y += compactLineHeight
            }

            // 🔹 NOTE
            if (!note.isNullOrEmpty()) {
                val notePaint = Paint(paint).apply {
                    textSize = 16f * scale
                }

                canvas.drawText("• $note", nameX + (12f * scale), y, notePaint)
                y += compactLineHeight
            }

            // 🔹 DIVIDER
            canvas.drawLine(padding, y, receiptWidth - padding, y, boxPaint)
            y += 6f * scale
        }

        y += 6f * scale
    }

    // =========================
    // FOOTER
    // =========================
    fun drawFooter() {

        y += 30f * scale

        val centerX = receiptWidth / 2

        val centerPaint = Paint(paint).apply {
            textAlign = Paint.Align.CENTER
        }

        canvas.drawText("---- END ----", centerX, y, centerPaint)

        y += 40f * scale
    }
}

//class KitchenDrawer(
//    private val canvas: Canvas,
//    private val receiptWidth: Float = 384f   // ✅ 384 = 48mm, use 256f for 32mm
//)
//{
//
//    var y = 30f
//
//    // ✅ Dynamic scaling (VERY IMPORTANT)
//    private val scale = receiptWidth / 384f
//
//    private val padding = 12f * scale
//    private val lineHeight = 22f * scale
//
//    private val paint = Paint().apply {
//        color = Color.BLACK
//        textSize = 18f * scale
//        isAntiAlias = true
//        textAlign = Paint.Align.LEFT
//    }
//
//    private val boldPaint = Paint(paint).apply {
//        textSize = 20f * scale
//        typeface = Typeface.DEFAULT_BOLD
//    }
//
//    private val rightPaint = Paint(paint).apply {
//        textAlign = Paint.Align.RIGHT
//    }
//
//    private val boxPaint = Paint().apply {
//        color = Color.BLACK
//        style = Paint.Style.STROKE
//        strokeWidth = 1f * scale
//    }
//
//    // =========================
//    // HEADER
//    // =========================
//    fun drawHeader(kotNumber: String, orderType: String) {
//
//        val boxHeight = 60f * scale
//
//        canvas.drawRect(
//            padding,
//            y,
//            receiptWidth - padding,
//            y + boxHeight,
//            boxPaint
//        )
//
//        boldPaint.textSize = 22f * scale
//        paint.textSize = 18f * scale
//
//        canvas.drawText("KOT:", padding + 8f, y + (25f * scale), boldPaint)
//        canvas.drawText(kotNumber, padding + (80f * scale), y + (25f * scale), paint)
//
//        val time = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date())
//        canvas.drawText("Date: $time", padding + 8f, y + (45f * scale), paint)
//
//        y += boxHeight + (10f * scale)
//
//        boldPaint.textSize = 20f * scale
//
//        canvas.drawText("Table: $orderType", padding, y, boldPaint)
//        y += lineHeight
//
//        canvas.drawLine(padding, y, receiptWidth - padding, y, boxPaint)
//        y += 10f * scale
//
//        canvas.drawText("Item", padding, y, boldPaint)
//        canvas.drawText("Qty", receiptWidth - padding, y, rightPaint)
//
//        y += 10f * scale
//        canvas.drawLine(padding, y, receiptWidth - padding, y, boxPaint)
//        y += lineHeight
//    }
//
//    // =========================
//    // ITEMS
//    // =========================
//    fun drawItems(items: List<PosKotItemEntity>) {
//
//        val compactLineHeight = 26f * scale
//
//        val nameX = padding
//        val qtyX = receiptWidth - padding
//        val maxNameWidth = receiptWidth - (80f * scale)
//
//        if (items.isEmpty()) {
//            canvas.drawText("No items", nameX, y, paint)
//            y += compactLineHeight
//            return
//        }
//
//        items.forEach { item ->
//
//            val name = item.name?.trim().takeUnless { it.isNullOrEmpty() } ?: "Item"
//            val qty = item.quantity ?: 0.0
//            val note = item.note
//
//            val words = name.split(" ")
//            var line = ""
//
//            // 🔹 TEXT WRAP
//            for (word in words) {
//
//                val testLine = if (line.isEmpty()) word else "$line $word"
//
//                if (paint.measureText(testLine) > maxNameWidth) {
//
//                    if (line.isNotEmpty()) {
//                        canvas.drawText(line, nameX, y, paint)
//                        y += compactLineHeight
//                    }
//
//                    line = word
//                } else {
//                    line = testLine
//                }
//            }
//
//            // 🔹 LAST LINE + QTY
//            if (line.isNotEmpty()) {
//
//                canvas.drawText(line, nameX, y, paint)
//
//                val qtyText = try {
//                    String.format(Locale.US, "%.0f", qty)
//                } catch (e: Exception) {
//                    "0"
//                }
//
//                canvas.drawText(qtyText, qtyX, y, rightPaint)
//
//                y += compactLineHeight
//            }
//
//            // 🔹 NOTE
//            if (!note.isNullOrEmpty()) {
//                val notePaint = Paint(paint).apply {
//                    textSize = 16f * scale
//                }
//
//                canvas.drawText("• $note", nameX + (12f * scale), y, notePaint)
//                y += compactLineHeight
//            }
//
//            // 🔹 DIVIDER
//            canvas.drawLine(padding, y, receiptWidth - padding, y, boxPaint)
//            y += 6f * scale
//        }
//
//        y += 6f * scale
//    }
//
//    // =========================
//    // FOOTER
//    // =========================
//    fun drawFooter() {
//
//        y += 30f * scale
//
//        val centerX = receiptWidth / 2
//
//        val centerPaint = Paint(paint).apply {
//            textAlign = Paint.Align.CENTER
//        }
//
//        canvas.drawText("---- END ----", centerX, y, centerPaint)
//
//        y += 40f * scale
//    }
//}