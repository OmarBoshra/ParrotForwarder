package com.example.parrot.presentation.events.componentevents

import java.io.File

sealed class HomeComponentEvents {
    class OnFileClicked(val file: File) : HomeComponentEvents()
    class OnFileSelected(val file: File) : HomeComponentEvents()
}
