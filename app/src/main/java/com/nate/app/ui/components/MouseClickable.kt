package com.nate.app.ui.components

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter

@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.mouseClickable(onClick: () -> Unit): Modifier =
    pointerInteropFilter { event ->
        when (event.actionMasked) {
            android.view.MotionEvent.ACTION_UP -> {
                onClick()
                true
            }
            else -> false
        }
    }
