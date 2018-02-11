package com.adsamcik.draggable

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.PointF
import android.support.v7.widget.AppCompatImageButton
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator


class DraggableImageButton : AppCompatImageButton {
    private val mDeadZone = Utility.dpToPx(context, 16)

    private var mInitialTranslation: PointF = PointF()
    private var mTargetTranslation: PointF = PointF()

    private var mCurrentState = false
    private var mTouchInitialPosition: PointF = PointF()


    private var mDragAxis = DragAxis.None

    private var mTargetView: View? = null
    private var mAnchor = DragTargetAnchor.TopLeft
    private var mMarginDp = 0

    private val payloads = ArrayList<DraggablePayload<*>>()

    private var activeAnimation: ValueAnimator? = null

    private var defaultTranslationZ = translationZ
    private var targetTranslationZ = translationZ

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    /**
     * Sets dragging axis
     * Note:  XY axis is not yet supported
     */
    fun setDrag(axis: DragAxis) {
        mDragAxis = axis
    }

    /**
     * Sets target position view, anchor on that view and margin
     * This is used to determine the second position of the button
     */
    fun setTarget(target: View, anchor: DragTargetAnchor, marginDp: Int) {
        this.mTargetView = target
        this.mAnchor = anchor
        this.mMarginDp = marginDp

        mInitialTranslation.x = translationX
        mInitialTranslation.y = translationY
    }

    /**
     * Adds payload to the button
     * Payloads cannot be dragged and are dependent on this buttons drag position
     */
    fun addPayload(payload: DraggablePayload<*>) {
        payloads.add(payload)
    }

    /**
     * Removes payload
     */
    fun removePayload(payload: DraggablePayload<*>) {
        payloads.remove(payload)
    }

    /**
     * Sets target translationZ
     * In some cases it might be desired to have different Z translation in the final position
     */
    fun setTargetTranslationZ(translation: Float) {
        targetTranslationZ = translation
        if (mCurrentState && activeAnimation == null)
            translationZ = translation
    }

    /**
     * Sets default translationZ
     * In some cases it might be desired to have different Z translation in the final position
     */
    override fun setTranslationZ(translationZ: Float) {
        defaultTranslationZ = translationZ

        if (!mCurrentState && activeAnimation == null)
            super.setTranslationZ(translationZ)
    }

    /**
     * This function needs to be called when the activity that was set for payloads receives appropriate permission
     * Otherwise permission won't be received, because they require activity
     */
    fun onPermissionResponse(requestCode: Int, success: Boolean) {
        payloads.forEach { (it as IOnDemandView).onPermissionResponse(requestCode, success) }
    }

    override fun performClick(): Boolean {
        super.performClick()

        if (mTargetView != null && this.mDragAxis != DragAxis.None) {
            moveToState(!mCurrentState)
        }

        return true
    }

    private fun handleAnimatorListeners(animator: ValueAnimator, state: Boolean) {
        if (state != mCurrentState)
            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    payloads.forEach { it.onStateChange(state) }
                }
            })
    }

    private fun moveToState(state: Boolean) {
        var target: Float
        var animator: ValueAnimator? = null
        if (this.mDragAxis == DragAxis.X || this.mDragAxis == DragAxis.XY) {
            target = if (state) mTargetTranslation.x else mInitialTranslation.x
            animator = animate(mInitialTranslation.x, mTargetTranslation.x, translationX, target, ::setTranslationX)
        }

        if (this.mDragAxis == DragAxis.Y || this.mDragAxis == DragAxis.XY) {
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
            payloads.forEach { payload -> payload.onDrag(percentage) }

            updateTranslationZ(percentage)
        }

        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.duration = 200
        valueAnimator.start()
        activeAnimation = valueAnimator
        return valueAnimator
    }

    private fun setHorizontalTranslation(desire: Float) {
        if (mTargetView != null) {
            if (Utility.between(mInitialTranslation.x, mTargetTranslation.x, desire)) {
                translationX = desire
                val percentage = Utility.betweenInPercent(mInitialTranslation.x, mTargetTranslation.x, desire)
                payloads.forEach { payload -> payload.onDrag(percentage) }
                updateTranslationZ(percentage)
            }
        } else
            translationX = desire
    }

    private fun setVerticalTranslation(desire: Float) {
        if (mTargetView != null) {
            if (Utility.between(mInitialTranslation.y, mTargetTranslation.y, desire)) {
                translationY = desire
                val percentage = Utility.betweenInPercent(mInitialTranslation.y, mTargetTranslation.y, desire)
                payloads.forEach { payload -> payload.onDrag(percentage) }
                updateTranslationZ(percentage)
            }
        } else
            translationY = desire
    }

    private fun updateTranslationZ(percentage: Float) {
        if (defaultTranslationZ != targetTranslationZ) {
            val translationZ = defaultTranslationZ + (targetTranslationZ - defaultTranslationZ) * percentage
            this.translationZ = translationZ + 1
        }
    }

    private fun calculateTargetTranslation() = Utility.calculateTargetTranslation(this, mTargetView!!, mAnchor, Utility.dpToPx(context, mMarginDp))

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val x = event!!.x
        val y = event.y
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                mTouchInitialPosition.x = event.rawX
                mTouchInitialPosition.y = event.rawY

                mTargetTranslation = calculateTargetTranslation()

                if (activeAnimation != null) {
                    activeAnimation!!.cancel()
                    activeAnimation = null
                }

                payloads.forEach { it.initializeView() }
            }
            MotionEvent.ACTION_UP -> {
                val changeX = event.rawX - mTouchInitialPosition.x
                val changeY = event.rawY - mTouchInitialPosition.y
                val distanceX = Math.abs(changeX)
                val distanceY = Math.abs(changeY)

                if (distanceX < mDeadZone && distanceY < mDeadZone)
                    performClick()
                else if (mTargetView != null) {
                    var move = false

                    mTargetTranslation = calculateTargetTranslation()

                    if (mDragAxis.isHorizontal() && mDragAxis.isVertical()) {
                        TODO("This is not yet implemented")
                    } else if (mDragAxis.isVertical()) {
                        move = (Math.abs(translationY - mInitialTranslation.y) > Math.abs(translationY - mTargetTranslation.y)) xor mCurrentState
                    } else if (mDragAxis.isHorizontal()) {
                        move = (Math.abs(translationX - mInitialTranslation.x) > Math.abs(translationX - mTargetTranslation.x)) xor mCurrentState
                    }

                    if (move)
                        moveToState(!mCurrentState)
                    else
                        moveToState(mCurrentState)
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
            }
            MotionEvent.ACTION_POINTER_UP -> {
            }
            MotionEvent.ACTION_MOVE -> {
                if (this.mDragAxis == DragAxis.X || this.mDragAxis == DragAxis.XY)
                    setHorizontalTranslation(translationX + x)

                if (this.mDragAxis == DragAxis.Y || this.mDragAxis == DragAxis.XY)
                    setVerticalTranslation(translationY + y)
            }
        }
        return true
    }
}