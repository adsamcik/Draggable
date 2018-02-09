package com.adsamcik.draggable

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Point
import android.graphics.PointF
import android.support.v7.widget.AppCompatImageButton
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator

class DraggableImageButton : AppCompatImageButton {
    val TAG = "DraggableImageButton"

    private val mDeadZone = Utility.dpToPx(context, 16)

    private var mInitialPosition: Point = Point()
    private var mInitialTranslation: PointF = PointF()
    private var mTargetTranslation: Point = Point()

    private var mCurrentState = true
    private var mTouchInitialPosition: PointF = PointF()


    private var mDragAxis = DragAxis.None

    private var mTargetView: View? = null
    private var mAnchor = DragTargetAnchor.TopLeft
    private var mMarginDp = 0

    private val payloads = ArrayList<DraggablePayload<*>>()

    private var activeAnimation: ValueAnimator? = null

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

    fun attachPayload(payload: DraggablePayload<*>) {
        payloads.add(payload)
    }

    fun removePayload(payload: DraggablePayload<*>) {
        payloads.remove(payload)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val position = Utility.getLocationOnScreen(this)
        mInitialPosition.x = position[0]
        mInitialPosition.y = position[1]
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
            target = if (state) mTargetTranslation.x.toFloat() else mInitialTranslation.x
            animate(translationX, target, ::setTranslationX, DraggablePayload<*>::onHorizontalDrag)
        }

        if (this.mDragAxis == DragAxis.Y || this.mDragAxis == DragAxis.XY) {
            target = if (state) mTargetTranslation.y.toFloat() else mInitialTranslation.y
            animate(translationY, target, ::setTranslationY, DraggablePayload<*>::onVerticalDrag)
        }

        mCurrentState = state
    }

    private fun animate(thisTranslation: Float, targetTranslation: Float, assignListener: (Float) -> Unit, onPayloadDragMethod: (payload: DraggablePayload<*>, percentage: Float) -> Unit) {
        val diff = targetTranslation - thisTranslation

        animate {
            assignListener.invoke(thisTranslation + it * diff)
            payloads.forEach { payload -> onPayloadDragMethod.invoke(payload, it) }
        }
    }

    private fun animate(updateListener: (Float) -> Unit) {
        val valueAnimator = ValueAnimator.ofFloat(0f, 1f)

        valueAnimator.addUpdateListener {
            val value = it.animatedValue as Float
            updateListener.invoke(value)
        }

        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.duration = 2000
        valueAnimator.start()
        activeAnimation = valueAnimator
    }


    private fun setHorizontalTranslation(desire: Float) {
        if (mTargetView != null) {
            if (Utility.between(mTargetTranslation.x, mInitialPosition.x, desire)) {
                translationX = desire
                if (payloads.isNotEmpty()) {
                    val percentage = Utility.betweenInPercent(mTargetTranslation.x, mInitialPosition.x, desire)
                    payloads.forEach { payload -> payload.onHorizontalDrag(percentage) }
                }
            }
        } else
            translationX = desire
    }

    private fun setVerticalTranslation(desire: Float) {
        if (mTargetView != null) {
            if (Utility.between(mTargetTranslation.y, mInitialPosition.y, desire)) {
                translationY = desire
                if (payloads.isNotEmpty()) {
                    val percentage = Utility.betweenInPercent(mTargetTranslation.y, mInitialPosition.y, desire)
                    payloads.forEach { payload -> payload.onVerticalDrag(percentage) }
                }
            }
        } else
            translationY = desire
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

                    } else if (mDragAxis.isVertical()) {
                        move = (Math.abs(changeY - mInitialPosition.y) > Math.abs(changeY - mTargetTranslation.y)) xor mCurrentState
                    } else if (mDragAxis.isHorizontal()) {
                        move = (Math.abs(changeX - mInitialPosition.x) > Math.abs(changeX - mTargetTranslation.x)) xor mCurrentState
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