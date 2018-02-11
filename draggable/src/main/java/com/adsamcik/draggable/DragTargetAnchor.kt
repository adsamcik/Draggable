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
        return addPaddingOffset(parentView, when (this) {
            DragTargetAnchor.Left -> Point(0, parentView.height / 2 - thisView.height / 2)
            DragTargetAnchor.Middle -> Point(parentView.width / 2 - thisView.height / 2, parentView.height / 2 - thisView.height / 2)
            DragTargetAnchor.Right -> Point(parentView.width - thisView.width, parentView.height / 2 - thisView.height / 2)

            DragTargetAnchor.TopLeft -> Point(0, 0)
            DragTargetAnchor.Top -> Point(parentView.width / 2 - thisView.width / 2, 0)
            DragTargetAnchor.TopRight -> Point(parentView.width - thisView.width, 0)

            DragTargetAnchor.BottomRight -> Point(parentView.width - thisView.width, parentView.height - thisView.height)
            DragTargetAnchor.Bottom -> Point(parentView.width / 2 - thisView.height / 2, parentView.height - thisView.height)
            DragTargetAnchor.BottomLeft -> Point(0, parentView.height - thisView.height)
        })
    }

    private fun addPaddingOffset(parentView: View, point: Point): Point {
        when (this) {
            DragTargetAnchor.Left -> {
                point.x += parentView.paddingLeft
                point.y += parentView.paddingTop - parentView.paddingBottom
            }
            DragTargetAnchor.Middle -> {
                point.x += parentView.paddingRight - parentView.paddingLeft
                point.y += parentView.paddingTop - parentView.paddingBottom
            }
            DragTargetAnchor.Right -> {
                point.x -= parentView.paddingRight
                point.y += parentView.paddingTop - parentView.paddingBottom
            }

            DragTargetAnchor.TopLeft -> {
                point.x += parentView.paddingLeft
                point.y += parentView.paddingTop
            }
            DragTargetAnchor.Top -> {
                point.x += parentView.paddingRight - parentView.paddingLeft
                point.y += parentView.paddingTop
            }
            DragTargetAnchor.TopRight -> {
                point.x -= parentView.paddingRight
                point.y += parentView.paddingTop
            }

            DragTargetAnchor.BottomLeft -> {
                point.x += parentView.paddingLeft
                point.y -= parentView.paddingBottom
            }
            DragTargetAnchor.Bottom -> {
                point.x += parentView.paddingRight - parentView.paddingLeft
                point.y -= parentView.paddingBottom
            }
            DragTargetAnchor.BottomRight -> {
                point.x -= parentView.paddingRight
                point.y -= parentView.paddingBottom
            }
        }
        return point
    }
}