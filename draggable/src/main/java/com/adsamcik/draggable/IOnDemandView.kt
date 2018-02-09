package com.adsamcik.draggable

import android.support.v4.app.FragmentActivity

interface IOnDemandView {
    /**
     * Called when entering the tab
     *
     * @return if tab successfully loaded
     */
    fun onEnter(activity: FragmentActivity)

    /**
     * Called when leaving tab
     */
    fun onLeave(activity: FragmentActivity)

    /**
     * Called when permissions result comes back
     *
     * @param success success
     */
    fun onPermissionResponse(requestCode: Int, success: Boolean)
}