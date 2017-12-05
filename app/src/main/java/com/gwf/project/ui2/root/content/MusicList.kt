package com.gwf.project.ui2.root.content

import android.view.View
import android.view.ViewGroup
import com.gwf.project.R
import com.gwf.project.fragmentui.FragmentUI
import kotlinx.android.synthetic.main.title_bar.view.*

/**
 * Created by gwf on 17-12-1.
 */
class MusicList : FragmentUI() {
    companion object {
        private val TAG: String = MusicList::class.java.getSimpleName()
    }

    override fun onCreateContainer(): ViewGroup {
        return View.inflate(mContext, R.layout.music_list, null) as ViewGroup
    }

    override fun onCreateChildUIContainer(): ViewGroup? {
        return null
    }

    override fun onInitialize() {
        mContainer?.title?.text = "Music List"

        mContainer?.back?.setOnClickListener {
            //this.exitSelf()
        }
    }

    override fun onShow() {

    }

    override fun onHide() {

    }
}