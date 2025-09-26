package com.example.parrot.presentation.epoxy.controllers

import com.airbnb.epoxy.TypedEpoxyController
import com.example.dinnerapp.presentation.events.eventlisteners.HomeComponentEventListener
import com.example.parrot.presentation.epoxy.models.empty
import com.example.parrot.presentation.epoxy.models.home
import com.example.parrot.presentation.models.FileItem
import com.example.parrot.utils.ItemState
import com.example.parrot.utils.ListState
import com.example.parrot.utils.ListState.Empty
import com.example.parrot.utils.ListState.Loading
import com.example.parrot.utils.ListState.Loaded
import com.example.parrot.utils.ListState.Error
import com.example.parrot.utils.Constants
import timber.log.Timber

class HomeFragmentEpoxyController(
    private val listener: HomeComponentEventListener? = null,
) : TypedEpoxyController<ListState<List<ItemState<FileItem?>?>?>?>() {
    override fun buildModels(state: ListState<List<ItemState<FileItem?>?>?>?) {
        when (state) {
            is Loading -> buildLoadingState(state)
            is Error -> buildErrorState(state)
            is Empty -> buildEmptyState(state)
            is Loaded -> buildLoadedState(state.itemState)
            null -> {} // no-op
        }
    }

    private fun buildEmptyState(currentState: Empty) {
        empty {
            id(currentState.message)
        }
    }

    private fun buildErrorState(errorState: Error) {
//        error {
//            id(errorState.message)
//            message(errorState.message)
//        }
    }

    private fun buildLoadingState(loadingState: Loading) {
//        loading {
//            id(errorState.message)
//            message(errorState.message)
//        }
    }

    private fun buildLoadedState(data: List<ItemState<FileItem?>?>?) {
        Timber.tag(Constants.LogTags.VALUE).d( "Sent data :- $data")
        data?.forEach {
            home {
                id(it?.id)
                itemState(it)
                homeComponentEventListener(this@HomeFragmentEpoxyController.listener)
            }
        }
    }

    //endregion
}
