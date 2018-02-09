package com.adsamcik.draggabletest

import android.app.Activity
import android.graphics.Point
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import com.adsamcik.draggable.*


class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<DraggableImageButton>(R.id.leftRightButton)
        button.setDrag(DragAxis.Y)

        val parent = button.parent as View

        parent.measure(View.MeasureSpec.AT_MOST, View.MeasureSpec.AT_MOST)

        button.translationX = Utility.dpToPx(this, 36).toFloat()
        button.setTarget(parent, DragTargetAnchor.BottomRight, 8)
        parent as ViewGroup
        button.attachPayload(DraggablePayload(this, ViewClass::class.java, Point(-1080, 0), parent, DragTargetAnchor.TopRight, 0))
        //button.attachPayload(DraggablePayload(this, ViewClass::class.java, Point(0, 0), parent, DragTargetAnchor.Middle, 0, WRAP_CONTENT, WRAP_CONTENT))
        /*button.attachPayload(DraggablePayload(this, ViewClass::class.java, Point(0, 0), parent as ViewGroup, DragTargetAnchor.TopLeft, 0))
        button.attachPayload(DraggablePayload(this, ViewClass::class.java, Point(0, 0), parent as ViewGroup, DragTargetAnchor.Middle, 0))
        button.attachPayload(DraggablePayload(this, ViewClass::class.java, Point(0, 0), parent as ViewGroup, DragTargetAnchor.Left, 0))
        button.attachPayload(DraggablePayload(this, ViewClass::class.java, Point(0, 0), parent as ViewGroup, DragTargetAnchor.Right, 0))
        button.attachPayload(DraggablePayload(this, ViewClass::class.java, Point(0, 0), parent as ViewGroup, DragTargetAnchor.Bottom, 0))
        button.attachPayload(DraggablePayload(this, ViewClass::class.java, Point(0, 0), parent as ViewGroup, DragTargetAnchor.BottomLeft, 0))
        button.attachPayload(DraggablePayload(this, ViewClass::class.java, Point(0, 0), parent as ViewGroup, DragTargetAnchor.BottomRight, 0))*/
        //button.attachView(ViewClass::class.java, DragTargetAnchor.Right, 0)

    }
}
