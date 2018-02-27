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

        leftButton.dragAxis = DragAxis.X
        leftButton.translationX = dpToPx(this, 16).toFloat()
        leftButton.setTarget(parent, DragTargetAnchor.TopRight, 8)
        leftButton.targetTranslationZ = dpToPx(this, 25).toFloat()
        val leftPayload = DraggablePayload(this, ViewClass::class.java, Point(dpToPx(this, 32), 0), parent, DragTargetAnchor.Right, 0)
        leftPayload.initialTranslationZ = -1f
        leftPayload.targetTranslationZ = dpToPx(this, 26).toFloat()
        leftPayload.destroyPayloadAfter = 500
        leftButton.addPayload(leftPayload)
        leftPayload.onInitialized = { it.view!!.setBackgroundColor(Color.BLUE) }

        rightButton.dragAxis = DragAxis.X
        rightButton.translationX = -dpToPx(this, 16).toFloat()
        rightButton.setTarget(parent, DragTargetAnchor.TopLeft, 8)
        rightButton.targetTranslationZ = 200f
        rightButton.addPayload(DraggablePayload(this, ViewClass::class.java, Point(displayMetrics.widthPixels, 0), parent, DragTargetAnchor.Left, 0))

        topButton.dragAxis = DragAxis.XY
        topButton.translationY = dpToPx(this, 16).toFloat()
        topButton.setTarget(parent, DragTargetAnchor.BottomRight, 8)
        topButton.targetTranslationZ = 200f
        topButton.increaseTouchAreaBy(dpToPx(this, 32))
        val topPayload = DraggablePayload(this, ViewClass::class.java, Point(0, -displayMetrics.heightPixels), parent, DragTargetAnchor.Bottom, 0)
        topPayload.onInitialized = { it.view!!.setBackgroundColor(Color.CYAN) }
        topButton.addPayload(topPayload)

        bottomButton.dragAxis = DragAxis.Y
        bottomButton.translationY = -dpToPx(this, 56).toFloat()
        bottomButton.setTarget(parent, DragTargetAnchor.TopRight, 8)
        bottomButton.targetTranslationZ = 200f
        bottomButton.addPayload(DraggablePayload(this, ViewClass::class.java, Point(0, displayMetrics.heightPixels / 2), parent, DragTargetAnchor.Top, 0))
        bottomButton.onEnterStateListener = { button, state ->
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
        /*button.addPayload(DraggablePayload(this, ViewClass::class.java, Point(0, 0), parent as ViewGroup, DragTargetAnchor.TopLeft, 0))
        button.addPayload(DraggablePayload(this, ViewClass::class.java, Point(0, 0), parent as ViewGroup, DragTargetAnchor.Middle, 0))
        button.addPayload(DraggablePayload(this, ViewClass::class.java, Point(0, 0), parent as ViewGroup, DragTargetAnchor.Left, 0))
        button.addPayload(DraggablePayload(this, ViewClass::class.java, Point(0, 0), parent as ViewGroup, DragTargetAnchor.Right, 0))
        button.addPayload(DraggablePayload(this, ViewClass::class.java, Point(0, 0), parent as ViewGroup, DragTargetAnchor.Bottom, 0))
        button.addPayload(DraggablePayload(this, ViewClass::class.java, Point(0, 0), parent as ViewGroup, DragTargetAnchor.BottomLeft, 0))
        button.addPayload(DraggablePayload(this, ViewClass::class.java, Point(0, 0), parent as ViewGroup, DragTargetAnchor.BottomRight, 0))*/
        //button.attachView(ViewClass::class.java, DragTargetAnchor.Right, 0)

    }
}
