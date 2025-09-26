package com.example.parrot.procedures

import android.content.Context
import com.example.parrot.R
import com.example.parrot.presentation.models.FileItem
import com.example.parrot.utils.Constants.LogTags.OPERATION
import com.example.parrot.utils.Constants.LogTags.VALUE
import com.example.parrot.utils.Constants.StateConstants.EMPTY
import com.example.parrot.utils.Constants.StateConstants.LOADING
import com.example.parrot.utils.ConstraintException
import com.example.parrot.utils.ItemState
import com.example.parrot.utils.ListState
import com.example.parrot.utils.ListState.Error
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class LoadFoldersProcedure
    @Inject
    constructor(
        @ApplicationContext val applicationContext: Context,
    ) {
        fun orchestrate(folder: File?): Flow<ListState<List<ItemState<FileItem?>?>?>> =
            flow {
                Timber.tag(OPERATION).d("initialize: Starting folder loading process.")
                emit(ListState.Loading(LOADING))

                val fileItems = handleFilesAndFolders(folder)
                if (checkIfEmpty(fileItems, ::emit)) return@flow

                val fileItemsModel = packTheFiles(fileItems)
                emit(createFilesLoadedState(fileItemsModel))
            }.catch { e ->
                val errorState =
                    when (e) {
                        is ConstraintException -> Error(e.violation)
                        else -> Error(e.message ?: "An unexpected error occurred.")
                    }
                emit(errorState)
            }

        private fun handleFilesAndFolders(folder: File?): List<File> {
            return if (folder != null) {
                listFilesInFolder(folder)
            } else {
                getCategorizedFolders()
            }
        }

        private fun listFilesInFolder(folder: File): List<File> {
            return folder.listFiles()?.toList() ?: emptyList()
        }

        private fun getCategorizedFolders(): List<File> {
            val baseDir = applicationContext.filesDir
            return listOf("word_markdowns", "excel_markdowns", "merged_images")
                .map { File(baseDir, it) }
                .filter { it.exists() && it.isDirectory }
        }

        private suspend fun checkIfEmpty(
            files: List<File>,
            emit: suspend (ListState<List<ItemState<FileItem?>?>?>) -> Unit,
        ): Boolean {
            if (files.isEmpty()) {
                Timber.tag(VALUE).d("initialize: No category folders found after getCategorizedFolders. Emitting Empty state directly.")
                emit(ListState.Empty(EMPTY))
                return true
            } else {
                Timber.tag(VALUE).d("initialize: Categorized folders are not empty. Proceeding to pack files.")
                return false
            }
        }

        private fun packTheFiles(files: List<File>): List<FileItem> {
            val fileItems: MutableList<FileItem> = mutableListOf()
            files.forEach { file ->
                val fileItem =
                    FileItem(
                        name = file.name,
                        file = file,
                        iconResId = getIconResource(file),
                    )
                fileItems.add(fileItem)
            }
            return fileItems.toList()
        }

        private fun getIconResource(file: File): Int {
            return if (file.isDirectory) {
                R.drawable.ic_folder
            } else {
                when (file.extension.lowercase()) {
                    "png" -> R.drawable.ic_images
                    "md" -> R.drawable.ic_docs
                    else -> {
                        throw ConstraintException("Unsupported file type: ${file.extension}")
                    }
                }
            }
        }

        private fun createFilesLoadedState(files: List<FileItem?>): ListState.Loaded<List<ItemState.ItemNotSelected<FileItem?>?>?> {
            val items: List<ItemState.ItemNotSelected<FileItem?>?> =
                mutableListOf<ItemState.ItemNotSelected<FileItem?>>().apply {
                    files.forEachIndexed { index,file ->
                        file?.let {
                            add(
                                ItemState.ItemNotSelected(
                                    it,index.toString()
                                ),
                            )
                        }
                    }
                }
            Timber.tag(OPERATION).d("initialize: Files packed and loaded state created. Emitting Loaded state.")
            return ListState.Loaded(items)
        }
    }
