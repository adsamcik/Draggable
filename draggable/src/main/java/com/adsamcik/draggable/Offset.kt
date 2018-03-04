package com.adsamcik.draggable

import com.adsamcik.draggable.Utility.toPx

/**
 * Offset object containing horizontal and vertical offset
 */
class Offset(var horizontal: Int, var vertical: Int) {
    constructor() : this(0, 0)
    constructor(offset: Int) : this(offset, offset)
    constructor(offset: Offset) : this(offset.horizontal, offset.vertical)

    /**
     * Updates this object with value from [offsets]
     * @param offsets Offset in density independent pixels
     */
    fun setWithDpAsPx(offsets: Offset) {
        horizontal = offsets.horizontal.toPx()
        vertical = offsets.vertical.toPx()
    }

    override fun toString() = "(horizontal: $horizontal, vertical: $vertical)"
}