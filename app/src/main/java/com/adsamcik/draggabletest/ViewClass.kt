package com.adsamcik.draggabletest

import android.app.Fragment
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.adsamcik.draggable.IOnDemandView

class ViewClass : Fragment(), IOnDemandView {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater!!.inflate(R.layout.fragment_view_class, container, false)
        return view
    }
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