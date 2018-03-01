package com.adsamcik.draggable

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.PointF
import android.graphics.Rect
import android.support.v7.widget.AppCompatImageButton
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.*
import com.adsamcik.draggable.delegates.DraggableTouchDelegate
import com.adsamcik.draggable.delegates.TouchDelegateComposite
import kotlin.math.roundToInt


class DraggableImageButton : AppCompatImageButton {
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

    private var targetViewId: Int = View.NO_ID

    /**
     * Anchor sets to which side of the [targetView] should the button
     * attach in target position
     */
    var targetAnchor = DragTargetAnchor.LeftTop

    /**
     * Margin in pixels
     */
    var targetMargin = 0

    //Listeners
    var onEnterStateListener: EnterStateListener? = null
    var onLeaveStateListener: ExitStateListener? = null

    //Animation parameters
    /**
     * Full length of the animation in milliseconds
     */
    var fullAnimationLength: Long = 250

    /**
     * Animation interpolator
     */
    var animationInterpolator: TimeInterpolator = LinearInterpolator()

    private var mCurrentState = State.INITIAL
    private val mPayloads = ArrayList<DraggablePayload<*>>()
    private var mActiveAnimation: ValueAnimator? = null

    //Translation X and Y
    private var mInitialTranslation = PointF()
    private var mTargetTranslation = PointF()

    //Gesture variables
    private var mTouchInitialPosition = PointF()
    private var mTouchLastPosition = PointF()
    private var mVelocityTracker: VelocityTracker? = null
    private var mDragDirection = DragAxis.None
    private var mDrag = false

    private val mSlop: Int
    private val mMaxFlingVelocity: Int
    private val mMinFlingVelocity: Int

    private var mTouchDelegate: DraggableTouchDelegate? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr)
    }

    init {
        val vc = ViewConfiguration.get(context)
        mSlop = vc.scaledTouchSlop
        mMinFlingVelocity = vc.scaledMinimumFlingVelocity
        mMaxFlingVelocity = vc.scaledMaximumFlingVelocity
    }

    private fun init(context: Context, attrs: AttributeSet, defStyleAttr: Int) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.DraggableImageButton, defStyleAttr, 0)
        init(a)
        a.recycle()
    }

    private fun init(context: Context, attrs: AttributeSet) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.DraggableImageButton)
        init(a)
        a.recycle()
    }

    private fun init(typedArray: TypedArray) {
        val axisRaw = typedArray.getInt(R.styleable.DraggableImageButton_axis, -1)
        if (axisRaw >= 0)
            dragAxis = DragAxis.values()[axisRaw]

        //target
        targetTranslationZ = typedArray.getDimension(R.styleable.DraggableImageButton_targetTranslationZ, targetTranslationZ)
        targetViewId = typedArray.getResourceId(R.styleable.DraggableImageButton_targetView, View.NO_ID)
        targetMargin = typedArray.getDimension(R.styleable.DraggableImageButton_targetMargin, targetMargin.toFloat()).roundToInt()
        val anchor = typedArray.getInt(R.styleable.DraggableImageButton_targetAnchor, -1)
        if (anchor >= 0)
            targetAnchor = DragTargetAnchor.fromInt(anchor)

        //touchArea
        val leftTA = typedArray.getInteger(R.styleable.DraggableImageButton_extendLeftTouchArea, 0)
        val topTA = typedArray.getInteger(R.styleable.DraggableImageButton_extendTopTouchArea, 0)
        val rightTA = typedArray.getInteger(R.styleable.DraggableImageButton_extendRightTouchArea, 0)
        val bottomTA = typedArray.getInteger(R.styleable.DraggableImageButton_extendBottomTouchArea, 0)
        if (leftTA != 0 || topTA != 0 || rightTA != 0 || bottomTA != 0)
            increaseTouchAreaBy(leftTA, topTA, rightTA, bottomTA)

        //animation
        fullAnimationLength = typedArray.getInt(R.styleable.DraggableImageButton_animationLength, fullAnimationLength.toInt()).toLong()
        val interpolatorId = typedArray.getInt(R.styleable.DraggableImageButton_interpolator, -1)
        if (interpolatorId >= 0) {
            animationInterpolator = when (interpolatorId) {
                0 -> LinearInterpolator()
                1 -> OvershootInterpolator()
                2 -> BounceInterpolator()
                3 -> AccelerateInterpolator()
                4 -> DecelerateInterpolator()
                5 -> AccelerateDecelerateInterpolator()
                else -> throw IllegalArgumentException("Invalid interpolator value")
            }
        }

    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (targetViewId != View.NO_ID && targetView == null)
            targetView = rootView.findViewById(targetViewId)
    }

    /**
     * All payloads attached to the button
     */
    val payloads: Array<out DraggablePayload<*>> get() = mPayloads.toTypedArray()

    /**
     * Sets target position view, anchor on that view and margin
     * This is used to determine the second position of the button
     */
    fun setTarget(target: View, anchor: DragTargetAnchor, marginDp: Int) {
        this.targetView = target
        this.targetAnchor = anchor
        this.targetMargin = marginDp

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
     * Runs given function on each payload
     *
     * @param func Function to run on each payload
     */
    fun forEachPayload(func: (DraggablePayload<*>) -> Unit) {
        mPayloads.forEach(func)
    }

    /**
     * This function needs to be called when the activity that was set for payloads receives appropriate permission
     * Otherwise permission won't be received, because they require activity
     */
    fun onPermissionResponse(requestCode: Int, success: Boolean) {
        mPayloads.forEach { (it as IOnDemandView).onPermissionResponse(requestCode, success) }
    }

    /**
     * Increases touch area of the button by given value on all sides in pixels
     *
     * @param value Extend touch area uniformly on all sides by this value in pixels
     */
    fun increaseTouchAreaBy(value: Int) {
        increaseTouchAreaBy(value, value, value, value)
    }

    /**
     * Increases touch area of the button by given values in pixels
     *
     * @param left Extend touch are to the left by pixels
     * @param top Extend touch area above by pixels
     * @param right Extend touch are to the right by pixels
     * @param bottom Extend touch are below by pixels
     */
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

    /**
     * Restores default touch area of the button
     */
    fun restoreDefaultTouchArea() {
        if (mTouchDelegate != null) {
            val parent = parent as View
            val delegate = parent.touchDelegate
            if (delegate is TouchDelegateComposite) {
                delegate.removeDelegate(mTouchDelegate!!)

                if (delegate.count == 0)
                    parent.touchDelegate = null
            }
        }
    }

    fun setTargetMarginDp(dp: Int) {
        targetMargin = Utility.dpToPx(context, dp)
    }

    override fun performClick(): Boolean {
        if (mDragDirection == DragAxis.XY)
            throw UnsupportedOperationException("You can't perform click operation on both axes")

        super.performClick()

        if (targetView != null && dragAxis != DragAxis.None) {
            mDragDirection = dragAxis
            moveToState(!mCurrentState)
        }

        return true
    }

    private fun handleAnimatorListeners(animator: ValueAnimator, state: State) {
        val button = this
        val stateChange = state != mCurrentState
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (stateChange) {
                    mPayloads.forEach { it.onStateChange(state) }
                    onEnterStateListener?.invoke(button, state, mDragDirection)
                }

                if (state == State.INITIAL)
                    mPayloads.forEach { it.onInitialPosition() }
            }
        })

    }

    private fun moveToState(mState: State) {
        val target: Float
        val animator: ValueAnimator
        if (dragAxis.isHorizontal() && mDragDirection.isHorizontal()) {
            target = if (mState == State.INITIAL) mInitialTranslation.x else mTargetTranslation.x
            animator = animate(mInitialTranslation.x, mTargetTranslation.x, translationX, target, ::setTranslationX)
        } else if (dragAxis.isVertical() && mDragDirection.isVertical()) {
            target = if (mState == State.INITIAL) mInitialTranslation.y else mTargetTranslation.y
            animator = animate(mInitialTranslation.y, mTargetTranslation.y, translationY, target, ::setTranslationY)
        } else
            throw IllegalStateException("Not sure to which state should I move.")

        handleAnimatorListeners(animator, mState)

        mCurrentState = mState
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


        //Shortening animations by percentage does not look very good even with linear interpolation
        valueAnimator.interpolator = animationInterpolator
        valueAnimator.duration = fullAnimationLength
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

    private fun calculateTargetTranslation() = Utility.calculateTargetTranslation(this, targetView!!, targetAnchor, Utility.dpToPx(context, targetMargin))

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (dragAxis == DragAxis.None || targetView == null)
            return false

        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                mTouchInitialPosition.x = event.rawX
                mTouchInitialPosition.y = event.rawY

                mTargetTranslation = calculateTargetTranslation()
                mDrag = false

                if (mCurrentState == State.INITIAL)
                    mDragDirection = DragAxis.None

                if (mActiveAnimation != null) {
                    mActiveAnimation!!.cancel()
                    mActiveAnimation = null
                }

                mPayloads.forEach { it.initializeView() }

                mVelocityTracker = VelocityTracker.obtain()
                mVelocityTracker!!.addMovement(event)

                onLeaveStateListener?.invoke(this, mCurrentState)
            }
            MotionEvent.ACTION_UP -> {
                val velocityTracker = mVelocityTracker!!
                if (mDragDirection == DragAxis.None)
                    performClick()
                else if (targetView != null) {
                    var move = false

                    mTargetTranslation = calculateTargetTranslation()

                    velocityTracker.computeCurrentVelocity(1000)

                    //Calculate whether we moved enough to move to different state
                    //First it if velocity is not within fling bounds
                    //Second it calculates how far we moved and uses xor with boolean that represents if current state is initial
                    //Xor simplifies the actual condition so it can be the same for both states
                    if (dragAxis.isVertical() && mDragDirection.isVertical()) {
                        val velocity = Math.abs(velocityTracker.yVelocity)
                        move = (velocity in mMinFlingVelocity..mMaxFlingVelocity) ||
                                (Math.abs(translationY - mInitialTranslation.y) < Math.abs(translationY - mTargetTranslation.y)) xor
                                (mCurrentState == State.INITIAL)
                    } else if (dragAxis.isHorizontal() && mDragDirection.isHorizontal()) {
                        val velocity = Math.abs(velocityTracker.xVelocity)
                        val translationX = translationX
                        move = (velocity in mMinFlingVelocity..mMaxFlingVelocity) ||
                                (Math.abs(translationX - mInitialTranslation.x) < Math.abs(translationX - mTargetTranslation.x)) xor
                                (mCurrentState == State.INITIAL)
                    }

                    if (move)
                        moveToState(!mCurrentState)
                    else
                        moveToState(mCurrentState)
                }

                velocityTracker.recycle()
            }
            MotionEvent.ACTION_MOVE -> {
                mVelocityTracker!!.addMovement(event)

                if (!mDrag) {
                    //Checks whether we dragged far enough to consider this a drag gesture
                    val xDiff = Math.abs(event.rawX - mTouchInitialPosition.x)
                    val yDiff = Math.abs(event.rawY - mTouchInitialPosition.y)
                    if (xDiff > yDiff) {
                        if (xDiff > mSlop && !mDragDirection.isVertical()) {
                            if (!mDragDirection.isHorizontal())
                                mDragDirection = DragAxis.X
                            mDrag = true
                        }
                    } else {
                        if (yDiff > mSlop && !mDragDirection.isHorizontal()) {
                            if (!mDragDirection.isVertical())
                                mDragDirection = DragAxis.Y
                            mDrag = true
                        }
                    }
                }

                if (mDrag) {
                    if (dragAxis.isHorizontal() && mDragDirection.isHorizontal()) {
                        setHorizontalTranslation(translationX + event.rawX - mTouchLastPosition.x)
                    } else if (dragAxis.isVertical() && mDragDirection.isVertical()) {
                        setVerticalTranslation(translationY + event.rawY - mTouchLastPosition.y)
                    }
                }
            }
        }

        mTouchLastPosition.x = event.rawX
        mTouchLastPosition.y = event.rawY
        return true
    }

    enum class State {
        INITIAL {
            override operator fun not() = TARGET
        },
        TARGET {
            override operator fun not() = INITIAL
        };

        abstract operator fun not(): State
    }
}