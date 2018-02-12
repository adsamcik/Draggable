package com.adsamcik.draggable

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PointF
import android.graphics.Rect
import android.support.v7.widget.AppCompatImageButton
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator

typealias StateListener = (button: DraggableImageButton) -> Unit

class DraggableImageButton : AppCompatImageButton {
    /**
     * Deadzone acts as an area in which every movement is still recognized as click
     * Default value is 16 dp
     */
    var deadZone = Utility.dpToPx(context, 16)
        set(value) {
            field = Utility.dpToPx(context, value)
        }

    /**
     * Axis along which object can be dragged
     * Note: Axis XY might not work yet
     */
    var dragAxis = DragAxis.None


    /**
     * Translation Z in the default state
     */
    var defaultTranslationZ = translationZ

    /**
     * Translation Z in the target state
     */
    var targetTranslationZ = translationZ

    /**
     * Target view which is used for target
     */
    var targetView: View? = null

    /**
     * Anchor sets to which side of the [targetView] should the button
     * attach in target position
     */
    var anchor = DragTargetAnchor.TopLeft

    /**
     * Margin in density independent pixels
     */
    var marginDp = 0

    //Listeners
    var onEnterInitialStateListener: StateListener? = null
    var onEnterTargetStateListener: StateListener? = null

    private var mCurrentState = false
    private val mPayloads = ArrayList<DraggablePayload<*>>()
    private var mActiveAnimation: ValueAnimator? = null

    //Translation X and Y
    private var mInitialTranslation = PointF()
    private var mTargetTranslation = PointF()

    //Gesture variables
    private var mTouchInitialPosition = PointF()
    private var mTouchLastPosition = PointF()

    private var mTouchDelegate: DraggableTouchDelegate? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    /**
     * Sets target position view, anchor on that view and margin
     * This is used to determine the second position of the button
     */
    fun setTarget(target: View, anchor: DragTargetAnchor, marginDp: Int) {
        this.targetView = target
        this.anchor = anchor
        this.marginDp = marginDp

        mInitialTranslation.x = translationX
        mInitialTranslation.y = translationY
    }

    /**
     * Adds payload to the button
     * Payloads cannot be dragged and are dependent on this buttons drag position
     */
    fun addPayload(payload: DraggablePayload<*>) {
        mPayloads.add(payload)
    }

    /**
     * Removes payload
     * Does not destroy or detach the view
     */
    fun removePayload(payload: DraggablePayload<*>) {
        mPayloads.remove(payload)
    }

    /**
     * This function needs to be called when the activity that was set for payloads receives appropriate permission
     * Otherwise permission won't be received, because they require activity
     */
    fun onPermissionResponse(requestCode: Int, success: Boolean) {
        mPayloads.forEach { (it as IOnDemandView).onPermissionResponse(requestCode, success) }
    }

    fun increaseTouchAreaBy(value: Int) {
        increaseTouchAreaBy(value, value, value, value)
    }

    fun increaseTouchAreaBy(left: Int, top: Int, right: Int, bottom: Int) {
        if (mTouchDelegate == null) {
            val parentView = parent as View
            parentView.post {
                val hitRect = Rect()
                //getHitRect(hitRect)

                hitRect.left = left
                hitRect.top = top
                hitRect.right = right
                hitRect.bottom = bottom

                val touchDelegate = DraggableTouchDelegate(hitRect, this)
                TouchDelegateComposite.addTouchDelegateOn(parentView, touchDelegate)
                mTouchDelegate = touchDelegate
            }
        } else {
            mTouchDelegate!!.updateOffsets(left, top, right, bottom)
        }
    }

    override fun performClick(): Boolean {
        super.performClick()

        if (targetView != null && this.dragAxis != DragAxis.None) {
            moveToState(!mCurrentState)
        }

        return true
    }

    private fun handleAnimatorListeners(animator: ValueAnimator, state: Boolean) {
        if (state != mCurrentState) {
            val button = this
            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    mPayloads.forEach { it.onStateChange(state) }
                    if (state)
                        onEnterInitialStateListener?.invoke(button)
                    else
                        onEnterTargetStateListener?.invoke(button)
                }
            })
        }
    }

    private fun moveToState(state: Boolean) {
        var target: Float
        var animator: ValueAnimator? = null
        if (this.dragAxis == DragAxis.X || this.dragAxis == DragAxis.XY) {
            target = if (state) mTargetTranslation.x else mInitialTranslation.x
            animator = animate(mInitialTranslation.x, mTargetTranslation.x, translationX, target, ::setTranslationX)
        }

        if (this.dragAxis == DragAxis.Y || this.dragAxis == DragAxis.XY) {
            target = if (state) mTargetTranslation.y else mInitialTranslation.y
            animator = animate(mInitialTranslation.y, mTargetTranslation.y, translationY, target, ::setTranslationY)
        }

        if (animator != null)
            handleAnimatorListeners(animator, state)

        mCurrentState = state
    }

    private fun animate(initialConstraintTranslation: Float,
                        targetConstraintTranslation: Float,
                        thisTranslation: Float,
                        targetTranslation: Float,
                        assignListener: (Float) -> Unit): ValueAnimator {
        val valueAnimator = ValueAnimator.ofFloat(thisTranslation, targetTranslation)

        valueAnimator.addUpdateListener {
            val value = it.animatedValue as Float

            assignListener.invoke(value)
            val percentage = Utility.betweenInPercent(initialConstraintTranslation, targetConstraintTranslation, value)
            mPayloads.forEach { payload -> payload.onDrag(percentage) }

            updateTranslationZ(percentage)
        }

        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.duration = 200
        valueAnimator.start()
        mActiveAnimation = valueAnimator
        return valueAnimator
    }

    private fun setHorizontalTranslation(desire: Float) {
        if (targetView != null) {
            if (Utility.between(mInitialTranslation.x, mTargetTranslation.x, desire)) {
                translationX = desire
                val percentage = Utility.betweenInPercent(mInitialTranslation.x, mTargetTranslation.x, desire)
                mPayloads.forEach { payload -> payload.onDrag(percentage) }
                updateTranslationZ(percentage)
            }
        } else
            translationX = desire
    }

    private fun setVerticalTranslation(desire: Float) {
        if (targetView != null) {
            if (Utility.between(mInitialTranslation.y, mTargetTranslation.y, desire)) {
                translationY = desire
                val percentage = Utility.betweenInPercent(mInitialTranslation.y, mTargetTranslation.y, desire)
                mPayloads.forEach { payload -> payload.onDrag(percentage) }
                updateTranslationZ(percentage)
            }
        } else
            translationY = desire
    }

    private fun updateTranslationZ(percentage: Float) {
        if (defaultTranslationZ != targetTranslationZ) {
            val translationZ = defaultTranslationZ + (targetTranslationZ - defaultTranslationZ) * percentage
            super.setTranslationZ(translationZ)
        }
    }

    private fun calculateTargetTranslation() = Utility.calculateTargetTranslation(this, targetView!!, anchor, Utility.dpToPx(context, marginDp))

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                mTouchInitialPosition.x = event.rawX
                mTouchInitialPosition.y = event.rawY

                mTargetTranslation = calculateTargetTranslation()

                if (mActiveAnimation != null) {
                    mActiveAnimation!!.cancel()
                    mActiveAnimation = null
                }

                mPayloads.forEach { it.initializeView() }
            }
            MotionEvent.ACTION_UP -> {
                val changeX = event.rawX - mTouchInitialPosition.x
                val changeY = event.rawY - mTouchInitialPosition.y
                val distanceX = Math.abs(changeX)
                val distanceY = Math.abs(changeY)

                if (distanceX < deadZone && distanceY < deadZone)
                    performClick()
                else if (targetView != null) {
                    var move = false

                    mTargetTranslation = calculateTargetTranslation()

                    if (dragAxis.isHorizontal() && dragAxis.isVertical()) {
                        TODO("This is not yet implemented")
                    } else if (dragAxis.isVertical()) {
                        move = (Math.abs(translationY - mInitialTranslation.y) > Math.abs(translationY - mTargetTranslation.y)) xor mCurrentState
                    } else if (dragAxis.isHorizontal()) {
                        move = (Math.abs(translationX - mInitialTranslation.x) > Math.abs(translationX - mTargetTranslation.x)) xor mCurrentState
                    }

                    if (move)
                        moveToState(!mCurrentState)
                    else
                        moveToState(mCurrentState)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val changeX = event.rawX - mTouchLastPosition.x
                val changeY = event.rawY - mTouchLastPosition.y
                if (this.dragAxis == DragAxis.X || this.dragAxis == DragAxis.XY)
                    setHorizontalTranslation(translationX + changeX)

                if (this.dragAxis == DragAxis.Y || this.dragAxis == DragAxis.XY)
                    setVerticalTranslation(translationY + changeY)
            }
        }

        mTouchLastPosition.x = event.rawX
        mTouchLastPosition.y = event.rawY
        return true
    }
}