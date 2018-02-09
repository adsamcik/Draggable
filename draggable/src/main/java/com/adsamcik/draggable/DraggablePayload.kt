package com.adsamcik.draggable

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Fragment
import android.graphics.Point
import android.support.annotation.ColorInt
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout

class DraggablePayload<T>(private val mActivity: Activity,
                          private val mClass: Class<T>,
                          private val mInitialTranslation: Point,
                          private val mParent: ViewGroup,
                          private val mAnchor: DragTargetAnchor,
                          mMarginDp: Int,
                          private val mWidth: Int = MATCH_PARENT,
                          private val mHeight: Int = MATCH_PARENT) where T : Fragment, T : IOnDemandView {
    private var mWrapper: FrameLayout? = null
    private var mMarginPx = Utility.dpToPx(mActivity, mMarginDp)

    private var mBackgroundColor = 0

    fun setBackgroundColor(@ColorInt color: Int) {
        mWrapper?.setBackgroundColor(color)
        mBackgroundColor = color
    }

    /**
     * Initializes view
     * Does nothing if view is already created
     */
    @SuppressLint("ResourceType")
    fun initializeView() {
        if (mWrapper == null) {
            val cView = FrameLayout(mActivity)
            cView.id = 1695841
            cView.layoutParams = ViewGroup.LayoutParams(mWidth, mHeight)
            cView.setBackgroundColor(mBackgroundColor)
            //cView.translationZ = 1000f
            cView.translationX = mInitialTranslation.x.toFloat()
            cView.translationY = mInitialTranslation.y.toFloat()
            mParent.addView(cView)
            mWrapper = cView

            val ft = mActivity.fragmentManager.beginTransaction()
            ft.replace(cView.id, mClass.newInstance() as Fragment)
            ft.commit()
        }
    }

    internal fun setTranslationZ(value: Float) {
        if (mWrapper == null)
            throw IllegalStateException("mWrapper was not initialized")

        mWrapper!!.translationZ = value
    }

    internal fun onDrag(percentage: Float) {
        if (mWrapper == null)
            throw IllegalStateException("mWrapper was not initialized")

        val targetTranslation = Utility.calculateTargetTranslation(mWrapper!!, mParent, mAnchor, mMarginPx)
        mWrapper!!.translationX = mInitialTranslation.x.toFloat() + (targetTranslation.x - mInitialTranslation.x) * percentage
        mWrapper!!.translationY = mInitialTranslation.y.toFloat() + (targetTranslation.y - mInitialTranslation.y) * percentage
    }
}