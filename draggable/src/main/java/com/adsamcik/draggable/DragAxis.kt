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

    abstract fun isVertical(): Boolean
    abstract fun isHorizontal(): Boolean
}