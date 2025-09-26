package com.example.parrot.presentation.features.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.parrot.databinding.FragmentHomeBinding
import com.example.parrot.presentation.epoxy.controllers.HomeFragmentEpoxyController
import com.example.parrot.presentation.events.fragmentevents.HomeFragmentEvents
import com.example.parrot.presentation.fragments.BaseFragment
import com.example.parrot.utils.Constants.LogTags.EVENT
import com.example.parrot.utils.Constants.LogTags.VALUE
import com.example.parrot.utils.ListState
import com.example.parrot.utils.launchAndRepeatWithViewLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean)
    -> FragmentHomeBinding
        get() = FragmentHomeBinding::inflate

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var controller: HomeFragmentEpoxyController

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        // Setting up the controller here in order to listen to events through the viewmodel.
        controller = HomeFragmentEpoxyController(listener = viewModel)

        launchAndRepeatWithViewLifecycle {
            launch { setup() }
            launch { observeFilesUpdates() }
            launch { showFolders() }
        }
    }

    private fun setup(){
        binding.fileList.setController(controller)
    }

    // region observables .

    private suspend fun observeFilesUpdates() {
        viewModel.onFilesUpdateFlow.collect {
            Timber.tag(VALUE).d("collection success $it")
            controller.setData(it)
        }
    }

    //endregion

    // region Fragment Events
    private fun showFolders() {
        Timber.tag(EVENT).d("show files")
        viewModel.onEvent(HomeFragmentEvents.ShowFiles)
    }

    //endregion
}
