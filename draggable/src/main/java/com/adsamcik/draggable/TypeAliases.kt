package com.adsamcik.draggable

import android.support.v4.app.Fragment

typealias EnterStateListener = (button: DraggableImageButton, state: DraggableImageButton.State, axis: DragAxis) -> Unit
typealias ExitStateListener = (button: DraggableImageButton, state: DraggableImageButton.State) -> Unit
typealias PayloadListener = (button: Fragment) -> Unit