package com.example.dinnerapp.presentation.events.eventlisteners

import com.example.parrot.presentation.events.componentevents.HomeComponentEvents

interface HomeComponentEventListener {
    fun onEvent(event: HomeComponentEvents)
}
