package com.example.parrot.utils


sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Violation<out T>(val message: String) : Result<T>()
    data class Error<out T>(val message: String = "An unexpected error occurred.") : Result<T>()
}
