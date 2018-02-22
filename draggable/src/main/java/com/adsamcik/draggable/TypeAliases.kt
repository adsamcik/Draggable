package com.adsamcik.draggable

import android.support.v4.app.Fragment

typealias StateListener = (button: DraggableImageButton, state: DraggableImageButton.State) -> Unit
typealias PayloadListener = (button: Fragment) -> Unit