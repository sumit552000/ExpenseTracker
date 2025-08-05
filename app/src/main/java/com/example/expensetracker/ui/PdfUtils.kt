package com.example.expensetracker.ui

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.expensetracker.data.Expense
import java.io.File
import java.io.FileOutputStream
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

object PdfUtils {
    @SuppressLint("NewApi")
    fun exportMonthlyReport(context: Context, expenses: List<Expense>) {
        try {
            val pdfDocument = PdfDocument()
            val paint = Paint()
            paint.textSize = 12f

            // Group by Month-Year
            val grouped = expenses.groupBy { expense ->
                val parts = expense.date.split("-")
                val year = parts[0]
                val month = parts[1].toInt()
                val monthName = Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                "$monthName $year"
            }

            var pageNumber = 1
            var y = 50
            fun newPage(): PdfDocument.Page {
                val pageInfo = PdfDocument.PageInfo.Builder(600, 800, pageNumber).create()
                return pdfDocument.startPage(pageInfo)
            }

            var page = newPage()
            val canvas = page.canvas

            grouped.forEach { (monthYear, monthExpenses) ->
                paint.textSize = 16f
                paint.isFakeBoldText = true
                canvas.drawText(monthYear, 220f, y.toFloat(), paint)
                y += 40

                // Table header
                paint.textSize = 14f
                canvas.drawText("Title", 40f, y.toFloat(), paint)
                canvas.drawText("Category", 240f, y.toFloat(), paint)
                canvas.drawText("Amount", 440f, y.toFloat(), paint)
                y += 25

                paint.textSize = 12f
                var total = 0.0

                monthExpenses.forEach { expense ->
                    if (y > 750) {
                        pdfDocument.finishPage(page)
                        pageNumber++
                        page = newPage()
                        y = 50
                    }
                    canvas.drawText(expense.title, 40f, y.toFloat(), paint)
                    canvas.drawText(expense.category, 240f, y.toFloat(), paint)
                    canvas.drawText("₹${expense.amount}", 440f, y.toFloat(), paint)
                    y += 20
                    total += expense.amount
                }

                y += 20
                paint.isFakeBoldText = true
                canvas.drawText("Total:", 240f, y.toFloat(), paint)
                canvas.drawText("₹$total", 440f, y.toFloat(), paint)
                y += 50
            }

            pdfDocument.finishPage(page)

            // ✅ Save in app's Documents dir (no permission required)
            val docsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            if (docsDir != null && !docsDir.exists()) docsDir.mkdirs()
            val file = File(docsDir, "ExpenseReport.pdf")
            FileOutputStream(file).use { out -> pdfDocument.writeTo(out) }
            pdfDocument.close()

            Toast.makeText(context, "PDF saved to ${file.absolutePath}", Toast.LENGTH_LONG).show()

            // ✅ Open the PDF
            openPdf(context, file)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to save PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun openPdf(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",  // must match manifest provider
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NO_HISTORY
        }

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No PDF viewer installed!", Toast.LENGTH_LONG).show()
        }
    }
}