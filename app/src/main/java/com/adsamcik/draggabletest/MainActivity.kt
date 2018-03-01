package com.adsamcik.draggabletest

import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.util.DisplayMetrics
import android.view.ViewGroup
import com.adsamcik.draggable.DragAxis
import com.adsamcik.draggable.DragTargetAnchor
import com.adsamcik.draggable.DraggableImageButton
import com.adsamcik.draggable.DraggablePayload
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : FragmentActivity() {
    private fun dpToPx(c: Context, dp: Int): Int = Math.round(dp * c.resources.displayMetrics.density)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val parent = leftButton.parent as ViewGroup

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val leftPayload = DraggablePayload(this, ViewClass::class.java, Point(dpToPx(this, 32), 0), parent, DragTargetAnchor.RightMiddle, 0)
        leftPayload.initialTranslationZ = -1f
        leftPayload.targetTranslationZ = dpToPx(this, 24).toFloat()
        leftPayload.destroyPayloadAfter = 500
        leftButton.addPayload(leftPayload)
        leftPayload.onInitialized = { it.view!!.setBackgroundColor(Color.BLUE) }

        topButton.dragAxis = DragAxis.XY
        topButton.translationY = dpToPx(this, 16).toFloat()
        topButton.setTarget(parent, DragTargetAnchor.RightBottom, 8)
        topButton.targetTranslationZ = 200f
        topButton.increaseTouchAreaBy(dpToPx(this, 32))
        val topPayload = DraggablePayload(this, ViewClass::class.java, Point(0, -displayMetrics.heightPixels), parent, DragTargetAnchor.MiddleBottom, 0)
        topPayload.onInitialized = { it.view!!.setBackgroundColor(Color.CYAN) }
        topButton.addPayload(topPayload)

        topButton.onEnterStateListener = { button, state, axis ->
            if (axis == DragAxis.Y) {
                if (state == DraggableImageButton.State.INITIAL)
                    button.setBackgroundColor(Color.YELLOW)
                else
                    button.setBackgroundColor(Color.BLUE)
            } else {
                if (state == DraggableImageButton.State.INITIAL)
                    button.setBackgroundColor(Color.GRAY)
                else
                    button.setBackgroundColor(Color.GREEN)
            }
        }

        bottomButton.dragAxis = DragAxis.Y
        bottomButton.translationY = -dpToPx(this, 56).toFloat()
        bottomButton.setTarget(parent, DragTargetAnchor.RightTop, 8)
        bottomButton.targetTranslationZ = 200f
        bottomButton.addPayload(DraggablePayload(this, ViewClass::class.java, Point(0, displayMetrics.heightPixels / 2), parent, DragTargetAnchor.MiddleTop, 0))
        bottomButton.onEnterStateListener = { button, state, _ ->
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
        bottomButton.increaseTouchAreaBy(dpToPx(this, 64), dpToPx(this, 64), dpToPx(this, 64), dpToPx(this, 64))


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
}
