package com.example.parrot.presentation.features.home

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dinnerapp.presentation.events.eventlisteners.HomeComponentEventListener
import com.example.parrot.opertions.FileOperations
import com.example.parrot.presentation.events.componentevents.HomeComponentEvents
import com.example.parrot.presentation.events.eventlisteners.HomeFragmentEventListener
import com.example.parrot.presentation.events.fragmentevents.HomeFragmentEvents
import com.example.parrot.presentation.models.FileItem
import com.example.parrot.utils.Constants.LogTags.ERROR
import com.example.parrot.utils.Constants.LogTags.EVENT
import com.example.parrot.utils.Constants.LogTags.VIOLATION
import com.example.parrot.utils.ConstraintException
import com.example.parrot.utils.ItemState
import com.example.parrot.utils.ListState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        @ApplicationContext private val applicationContext: Context,
        private val fileOperations: FileOperations,
    ) : ViewModel(), HomeFragmentEventListener, HomeComponentEventListener {
        private val _onFilesUpdate =
            MutableStateFlow<ListState<List<ItemState<FileItem?>?>?>?>(null)
        val onFilesUpdateFlow = _onFilesUpdate

        // region Fragment events.

        override fun onEvent(event: HomeFragmentEvents) {
            fragmentEventsHandler(event)
        }

        private fun fragmentEventsHandler(event: HomeFragmentEvents) {
            when (event) {
                is HomeFragmentEvents.ShowFiles -> {
                    Timber.tag(EVENT).d("Triggered ShowFiles HomeFragmentEvent")
                    showFolders()
                }
            }
        }

        //endregion

        // region files retrieval Logic

        private fun showFolders() {
            viewModelScope.launch {
                fileOperations.runFileFetching(null).collect { state ->
                    _onFilesUpdate.emit(state)
                }
            }
        }

        //endregion
        // region component events

        override fun onEvent(event: HomeComponentEvents) {
            componentEventsHandler(event)
        }

        private fun componentEventsHandler(event: HomeComponentEvents) {
            when (event) {
                is HomeComponentEvents.OnFileClicked -> {
                    Timber.tag(EVENT).d("Triggered open file")
                    filesAndFoldersRouter(event.file)
                }
                is HomeComponentEvents.OnFileSelected -> {
                    Timber.tag(EVENT).d("Triggered Files selection")
                }
            }
        }

        private fun filesAndFoldersRouter(file: File) {
            // Open the folder
            if (file.isDirectory) {
                viewModelScope.launch {
                    fileOperations.runFileFetching(file).collect { state ->
                        _onFilesUpdate.emit(state)
                    }
                }
            } else {
                // Open the file
                try {
                    val intent = openFileExternally(
                        applicationContext,
                        file,
                        fileOperations.getMimeType(file), // Ensure this provides a good MIME type
                    )
                    if (intent.resolveActivity(applicationContext.packageManager) != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        applicationContext.startActivity(intent)
                    } else {
                        Timber.e(ERROR, "No app found to open this file type")
                    }
                } catch (e: ActivityNotFoundException) {
                    Timber.e(ERROR, "error is:- " + e.message)
                    // todo addd error for it
                } catch (e: ConstraintException) {
                    Timber.e(VIOLATION, "violation is" + e.message)
                    // todo send flow for it
                }
            }
        }
        // region open file

        fun openFileExternally(
            context: Context,
            file: File,
            mimeType: String,
        ): Intent {
            val uri =
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file,
                )
            val viewIntent =
                Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, mimeType)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            // Wrap in a chooser
            return Intent.createChooser(viewIntent, "Open ${file.name} with")
        }

//        fun openFileExternally(
//            context: Context,
//            file: File,
//            mimeType: String,
//        ): Intent {
//            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
//            return Intent(Intent.ACTION_VIEW).apply {
//                setDataAndType(uri, mimeType)
//                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            }
//        }

        //endregion

        //endregion
    }
