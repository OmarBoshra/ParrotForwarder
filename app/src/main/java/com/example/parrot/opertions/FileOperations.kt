package com.example.parrot.opertions

import android.content.Intent
import android.webkit.MimeTypeMap
import com.example.parrot.presentation.models.FileItem
import com.example.parrot.procedures.FileProcessingProcedure
import com.example.parrot.procedures.LoadFoldersProcedure
import com.example.parrot.utils.ItemState
import com.example.parrot.utils.ListState
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject

class FileOperations
    @Inject
    constructor(
        private val fileProcessingProcedure: FileProcessingProcedure,
        private val loadFoldersProcedure: LoadFoldersProcedure,
    ) {
        fun runFileProcessing(shareIntent: Intent) {
            fileProcessingProcedure.handleShareIntent(shareIntent)
        }

        fun runFileFetching(folder: File?): Flow<ListState<List<ItemState<FileItem?>?>?>> = loadFoldersProcedure.orchestrate(folder)

        fun getMimeType(file: File): String {
            val extension = file.extension.lowercase()
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "application/octet-stream"
        }
    }
