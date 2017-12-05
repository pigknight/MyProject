package com.gwf.project.ui2.root

import android.view.View
import android.view.ViewGroup
import com.gwf.project.R
import com.gwf.project.fragmentui.FragmentUI
import kotlinx.android.synthetic.main.title_bar.view.*

/**
 * Created by gwf on 17-12-1.
 */
class Playing : FragmentUI() {
    companion object {
        private val TAG: String = Playing::class.java.getSimpleName()
    }

    override fun onCreateContainer(): ViewGroup {
        return View.inflate(mContext, R.layout.playing, null) as ViewGroup
    }

    override fun onCreateChildUIContainer(): ViewGroup? {
        return null
    }

    override fun onInitialize() {
        mContainer?.title?.text = "Playing"
        mContainer?.back?.setOnClickListener {
            //this.exitSelf(R.anim.push_top_in, R.anim.push_bootom_out)
        }
    }

    override fun onShow() {

    }

    override fun onHide() {

    }
}