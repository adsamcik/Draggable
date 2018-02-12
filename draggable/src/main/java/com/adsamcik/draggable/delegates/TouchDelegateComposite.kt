package com.adsamcik.draggable.delegates

import android.graphics.Rect
import android.view.MotionEvent
import android.view.TouchDelegate
import android.view.View


internal class TouchDelegateComposite(view: View) : TouchDelegate(emptyRect, view) {
    private val delegates = ArrayList<AbstractTouchDelegate>()
    val count: Int get() = delegates.size

    fun addDelegate(delegate: AbstractTouchDelegate) {
        delegates.add(delegate)
    }

    fun removeDelegate(delegate: AbstractTouchDelegate) {
        delegates.remove(delegate)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        delegates.sortByDescending { it.view.translationZ}
        delegates.forEach {
            event.setLocation(x, y)
            if (it.onTouchEvent(event))
                return true
        }

        return false
    }

    companion object {
        val emptyRect = Rect()

        fun addTouchDelegateOn(view: View, delegate: AbstractTouchDelegate): TouchDelegateComposite {
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
                composite.addDelegate(WrapperTouchDelegate(view,    delegate))

            view.touchDelegate = composite
            return composite
        }
    }
}