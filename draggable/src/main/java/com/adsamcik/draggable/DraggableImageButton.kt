package com.adsamcik.draggable

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.PointF
import android.graphics.Rect
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.support.v7.widget.AppCompatImageButton
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.*
import com.adsamcik.touchdelegate.DraggableTouchDelegate
import com.adsamcik.touchdelegate.TouchDelegateComposite
import org.jetbrains.annotations.NotNull
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

    /**
     * Anchor sets to which side of the [targetView] should the button
     * attach in target position
     */
    var targetAnchor = DragTargetAnchor.LeftTop

    /**
     * Offset in pixels
     */
    var targetOffset = Offset(0)

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

    var state = State.INITIAL
        private set
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

    //Attribute temporaries
    private var targetViewId: Int = View.NO_ID
    private var touchRect: Rect? = null

    private var mFragmentTag: String = Math.random().toString()

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

    /**
     * Attribute initialization
     */
    private fun init(context: Context, attrs: AttributeSet, defStyleAttr: Int) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.DraggableImageButton, defStyleAttr, 0)
        init(a)
        a.recycle()
    }

    /**
     * Attribute initialization
     */
    private fun init(context: Context, attrs: AttributeSet) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.DraggableImageButton)
        init(a)
        a.recycle()
    }

    /**
     * Attribute initialization
     */
    private fun init(typedArray: TypedArray) {
        val axisRaw = typedArray.getInt(R.styleable.DraggableImageButton_axis, -1)
        if (axisRaw >= 0)
            dragAxis = DragAxis.values()[axisRaw]

        //target
        targetTranslationZ = typedArray.getDimension(R.styleable.DraggableImageButton_targetTranslationZ, targetTranslationZ)
        targetViewId = typedArray.getResourceId(R.styleable.DraggableImageButton_targetView, View.NO_ID)

        targetOffset.vertical = typedArray.getDimension(R.styleable.DraggableImageButton_targetOffsetVertical, targetOffset.vertical.toFloat()).roundToInt()
        targetOffset.horizontal = typedArray.getDimension(R.styleable.DraggableImageButton_targetOffsetHorizontal, targetOffset.horizontal.toFloat()).roundToInt()

        val anchor = typedArray.getInt(R.styleable.DraggableImageButton_targetAnchor, -1)
        if (anchor >= 0)
            targetAnchor = DragTargetAnchor.fromInt(anchor)

        //touchArea
        val leftTA = typedArray.getDimension(R.styleable.DraggableImageButton_extendLeftTouchArea, 0f).roundToInt()
        val topTA = typedArray.getDimension(R.styleable.DraggableImageButton_extendTopTouchArea, 0f).roundToInt()
        val rightTA = typedArray.getDimension(R.styleable.DraggableImageButton_extendRightTouchArea, 0f).roundToInt()
        val bottomTA = typedArray.getDimension(R.styleable.DraggableImageButton_extendBottomTouchArea, 0f).roundToInt()
        if (leftTA != 0 || topTA != 0 || rightTA != 0 || bottomTA != 0)
            touchRect = Rect(leftTA, topTA, rightTA, bottomTA)

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

    /**
     * All payloads attached to the button
     */
    val payloads: Array<out DraggablePayload<*>> get() = mPayloads.toTypedArray()

    /**
     * Sets target position view, anchor on that view
     * This is used to determine the second position of the button
     */
    fun setTarget(target: View, anchor: DragTargetAnchor) {
        this.targetView = target
        this.targetAnchor = anchor

        mInitialTranslation.x = translationX
        mInitialTranslation.y = translationY
    }

    fun setTargetOffsetDp(offset: Offset) {
        this.targetOffset.setWithDpAsPx(offset)
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
     * Moves button to given [state]
     * If [force] is true, the view will always move to the given state, ignoring any user input
     *
     * @param state State to which the button should move
     * @param force If true, ignores user input and always animates to given state
     */
    fun moveToState(state: State, animate: Boolean, force: Boolean) {
        if (targetView == null)
            throw RuntimeException("You cannot move to state without target view")

        mTargetTranslation = calculateTargetTranslation()

        if (force) {
            if (state == State.INITIAL && translationX == mInitialTranslation.x && translationY == mInitialTranslation.y)
                return
            else if (state == State.TARGET && translationX == mTargetTranslation.x && translationY == mTargetTranslation.y)
                return

            mDrag = false
            if (this.state == State.INITIAL)
                mDragDirection = dragAxis
            moveToStateInternal(state, animate)
        } else if (state != this.state) {
            mDrag = false
            if (this.state == State.INITIAL)
                mDragDirection = dragAxis
            moveToStateInternal(state, animate)
        }
    }

    /**
     * Increases touch area of the button by given value on all sides in pixels
     *
     * @param value Extend touch area uniformly on all sides by this value in pixels
     */
    fun extendTouchAreaBy(value: Int) {
        extendTouchAreaBy(value, value, value, value)
    }

    /**
     * Increases touch area of the button by given values in pixels
     *
     * @param left Extend touch are to the left by pixels
     * @param top Extend touch area above by pixels
     * @param right Extend touch are to the right by pixels
     * @param bottom Extend touch are below by pixels
     */
    @Synchronized
    fun extendTouchAreaBy(left: Int, top: Int, right: Int, bottom: Int) {
        if (mTouchDelegate == null) {
            val parentView = parent as View

            val hitRect = Rect()
            hitRect.left = left
            hitRect.top = top
            hitRect.right = right
            hitRect.bottom = bottom
            val touchDelegate = DraggableTouchDelegate(hitRect, this)
            mTouchDelegate = touchDelegate

            parentView.post {
                TouchDelegateComposite.addTouchDelegateOn(parentView, touchDelegate)
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

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (targetViewId != View.NO_ID && targetView == null)
            targetView = rootView.findViewById(targetViewId)

        val touchRect = touchRect
        if (touchRect != null) {
            extendTouchAreaBy(touchRect.left, touchRect.top, touchRect.right, touchRect.bottom)
            this.touchRect = null
        }
    }

    override fun performClick(): Boolean {
        if (!isClickable)
            return false

        if (mDragDirection == DragAxis.XY)
            throw UnsupportedOperationException("You can't perform click operation on both axes")

        super.performClick()

        if (targetView != null && dragAxis != DragAxis.None) {
            mDragDirection = dragAxis
            moveToStateInternal(!state, true)
        }

        return true
    }

    private fun handleAnimatorListeners(animator: ValueAnimator, state: State) {
        val stateChange = state != this.state
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                onEnterState(state, stateChange)
            }
        })
    }

    private fun onEnterState(state: State, stateChange: Boolean) {
        if (stateChange) {
            mPayloads.forEach { it.onStateChange(state) }
            onEnterStateListener?.invoke(this, state, mDragDirection)
        }

        if (state == State.INITIAL)
            mPayloads.forEach { it.onInitialPosition() }
    }

    private fun moveToStateInternal(mState: State, animate: Boolean) {
        val target: Float
        val animator: ValueAnimator

        if (dragAxis.isHorizontal() && mDragDirection.isHorizontal()) {
            target = if (mState == State.INITIAL) mInitialTranslation.x else mTargetTranslation.x
            if (animate)
                animator = animate(mInitialTranslation.x, mTargetTranslation.x, translationX, target, ::setTranslationX)
            else {
                move(mInitialTranslation.x, mTargetTranslation.x, target, ::setTranslationX)
                onEnterState(mState, mState != state)
                return
            }
        } else if (dragAxis.isVertical() && mDragDirection.isVertical()) {
            target = if (mState == State.INITIAL) mInitialTranslation.y else mTargetTranslation.y
            if (animate)
                animator = animate(mInitialTranslation.y, mTargetTranslation.y, translationY, target, ::setTranslationY)
            else {
                move(mInitialTranslation.y, mTargetTranslation.y, target, ::setTranslationY)
                onEnterState(mState, mState != state)
                return
            }
        } else
            throw IllegalStateException("Not sure to which state should I move.")

        handleAnimatorListeners(animator, mState)
        state = mState
    }

    private fun animate(initialConstraintTranslation: Float,
                        targetConstraintTranslation: Float,
                        thisTranslation: Float,
                        targetTranslation: Float,
                        assignListener: (Float) -> Unit): ValueAnimator {
        val valueAnimator = ValueAnimator.ofFloat(thisTranslation, targetTranslation)

        valueAnimator.addUpdateListener {
            positionUpdate(initialConstraintTranslation, targetConstraintTranslation, it.animatedValue as Float, assignListener)
        }


        //Shortening animations by percentage does not look very good even with linear interpolation
        valueAnimator.interpolator = animationInterpolator
        valueAnimator.duration = fullAnimationLength
        valueAnimator.start()
        mActiveAnimation = valueAnimator
        return valueAnimator
    }

    private fun move(initialConstraintTranslation: Float,
                     targetConstraintTranslation: Float,
                     targetTranslation: Float,
                     assignListener: (Float) -> Unit) {
        positionUpdate(initialConstraintTranslation, targetConstraintTranslation, targetTranslation, assignListener)
    }

    private fun positionUpdate(initialConstraintTranslation: Float,
                               targetConstraintTranslation: Float,
                               value: Float,
                               assignListener: (Float) -> Unit) {
        assignListener.invoke(value)
        val percentage = Utility.betweenInPercent(initialConstraintTranslation, targetConstraintTranslation, value)
        mPayloads.forEach { payload -> payload.onDrag(percentage) }

        updateTranslationZ(percentage)
    }

    private fun setHorizontalTranslation(desire: Float) = setTranslation(desire, mInitialTranslation.x, mTargetTranslation.x, this::setTranslationX)

    private fun setVerticalTranslation(desire: Float) = setTranslation(desire, mInitialTranslation.y, mTargetTranslation.y, this::setTranslationY)

    private fun setTranslation(desire: Float, initialTranslation: Float, targetTranslation: Float, translationSetter: (Float) -> Unit) {
        if (targetView != null) {
            if (Utility.between(initialTranslation, targetTranslation, desire)) {
                translationSetter.invoke(desire)
                val percentage = Utility.betweenInPercent(initialTranslation, targetTranslation, desire)
                mPayloads.forEach { payload -> payload.onDrag(percentage) }
                updateTranslationZ(percentage)
            }
        } else
            translationSetter.invoke(desire)
    }

    private fun updateTranslationZ(percentage: Float) {
        if (defaultTranslationZ != targetTranslationZ) {
            val translationZ = defaultTranslationZ + (targetTranslationZ - defaultTranslationZ) * percentage
            super.setTranslationZ(translationZ)
        }
    }

    private fun calculateTargetTranslation() = Utility.calculateTargetTranslation(this, targetView!!, targetAnchor, targetOffset)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (dragAxis == DragAxis.None || targetView == null)
            return false

        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                mTouchInitialPosition.x = event.rawX
                mTouchInitialPosition.y = event.rawY

                mTargetTranslation = calculateTargetTranslation()
                mDrag = false

                if (state == State.INITIAL)
                    mDragDirection = DragAxis.None

                if (mActiveAnimation != null) {
                    mActiveAnimation!!.cancel()
                    mActiveAnimation = null
                }

                mPayloads.forEach { it.initializeView() }

                mVelocityTracker = VelocityTracker.obtain()
                mVelocityTracker!!.addMovement(event)

                onLeaveStateListener?.invoke(this, state)
            }
            MotionEvent.ACTION_UP -> {
                val velocityTracker = mVelocityTracker!!
                mVelocityTracker = null

                if (!mDrag)
                    performClick()
                else if (targetView != null) {
                    mTargetTranslation = calculateTargetTranslation()

                    velocityTracker.computeCurrentVelocity(1000)

                    //Calculate whether we moved enough to move to different state
                    //First it if velocity is not within fling bounds
                    //Second it calculates how far we moved and uses xor with boolean that represents if current state is initial
                    //Xor simplifies the actual condition so it can be the same for both states
                    val move = if (dragAxis.isVertical() && mDragDirection.isVertical()) {
                        val velocity = Math.abs(velocityTracker.yVelocity)
                        (velocity in mMinFlingVelocity..mMaxFlingVelocity) ||
                                (Math.abs(translationY - mInitialTranslation.y) < Math.abs(translationY - mTargetTranslation.y)) xor
                                (state == State.INITIAL)
                    } else if (dragAxis.isHorizontal() && mDragDirection.isHorizontal()) {
                        val velocity = Math.abs(velocityTracker.xVelocity)
                        val translationX = translationX
                        (velocity in mMinFlingVelocity..mMaxFlingVelocity) ||
                                (Math.abs(translationX - mInitialTranslation.x) < Math.abs(translationX - mTargetTranslation.x)) xor
                                (state == State.INITIAL)
                    } else
                        return true

                    if (move)
                        moveToStateInternal(!state, true)
                    else
                        moveToStateInternal(state, true)
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

    fun saveFragments(bundle: Bundle) {
        payloads.forEach { it.saveFragment(bundle) }
    }

    fun restoreFragments(bundle: Bundle) {
        payloads.forEach { it.restoreFragment(bundle) }

        if (dragAxis != DragAxis.None && mDragDirection != DragAxis.None)
            moveToStateInternal(state, false)
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()

        val ss = SavedState(superState)
        ss.state = state
        ss.dragDirection = mDragDirection
        ss.payloadFragmentTags = mPayloads.map { it.mFragmentTag }
        ss.payloadWrapperId = mPayloads.map { it.wrapper?.id ?: View.NO_ID }.toIntArray()

        return ss
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        if (targetViewId != View.NO_ID && targetView == null)
            targetView = rootView.findViewById(targetViewId)

        super.onRestoreInstanceState(state.superState)

        mDragDirection = state.dragDirection

        //Basic check to ensure payloads are likely to be restored correctly
        if (payloads.size == state.payloadFragmentTags.size) {
            for (i in 0 until payloads.size) {
                payloads[i].restoreFragment(state.payloadWrapperId[i], state.payloadFragmentTags[i])
            }
        }

        if (dragAxis != DragAxis.None && mDragDirection != DragAxis.None) {
            targetView?.post {
                mTargetTranslation = calculateTargetTranslation()
                moveToStateInternal(state.state, false)
            }
        }
    }

    internal class SavedState : View.BaseSavedState {
        lateinit var state: State
        lateinit var dragDirection: DragAxis
        lateinit var payloadFragmentTags: List<String>
        lateinit var payloadWrapperId: IntArray

        private constructor(source: Parcel) : super(source) {
            state = State.values()[source.readInt()]
            dragDirection = DragAxis.values()[source.readInt()]
            payloadFragmentTags = ArrayList()
            source.readStringList(payloadFragmentTags)

            payloadWrapperId = IntArray(payloadFragmentTags.size)
            source.readIntArray(payloadWrapperId)
        }

        constructor(superState: Parcelable) : super(superState)

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(state.ordinal)
            out.writeInt(dragDirection.ordinal)
            out.writeStringList(payloadFragmentTags)
            out.writeIntArray(payloadWrapperId)
        }

        @JvmField
        @NotNull
        val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {

            override fun createFromParcel(source: Parcel): SavedState {
                return SavedState(source)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }
}