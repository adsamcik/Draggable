package com.adsamcik.draggable

import android.annotation.SuppressLint
import android.graphics.Point
import android.graphics.PointF
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE
import android.support.v4.app.FragmentTransaction.TRANSIT_NONE
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import java.util.*
import kotlin.concurrent.schedule
import kotlin.math.sign


class DraggablePayload<T>(private val mActivity: FragmentActivity,
                          private val mClass: Class<T>,
                          private val mParent: ViewGroup,
                          private val mTargetView: View
) where T : Fragment, T : IOnDemandView {
    /**
     * Margin converted to pixels
     */
    var margin = Margins(0)

    /**
     * Anchor to the target view
     */
    var anchor: DragTargetAnchor = DragTargetAnchor.LeftTop

    /**
     * If true, payload behaves as if it was in target position all the time
     * TranslationZ remains unaffected by this option
     */
    var stickToTarget = false

    /**
     * Width of the view
     * does not update the views width after its creation
     */
    var width: Int = MATCH_PARENT

    /**
     * Height of the view
     * does not update the views height after its creation
     */
    var height: Int = MATCH_PARENT

    /**
     * Initial translation of the view
     */
    var initialTranslation = Point(0, 0)

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
     * Determines time after which payloads are destroyed in initial state in milliseconds
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
     * Created fragment
     */
    private var mFragment: T? = null

    /**
     * Timer task that is used to trigger destroy after timeout
     */
    private var destroyTimerTask: TimerTask? = null

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
     * Sets margins from dp to px
     */
    fun setMarginsDp(margins: Margins) {
        margin.setFromDpToPx(mActivity, margins)
    }

    private var initialOnScreen: Point = Point(0, 0)
    private var targetOffset: Point = Point(0, 0)

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
            cView.layoutParams = ViewGroup.LayoutParams(width, height)
            cView.setBackgroundColor(backgroundColor)
            cView.translationZ = initialTranslationZ
            mParent.addView(cView)
            wrapper = cView

            if (stickToTarget) {
                onDrag(0f)
            } else {
                cView.translationX = initialTranslation.x.toFloat()
                cView.translationY = initialTranslation.y.toFloat()
            }

            val newInst = mClass.newInstance()
            val ft = mActivity.supportFragmentManager.beginTransaction()
            ft.replace(cView.id, newInst as Fragment)
            ft.setTransition(TRANSIT_FRAGMENT_FADE)
            if (onInitialized != null)
                ft.runOnCommit {
                    val thisOnScreen = Utility.getLocationOnScreen(wrapper!!)
                    initialOnScreen.x = thisOnScreen[0]
                    initialOnScreen.y = thisOnScreen[1]

                    onInitialized?.invoke(newInst)
                }
            ft.commit()

            mFragment = newInst

            val thisOnScreen = Utility.getLocationOnScreen(wrapper!!)
            initialOnScreen.x = thisOnScreen[0]
            initialOnScreen.y = thisOnScreen[1]
        } else if (destroyTimerTask != null) {
            destroyTimerTask!!.cancel()
        }
    }

    private fun calculateTargetTranslation(initialPosition: Point, toView: View, offset: Point, marginPx: Int): PointF {
        val targetOnScreen = Utility.getLocationOnScreen(toView)
        val targetX = (targetOnScreen[0] - initialPosition.x) + offset.x
        val targetY = (targetOnScreen[1] - initialPosition.y) + offset.y
        Log.d("Draggable", "(${targetOnScreen[1]} - ${initialPosition.y}) + ${offset.y} = $targetY")
        return PointF(targetX - targetX.sign * marginPx.toFloat(), targetY - targetY.sign * marginPx.toFloat())
    }

    internal fun onDrag(percentage: Float) {
        if (wrapper == null)
            initializeView()

        removeTimer()

        val wrapper = wrapper!!
        targetOffset = anchor.calculateEdgeOffset(wrapper, mTargetView)

        if (stickToTarget) {
            val targetOnScreen = Utility.getLocationOnScreen(mTargetView)
            val parentOnScreen = Utility.getLocationOnScreen(mParent)

            wrapper.translationX = (targetOnScreen[0] - parentOnScreen[0] + targetOffset.x).toFloat()
            wrapper.translationY = (targetOnScreen[1] - parentOnScreen[1] + targetOffset.y).toFloat()
        } else {
            val targetTranslation = calculateTargetTranslation(initialOnScreen, mTargetView, targetOffset, margin)
            wrapper.translationX = initialTranslation.x.toFloat() + targetTranslation.x * percentage
            wrapper.translationY = initialTranslation.y.toFloat() + targetTranslation.y * percentage
        }

        //val targetOnScreen = Utility.getLocationOnScreen(mTargetView)
        //Log.d("Draggable", "Progress $percentage translation y ${wrapper.translationY} target view ${mTargetView.translationY} target on screen ${targetOnScreen[1]}")

        if (initialTranslationZ != targetTranslationZ)
            wrapper.translationZ = initialTranslationZ + (targetTranslationZ - initialTranslationZ) * percentage

        wrapper.invalidate()
    }

    internal fun onInitialPosition() {
        if (destroyPayloadAfter > IMMEDIATELY) {
            destroyTimerTask = Timer("Destroy", true).schedule(destroyPayloadAfter) { destroyFragment() }
        } else if (destroyPayloadAfter == IMMEDIATELY) {
            destroyFragment()
        }
    }

    private fun destroyFragment() {
        if (mFragment == null)
            throw RuntimeException("Fragment is already null")
        else if (mFragment!!.isStateSaved)
            return

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
        destroyTimerTask?.cancel()
        destroyTimerTask = null
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