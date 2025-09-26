package com.example.parrot.utils

sealed class ListState<out T> {
    data class Empty(val message: String) : ListState<Nothing>()
    data class Loading(val message: String) : ListState<Nothing>()
    data class Loaded<T>(val itemState: T) : ListState<T>()
    data class Error(val message: String = "An unexpected error occurred.") : ListState<Nothing>()
}
