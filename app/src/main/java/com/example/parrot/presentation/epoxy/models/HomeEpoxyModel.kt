package com.example.parrot.presentation.epoxy.models

import android.annotation.SuppressLint
import android.widget.ImageView
import android.widget.LinearLayout
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.example.parrot.R
import com.example.parrot.databinding.ListItemFileBinding
import com.example.parrot.presentation.events.componentevents.HomeComponentEvents
import com.example.parrot.presentation.models.FileItem
import com.example.parrot.utils.Constants.LogTags.OPERATION
import com.example.parrot.utils.Constants.LogTags.VALUE
import com.example.parrot.utils.ItemState
import com.example.parrot.utils.epoxy.BaseEpoxyModelWithViewHolder
import com.google.android.material.textview.MaterialTextView
import timber.log.Timber

@SuppressLint("NonConstantResourceId")
@EpoxyModelClass(layout = R.layout.list_item_file)
abstract class HomeEpoxyModel :
    BaseEpoxyModelWithViewHolder<ListItemFileBinding>() {
    @EpoxyAttribute
    var itemState: ItemState<FileItem?>? = null

    // onBind
    override fun ListItemFileBinding.bind() {
        val fileItem =
            updateBasedOnItemState(
                itemState,
                fileName,
                fileIcon,
            )
        Timber.tag(VALUE).d( "Updated file item $fileItem")
        setupListeners(root, fileItem)
    }

    private fun updateBasedOnItemState(
        itemState: ItemState<FileItem?>?,
        fileName: MaterialTextView,
        fileIcon: ImageView,
    ): FileItem? {
        val fileItem =
            when (itemState) {
                is ItemState.ItemNotSelected -> {
                    Timber.tag(OPERATION).d( "Load non selected Item")

                    // Update text.
                    fileName.text =
                        itemState.data?.name
                    // Update icon.
                    itemState.data?.iconResId?.let { iconResId ->
                        fileIcon.setImageResource(iconResId)
                    }
                    // return fileitem for the listeners
                    itemState.data
                }

                is ItemState.ItemSelected -> {
                    Timber.tag(OPERATION).d( "to select item")
                    // todo different selection ui look
                    // return fileitem for the listeners
                    itemState.data
                }

                null -> {
                    return null
                }
            }

        return fileItem
    }

    private fun setupListeners(
        root: LinearLayout,
        fileItem: FileItem?,
    ) {
        root.setOnLongClickListener {
            fileItem?.let { fileItem ->
                fileItem.file?.let { file ->
                    homeComponentEventListener?.onEvent(
                        HomeComponentEvents.OnFileSelected(
                            file,
                        ),
                    )
                }
            }
            true
        }

        root.setOnClickListener {
            // Clicking on the file would imply open it , if its a folder then we reveal its contents
            fileItem?.let { fileItem ->
                fileItem.file?.let { file ->
                    homeComponentEventListener?.onEvent(
                        HomeComponentEvents.OnFileClicked(
                            file,
                        ),
                    )
                }
            }
        }
    }
}
