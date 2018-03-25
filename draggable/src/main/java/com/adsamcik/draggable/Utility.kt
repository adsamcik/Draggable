package com.adsamcik.draggable

import android.content.res.Resources
import android.graphics.PointF
import android.view.View

internal object Utility {
    internal fun Int.toDp() = (this / Resources.getSystem().displayMetrics.density).toInt()
    internal fun Int.toPx() = (this * Resources.getSystem().displayMetrics.density).toInt()

    internal fun calculateTargetTranslation(sourceView: View, toView: View, anchor: DragTargetAnchor, offset: Offset): PointF {
        val thisOnScreen = sourceView.locationOnScreen()
        val targetOnScreen = toView.locationOnScreen()
        val targetRelPos = anchor.calculateEdgeOffsetWithPadding(sourceView, toView)
        val targetX = (targetOnScreen[0] - thisOnScreen[0]) + targetRelPos.x + sourceView.translationX
        val targetY = (targetOnScreen[1] - thisOnScreen[1]) + targetRelPos.y + sourceView.translationY
        //Log.d("Draggable", "(${targetOnScreen[1]} - ${thisOnScreen[1]}) + ${targetRelPos.y} + ${sourceView.translationY} = $targetY")
        return PointF(targetX + offset.horizontal, targetY + offset.vertical)
    }

    internal fun between(firstConstraint: Int, secondConstraint: Int, number: Float): Boolean {
        return if (firstConstraint > secondConstraint)
            number in secondConstraint..firstConstraint
        else
            number in firstConstraint..secondConstraint
    }

    internal fun between(firstConstraint: Float, secondConstraint: Float, number: Float): Boolean {
        return if (firstConstraint > secondConstraint)
            number in secondConstraint..firstConstraint
        else
            number in firstConstraint..secondConstraint
    }

    internal fun betweenInPercent(firstConstraint: Int, secondConstraint: Int, number: Float): Float {
        return (number - firstConstraint) / (secondConstraint - firstConstraint)
    }

    internal fun betweenInPercent(firstConstraint: Float, secondConstraint: Float, number: Float): Float {
        return if (firstConstraint == secondConstraint)
            0f
        else
            (number - firstConstraint) / (secondConstraint - firstConstraint)
    }
}