package com.adsamcik.draggable

import android.content.Context

class Margins(var left: Int, var top: Int, var right: Int, var bottom: Int) {
    constructor(margin: Int) : this(margin, margin, margin, margin)
    constructor(horizontal: Int, vertical: Int) : this(horizontal, vertical, horizontal, vertical)
    constructor(margins: Margins) : this(margins.left, margins.top, margins.right, margins.bottom)

    fun setWith(margins: Margins, func: (Int) -> Int) {
        left = func.invoke(margins.left)
        top = func.invoke(margins.top)
        right = func.invoke(margins.right)
        bottom = func.invoke(margins.bottom)
    }

    fun setFromDpToPx(context: Context, margins: Margins) {
        val dpPx = Utility.dpToPx(context, 1)
        left = dpPx * margins.left
        top = dpPx * margins.top
        right = dpPx * margins.right
        bottom = dpPx * margins.bottom
    }
}