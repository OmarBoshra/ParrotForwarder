package com.example.parrot.presentation.features.datasanitization

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parrot.di.DispatcherIO
import com.example.parrot.di.DispatcherMain
import com.example.parrot.opertions.FileOperations
import com.example.parrot.presentation.events.eventlisteners.ProcessingFragmentEventListener
import com.example.parrot.presentation.events.fragmentevents.ProcessingFragmentEvents
import com.example.parrot.utils.Constants.LogTags.EVENT
import com.example.parrot.utils.ConstraintException
import com.example.parrot.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProgressingViewModel
    @Inject
    constructor(
        private val fileOperations: FileOperations,
        @DispatcherIO private val ioDispatcher: CoroutineDispatcher,
        @DispatcherMain private val mainDispatcher: CoroutineDispatcher,
    ) : ViewModel(), ProcessingFragmentEventListener {
        private val _onSanitizationSuccess =
            MutableSharedFlow<Unit>()
        val onSanitizationSuccessFlow = _onSanitizationSuccess.asSharedFlow()
        private val _onSanitizationError =
            MutableSharedFlow<String>()
        val onSanitizationErrorFlow = _onSanitizationError.asSharedFlow()

        /**
         * Fragment events.
         */
        override fun onEvent(event: ProcessingFragmentEvents) {
            fragmentEventsHandler(event)
        }

        private fun fragmentEventsHandler(event: ProcessingFragmentEvents) {
            when (event) {
                is ProcessingFragmentEvents.ProcessFiles -> {
                    Timber.tag(EVENT).d("Received ProcessingFragmentEvents, initializing processing")
                    processFiles(event.shareIntent)
                }
            }
        }

        //endregion

        // region Sanitization Logic
        private fun processFiles(shareIntent: Intent) {
            viewModelScope.launch(ioDispatcher) {
                try {
                    fileOperations.runFileProcessing(shareIntent)
                    withContext(mainDispatcher) {
                        sendSanitizationResult(Result.Success(Unit))
                    }
                } catch (e: ConstraintException) {
                    withContext(mainDispatcher) {
                        sendSanitizationResult(Result.Violation(e.violation))
                    }
                } catch (e: Exception) {
                    withContext(mainDispatcher) {
                        e.message?.let { message -> sendSanitizationResult(Result.Error(message)) } ?: run {
                            sendSanitizationResult(Result.Error())
                        }
                    }
                }
            }
        }

        private suspend fun sendSanitizationResult(result: Result<Unit>) {
            when (result) {
                is Result.Success -> {
                    _onSanitizationSuccess.emit(Unit)
                }
                is Result.Error -> {
                    _onSanitizationError.emit(result.message)
                }
                is Result.Violation -> {
                    _onSanitizationError.emit(result.message)
                }
            }
        }
    }

// endregion
