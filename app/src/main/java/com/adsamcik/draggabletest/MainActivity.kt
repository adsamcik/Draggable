package com.adsamcik.draggabletest

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.adsamcik.draggable.DragAxis
import com.adsamcik.draggable.DragTargetAnchor
import com.adsamcik.draggable.DraggableImageButton
import com.adsamcik.draggable.DraggablePayload
import com.adsamcik.draggable.Offset
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {
	private fun dpToPx(c: Context, dp: Int): Int =
			(dp * c.resources.displayMetrics.density).roundToInt()

	private val leftButton by lazy { findViewById<DraggableImageButton>(R.id.leftButton) }
	private val rightButton by lazy { findViewById<DraggableImageButton>(R.id.rightButton) }
	private val topButton by lazy { findViewById<DraggableImageButton>(R.id.topButton) }
	private val bottomButton by lazy { findViewById<DraggableImageButton>(R.id.bottomButton) }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)


		val parent = leftButton.parent as ViewGroup

		val displayMetrics = DisplayMetrics()
		windowManager.defaultDisplay.getMetrics(displayMetrics)

		val leftPayload = DraggablePayload(this, ViewClass::class.java, parent, leftButton)
		leftPayload.anchor = DragTargetAnchor.Middle
		leftPayload.initialTranslationZ = -1f
		leftPayload.targetTranslationZ = dpToPx(this, 24).toFloat()
		leftPayload.destroyPayloadAfter = 500
		leftPayload.onInitialized = { requireNotNull(it.view).setBackgroundColor(Color.BLUE) }
		leftPayload.stickToTarget = true
		leftButton.addPayload(leftPayload)

		topButton.extendTouchAreaBy(dpToPx(this, 32))
		val topPayload = DraggablePayload(this, ViewClass::class.java, parent, leftButton)
		topPayload.initialTranslation = Point(0, -400)
		topPayload.anchor = DragTargetAnchor.LeftBottom
		topPayload.onInitialized = {
			val view = requireNotNull(it.view)
			view.setBackgroundColor(Color.CYAN)
			view.setOnClickListener { }
		}
		topPayload.targetTranslationZ = dpToPx(this, 16).toFloat()
		topButton.addPayload(topPayload)
		topButton.onEnterStateListener = { button, state, axis, _ ->
			if (axis == DragAxis.Y) {
				if (state == DraggableImageButton.State.INITIAL)
					button.setBackgroundColor(Color.YELLOW)
				else
					button.setBackgroundColor(Color.BLUE)
			} else if (axis == DragAxis.X) {
				if (state == DraggableImageButton.State.INITIAL) {
					button.setBackgroundColor(Color.GRAY)
					bottomButton.moveToState(DraggableImageButton.State.INITIAL, true)
				} else {
					button.setBackgroundColor(Color.GREEN)
					bottomButton.moveToState(DraggableImageButton.State.TARGET, false)
				}
			}
		}

		bottomButton.dragAxis = DragAxis.Y
		//bottomButton.translationY = -dpToPx(this, 56).toFloat()
		bottomButton.setTarget(parent, DragTargetAnchor.LeftTop)
		bottomButton.setTargetOffsetDp(Offset(8))
		bottomButton.targetTranslationZ = dpToPx(this, 24).toFloat()
		val payload = DraggablePayload(this, ViewClass::class.java, parent, parent)
		payload.initialTranslation = Point(0, displayMetrics.heightPixels / 2)
		payload.anchor = DragTargetAnchor.LeftTop
		payload.setOffsetsDp(Offset(16))
		bottomButton.addPayload(payload)
		bottomButton.onEnterStateListener = { button, state, _, change ->
			Log.d("StateChange", "Enter state $state change? $change")
			when (state) {
                DraggableImageButton.State.INITIAL -> {
                    button.setBackgroundColor(Color.GREEN)
                    button.forEachPayload { it.backgroundColor = Color.GREEN }
                }
                DraggableImageButton.State.TARGET -> {
                    button.setBackgroundColor(Color.RED)
                    button.forEachPayload { it.backgroundColor = Color.RED }
                }
			}
		}
		bottomButton.onLeaveStateListener = { _, state ->
			Log.d("StateChange", "Leave state $state")
		}

		val lameButton = Button(this)
		lameButton.id = View.generateViewId()
		parent.addView(lameButton)

		//bottomButton.extendTouchAreaBy(dpToPx(this, 64), dpToPx(this, 64), dpToPx(this, 64), dpToPx(this, 64))


		//button.addPayload(DraggablePayload(this, ViewClass::class.java, Point(0, 0), parent, DragTargetAnchor.Middle, 0, WRAP_CONTENT, WRAP_CONTENT))
		/*button.addPayload(DraggablePayload(this, ViewClass::class.java, Point(0, 0), parent as ViewGroup, DragTargetAnchor.LeftTop, 0))
		button.addPayload(DraggablePayload(this, ViewClass::class.java, Point(0, 0), parent as ViewGroup, DragTargetAnchor.Middle, 0))
		button.addPayload(DraggablePayload(this, ViewClass::class.java, Point(0, 0), parent as ViewGroup, DragTargetAnchor.LeftMiddle, 0))
		button.addPayload(DraggablePayload(this, ViewClass::class.java, Point(0, 0), parent as ViewGroup, DragTargetAnchor.RightMiddle, 0))
		button.addPayload(DraggablePayload(this, ViewClass::class.java, Point(0, 0), parent as ViewGroup, DragTargetAnchor.MiddleBottom, 0))
		button.addPayload(DraggablePayload(this, ViewClass::class.java, Point(0, 0), parent as ViewGroup, DragTargetAnchor.LeftBottom, 0))
		button.addPayload(DraggablePayload(this, ViewClass::class.java, Point(0, 0), parent as ViewGroup, DragTargetAnchor.RightBottom, 0))*/
		//button.attachView(ViewClass::class.java, DragTargetAnchor.RightMiddle, 0)

	}

	override fun onBackPressed() {
		//super.onBackPressed()
		topButton.moveToState(DraggableImageButton.State.INITIAL, true)
		leftButton.moveToState(DraggableImageButton.State.INITIAL, false)
		//rightButton.moveToState(DraggableImageButton.State.TARGET, false)
		bottomButton.moveToState(DraggableImageButton.State.TARGET, true)
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)

		topButton.saveFragments(outState)
		leftButton.saveFragments(outState)
		bottomButton.saveFragments(outState)
		rightButton.saveFragments(outState)
	}

	override fun onRestoreInstanceState(savedInstanceState: Bundle) {
		super.onRestoreInstanceState(savedInstanceState)

		topButton.restoreFragments(savedInstanceState)
		leftButton.restoreFragments(savedInstanceState)
		bottomButton.restoreFragments(savedInstanceState)
		rightButton.restoreFragments(savedInstanceState)
	}
}
