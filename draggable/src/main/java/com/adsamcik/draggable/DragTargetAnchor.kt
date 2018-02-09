package com.adsamcik.draggable

import android.graphics.Point
import android.view.View

enum class DragTargetAnchor {
    Top,
    TopRight,
    Right,
    BottomRight,
    Bottom,
    BottomLeft,
    Left,
    TopLeft,
    Middle;

    fun calculateEdgeOffset(parentView: View, thisView: View): Point {
        return when (this) {
            DragTargetAnchor.Top -> Point(parentView.width / 2 - thisView.width / 2, 0)
            DragTargetAnchor.TopRight -> Point(parentView.width - thisView.width, 0)
            DragTargetAnchor.Right -> Point(parentView.width - thisView.width, parentView.height / 2 - thisView.height / 2)
            DragTargetAnchor.BottomRight -> Point(parentView.width - thisView.width, parentView.height - thisView.height)
            DragTargetAnchor.Bottom -> Point(parentView.width / 2 - thisView.height / 2, parentView.height - thisView.height)
            DragTargetAnchor.BottomLeft -> Point(0, parentView.height - thisView.height)
            DragTargetAnchor.Left -> Point(0, parentView.height / 2 - thisView.height / 2)
            DragTargetAnchor.TopLeft -> Point(0, 0)
            DragTargetAnchor.Middle -> Point(parentView.width / 2 - thisView.height / 2, parentView.height / 2 - thisView.height / 2)
        }
    }
}