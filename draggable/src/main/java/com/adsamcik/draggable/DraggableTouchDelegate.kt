package com.adsamcik.draggable

import android.graphics.Rect
import android.view.MotionEvent
import android.view.TouchDelegate
import android.view.View

internal class DraggableTouchDelegate(private val rect: Rect, private val view: View) : TouchDelegate(Rect(), view) {
    private val mOffsetRect = Rect()

    private var mDelegateTargeted = false

    fun updateOffsetRect() {
        //view.getHitRect(mOffsetRect)
        val tX = view.translationX.toInt()
        val tY = view.translationY.toInt()
        mOffsetRect.left = view.left + tX - rect.left
        mOffsetRect.top = view.top + tY - rect.top
        mOffsetRect.right = view.right + tX + rect.right
        mOffsetRect.bottom = view.bottom + tY + rect.bottom
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        updateOffsetRect()
        var sendToDelegate = false
        when (event.action) {
            MotionEvent.ACTION_UP -> {
                if (mDelegateTargeted) {
                    mDelegateTargeted = false
                    sendToDelegate = true
                }
            }
            MotionEvent.ACTION_DOWN -> {
                if (mOffsetRect.contains(event.x.toInt(), event.y.toInt())) {
                    mDelegateTargeted = true
                    sendToDelegate = true
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (mDelegateTargeted)
                    sendToDelegate = true
            }
            MotionEvent.ACTION_CANCEL -> {
                mDelegateTargeted = false
                sendToDelegate = true
            }
        }

        if (sendToDelegate)
            return view.dispatchTouchEvent(event)

        return false
    }

}