package com.nate.app.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DetailsRef(
    val id: Int,
    val mediaType: String,
) : Parcelable
