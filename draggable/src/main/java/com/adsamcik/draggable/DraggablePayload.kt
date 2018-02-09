package com.adsamcik.draggable

import android.content.Context
import android.graphics.Color
import android.support.v4.app.Fragment
import android.view.ViewGroup
import android.widget.FrameLayout

class DraggablePayload<T>(private val context: Context, val mClass: Class<T>, val mInitialTranslation: Int, val mParent: ViewGroup, val mAnchor: DragTargetAnchor, val mMarginDp: Int) where T : Fragment, T : IOnDemandView {
    var mWrapper: FrameLayout? = null


    private fun initializeView(): FrameLayout {
        return if (mWrapper == null) {
            val cView = FrameLayout(context)
            cView.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            cView.setBackgroundColor(Color.parseColor("#aa0000ff"))
            cView.translationZ = 1000f
            cView.translationX = -mParent.width.toFloat()
            mParent.addView(cView)
            mWrapper = cView
            cView
        } else
            mWrapper!!
    }

    fun onHorizontalDrag(percentage: Float) {

    }

    fun onVerticalDrag(percentage: Float) {

    }
}