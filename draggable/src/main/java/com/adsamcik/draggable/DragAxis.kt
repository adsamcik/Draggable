package com.adsamcik.draggable

enum class DragAxis {
    None {
        override fun isHorizontal(): Boolean = false
        override fun isVertical(): Boolean = false
    },
    X {
        override fun isHorizontal(): Boolean = true
        override fun isVertical(): Boolean = false
    },
    Y {
        override fun isHorizontal(): Boolean = false
        override fun isVertical(): Boolean = true
    },
    XY {
        override fun isHorizontal(): Boolean = true
        override fun isVertical(): Boolean = true
    };

    /**
     * Returns whether the axis is vertical
     * @return true if axis is vertical
     */
    abstract fun isVertical(): Boolean

    /**
     * Returns whether the axis is horizontal
     * @return true if axis is horizontal
     */
    abstract fun isHorizontal(): Boolean
}