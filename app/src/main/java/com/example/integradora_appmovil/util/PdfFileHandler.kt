package com.example.integradora_appmovil.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

object PdfFileHandler {

    fun openPdf(context: Context, fileName: String, bytes: ByteArray, mimeType: String = "application/pdf") {
        val safeFileName = fileName.ifBlank { "comprobante.pdf" }
            .replace(Regex("[^A-Za-z0-9._-]"), "_")
        val targetFile = File(context.cacheDir, safeFileName)

        targetFile.outputStream().use { output ->
            output.write(bytes)
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            targetFile
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType.ifBlank { "application/pdf" })
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "No se encontró una app para abrir el PDF", Toast.LENGTH_LONG).show()
        }
    }
}
