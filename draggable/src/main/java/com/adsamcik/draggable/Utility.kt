package com.adsamcik.draggable

import android.content.Context

fun dpToPx(c: Context, dp: Int): Int = Math.round(dp * c.resources.displayMetrics.density)