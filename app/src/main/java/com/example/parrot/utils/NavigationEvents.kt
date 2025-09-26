package com.example.parrot.utils

import android.content.Intent

sealed class NavigationEvents {
    data object ToHome : NavigationEvents()
    data class ToProcessing(val sharedIntent: Intent) : NavigationEvents()

    data class ToDrinkCategories(val selectedMealCategoryName: String) : NavigationEvents()
}
