package com.adsamcik.draggable

import android.graphics.Point
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction.TRANSIT_NONE
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.schedule
import kotlin.math.roundToInt


/**
 * Payload which can be attached to [DraggableImageButton].
 */
@Suppress("TooManyFunctions")
class DraggablePayload<T>(
		private val mActivity: FragmentActivity,
		private val mClass: Class<T>,
		private val mParent: ViewGroup,
		private val mTargetView: View,
) where T : Fragment, T : IOnDemandView {
	companion object {
		/**
		 * Fragment destruction constant indicating it never be destroyed after entering [DraggableImageButton.State.INITIAL]
		 * Constant is set to [destroyPayloadAfter] property
		 */
		const val NEVER = -1L

		/**
		 * Fragment destruction constant indicating
		 * it should be immediately destroyed after entering [DraggableImageButton.State.INITIAL]
		 * Constant is set to [destroyPayloadAfter] property
		 */
		const val IMMEDIATELY = 0L
	}

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
	var destroyPayloadAfter: Long = NEVER

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
	 * Lock for destroy task
	 */
	private var destroyLock = ReentrantLock()

	/**
	 * Last known state, used if fragment was not initialized.
	 */
	private var state: DraggableImageButton.State = DraggableImageButton.State.INITIAL

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
		this.offsets.setWithDp(offsets)
	}

	private var targetOffset: Point = Point(0, 0)

	/**
	 * Initializes view
	 * Only cancels destroy timer if it was already initialized
	 * It is called automatically when drag starts
	 */
	fun initializeView() {
		if (wrapper == null) {
			val cView = createWrapper()
			if (mFragment == null) {
				val ft = mActivity.supportFragmentManager.beginTransaction()
				val newInst = mClass.newInstance()
				ft.replace(cView.id, newInst, mFragmentTag)

				ft.runOnCommit {
					mFragment = newInst
					onInitialized?.invoke(newInst)
				}
				ft.commitAllowingStateLoss()
			}
			wrapper = cView
		}
	}

	private fun View.generateUniqueId() {
		while (true) {
			val id = View.generateViewId()
			if (mParent.rootView.findViewById<View>(id) == null) {
				this.id = id
				return
			}
		}
	}

	/**
	 * Creates wrapper frame layout so there is view to handle even when fragment has no view yet
	 */
	private fun createWrapper(): FrameLayout {
		val cView = FrameLayout(mActivity)
		cView.generateUniqueId()
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
		// id is not reused because it might no longer be unique
		if (id != View.NO_ID) {
			wrapper = createWrapper()
		}
		this.mFragmentTag = tag
	}

	@Suppress("UNCHECKED_CAST")
	internal fun restoreFragment(bundle: Bundle) {
		if (wrapper != null) {
			val fragmentManager = mActivity.supportFragmentManager
			@Suppress("SwallowedException")
			// It is not desirable to pass this exception further
			try {
				val fragment = fragmentManager.findFragmentByTag(mFragmentTag)
						?: fragmentManager.getFragment(bundle, mFragmentTag)
				mFragment = fragment as T

				fragmentManager.beginTransaction()
						.remove(fragment)
						.commitNow()

				fragmentManager.beginTransaction()
						.replace(requireNotNull(wrapper).id, fragment)
						.commitNow()

				onInitialized?.invoke(fragment)
			} catch (e: IllegalStateException) {
				// In some edge cases getFragment throws IllegalException
				// The workaround for this is to just force recreation of payloads.
				mParent.removeAllViews()
				wrapper = null
				mFragment = null
				initializeView()
			}
		}
	}

	internal fun saveFragment(bundle: Bundle) {
		val fragment = mFragment
		if (fragment != null) {
			mActivity.supportFragmentManager.putFragment(bundle, mFragmentTag, fragment)
		}
	}

	private fun calculateTargetTranslation(toView: View, offset: Point, offsets: Offset): Point {
		val wrapper = requireNotNull(wrapper)
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
		if (wrapper == null) {
			initializeView()
		}

		val wrapper = requireNotNull(wrapper)

		if (stickToTarget) {
			moveWithTarget()
		} else {
			moveWithPercentage(percentage)
		}

		if (initialTranslationZ != targetTranslationZ) {
			wrapper.translationZ = initialTranslationZ + (targetTranslationZ - initialTranslationZ) * percentage
		}

		wrapper.invalidate()
	}

	private fun moveWithTarget() {
		val wrapper = requireNotNull(wrapper)
		val targetOnScreen = mTargetView.locationOnScreen()
		val parentOnScreen = mParent.locationOnScreen()
		targetOffset = anchor.calculateEdgeOffset(wrapper, mTargetView)

		wrapper.translationX = (targetOnScreen[0] - parentOnScreen[0] + targetOffset.x + offsets.horizontal).toFloat()
		wrapper.translationY = (targetOnScreen[1] - parentOnScreen[1] + targetOffset.y + offsets.vertical).toFloat()
	}

	private fun moveWithPercentage(percentage: Float) {
		val wrapper = wrapper ?: throw NullPointerException("Wrapper is null")
		targetOffset = anchor.calculateEdgeOffsetWithPadding(wrapper, mTargetView)
		val targetTranslation = calculateTargetTranslation(mTargetView, targetOffset, offsets)
		wrapper.translationX = initialTranslation.x.toFloat() + targetTranslation.x * percentage
		wrapper.translationY = initialTranslation.y.toFloat() + targetTranslation.y * percentage
	}

	@Synchronized
	private fun onInitialPosition() {
		destroyLock.lock()

		if (destroyTimerTask == null) {
			if (destroyPayloadAfter > IMMEDIATELY) {
				destroyTimerTask = Timer(
						"Destroy",
						true
				).schedule(destroyPayloadAfter) { destroyFragment() }
			} else if (destroyPayloadAfter == IMMEDIATELY) {
				destroyFragment()
			}
		}

		destroyLock.unlock()
	}

	@Synchronized
	private fun destroyFragment() {
		val fragment = mFragment
		if (fragment?.isStateSaved != false) {
			return
		}

		destroyLock.lock()
		removeTimer()

		onBeforeDestroyed?.invoke(fragment)

		val ft = mActivity.supportFragmentManager.beginTransaction()
		ft.remove(fragment)
		ft.setTransition(TRANSIT_NONE)
		ft.commitAllowingStateLoss()

		mFragment = null
		val wrapper = wrapper
		this.wrapper = null
		mParent.post {
			mParent.removeView(wrapper)
		}
		destroyLock.unlock()
	}

	@Synchronized
	private fun removeTimer() {
		destroyLock.lock()

		destroyTimerTask?.cancel()
		destroyTimerTask = null

		destroyLock.unlock()
	}

	/**
	 * Called when there is permission response
	 *
	 * @param requestCode Request code of the permission
	 * @param success True if all permissions succeeded
	 */
	internal fun onPermissionResponse(requestCode: Int, success: Boolean) =
			mFragment?.onPermissionResponse(requestCode, success)


	/**
	 * Called when entering state
	 *
	 * @param state State which is being entered
	 */
	internal fun onEnterState(state: DraggableImageButton.State) {
		when (state) {
			DraggableImageButton.State.TARGET -> mFragment?.onEnter(mActivity)
			DraggableImageButton.State.INITIAL -> {
				mFragment?.onLeave(mActivity)
				onInitialPosition()
			}
		}
	}

	/**
	 * Called when leaving state
	 *
	 * @param state State which is being leaved
	 */
	internal fun onLeaveState(state: DraggableImageButton.State) {
		if (state == DraggableImageButton.State.INITIAL) {
			removeTimer()
		}
	}
}
