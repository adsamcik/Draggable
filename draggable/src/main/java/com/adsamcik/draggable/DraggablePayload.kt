package com.adsamcik.draggable

import android.annotation.SuppressLint
import android.graphics.Point
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentTransaction.TRANSIT_NONE
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import java.util.*
import kotlin.concurrent.schedule
import kotlin.math.roundToInt


class DraggablePayload<T>(private val mActivity: FragmentActivity,
                          private val mClass: Class<T>,
                          private val mParent: ViewGroup,
                          private val mTargetView: View
) where T : Fragment, T : IOnDemandView {
    /**
     * Offset in pixels
     */
    var offsets = Offset(0)

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
    var width: Int = WRAP_CONTENT

    /**
     * Height of the view
     * does not update the views height after its creation
     */
    var height: Int = WRAP_CONTENT

    /**
     * Initial translation of the view
     */
    var initialTranslation = Point(0, 0)

    /**
     * Called after fragment is initialized
     */
    var onInitialized: PayloadListener<T>? = null

    /**
     * Called before fragment is destroyed
     * Fragment is not guaranteed to have a view
     */
    var onBeforeDestroyed: PayloadListener<T>? = null

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
     * Fragment tag used for finding existing fragment
     * It is nearly impossible for there to be 2 fragments with the same tag
     * Uses mClass to make sure it is always at least the same type and never causes cast crash
     */
    internal var mFragmentTag = "${Math.random()}Draggable${mClass.name}"

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
     * Sets offsets from dp to px
     */
    fun setOffsetsDp(offsets: Offset) {
        this.offsets.setWithDpAsPx(offsets)
    }

    private var targetOffset: Point = Point(0, 0)

    /**
     * Initializes view
     * Only cancels destroy timer if it was already initialized
     * It is called automatically when drag starts
     */
    @SuppressLint("ResourceType")
    fun initializeView() {
        if (wrapper == null) {
            val cView = createWrapper()
            if (mFragment == null) {
                val ft = mActivity.supportFragmentManager.beginTransaction()
                val newInst = mClass.newInstance()
                ft.replace(cView.id, newInst, mFragmentTag)

                if (onInitialized != null)
                    ft.runOnCommit {
                        onInitialized?.invoke(mFragment!!)
                    }
                ft.commitAllowingStateLoss()

                mFragment = newInst
            }
            wrapper = cView
        } else if (destroyTimerTask != null) {
            destroyTimerTask!!.cancel()
        }
    }

    /**
     * Creates wrapper frame layout so there is view to handle even when fragment has no view yet
     */
    private fun createWrapper(): FrameLayout {
        val cView = FrameLayout(mActivity)
        cView.id = View.generateViewId()
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
        return cView
    }

    internal fun restoreFragment(id: Int, tag: String) {
        //id is not reused because it might no longer be unique
        if (id != View.NO_ID)
            wrapper = createWrapper()
        this.mFragmentTag = tag
    }

    @Suppress("UNCHECKED_CAST")
    internal fun restoreFragment(bundle: Bundle) {
        val wrapper = wrapper
        if (wrapper != null) {
            val fragmentManager = mActivity.supportFragmentManager
            val fragment = fragmentManager.findFragmentByTag(mFragmentTag)
                    ?: fragmentManager.getFragment(bundle, mFragmentTag)
            mFragment = fragment as T

            //Problem with this is that top view loses it's width and height for some reason
            /*val view = fragment.view
            if (view != null) {
                (view.parent as ViewGroup?)?.removeView(view)
                wrapper.addView(view)
            } else {*/

            //Disadvantage of this is that view goes through this -> OnCreate, OnDestroy, OnCreate because of the remove and replace
            fragmentManager.beginTransaction().remove(fragment).commitNow()
            fragmentManager.beginTransaction().replace(wrapper.id, fragment).commitNow()
            //}

            onInitialized?.invoke(fragment)
        }
    }

    internal fun saveFragment(bundle: Bundle) {
        if (mFragment != null)
            mActivity.supportFragmentManager.putFragment(bundle, mFragmentTag, mFragment)
    }

    private fun calculateTargetTranslation(toView: View, offset: Point, offsets: Offset): Point {
        val wrapper = wrapper!!
        val location = wrapper.locationOnScreen()
        location[0] -= wrapper.translationX.roundToInt() - initialTranslation.x
        location[1] -= wrapper.translationY.roundToInt() - initialTranslation.y

        val targetOnScreen = toView.locationOnScreen()
        val targetX = (targetOnScreen[0] - location[0]) + offset.x
        val targetY = (targetOnScreen[1] - location[1]) + offset.y
        //Log.d("Draggable", "(${targetOnScreen[1]} - ${initialPosition.y}) + ${offset.y} = $targetY")
        return Point(targetX + offsets.horizontal, targetY + offsets.vertical)
    }

    internal fun onDrag(percentage: Float) {
        if (wrapper == null)
            initializeView()

        removeTimer()

        val wrapper = wrapper!!

        if (stickToTarget)
            moveWithTarget()
        else
            moveWithPercentage(percentage)

        //val targetOnScreen = Utility.getLocationOnScreen(mTargetView)
        //Log.d("Draggable", "Progress $percentage translation y ${wrapper.translationY} target view ${mTargetView.translationY} target on screen ${targetOnScreen[1]}")

        if (initialTranslationZ != targetTranslationZ)
            wrapper.translationZ = initialTranslationZ + (targetTranslationZ - initialTranslationZ) * percentage

        wrapper.invalidate()
    }

    private fun moveWithTarget() {
        val wrapper = wrapper!!
        val targetOnScreen = mTargetView.locationOnScreen()
        val parentOnScreen = mParent.locationOnScreen()
        targetOffset = anchor.calculateEdgeOffset(wrapper, mTargetView)

        wrapper.translationX = (targetOnScreen[0] - parentOnScreen[0] + targetOffset.x + offsets.horizontal).toFloat()
        wrapper.translationY = (targetOnScreen[1] - parentOnScreen[1] + targetOffset.y + offsets.vertical).toFloat()
    }

    private fun moveWithPercentage(percentage: Float) {
        val wrapper = wrapper!!
        targetOffset = anchor.calculateEdgeOffsetWithPadding(wrapper, mTargetView)
        val targetTranslation = calculateTargetTranslation(mTargetView, targetOffset, offsets)
        wrapper.translationX = initialTranslation.x.toFloat() + targetTranslation.x * percentage
        wrapper.translationY = initialTranslation.y.toFloat() + targetTranslation.y * percentage
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
        ft.commitAllowingStateLoss()

        mFragment = null
        val wrapper = wrapper
        this.wrapper = null
        mParent.post {
            mParent.removeView(wrapper)
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