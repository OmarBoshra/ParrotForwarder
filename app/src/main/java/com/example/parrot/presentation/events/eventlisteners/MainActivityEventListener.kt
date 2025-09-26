package com.example.parrot.presentation.events.eventlisteners

import com.example.parrot.presentation.events.activityevents.MainActivityEvents
import kotlinx.coroutines.Job

interface MainActivityEventListener {
    fun onEvent(event: MainActivityEvents): Job
}
