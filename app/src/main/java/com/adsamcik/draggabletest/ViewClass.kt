package com.adsamcik.draggabletest

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.util.AttributeSet
import android.view.View
import com.adsamcik.draggable.IOnDemandView

class ViewClass : Fragment(), IOnDemandView {
    override fun onEnter(activity: FragmentActivity) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onLeave(activity: FragmentActivity) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPermissionResponse(requestCode: Int, success: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onHomeAction() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}