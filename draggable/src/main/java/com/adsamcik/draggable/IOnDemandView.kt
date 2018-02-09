package com.adsamcik.draggable

import android.app.Activity

interface IOnDemandView {
    /**
     * Called when entering the tab
     *
     * @return if tab successfully loaded
     */
    fun onEnter(activity: Activity)

    /**
     * Called when leaving tab
     */
    fun onLeave(activity: Activity)

    /**
     * Called when permissions result comes back
     *
     * @param success success
     */
    fun onPermissionResponse(requestCode: Int, success: Boolean)
}