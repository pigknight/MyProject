package com.gwf.project.ui2

import android.content.Context
import android.view.ViewGroup
import com.gwf.project.MainActivity
import com.gwf.project.R
import com.gwf.project.fragmentui.FragmentUI
import kotlin.properties.Delegates

/**
 * Created by gwf on 17-12-1.
 */
class Root : FragmentUI() {
    companion object {
        private val TAG: String = Root::class.java.getSimpleName()

        private var mInstance: Root by Delegates.notNull()

        fun initInstance(context: Context) {
            mInstance = Root()
            mInstance.set(context,true)
        }

        fun getInstance(): Root {
            if (mInstance == null)
                throw RuntimeException(TAG + ": Must called the initInstance() before to call getInstance().")
            return mInstance
        }
    }

    override fun onCreateContainer(): ViewGroup {
        return (mContext as MainActivity).getMainContainer()
    }

    override fun onCreateChildUIContainer(): ViewGroup? {
        return mContainer!!.findViewById(R.id.child_ui_container) as ViewGroup
    }

    override fun onInitialize() {
        mContainer?.setOnLongClickListener {
            //Root.getInstance().dumpUIArch()
            true
        }
    }

    override fun onShow() {

    }

    override fun onHide() {

    }
}