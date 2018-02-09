package com.adsamcik.draggabletest

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.adsamcik.draggable.DragAxis
import com.adsamcik.draggable.DragTargetAnchor
import com.adsamcik.draggable.DraggableImageButton

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<DraggableImageButton>(R.id.leftRightButton)
        button.setDrag(DragAxis.X)
        button.setTarget(button.parent as View, DragTargetAnchor.Right, 8)
        //button.attachView(ViewClass::class.java, DragTargetAnchor.Right, 0)

    }
}
