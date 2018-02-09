package com.adsamcik.draggable

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.PointF
import android.support.v4.app.Fragment
import android.support.v7.widget.AppCompatImageButton
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import kotlin.math.sign

class DraggableImageButton : AppCompatImageButton {
    val TAG = "DraggableImageButton"

    private val mDeadZone = dpToPx(context, 16)

    private var mInitialPosition: Point = Point()
    private var mInitialTranslation: PointF = PointF()
    private var mTargetTranslation: Point = Point()

    private var mCurrentState = true
    private var mTouchInitialPosition: PointF = PointF()


    private var mDragAxis = DragAxis.None

    private var mTargetView: View? = null
    private var mAnchor = DragTargetAnchor.TopLeft
    private var mMarginDp = 0

    private var mClass: Class<Fragment>? = null
    private var mClassWrapper: FrameLayout? = null
    private var mClassMarginDp = 0
    private var mClassAnchor = DragTargetAnchor.TopLeft

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

    /**
     * Attaches view to button and loads it on swipe to active state
     * This view needs to implement IOnDemandView interface and View class
     */
    fun <T> attachView(viewClass: Class<T>, anchor: DragTargetAnchor, marginDp: Int) where T : Fragment, T : IOnDemandView {
        this.mClass = viewClass as Class<Fragment>
        mClassMarginDp = marginDp
        mAnchor = anchor
        mClassWrapper = null
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val position = getLocationOnScreen(this)
        mInitialPosition.x = position[0]
        mInitialPosition.y = position[1]
    }

    private fun getLocationOnScreen(view: View): IntArray {
        val array = IntArray(2)
        view.getLocationOnScreen(array)
        return array
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
            animateHorizontal(target)
        }

        if (this.mDragAxis == DragAxis.Y || this.mDragAxis == DragAxis.XY) {
            target = if (state) mTargetTranslation.y.toFloat() else mInitialTranslation.y
            animateVertical(target)
        }

        mCurrentState = state
    }

    private fun animateHorizontal(targetTranslation: Float) {
        if (mClass != null) {
            val view = initializeView()
            val classTranslation = view.translationX
            val classTarget = calculateTargetTranslation(view).x + mClassAnchor.calculateEdgeOffset(this.parent as View, view).x
            animate(translationX, targetTranslation, classTranslation, classTarget.toFloat()) { buttonTranslation, viewTranslation ->
                translationX = buttonTranslation
                view.translationX = viewTranslation
            }
        } else
            animate(translationX, targetTranslation) { translationX = it }
    }

    private fun animateVertical(targetTranslation: Float) {
        if (mClass != null) {
            val view = initializeView()
            val classTranslation = view.translationY
            val classTarget = calculateTargetTranslation(view).y + mClassAnchor.calculateEdgeOffset(this.parent as View, view).y
            animate(translationY, targetTranslation, classTranslation, classTarget.toFloat()) { buttonTranslation, viewTranslation ->
                translationY = buttonTranslation
                view.translationY = viewTranslation
            }
        } else
            animate(translationY, targetTranslation) { translationY = it }
    }

    private fun initializeView(): FrameLayout {
        if (mClassWrapper == null) {
            val cView = FrameLayout(context)
            cView.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            cView.setBackgroundColor(Color.parseColor("#aa0000ff"))
            cView.translationZ = 1000f
            (mTargetView!!.parent as ViewGroup).addView(cView)
            mClassWrapper = cView
            return cView
        } else
            return mClassWrapper!!
    }

    private fun animate(thisTranslation: Float, targetTranslation: Float, assignListener: (Float) -> Unit) {
        val diff = targetTranslation - thisTranslation

        animate {
            assignListener.invoke(thisTranslation + it * diff)
        }
    }

    private fun animate(thisTranslation: Float,
                        targetTranslation: Float,
                        classTranslation: Float,
                        classTargetTranslation: Float,
                        assignListener: (buttonTranslation: Float,
                                         viewTranslation: Float) -> Unit) {
        val diff = targetTranslation - thisTranslation

        val classDiff = classTargetTranslation - classTranslation

        animate {
            assignListener.invoke(thisTranslation + it * diff, classTranslation + it * classDiff)
        }
    }

    private fun animate(updateListener: (Float) -> Unit) {
        val valueAnimator = ValueAnimator.ofFloat(0f, 1f)

        valueAnimator.addUpdateListener {
            val value = it.animatedValue as Float
            updateListener.invoke(value)
        }

        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.duration = 200
        valueAnimator.start()
    }

    private fun between(firstConstraint: Int, secondConstraint: Int, number: Float): Boolean {
        return if (firstConstraint > secondConstraint)
            number in secondConstraint..firstConstraint
        else
            number in firstConstraint..secondConstraint
    }

    private fun calculateTargetTranslation(target: View): Point {
        val thisOnScreen = getLocationOnScreen(this)
        val targetOnScreen = getLocationOnScreen(target)
        val targetRelPos = mAnchor.calculateEdgeOffset(target, this)
        val targetX = ((targetOnScreen[0] - thisOnScreen[0]) + targetRelPos.x + translationX).toInt()
        val targetY = ((targetOnScreen[1] - thisOnScreen[1]) + targetRelPos.y + translationY).toInt()
        val marginPx = dpToPx(context, mMarginDp)
        return Point(targetX - targetX.sign * marginPx, targetY - targetY.sign * marginPx)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val x = event!!.x
        val y = event.y
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                mTouchInitialPosition.x = event.rawX
                mTouchInitialPosition.y = event.rawY

                mTargetTranslation = calculateTargetTranslation(mTargetView!!)
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

                    mTargetTranslation = calculateTargetTranslation(mTargetView!!)

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
                if (this.mDragAxis == DragAxis.X || this.mDragAxis == DragAxis.XY) {
                    val desire = translationX + x
                    if (mTargetView != null) {
                        if (between(mTargetTranslation.x, mInitialPosition.x, desire))
                            translationX = desire

                    } else
                        translationX = desire
                }

                if (this.mDragAxis == DragAxis.Y || this.mDragAxis == DragAxis.XY) {
                    val desire = translationY + y
                    if (mTargetView != null) {
                        if (between(mTargetTranslation.y, mInitialPosition.y, desire))
                            translationY = desire

                    } else
                        translationY = desire

                }
            }
        }
        return true
    }
}