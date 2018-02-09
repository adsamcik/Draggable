package com.adsamcik.draggable

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

    private var mCurrentState = true
    private var mTouchInitialPosition: PointF = PointF()


    private var mDragAxis = DragAxis.None

    private var mTargetView: View? = null
    private var mAnchor = DragTargetAnchor.TopLeft
    private var mMarginDp = 0

    private val payloads = ArrayList<DraggablePayload<*>>()

    private var activeAnimation: ValueAnimator? = null

    private var initialTranslationZ = translationZ
    private var targetTranslationZ = translationZ

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun setDrag(axis: DragAxis) {
        mDragAxis = axis
    }

    fun setTarget(target: View, anchor: DragTargetAnchor, marginDp: Int) {
        this.mTargetView = target
        this.mAnchor = anchor
        this.mMarginDp = marginDp

        mInitialTranslation.x = translationX
        mInitialTranslation.y = translationY
    }


    fun addPayload(payload: DraggablePayload<*>) {
        payloads.add(payload)
    }

    fun removePayload(payload: DraggablePayload<*>) {
        payloads.remove(payload)
    }

    fun setTargetTranslationZ(translation: Float) {
        targetTranslationZ = translation
    }

    override fun performClick(): Boolean {
        super.performClick()

        if (mTargetView != null && this.mDragAxis != DragAxis.None) {
            moveToState(!mCurrentState)
        }

        return true
    }

    private fun moveToState(state: Boolean) {
        var target: Float
        if (this.mDragAxis == DragAxis.X || this.mDragAxis == DragAxis.XY) {
            target = if (state) mTargetTranslation.x else mInitialTranslation.x
            animate(mInitialTranslation.x, mTargetTranslation.x, translationX, target, ::setTranslationX)
        }

        if (this.mDragAxis == DragAxis.Y || this.mDragAxis == DragAxis.XY) {
            target = if (state) mTargetTranslation.y else mInitialTranslation.y
            animate(mInitialTranslation.y, mTargetTranslation.y, translationY, target, ::setTranslationY)
        }

        mCurrentState = state
    }

    private fun animate(initialConstraintTranslation: Float, targetConstraintTranslation: Float, thisTranslation: Float, targetTranslation: Float, assignListener: (Float) -> Unit) {
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
    }

    private fun setHorizontalTranslation(desire: Float) {
        if (mTargetView != null) {
            if (Utility.between(mTargetTranslation.x, mInitialTranslation.x, desire)) {
                translationX = desire
                val percentage = Utility.betweenInPercent(mTargetTranslation.x, mInitialTranslation.x, desire)
                payloads.forEach { payload -> payload.onDrag(percentage) }
                updateTranslationZ(percentage)
            }
        } else
            translationX = desire
    }

    private fun setVerticalTranslation(desire: Float) {
        if (mTargetView != null) {
            if (Utility.between(mTargetTranslation.y, mInitialTranslation.y, desire)) {
                translationY = desire
                val percentage = Utility.betweenInPercent(mTargetTranslation.y, mInitialTranslation.y, desire)
                payloads.forEach { payload -> payload.onDrag(percentage) }
                updateTranslationZ(percentage)
            }
        } else
            translationY = desire
    }

    private fun updateTranslationZ(percentage: Float) {
        if (initialTranslationZ != targetTranslationZ) {
            val translationZ = initialTranslationZ + (targetTranslationZ - initialTranslationZ) * percentage
            this.translationZ = translationZ + 1
            payloads.forEach { it.setTranslationZ(translationZ) }
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