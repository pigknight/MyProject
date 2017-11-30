package com.gwf.project.ui

import android.content.Context
import android.view.ViewGroup
import com.gwf.project.MainActivity
import com.gwf.project.R
import com.gwf.project.dynamicui.DynamicUI
import kotlin.properties.Delegates

/**
 * Created by Administrator on 2017/9/28.
 */
class Root(context: Context) : DynamicUI(context, true) {
    companion object {
        private val TAG: String = Root::class.java.getSimpleName()

        private var mInstance: Root by Delegates.notNull()

        fun initInstance(context: Context) {
            mInstance = Root(context)
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
        setDefaultEnterAnimation(R.anim.push_right_in, R.anim.push_left_out)
        setDefaultExitAnimation(R.anim.push_left_in, R.anim.push_right_out)

        mContainer?.setOnLongClickListener {
            Root.getInstance().dumpUIArch()
            true
        }
    }

    override fun onShow(reshow: Boolean) {

    }

    override fun onHide() {

    }
}