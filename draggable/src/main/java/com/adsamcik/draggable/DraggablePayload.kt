package com.adsamcik.draggable

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.support.v4.app.Fragment
import android.view.ViewGroup
import android.widget.FrameLayout

class DraggablePayload<T>(private val context: Context,
                          private val mClass: Class<T>,
                          private val mInitialTranslation: Point,
                          private val mParent: ViewGroup,
                          private val mAnchor: DragTargetAnchor,
                          mMarginDp: Int) where T : Fragment, T : IOnDemandView {
    private var mWrapper: FrameLayout? = null
    private var mMarginPx = Utility.dpToPx(context, mMarginDp)


    internal fun initializeView() {
        if (mWrapper == null) {
            val cView = FrameLayout(context)
            cView.layoutParams = FrameLayout.LayoutParams(Utility.dpToPx(context, 64), Utility.dpToPx(context, 64))
            cView.setBackgroundColor(Color.parseColor("#aa0000ff"))
            cView.translationZ = 1000f
            cView.translationX = mInitialTranslation.x.toFloat()
            cView.translationY = mInitialTranslation.y.toFloat()
            mParent.addView(cView)
            mWrapper = cView
        }
    }

    fun onDrag(percentage: Float) {
        if (mWrapper == null)
            throw IllegalStateException("mWrapper was not initialized")

        val targetTranslation = Utility.calculateTargetTranslation(mWrapper!!, mParent, mAnchor, mMarginPx)
        mWrapper!!.translationX = mInitialTranslation.x.toFloat() + targetTranslation.x * percentage
        mWrapper!!.translationY = mInitialTranslation.y.toFloat() + targetTranslation.y * percentage
    }
}