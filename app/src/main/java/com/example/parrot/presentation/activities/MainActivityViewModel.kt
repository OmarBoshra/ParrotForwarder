package com.example.parrot.presentation.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parrot.presentation.events.activityevents.MainActivityEvents
import com.example.parrot.presentation.events.eventlisteners.MainActivityEventListener
import com.example.parrot.utils.Constants.LogTags.EVENT
import com.example.parrot.utils.NavigationEvents
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class MainActivityViewModel : ViewModel(), MainActivityEventListener {
    private val _navigationEvents = MutableSharedFlow<NavigationEvents?>()
    val navigationEvents = _navigationEvents.asSharedFlow()

    /**
     * Activity events.
     */
    override fun onEvent(event: MainActivityEvents) =
        viewModelScope.launch {
            when (event) {
                is MainActivityEvents.ToHome -> {
                    navigationEventHandler(event.navigationEvent)
                }
                is MainActivityEvents.ToProcessing -> {
                    navigationEventHandler(event.navigationEvent)
                }
            }
        }

    // region event handlers.
    private suspend fun navigationEventHandler(event: NavigationEvents) {
        Timber.Forest.tag(EVENT).d("MainActivity Navigation event")
        when (event) {
            is NavigationEvents.ToHome -> {
                Timber.Forest.tag(EVENT).d("MainActivity Navigation ToHome event")
                _navigationEvents.emit(NavigationEvents.ToHome)
            }
            is NavigationEvents.ToProcessing -> {
                Timber.Forest.tag(EVENT).d("MainActivity Navigation ToProcessing event")
                _navigationEvents.emit(NavigationEvents.ToProcessing(event.sharedIntent))
            }
            else -> {
                Timber.Forest.tag(EVENT).d("unHandled MainActivity navigation event")
            }
        }
    }

    //endregion
}