package com.example.parrot.presentation.events.fragmentevents

import android.content.Intent

sealed class ProcessingFragmentEvents {
    data class ProcessFiles(val shareIntent: Intent) : ProcessingFragmentEvents()
}
