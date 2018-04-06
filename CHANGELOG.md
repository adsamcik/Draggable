Changelog
=========

## [0.11.1] - 6-4-2018

* Add hasChanged variable to onEnter to distinguish between true state changes

## [0.11.1] - 2-4-2018

* Fixed crash during activity restoration

## [0.11.0] - 31-3-2018

* EnterState is now called when listener is attached and button is in state (Reason for this larger version bump)
* Id should now be properly unique
* Destroy fragment when fragment is no longer existing no longer crashes library
* Timer should now be properly removed when onDestroyFragment is called

## [0.10.4] - 31-3-2018

* Fixed many bugs in fragment recreation which should now work much better

#### Known issues ####

* __Fragments are recreated twice__ (the first recreation is destroyed). 
This is caused by removing and adding fragment to different container using FragmentTransaction. 
There is a solution around this (changing view parent) but the problem 
with that solution is that the top view in the hierarchy of that fragment 
loses width and height.

## [0.10.3] - 30-3-2018

* Fixed fling behaving incorrectly due to absolute fling value
* Improved unique id generation for DraggablePayload
* State changes should be reported much more reliably now

## [0.10.2] - 27-3-2018

* Updated touch delegate to solve some issues

## [0.10.1] - 26-3-2018

* OnState is now properly called during fragment restoration

## [0.10.0] - 25-3-2018

* Add state saving including fragments. Needs to be called from activity to work properly. Currently not too tested, but it seems to work.

#### Known issues ####
   

* Button properties are not retained on rotation (eg. background color)

## [0.9.3] - 15-3-2018

* Extended touch area now checks if the view is visible

## [0.9.1] - 4-3-2018

* Fix moveToState crash when moving from the same state to the same state
* Fix uninitialized target position when moveToState is called before button is touched

## [0.9.0] - 4-3-2018

* Add moveToState function with options to force the move and/or not use animation
* TouchDelegates now have their own library
* onInitialize and onBeforeDestroyed now return fragments type instead of just Fragment
* Fix crash when touch area was set from xml

## [0.8.1] - 3-3-2018

* Hotfixed getInteger on xml extend touch area

## [0.8.0] - 3-3-2018

* Reworked payloads which should now have more stable movement
* Replaced margin with offset on X and Y axes
* Add option to stick payload to its target (This gives option to actually stick payload to the button, does not support other objects because position is updated onDrag)
* Add option to properly disable clicking
* Fixed clicking on button
* Fix no idea where to move issue

## [0.7.1] - 2-3-2018

* Fixed incorrect targetAnchor values in xml
* Renamed middle anchor to center anchor to better represent it's centering on both axes

## [0.7.0] - 1-3-2018

* Add xml attributes
* Fixed crash when targetView is null

## [0.6.0] - 26-2-2018

* Reworked how XY axis works. Axis movement is always restricted to single axis, but user can move it on either X or Y
* OnEnterListeners now also provide axis along which the button moved
* Improved drag axis detection. It should be now more consistent with
* Add option to disabled drag with DragAxis.None

## [0.5.3] - 26-2-2018

* Reverted previous cleanup add. Sorry for that :(
* Resolved crash

## [0.5.2] - 25-2-2018

* Add cleanup to payloads
* Minor javadoc update

## [0.5.1] - 22-2-2018

* Fix swapped onEnter/onExit events
* Remove Log

## [0.5.0] - 22-2-2018

* Add onLeaveState
* Changed onEnterState
* Add forEachPayload
* Fix bugs

## [0.4.1] - 13-2-2018

* Add onBeforeDestroyed to Payload
* Moved onInitialized to Payload

## [0.4.0] - 13-2-2018

* Add support for timed payload destruction
* Reworked internal states and fixed several bugs cause with unclear state value
* Add onPayloadInitialized listener

## [0.3.0] - 13-2-2018

* Add support for extended touch area
* Add fling
* Improved overall gesture handling
* Exposed animation interpolator, length and payloads

## [0.2.0] - 11-2-2018

* Improved control over translation Z
* Add listeners for enter initial and target state
* *BREAKING CHANGE* Updated available methods and changed some to accessors
* Buttons now calculate with padding
* Fixed issue with buttons being incorrectly dragged

## [0.1.2] - 10-2-2018

* Fixed payload moving in opposite direction when moving button from higher coordinate to lower coordinate

## [0.1.1] - 10-2-2018

* Moved to FragmentActivity and support Fragments (They should provide more consistency and be more widely used)

## [0.1.0] - 9-2-2018

* Initial release
