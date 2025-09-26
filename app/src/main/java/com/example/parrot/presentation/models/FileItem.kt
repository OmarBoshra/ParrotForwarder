package com.example.parrot.presentation.models

import android.net.Uri
import android.os.Parcelable
import androidx.annotation.DrawableRes
import com.example.parrot.R
import kotlinx.parcelize.Parcelize
import java.io.File

@Parcelize
data class FileItem(
    val name: String? = "",
    var isChecked: Boolean = false,
    var file: File? = null,
    @DrawableRes var iconResId: Int = R.drawable.ic_folder
) : Parcelable
