package com.adsamcik.draggable.delegates

import android.view.MotionEvent
import android.view.View

abstract class AbstractTouchDelegate {
    abstract val view: View
    abstract fun onTouchEvent(event: MotionEvent): Boolean
}