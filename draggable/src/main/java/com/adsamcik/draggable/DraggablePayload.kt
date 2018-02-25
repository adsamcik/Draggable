package com.adsamcik.draggable

import android.annotation.SuppressLint
import android.graphics.Point
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE
import android.support.v4.app.FragmentTransaction.TRANSIT_NONE
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import java.util.*
import kotlin.concurrent.schedule


class DraggablePayload<T>(private val mActivity: FragmentActivity,
                          private val mClass: Class<T>,
                          private val mInitialTranslation: Point,
                          private val mParent: ViewGroup,
                          private val mAnchor: DragTargetAnchor,
                          mMarginDp: Int,
                          private val mWidth: Int = MATCH_PARENT,
                          private val mHeight: Int = MATCH_PARENT) where T : Fragment, T : IOnDemandView {
    private var mFragment: T? = null
    private var mMargin = Utility.dpToPx(mActivity, mMarginDp)
    private var timerTask: TimerTask? = null

    /**
     * Called after fragment is initialized
     */
    var onInitialized: PayloadListener? = null

    /**
     * Called before fragment is destroyed
     * Fragment is not guaranteed to have a view
     */
    var onBeforeDestroyed: PayloadListener? = null

    /**
     * Wrapper of payloads fragment
     */
    var wrapper: FrameLayout? = null
        private set

    /**
     * Background color
     */
    var backgroundColor = 0
        set(value) {
            wrapper?.setBackgroundColor(value)
            field = value
        }

    /**
     * Determines time after which payloads are destroyed in initial state in miliseconds
     *
     * Negative numbers serve as infinite
     */
    var destroyPayloadAfter: Long = -1

    /**
     * Initial translation z
     *
     * Does not update current translation z because payload does not store state information
     */
    var initialTranslationZ: Float = 0f

    /**
     *  Target translation z
     *
     *  Does not update current translation z because payload does not store state information
     */
    var targetTranslationZ: Float = 0f

    /**
     * Sets translation z (elevation)
     * This sets both initial and target translations to given value
     * Updates current translation z
     *
     * @param translationZ Desired translation z
     */
    fun setTranslationZ(translationZ: Float) {
        initialTranslationZ = translationZ
        targetTranslationZ = translationZ
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
            cView.setBackgroundColor(backgroundColor)
            cView.translationZ = initialTranslationZ
            cView.translationX = mInitialTranslation.x.toFloat()
            cView.translationY = mInitialTranslation.y.toFloat()
            mParent.addView(cView)
            wrapper = cView

            val newInst = mClass.newInstance()
            val ft = mActivity.supportFragmentManager.beginTransaction()
            ft.replace(cView.id, newInst as Fragment)
            ft.setTransition(TRANSIT_FRAGMENT_FADE)
            if (onInitialized != null)
                ft.runOnCommit { onInitialized?.invoke(newInst) }
            ft.commit()

            mFragment = newInst
        } else if (timerTask != null) {
            timerTask!!.cancel()
        }
    }

    internal fun onDrag(percentage: Float) {
        if (wrapper == null)
            throw IllegalStateException("mWrapper was not initialized")

        removeTimer()

        val wrapper = wrapper!!

        val targetTranslation = Utility.calculateTargetTranslation(wrapper, mParent, mAnchor, mMargin)
        wrapper.translationX = mInitialTranslation.x.toFloat() + (targetTranslation.x - mInitialTranslation.x) * percentage
        wrapper.translationY = mInitialTranslation.y.toFloat() + (targetTranslation.y - mInitialTranslation.y) * percentage

        if (initialTranslationZ != targetTranslationZ)
            wrapper.translationZ = initialTranslationZ + (targetTranslationZ - initialTranslationZ) * percentage

    }

    internal fun onInitialPosition() {
        if (destroyPayloadAfter > IMMEDIATELY) {
            timerTask = Timer("Destroy", true).schedule(destroyPayloadAfter) { destroyFragment() }
        } else if (destroyPayloadAfter == IMMEDIATELY) {
            destroyFragment()
        }
    }

    private fun destroyFragment() {
        if (mFragment == null)
            throw RuntimeException("Fragment is already null")

        onBeforeDestroyed?.invoke(mFragment!!)

        val ft = mActivity.supportFragmentManager.beginTransaction()
        ft.remove(mFragment)
        ft.setTransition(TRANSIT_NONE)
        ft.commit()

        mFragment = null
        launch(UI) {
            mParent.removeView(wrapper)
            wrapper = null
        }

    }

    private fun removeTimer() {
        timerTask?.cancel()
        timerTask = null
    }

    /**
     * Called when payload should clean everything
     */
    fun cleanup() {
        removeTimer()
        destroyFragment()
    }

    /**
     * Called when there is permission response
     *
     * @param requestCode Request code of the permission
     * @param success True if all permissions succeeded
     */
    internal fun onPermissionResponse(requestCode: Int, success: Boolean) = mFragment?.onPermissionResponse(requestCode, success)

    /**
     * Called when state change is finished
     *
     * @param state Current button state
     */
    internal fun onStateChange(state: DraggableImageButton.State) {
        when (state) {
            DraggableImageButton.State.TARGET -> mFragment?.onEnter(mActivity)
            DraggableImageButton.State.INITIAL -> mFragment?.onLeave(mActivity)
        }
    }

    companion object {
        const val NEVER = -1L
        const val IMMEDIATELY = 0L
    }
}