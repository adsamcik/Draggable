[![Build Status](https://travis-ci.org/adsamcik/Draggable.svg?branch=master)](https://travis-ci.org/adsamcik/Draggable)
[ ![Download](https://api.bintray.com/packages/adsamcik/android-components/draggable/images/download.svg) ](https://bintray.com/adsamcik/android-components/draggable/_latestVersion)

# Draggable

Draggable is Android library providing draggable elements. For now only supported element is 
DraggableImageButton.

# Features
- Dragging along X or Y axis (both axis in future update)
- Anchor based customization with basic margin
- Payload (Fragment that is asociated with the button and can have independent from and to locations)
- Support for Enter/Leave and permission callbacks on Fragments
- Written using Kotlin

# Example (Kotlin)

    val button = findViewById<DraggableImageButton>(R.id.leftRightButton)
    button.setDrag(DragAxis.Y) //sets axis along which the drag will happen

    val parent = button.parent as View //finds parent
    parent.measure(View.MeasureSpec.AT_MOST, View.MeasureSpec.AT_MOST)

    button.setTarget(parent, DragTargetAnchor.BottomRight, 8) //Sets target, anchor and margin
    button.setTargetTranslationZ(200f) // sets translationZ in target position
    
    //Payload
    val payload = DraggablePayload(this, ViewClass::class.java, Point(-1080, 0), parent as ViewGroup, DragTargetAnchor.TopRight, 0)
    button.addPayload(payload)
