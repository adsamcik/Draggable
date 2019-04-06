[![Build Status](https://travis-ci.org/adsamcik/Draggable.svg?branch=master)](https://travis-ci.org/adsamcik/Draggable)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/51e4ee2c8cbf42e58d4013e3d6ca2fa8)](https://www.codacy.com/app/adsamcik/Draggable?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=adsamcik/Draggable&amp;utm_campaign=Badge_Grade)
[ ![Download](https://api.bintray.com/packages/adsamcik/android-components/draggable/images/download.svg) ](https://bintray.com/adsamcik/android-components/draggable/_latestVersion)

# Draggable

Draggable is Android library providing draggable elements. For now only supported element is 
DraggableImageButton.

## Versions
To easily understand how much has change since the previous version, here is explanation what change in version numbers mean

*MajorChange.BreakingChange.MinorChange*

Every major change will increment first number
Every breaking change will increment second number
Every minor change will increment third number

Version before 1.0.0 can have exceptions to this rule because they are considered pre-release and won't increment first number until the library is considered ready.

Eg. 

    0.1.0 -> 0.1.1 was most likely a bugfix
    0.1.1 -> 0.2.0 could change how public method works or remove it entirely 
    0.2.0 -> 1.0.0 there has been large change in the project (can be just under the hood though)

# Features
- Dragging along X or Y axis (both axis in future update)
- Anchor based customization with basic margin
- Payload (Fragment that is asociated with the button and can have independent from and to locations)
- Support for Enter/Leave and permission callbacks on Fragments
- Written using Kotlin

# Example (Kotlin)

    val button = findViewById<DraggableImageButton>(R.id.leftRightButton)
    button.dragAxis = DragAxis.Y //sets axis along which the drag will happen

    val parent = button.parent as View //finds parent

    button.setTarget(parent, DragTargetAnchor.BottomRight, 8) //Sets target, anchor and margin
    button.targetTranslationZ = 200f // sets translationZ in target position
    
    //Payload
    val payload = DraggablePayload(this, ViewClass::class.java, Point(-1080, 0), parent as ViewGroup, DragTargetAnchor.TopRight, 0)
    button.addPayload(payload)
