package com.adsamcik.draggable

import android.graphics.Point
import android.view.View

enum class DragTargetAnchor {
	LeftMiddle,
	MiddleTop,
	RightMiddle,
	MiddleBottom,
	RightTop,
	RightBottom,
	LeftBottom,
	LeftTop,
	Middle;

	/**
	 * Calculates position when view is anchored. Takes parents padding into account.
	 *
	 * @param parentView Parent view for the anchored view
	 * @param thisView View for which position is calculated
	 * @return Point that represents upper left position for the view
	 */
	fun calculateEdgeOffsetWithPadding(thisView: View, parentView: View): Point =
			addPaddingOffset(parentView, when (this) {
				DragTargetAnchor.LeftMiddle -> Point(0, parentView.height / 2 - thisView.height / 2)
				DragTargetAnchor.Middle -> Point(parentView.width / 2 - thisView.height / 2, parentView.height / 2 - thisView.height / 2)
				DragTargetAnchor.RightMiddle -> Point(parentView.width - thisView.width, parentView.height / 2 - thisView.height / 2)

				DragTargetAnchor.LeftTop -> Point(0, 0)
				DragTargetAnchor.MiddleTop -> Point(parentView.width / 2 - thisView.width / 2, 0)
				DragTargetAnchor.RightTop -> Point(parentView.width - thisView.width, 0)

				DragTargetAnchor.RightBottom -> Point(parentView.width - thisView.width, parentView.height - thisView.height)
				DragTargetAnchor.MiddleBottom -> Point(parentView.width / 2 - thisView.height / 2, parentView.height - thisView.height)
				DragTargetAnchor.LeftBottom -> Point(0, parentView.height - thisView.height)
			})

	/**
	 * Calculates position when view is anchored. Takes parents padding into account.
	 *
	 * @param parentView Parent view for the anchored view
	 * @param thisView View for which position is calculated
	 * @return Point that represents upper left position for the view
	 */
	fun calculateEdgeOffset(thisView: View, parentView: View): Point = when (this) {
		DragTargetAnchor.LeftMiddle -> Point(0, parentView.height / 2 - thisView.height / 2)
		DragTargetAnchor.Middle -> Point(parentView.width / 2 - thisView.height / 2, parentView.height / 2 - thisView.height / 2)
		DragTargetAnchor.RightMiddle -> Point(parentView.width - thisView.width, parentView.height / 2 - thisView.height / 2)

		DragTargetAnchor.LeftTop -> Point(0, 0)
		DragTargetAnchor.MiddleTop -> Point(parentView.width / 2 - thisView.width / 2, 0)
		DragTargetAnchor.RightTop -> Point(parentView.width - thisView.width, 0)

		DragTargetAnchor.RightBottom -> Point(parentView.width - thisView.width, parentView.height - thisView.height)
		DragTargetAnchor.MiddleBottom -> Point(parentView.width / 2 - thisView.height / 2, parentView.height - thisView.height)
		DragTargetAnchor.LeftBottom -> Point(0, parentView.height - thisView.height)
	}

	private fun addPaddingOffset(parentView: View, point: Point): Point {
		when (this) {
			DragTargetAnchor.LeftMiddle -> {
				point.x += parentView.paddingLeft
				point.y += parentView.paddingTop - parentView.paddingBottom
			}
			DragTargetAnchor.Middle -> {
				point.x += parentView.paddingRight - parentView.paddingLeft
				point.y += parentView.paddingTop - parentView.paddingBottom
			}
			DragTargetAnchor.RightMiddle -> {
				point.x -= parentView.paddingRight
				point.y += parentView.paddingTop - parentView.paddingBottom
			}

			DragTargetAnchor.LeftTop -> {
				point.x += parentView.paddingLeft
				point.y += parentView.paddingTop
			}
			DragTargetAnchor.MiddleTop -> {
				point.x += parentView.paddingRight - parentView.paddingLeft
				point.y += parentView.paddingTop
			}
			DragTargetAnchor.RightTop -> {
				point.x -= parentView.paddingRight
				point.y += parentView.paddingTop
			}

			DragTargetAnchor.LeftBottom -> {
				point.x += parentView.paddingLeft
				point.y -= parentView.paddingBottom
			}
			DragTargetAnchor.MiddleBottom -> {
				point.x += parentView.paddingRight - parentView.paddingLeft
				point.y -= parentView.paddingBottom
			}
			DragTargetAnchor.RightBottom -> {
				point.x -= parentView.paddingRight
				point.y -= parentView.paddingBottom
			}
		}
		return point
	}

	companion object {
		/**
		 * Converts integer values from 1 to 15 into [DragTargetAnchor]
		 * Integer is converted based on bit representation in following order left, top, right, bottom
		 * If left-right or top-bottom have the same values they are converted to middle on given axis
		 *
		 * eg. 0b0010 represents right anchor, 0b1011 represents middle bottom anchor
		 *
		 * @throws IllegalArgumentException Throws illegal argument if values are not withing range
		 */
		fun fromInt(anchorInt: Int): DragTargetAnchor =
				when (anchorInt) {
					0b1101, 0b1000 -> LeftMiddle
					0b1110, 0b0100 -> MiddleTop
					0b0111, 0b0010 -> RightMiddle
					0b1011, 0b0001 -> MiddleBottom
					0b0110 -> RightTop
					0b0011 -> RightBottom
					0b1001 -> LeftBottom
					0b1100 -> LeftTop
					0b1111, 0b0101, 0b1010 -> Middle
					else -> throw IllegalArgumentException("$anchorInt does not correspond to an anchor")
				}
	}
}