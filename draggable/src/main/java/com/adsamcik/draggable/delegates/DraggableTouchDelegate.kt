package com.adsamcik.draggable.delegates

import android.graphics.Rect
import android.util.Log
import android.view.MotionEvent
import android.view.View

internal class DraggableTouchDelegate(private val mOffsetRect: Rect, override val view: View) : AbstractTouchDelegate() {
    private val mHitRect = Rect()

    private var mDelegateTargeted = false

    private fun updateHitRect() {
        //view.getHitRect(mOffsetRect)
        val tX = view.x.toInt()
        val tY = view.y.toInt()
        mHitRect.left = tX - mOffsetRect.left
        mHitRect.top = tY - mOffsetRect.top
        mHitRect.right = tX + view.width + mOffsetRect.right
        mHitRect.bottom = tY + view.height + mOffsetRect.bottom
    }

    fun updateOffsets(left: Int, top: Int, right: Int, bottom: Int) {
        mOffsetRect.left = left
        mOffsetRect.top = top
        mOffsetRect.right = right
        mOffsetRect.bottom = bottom
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        updateHitRect()
        var sendToDelegate = false
        Log.d("TAG", mHitRect.flattenToString())
        when (event.action) {
            MotionEvent.ACTION_UP -> {
                if (mDelegateTargeted) {
                    mDelegateTargeted = false
                    sendToDelegate = true
                }
            }
            MotionEvent.ACTION_DOWN -> {
                if (mHitRect.contains(event.x.toInt(), event.y.toInt())) {
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