package com.it10x.foodappgstav7_18.printer.queue

import android.util.Log
import com.it10x.foodappgstav7_18.data.printqueue.PrintQueueEntity
import com.it10x.foodappgstav7_18.data.PrinterRole
import com.it10x.foodappgstav7_18.data.printqueue.PrintQueueDao

import com.it10x.foodappgstav7_18.printer.PrinterManager
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.util.UUID
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class PrintQueueManager(
    private val dao: PrintQueueDao,
    private val printerManager: PrinterManager
) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())


    private val channels = mutableMapOf<PrinterRole, Channel<PrintQueueEntity>>()

    init {
        PrinterRole.values().forEach { role ->
            val channel = Channel<PrintQueueEntity>(Channel.UNLIMITED)
            channels[role] = channel
            startWorker(channel)
        }

       // Log.e("QUEUE_INIT", "🔥 PrintQueueManager CREATED ${System.currentTimeMillis()}")

        scope.launch {
            loadPendingJobs()
        }
    }

    suspend fun enqueue(  role: PrinterRole,
                          text: String,
                          paymentMode: String? = null,
                          grandTotal: Double? = null) {

        val job = PrintQueueEntity(
            id = UUID.randomUUID().toString(),
            role = role.name,
            jobType = "TEXT",
            text = text,
            imagePath = null,
            paymentMode = paymentMode,
            grandTotal = grandTotal,
            status = "PENDING",
            retryCount = 0,
            createdAt = System.currentTimeMillis()
        )

        dao.insert(job)

        channels[role]?.send(job)   // ✅ ROLE BASED
    }


    suspend fun enqueueImage(
        role: PrinterRole,
        imagePath: String,
        paymentMode: String? = null,
        grandTotal: Double? = null
    ) {
        Log.d(
            "IMAGE_TEST",
            "Queue IMAGE job"
        )

        val job = PrintQueueEntity(
            id = UUID.randomUUID().toString(),
            role = role.name,
            jobType = "IMAGE",
            text = null,
            imagePath = imagePath,
            paymentMode = paymentMode,
            grandTotal = grandTotal,
            status = "PENDING",
            retryCount = 0,
            createdAt = System.currentTimeMillis()
        )

        dao.insert(job)

        channels[role]?.send(job)
    }

    private fun startWorker(channel: Channel<PrintQueueEntity>) {
        scope.launch {
            for (job in channel) {
                processJob(job)
            }
        }
    }


    private suspend fun processJob(job: PrintQueueEntity) {

        Log.d(
            "IMAGE_TEST",
            "Job Type = ${job.jobType}"
        )

        dao.updateStatus(job.id, "PRINTING", job.retryCount)

        Log.d(
            "PRINT_QUEUE",
            "Printing ${job.jobType} job ${job.id}"
        )

        suspendCancellableCoroutine<Unit> { cont ->

            when (job.jobType) {

                "TEXT" -> {

                    printerManager.printText(
                        PrinterRole.valueOf(job.role),
                        requireNotNull(job.text) {
                            "Text job has no text."
                        },
                        job.paymentMode,
                        job.grandTotal
                    ) {
                        if (cont.isActive) {
                            cont.resume(Unit)
                        }
                    }
                }

                "IMAGE" -> {

                    printerManager.printBitmap(
                        role = PrinterRole.valueOf(job.role),
                        imagePath = requireNotNull(job.imagePath) {
                            "Image job has no image path."
                        }
                    ) {
                        if (cont.isActive) {
                            cont.resume(Unit)
                        }
                    }
                }

                else -> {

                    Log.e(
                        "PRINT_QUEUE",
                        "Unknown job type ${job.jobType}"
                    )

                    if (cont.isActive) {
                        cont.resume(Unit)
                    }
                }
            }
        }

        dao.delete(job.id)

        Log.d(
            "PRINT_QUEUE",
            "DONE ${job.id}"
        )
    }


//    private suspend fun processJob(job: PrintQueueEntity) {
//
//        dao.updateStatus(job.id, "PRINTING", job.retryCount)
//
//        Log.d("PRINT_QUEUE", "Printing job ${job.id}")
//
//        suspendCancellableCoroutine<Unit> { cont ->
//
//            printerManager.printText(
//                PrinterRole.valueOf(job.role),
//                requireNotNull(job.text) { "Text job has no text." },
//                job.paymentMode,
//                job.grandTotal
//            ) {
//                if (cont.isActive) cont.resume(Unit)
//            }
//        }
//
//        // ✅ ALWAYS mark success after first attempt
//        dao.delete(job.id)
//
//        Log.d("PRINT_QUEUE", "DONE ${job.id}")
//    }

    private suspend fun loadPendingJobs() {
        val jobs = dao.getPending()

        jobs.forEach { job ->
            val role = PrinterRole.valueOf(job.role)
            channels[role]?.send(job)
        }
    }
}