package com.example.parrot.presentation.events.activityevents

import com.example.parrot.utils.NavigationEvents

sealed class MainActivityEvents {
    class ToHome(val navigationEvent: NavigationEvents) :
        MainActivityEvents()
    class ToProcessing(val navigationEvent: NavigationEvents) :
        MainActivityEvents()
}
