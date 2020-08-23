package com.adsamcik.draggable

import android.view.View

internal fun View.locationOnScreen(): IntArray {
	val array = IntArray(2)
	getLocationOnScreen(array)
	return array
}
