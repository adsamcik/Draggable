package com.adsamcik.draggable

import android.app.Activity

interface IOnDemandView {
    /**
     * Called when entering the tab
     *
     * @param activity Activity (It passes only Activity to allow better future support for changes)
     */
    fun onEnter(activity: Activity)

    /**
     * Called when leaving tab
     *
     * @param activity Activity (It passes only Activity to allow better future support for changes)
     */
    fun onLeave(activity: Activity)

    /**
     * Called when permissions result comes back
     *
     * @param requestCode Request code of the permission request
     * @param success True if all permissions succeeded
     */
    fun onPermissionResponse(requestCode: Int, success: Boolean)
}