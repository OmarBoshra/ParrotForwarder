package com.example.parrot.presentation.features.datasanitization

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.parrot.R
import com.example.parrot.databinding.FragmentProcessingBinding
import com.example.parrot.presentation.events.fragmentevents.ProcessingFragmentEvents
import com.example.parrot.presentation.fragments.BaseFragment
import com.example.parrot.utils.Constants.LogTags.EVENT
import com.example.parrot.utils.Constants.LogTags.USER_ACTION
import com.example.parrot.utils.launchAndRepeatWithViewLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.getValue

@AndroidEntryPoint
class ProcessingFragment : BaseFragment<FragmentProcessingBinding>() {
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean)
    -> FragmentProcessingBinding
        get() = FragmentProcessingBinding::inflate

    private val arguments: ProcessingFragmentArgs by navArgs()
    private val viewModel: ProgressingViewModel by viewModels()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        // Setting up the controller here in order to listen to events through the viewmodel.
        launchAndRepeatWithViewLifecycle {
            launch { observeSanitizationFailure() }
            launch { observeSanitizationSuccess() }
            launch { sanitizeFiles() }
        }
    }

    // region observables .

    private suspend fun observeSanitizationFailure() {
        viewModel.onSanitizationErrorFlow.collect { message ->
            Timber.tag(EVENT).d("ProcessingFragment sanitization Failed , showing error dialog, error is: $message")
            MaterialAlertDialogBuilder(requireContext())
                .setIcon(R.drawable.ic_info)
                .setTitle(R.string.error_dialog_header)
                .setMessage(R.string.error_dialog_body)
                .setPositiveButton(R.string.error_dialog_action_retry) { _, _ ->
                    // Positive button clicked
                    Timber.tag(USER_ACTION).d("user retries the sanitization")
                    sanitizeFiles()
                }
                .setNegativeButton(R.string.error_dialog_action_cancel) { _, _ ->
                    Timber.tag(USER_ACTION).d("user cancelled the sanitization")
                    requireActivity().finish()
                }
                .show()
        }
    }

    private suspend fun observeSanitizationSuccess() {
        viewModel.onSanitizationSuccessFlow.collect {
            Timber.tag(EVENT).d("ProcessingFragment sanitization Succeeded , navigating toHomePage")
            // send data to the new fragment
            val action =
                ProcessingFragmentDirections.actionProgressingFragmentToHomeFragment()
            findNavController().navigate(
                action,
            )
        }
    }

    //endregion

    // region Fragment Events

    private fun sanitizeFiles() {
        Timber.tag(EVENT).d("ProcessingFragment initialized processing")
        viewModel.onEvent(ProcessingFragmentEvents.ProcessFiles(arguments.shareIntent))
    }

    //endregion
}
