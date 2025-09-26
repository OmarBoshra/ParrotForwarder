package com.example.parrot.presentation.events.eventlisteners
import com.example.parrot.presentation.events.fragmentevents.HomeFragmentEvents
import com.example.parrot.presentation.events.fragmentevents.ProcessingFragmentEvents

interface HomeFragmentEventListener {
    fun onEvent(event: HomeFragmentEvents)
}
