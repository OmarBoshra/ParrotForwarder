package com.example.parrot.presentation.epoxy.models

import android.annotation.SuppressLint
import com.airbnb.epoxy.EpoxyModelClass
import com.example.parrot.R
import com.example.parrot.databinding.EmptyViewBinding
import com.example.parrot.utils.epoxy.BaseEpoxyModelWithViewHolder

@SuppressLint("NonConstantResourceId")
@EpoxyModelClass(layout = R.layout.empty_view)
abstract class EmptyModel :
    BaseEpoxyModelWithViewHolder<EmptyViewBinding>() {
    override fun EmptyViewBinding.bind() {
    }
}
