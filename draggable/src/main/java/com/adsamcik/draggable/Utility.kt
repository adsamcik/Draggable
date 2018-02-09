package com.adsamcik.draggable

import android.content.Context
import android.graphics.Point
import android.view.View
import kotlin.math.sign

object Utility {
    fun dpToPx(c: Context, dp: Int): Int = Math.round(dp * c.resources.displayMetrics.density)

    fun getLocationOnScreen(view: View): IntArray {
        val array = IntArray(2)
        view.getLocationOnScreen(array)
        return array
    }

    fun calculateTargetTranslation(sourceView: View, toView: View, anchor: DragTargetAnchor, marginPx: Int): Point {
        val thisOnScreen = getLocationOnScreen(sourceView)
        val targetOnScreen = getLocationOnScreen(toView)
        val targetRelPos = anchor.calculateEdgeOffset(toView, sourceView)
        val targetX = ((targetOnScreen[0] - thisOnScreen[0]) + targetRelPos.x + sourceView.translationX).toInt()
        val targetY = ((targetOnScreen[1] - thisOnScreen[1]) + targetRelPos.y + sourceView.translationY).toInt()
        return Point(targetX - targetX.sign * marginPx, targetY - targetY.sign * marginPx)
    }

    fun between(firstConstraint: Int, secondConstraint: Int, number: Float): Boolean {
        return if (firstConstraint > secondConstraint)
            number in secondConstraint..firstConstraint
        else
            number in firstConstraint..secondConstraint
    }

    fun betweenInPercent(firstConstraint: Int, secondConstraint: Int, number: Float): Float {
        return if (firstConstraint > secondConstraint)
            (number - secondConstraint) / (firstConstraint - secondConstraint)
        else
            (number - firstConstraint) / (secondConstraint - firstConstraint)
    }
}