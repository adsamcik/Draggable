package com.adsamcik.draggabletest

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.adsamcik.draggable.IOnDemandView

class ViewClass : Fragment(), IOnDemandView {
    override fun onPermissionResponse(requestCode: Int, success: Boolean) {
        Log.d("ViewClass", "Permission request")
    }

    override fun onEnter(activity: FragmentActivity) {
        Log.d("ViewClass", "Enter")
    }

    override fun onLeave(activity: FragmentActivity) {
        Log.d("ViewClass", "Exit")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("ViewClass", "Create")
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d("ViewClass", "CreateView")
        retainInstance = false
        return inflater.inflate(R.layout.fragment_view_class, container, false)
    }

    override fun onDestroyView() {
        Log.d("ViewClass", "DestroyView")
        super.onDestroyView()
    }

    override fun onDestroy() {
        Log.d("ViewClass", "Destroy")
        super.onDestroy()
    }
}