package com.it10x.foodappgstav7_18.ui.preview

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import java.io.File

class ReceiptPreviewActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val path = intent.getStringExtra("image") ?: return

        val bitmap =
            BitmapFactory.decodeFile(path)

        setContent {
            ReceiptPreviewScreen(bitmap)
        }
    }
}