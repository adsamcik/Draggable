package com.adsamcik.draggable.delegates

import android.view.MotionEvent
import android.view.TouchDelegate
import android.view.View

class WrapperTouchDelegate(override val view: View, private val delegate: TouchDelegate) : AbstractTouchDelegate() {
    override fun onTouchEvent(event: MotionEvent): Boolean = delegate.onTouchEvent(event)
}