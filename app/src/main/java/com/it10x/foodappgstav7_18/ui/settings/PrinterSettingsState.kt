package com.it10x.foodappgstav7_18.ui.settings

import com.it10x.foodappgstav7_18.data.PrinterConfig
import com.it10x.foodappgstav7_18.data.PrinterRole

data class PrinterSettingsState(
    val printers: Map<PrinterRole, PrinterConfig> = emptyMap()
)
