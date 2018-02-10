package com.adsamcik.draggabletest

import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.util.DisplayMetrics
import android.view.View
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

        leftButton.setDrag(DragAxis.X)
        leftButton.translationX = dpToPx(this, 16).toFloat()
        leftButton.setTarget(parent, DragTargetAnchor.TopRight, 8)
        leftButton.setTargetTranslationZ(200f)
        leftButton.addPayload(DraggablePayload(this, ViewClass::class.java, Point(-displayMetrics.widthPixels, 0), parent, DragTargetAnchor.Right, 0))

        rightButton.setDrag(DragAxis.X)
        rightButton.translationX = -dpToPx(this, 16).toFloat()
        rightButton.setTarget(parent, DragTargetAnchor.TopLeft, 8)
        rightButton.setTargetTranslationZ(200f)
        rightButton.addPayload(DraggablePayload(this, ViewClass::class.java, Point(displayMetrics.widthPixels, 0), parent, DragTargetAnchor.Left, 0))

        topButton.setDrag(DragAxis.Y)
        topButton.translationY = dpToPx(this, 16).toFloat()
        topButton.setTarget(parent, DragTargetAnchor.BottomRight, 8)
        topButton.setTargetTranslationZ(200f)
        topButton.addPayload(DraggablePayload(this, ViewClass::class.java, Point(0, -displayMetrics.heightPixels), parent, DragTargetAnchor.Bottom, 0))

        bottomButton.setDrag(DragAxis.Y)
        bottomButton.translationY = -dpToPx(this, 56).toFloat()
        bottomButton.setTarget(parent, DragTargetAnchor.TopRight, 8)
        bottomButton.setTargetTranslationZ(200f)
        bottomButton.addPayload(DraggablePayload(this, ViewClass::class.java, Point(0, displayMetrics.heightPixels), parent, DragTargetAnchor.Top, 0))



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
