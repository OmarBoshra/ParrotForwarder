package com.example.parrot.utils.epoxy

import androidx.viewbinding.ViewBinding
import com.airbnb.epoxy.EpoxyAttribute
import com.example.dinnerapp.presentation.events.eventlisteners.HomeComponentEventListener

abstract class BaseEpoxyModelWithViewHolder<in T : ViewBinding> :
    ViewBindingEpoxyModelWithHolder<T>() {
    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    var homeComponentEventListener: HomeComponentEventListener? = null
}
