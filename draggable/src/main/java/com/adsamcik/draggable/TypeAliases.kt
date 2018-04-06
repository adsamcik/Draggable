package com.adsamcik.draggable

typealias EnterStateListener = (button: DraggableImageButton, state: DraggableImageButton.State, axis: DragAxis, hasChanged: Boolean) -> Unit
typealias ExitStateListener = (button: DraggableImageButton, state: DraggableImageButton.State) -> Unit
typealias PayloadListener<T> = (fragment: T) -> Unit