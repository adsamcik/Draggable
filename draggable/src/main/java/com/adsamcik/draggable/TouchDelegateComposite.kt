package com.adsamcik.draggable

import android.graphics.Rect
import android.view.MotionEvent
import android.view.TouchDelegate
import android.view.View


internal class TouchDelegateComposite(view: View) : TouchDelegate(emptyRect, view) {
    private val delegates = ArrayList<TouchDelegate>()

    fun addDelegate(delegate: TouchDelegate) {
        delegates.add(delegate)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        delegates.forEach {
            if(it.onTouchEvent(event))
                return true
        }

        return false
    }

    companion object {
        val emptyRect = Rect()

        fun addTouchDelegateOn(view: View, delegate: TouchDelegate): TouchDelegateComposite {
            val composite = addTouchDelegateOn(view)
            composite.addDelegate(delegate)
            return composite
        }

        fun addTouchDelegateOn(view: View): TouchDelegateComposite {
            val delegate = view.touchDelegate

            if (delegate is TouchDelegateComposite)
                return delegate

            val composite = TouchDelegateComposite(view)
            if (delegate != null)
                composite.addDelegate(delegate)

            view.touchDelegate = composite
            return composite
        }
    }
}