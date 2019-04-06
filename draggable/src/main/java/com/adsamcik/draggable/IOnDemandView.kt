package com.adsamcik.draggable

import androidx.fragment.app.FragmentActivity

interface IOnDemandView {
	/**
	 * Called when entering the tab
	 *
	 * @param activity Activity (It passes only Activity to allow better future support for changes)
	 */
	fun onEnter(activity: FragmentActivity)

	/**
	 * Called when leaving tab
	 *
	 * @param activity Activity (It passes only Activity to allow better future support for changes)
	 */
	fun onLeave(activity: FragmentActivity)

	/**
	 * Called when permissions result comes back
	 *
	 * @param requestCode Request code of the permission request
	 * @param success True if all permissions succeeded
	 */
	fun onPermissionResponse(requestCode: Int, success: Boolean)
}