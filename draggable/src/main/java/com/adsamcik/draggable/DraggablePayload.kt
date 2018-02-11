package com.adsamcik.draggable

import android.annotation.SuppressLint
import android.graphics.Point
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
    /**
     * Wrapper of payloads fragment
     */
    var wrapper: FrameLayout? = null
        private set

    private var fragment: IOnDemandView? = null
    private var mMargin = Utility.dpToPx(mActivity, mMarginDp)

    /**
     * Background color
     */
    var mBackgroundColor = 0
        set(value) {
            wrapper?.setBackgroundColor(value)
            field = value
        }

    /**
     * Initial translation z
     *
     * Does not update current translation z because payload does not store state information
     */
    var mInitialTranslationZ: Float = 0f

    /**
     *  Target translation z
     *
     *  Does not update current translation z because payload does not store state information
     */
    var mTargetTranslationZ: Float = 0f

    /**
     * Sets translation z (elevation)
     * This sets both initial and target translations to given value
     * Updates current translation z
     *
     * @param translationZ Desired translation z
     */
    fun setTranslationZ(translationZ: Float) {
        mInitialTranslationZ = translationZ
        mTargetTranslationZ = translationZ
        wrapper?.translationZ = translationZ
    }

    /**
     * Initializes view
     * Does nothing if view is already created
     * It is called automatically when drag starts
     */
    @SuppressLint("ResourceType")
    fun initializeView() {
        if (wrapper == null) {
            val cView = FrameLayout(mActivity)
            cView.id = Random().nextInt(Int.MAX_VALUE - 2) + 1
            cView.layoutParams = ViewGroup.LayoutParams(mWidth, mHeight)
            cView.setBackgroundColor(mBackgroundColor)
            cView.translationZ = mInitialTranslationZ
            cView.translationX = mInitialTranslation.x.toFloat()
            cView.translationY = mInitialTranslation.y.toFloat()
            mParent.addView(cView)
            wrapper = cView

            val newInst = mClass.newInstance()
            val ft = mActivity.supportFragmentManager.beginTransaction()
            ft.replace(cView.id, newInst as Fragment)
            ft.commit()

            fragment = newInst
        }
    }

    internal fun onDrag(percentage: Float) {
        if (wrapper == null)
            throw IllegalStateException("mWrapper was not initialized")

        val wrapper = wrapper!!

        val targetTranslation = Utility.calculateTargetTranslation(wrapper, mParent, mAnchor, mMargin)
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