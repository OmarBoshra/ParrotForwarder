package com.example.parrot.presentation.features.datasanitization.utils

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import androidx.core.graphics.createBitmap
import com.example.parrot.utils.Constants.LogTags.ERROR
import com.example.parrot.utils.Constants.LogTags.OPERATION
import com.example.parrot.utils.Constants.LogTags.VALUE
import org.apache.poi.openxml4j.opc.OPCPackage
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xwpf.usermodel.*
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFParagraph
import org.apache.poi.xwpf.usermodel.XWPFTable
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object FileConverter {
    fun mergePngImages(
        resolver: ContentResolver,
        imageUris: List<Uri>,
        outputFile: File,
    ) {
        Timber.tag(OPERATION).d("Merging multiple images started")
        if (imageUris.isEmpty()) return

        val bitmaps =
            imageUris.mapNotNull { uri ->
                resolver.openInputStream(uri)?.use { input ->
                    BitmapFactory.decodeStream(input)
                }
            }

        if (bitmaps.isEmpty()) return

        val totalHeight = bitmaps.sumOf { it.height }
        val maxWidth = bitmaps.maxOf { it.width }

        val mergedBitmap = createBitmap(maxWidth, totalHeight)
        val canvas = Canvas(mergedBitmap)

        var yOffset = 0
        for (bmp in bitmaps) {
            canvas.drawBitmap(bmp, 0f, yOffset.toFloat(), null)
            yOffset += bmp.height
        }

        try {
            FileOutputStream(outputFile).use { out ->
                mergedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                Timber.tag(OPERATION).d("mergePngImages: Successfully merged %d images to %s", bitmaps.size, outputFile.absolutePath)
            }
        } catch (e: Exception) {
            Timber.tag(ERROR).e(e, "mergePngImages: Error merging images to %s", outputFile.absolutePath)
        }
    }

    fun convertExcelToMarkdown(
        input: InputStream,
        outputFile: File,
    ) {
        WorkbookFactory.create(input).use { workbook ->
            val sb = StringBuilder()

            workbook.forEach { sheet ->
                sb.append("## ${sheet.sheetName}\n")
                sheet.forEach { row ->
                    val cells = row.map { it.toString().trim() }
                    sb.append("| ${cells.joinToString(" | ")} |\n")
                    if (row.rowNum == 0) {
                        Timber.tag(VALUE).d("row number is %d", row.rowNum)
                        sb.append("|${cells.joinToString("|") { "---" }}|\n")
                    }
                }
                sb.append("\n")
            }
            Timber.tag(VALUE).d("row number is %s", sb.toString())
            outputFile.writeText(sb.toString())
        }
    }

    fun convertDocxToMarkdown(
        input: InputStream,
        outputFile: File,
    ): String {
        val tempDocx = File.createTempFile("input", ".docx", outputFile.parentFile)
        tempDocx.outputStream().use { out -> input.copyTo(out) }

        val pkg = OPCPackage.open(tempDocx.inputStream())
        val doc = XWPFDocument(pkg)

        // Build Markdown by traversing body elements
        val markdownBuilder = StringBuilder()
        var listLevel = 0
        var currentListType: ListType? = null

        for (bodyElement in doc.bodyElements) {
            when (bodyElement) {
                is XWPFParagraph -> {
                    val para = bodyElement as XWPFParagraph
                    val text = para.getText().trim()
                    if (text.isEmpty()) continue

                    // Detect heading level (direct or via style hierarchy)
                    val headingLevel = getOutlineLevel(para, doc)
                    if (headingLevel > 0) {
                        markdownBuilder.append("#".repeat(headingLevel)).append(" ").append(text).append("\n\n")
                        currentListType = null
                    } else if (isListItem(para)) {
                        val listType = detectListType(para)
                        if (listType != currentListType) {
                            listLevel = 0
                            currentListType = listType
                        }
                        listLevel++
                        val prefix =
                            when (listType) {
                                ListType.NUMBERED -> "$listLevel. "
                                ListType.BULLETED -> "- "
                                else -> ""
                            }
                        markdownBuilder.append(prefix).append(text).append("\n")
                    } else {
                        markdownBuilder.append(text).append("\n\n")
                        currentListType = null
                    }
                }
                is XWPFTable -> {
                    val tableMd = tableToMarkdown(bodyElement as XWPFTable)
                    markdownBuilder.append(tableMd).append("\n\n")
                }
                else -> {}
            }
        }

        // Cleanup
        var markdown = markdownBuilder.toString().trim()
        markdown = markdown.replace(Regex("\\n{3,}"), "\n\n")

        pkg.close()
        tempDocx.delete()

        Timber.tag("VALUE").d("Markdown: %s", markdown)
        outputFile.writeText(markdown)
        return markdown
    }

    // Helpers
    enum class ListType { NUMBERED, BULLETED, NONE }

    private fun getOutlineLevel(
        para: XWPFParagraph,
        doc: XWPFDocument,
    ): Int {
        // Direct on paragraph
        val pPr = para.getCTP().getPPr()
        pPr?.getOutlineLvl()?.getVal()?.let { valBig -> return valBig.toInt() + 1 }

        // Traverse style hierarchy (recursive, with depth limit)
        var styleId = para.styleID
        var depth = 0
        while (styleId != null && depth < 10) { // Prevent cycles
            val style = doc.styles?.getStyle(styleId) ?: break
            val stylePPr = style.getCTStyle()?.getPPr()
            stylePPr?.getOutlineLvl()?.getVal()?.let { valBig -> return valBig.toInt() + 1 }
            styleId = style.getBasisStyleID() // Traverse to based-on style
            depth++
        }

        // Fallback to style name
        return if (para.styleID?.startsWith("Heading") == true) {
            para.styleID.removePrefix("Heading").toIntOrNull() ?: 0
        } else {
            0
        }
    }

    private fun isListItem(para: XWPFParagraph): Boolean {
        return para.getNumID() != null
    }

    private fun detectListType(para: XWPFParagraph): ListType {
        val fmt = para.getNumFmt() ?: ""
        return if (fmt.startsWith("decimal") || fmt.contains("%")) ListType.NUMBERED else ListType.BULLETED
    }

    private fun tableToMarkdown(table: XWPFTable): String {
        val headerRow =
            table.rows.firstOrNull()?.let { row ->
                row.tableCells.joinToString("|") { cell ->
                    cell.getText().trim().replace("|", "\\|")
                }
            } ?: return ""

        val bodyRows =
            table.rows.drop(1).map { row ->
                row.tableCells.joinToString("|") { cell ->
                    cell.getText().trim().replace("|", "\\|")
                }
            }

        val separator = "|${"---|".repeat(headerRow.split("|").size)}"
        return "$headerRow\n$separator\n${bodyRows.joinToString("\n")}"
    }

    private fun addLinks(
        doc: XWPFDocument,
        markdown: String,
    ): String {
        var enhanced = markdown
        doc.paragraphs.forEach { para ->
            para.runs.forEach { run ->
                if (run is XWPFHyperlinkRun) {
                    val hyperlink = run.getHyperlink(doc)
                    val url = hyperlink.getURL()
                    val linkText = run.text()?.trim() ?: ""
                    if (linkText.isNotEmpty()) {
                        enhanced = enhanced.replace(linkText, "[$linkText]($url)")
                    }
                }
            }
        }
        return enhanced
    }
}
