package com.example.parrot.presentation.events.eventlisteners
import com.example.parrot.presentation.events.fragmentevents.ProcessingFragmentEvents

interface ProcessingFragmentEventListener {
    fun onEvent(event: ProcessingFragmentEvents)
}
