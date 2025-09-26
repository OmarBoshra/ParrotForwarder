package com.example.parrot.procedures

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.parrot.presentation.features.datasanitization.utils.FileConverter
import com.example.parrot.utils.Constants.LogTags.EVENT
import com.example.parrot.utils.Constants.LogTags.OPERATION
import com.example.parrot.utils.Constants.LogTags.VALUE
import com.example.parrot.utils.ConstraintException
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class FileProcessingProcedure
    @Inject
    constructor(
        @ApplicationContext val applicationContext: Context,
    ) {
        fun handleShareIntent(shareIntent: Intent) {
            when (shareIntent.action) {
                Intent.ACTION_SEND -> {
                    Timber.tag(EVENT).d("ACTION_SEND")
                    val uri = shareIntent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                    uri?.let {
                        Timber.tag(VALUE).d("URI is $it")
                        categorizeSharedFiles(listOf(it))
                    } ?: run {
                        Timber.tag(VALUE).d("URI is null")
                        throw ConstraintException("There is no uri , please try again")
                    }
                }
                Intent.ACTION_SEND_MULTIPLE -> {
                    Timber.tag(EVENT).d("ACTION_SEND_MULTIPLE ")
                    val uris = shareIntent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
                    uris?.let {
                        Timber.tag(VALUE).d("URI is $it")
                        categorizeSharedFiles(it)
                    } ?: run {
                        Timber.tag(VALUE).d("URI is null")
                        throw ConstraintException("There is no uri , please try again")
                    }
                }
            }
        }

        private fun categorizeSharedFiles(uris: List<Uri>) {
            val resolver = applicationContext.contentResolver
            Timber.tag(OPERATION).d("Starting to categorize ${uris.size} URIs.")
            // Type of files to be processed.
            val wordFiles = mutableListOf<Uri>()
            val excelFiles = mutableListOf<Uri>()
            val imageFiles = mutableListOf<Uri>()
            val otherFiles = mutableListOf<Uri>()
            // Iterating over the files and categorizing them.
            for (uri in uris) {
                val mimeType =
                    resolver.getType(uri)?.also {
                        Timber.tag(VALUE).d("Processing URI: $uri, MimeType: $it")
                    } ?: run {
                        Timber.tag(VALUE).e("MIME type is null for URI: $uri")
                        throw ConstraintException("Could not determine file type for one of the shared items. Please try again.")
                    }

                when {
                    mimeType.equals(
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        ignoreCase = true,
                    ) -> {
                        Timber.tag(OPERATION).d("adding word")
                        wordFiles.add(uri)
                    }

                    mimeType.equals(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        ignoreCase = true,
                    ) || mimeType.equals("application/vnd.ms-excel", ignoreCase = true) -> {
                        Timber.tag(OPERATION).d("adding excel")
                        excelFiles.add(uri)
                    }

                    mimeType.startsWith("image/") -> {
                        Timber.tag(OPERATION).d("adding image")
                        imageFiles.add(uri)
                    }

                    else -> {
                        Timber.tag(OPERATION).d("adding other file")
                        otherFiles.add(uri)
                    }
                }
            }
            Timber.tag(
                VALUE,
            ).d(
                "Categorization complete: %d Word, %d Excel, %d Image, %d Other files",
                wordFiles.size,
                excelFiles.size,
                imageFiles.size,
                otherFiles.size,
            )
            createSharedFilesDirectories(resolver, wordFiles, excelFiles, imageFiles)
        }

        private fun createSharedFilesDirectories(
            resolver: ContentResolver,
            wordFiles: List<Uri>,
            excelFiles: List<Uri>,
            imageFiles: List<Uri>,
        ) {
            Timber.tag(OPERATION).d("Prepare app-private directories")
            // Prepare app-private directories
            if (excelFiles.isNotEmpty()) {
                val excelDir = File(applicationContext.filesDir, "excel_markdowns").apply { mkdirs() }
                // 2. Excel → Markdown
                excelFiles.forEach { uri ->
                    resolver.openInputStream(uri)?.use { input ->
                        Timber.tag(OPERATION).d("Prepare mdfile")
                        val mdFile = File(excelDir, "${System.currentTimeMillis()}.md")
                        Timber.tag(VALUE).d("MdFile is $mdFile")
                        FileConverter.convertExcelToMarkdown(input, mdFile)
                    }
                }
            }
            if (wordFiles.isNotEmpty()) {
                val wordDir = File(applicationContext.filesDir, "word_markdowns").apply { mkdirs() }
                // 1. Word → Markdown
                wordFiles.forEach { uri ->
                    resolver.openInputStream(uri)?.use { input ->
                        Timber.tag(OPERATION).d("Prepare mdfile")
                        val mdFile = File(wordDir, "${System.currentTimeMillis()}.md")
                        Timber.tag(VALUE).d("MdFile is $mdFile")
                        FileConverter.convertDocxToMarkdown(input, mdFile)
                    }
                }
            }
            if (imageFiles.isNotEmpty()) {
                val imgDir = File(applicationContext.filesDir, "merged_images").apply { mkdirs() }
                // 3. Merge PNGs if more than one
                if (imageFiles.size > 1) {
                    Timber.tag(OPERATION).d("Merging multiple images")
                    val mergedFile = File(imgDir, "merged_${System.currentTimeMillis()}.png")
                    FileConverter.mergePngImages(resolver = resolver, imageFiles, mergedFile)
                } else if (imageFiles.size == 1) {
                    Timber.tag(OPERATION).d("Adding a single image")
                    resolver.openInputStream(imageFiles.first())?.use { input ->
                        File(imgDir, "image_${System.currentTimeMillis()}.png").outputStream()
                            .use { output ->
                                input.copyTo(output)
                            }
                    }
                }
            }
        }
    }
