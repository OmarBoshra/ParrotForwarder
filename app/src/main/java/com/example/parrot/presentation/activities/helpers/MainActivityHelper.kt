package com.example.parrot.presentation.activities.helpers

import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import com.example.parrot.R
import com.example.parrot.utils.Constants.LogTags.EVENT
import com.example.parrot.utils.NavigationEvents
import timber.log.Timber

object MainActivityHelper {
    fun navigationRouter(
        navHostFragment: NavHostFragment,
        navigationEvent: NavigationEvents?,
    ) {
        when (navigationEvent) {
            is NavigationEvents.ToHome -> {
                Timber.tag(EVENT).d("collected MainActivity Navigation toHome")
                val navController = navHostFragment.navController
                navController
                    .setGraph(R.navigation.nav_graph_through_main)
            }
            is NavigationEvents.ToProcessing -> {
                Timber.tag(EVENT).d("collected MainActivity Navigation ToProcessing")
                // Send the arguments.
                val bundle =
                    Bundle().apply {
                        putParcelable("shareIntent", navigationEvent.sharedIntent) // or putString, putUri, etc.
                    }
                val navController = navHostFragment.navController
                navController
                    .setGraph(R.navigation.nav_graph_through_share, bundle)
            }
            else -> {
                Timber.tag(EVENT).d("collected unHandled MainActivity Navigation event")
            }
        }
    }
}
