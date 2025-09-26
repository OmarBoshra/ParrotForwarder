package com.example.parrot.presentation.activities

import android.content.Intent
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.example.parrot.R
import com.example.parrot.databinding.ActivityMainBinding
import com.example.parrot.presentation.activities.helpers.MainActivityHelper
import com.example.parrot.presentation.events.activityevents.MainActivityEvents
import com.example.parrot.utils.Constants.LogTags.ACTIVITY_LIFECYCLE
import com.example.parrot.utils.Constants.LogTags.EVENT
import com.example.parrot.utils.NavigationEvents
import com.example.parrot.utils.launchAndRepeatWithViewLifecycle
import com.example.parrot.utils.storage.StoragePermissionHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {
    override val bindingInflater: (LayoutInflater) -> ActivityMainBinding
        get() = ActivityMainBinding::inflate

    private val viewModel: MainActivityViewModel by viewModels()

    @Inject
    lateinit var storageHandler: StoragePermissionHandler

    // region activity setups
    override fun setup() {
        // Setup storage permissions must be outside before lifecycle registerForActivityResult.
        Timber.tag(ACTIVITY_LIFECYCLE).d("$this in setup")
        launchAndRepeatWithViewLifecycle {
            launch { router() }
            launch { observeNavigationEvents() }
        }
    }

    //endregion.

    // region activity overrides

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.let {
            if (intent.action == Intent.ACTION_SEND || intent.action == Intent.ACTION_SEND_MULTIPLE) {
                sendShareIntent(intent)
            }
        }
    }

    //endregion

    // region activity events .

    private fun router() {
        if (intent?.action == Intent.ACTION_SEND || intent?.action == Intent.ACTION_SEND_MULTIPLE) {
            sendShareIntent(intent)
        } else {
            // Normal app startup flow
            toHomePage()
        }
    }

    private fun toHomePage() {
        viewModel.onEvent(MainActivityEvents.ToHome(NavigationEvents.ToHome))
        Timber.tag(EVENT).d("$this Navigating to HomePage")
    }

    private fun sendShareIntent(shareIntent: Intent) {
        Timber.tag(ACTIVITY_LIFECYCLE).d("$this received intent, Navigating to Processing")
        viewModel.onEvent(MainActivityEvents.ToProcessing(NavigationEvents.ToProcessing(shareIntent)))
    }

    //endregion

    // region activity observers .

    private suspend fun observeNavigationEvents() {
        viewModel.navigationEvents.collect { navigationEvent ->
            Timber.tag(EVENT).d("collected $this Navigation event")
            navigationEvent
                ?.let {
                    val navHostFragment =
                        supportFragmentManager.findFragmentById(R.id.main_content) as NavHostFragment
                    MainActivityHelper.navigationRouter(navHostFragment, navigationEvent)
                } ?: run {
                Timber.tag(EVENT).d("collected MainActivity NULL Navigation event")
            }
        }
    }

    //endregion
}
