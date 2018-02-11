package com.adsamcik.draggable

import android.annotation.SuppressLint
import android.graphics.Point
import android.support.annotation.ColorInt
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import java.util.*


class DraggablePayload<T>(private val mActivity: FragmentActivity,
                          private val mClass: Class<T>,
                          private val mInitialTranslation: Point,
                          private val mParent: ViewGroup,
                          private val mAnchor: DragTargetAnchor,
                          mMarginDp: Int,
                          private val mWidth: Int = MATCH_PARENT,
                          private val mHeight: Int = MATCH_PARENT) where T : Fragment, T : IOnDemandView {
    private var mWrapper: FrameLayout? = null
    private var mMarginPx = Utility.dpToPx(mActivity, mMarginDp)
    private var fragment: IOnDemandView? = null

    private var mBackgroundColor = 0

    private var mInitialTranslationZ: Float = 0f
    private var mTargetTranslationZ: Float = 0f

    /**
     * Sets payloads background
     * Can be called even after view is visible
     *
     * @param color Color int as ARGB
     */
    fun setBackgroundColor(@ColorInt color: Int) {
        mWrapper?.setBackgroundColor(color)
        mBackgroundColor = color
    }

    /**
     * Sets translation z (elevation)
     * This sets both initial and target translations to given value
     *
     * @param translationZ Desired translation z
     */
    fun setTranslationZ(translationZ: Float) {
        mInitialTranslationZ = translationZ
        mTargetTranslationZ = translationZ
    }

    /**
     * Sets initial translation z (elevation)
     *
     * @param translationZ Desired translation z
     */
    fun setInitialTranslationZ(translationZ: Float) {
        mInitialTranslationZ = translationZ
    }

    /**
     * Sets target translation z (elevation)
     *
     * @param translationZ Desired translation z
     */
    fun setTargetTranslationZ(translationZ: Float) {
        mTargetTranslationZ = translationZ
    }

    /**
     * Initializes view
     * Does nothing if view is already created
     * It is called automatically when drag starts
     */
    @SuppressLint("ResourceType")
    fun initializeView() {
        if (mWrapper == null) {
            val cView = FrameLayout(mActivity)
            cView.id = Random().nextInt(Int.MAX_VALUE - 2) + 1
            cView.layoutParams = ViewGroup.LayoutParams(mWidth, mHeight)
            cView.setBackgroundColor(mBackgroundColor)
            //cView.translationZ = 1000f
            cView.translationX = mInitialTranslation.x.toFloat()
            cView.translationY = mInitialTranslation.y.toFloat()
            mParent.addView(cView)
            mWrapper = cView

            val newInst = mClass.newInstance()
            val ft = mActivity.supportFragmentManager.beginTransaction()
            ft.replace(cView.id, newInst as Fragment)
            ft.commit()

            fragment = newInst
        }
    }

    internal fun onDrag(percentage: Float) {
        if (mWrapper == null)
            throw IllegalStateException("mWrapper was not initialized")

        val wrapper = mWrapper!!

        val targetTranslation = Utility.calculateTargetTranslation(wrapper, mParent, mAnchor, mMarginPx)
        wrapper.translationX = mInitialTranslation.x.toFloat() + (targetTranslation.x - mInitialTranslation.x) * percentage
        wrapper.translationY = mInitialTranslation.y.toFloat() + (targetTranslation.y - mInitialTranslation.y) * percentage

        if (mInitialTranslationZ != mTargetTranslationZ)
            wrapper.translationZ = mInitialTranslationZ + (mTargetTranslationZ - mInitialTranslationZ) * percentage

    }

    /**
     * Called when there is permission response
     *
     * @param requestCode Request code of the permission
     * @param success True if all permissions succeeded
     */
    internal fun onPermissionResponse(requestCode: Int, success: Boolean) = fragment?.onPermissionResponse(requestCode, success)

    /**
     * Called when state change is finished
     *
     * @param entering True if view went to active state
     */
    internal fun onStateChange(entering: Boolean) {
        if (entering)
            fragment?.onEnter(mActivity)
        else
            fragment?.onLeave(mActivity)
    }
}