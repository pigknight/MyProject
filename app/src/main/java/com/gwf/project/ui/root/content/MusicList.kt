package com.gwf.project.ui.root.content

import android.content.Context
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import com.gwf.project.R
import com.gwf.project.ui.Root
import com.gwf.project.ui.root.Content
import com.gwf.project.util.ui.DynamicUI
import kotlinx.android.synthetic.main.music_list.view.*
import kotlinx.android.synthetic.main.title_bar.view.*



/**
 * Created by Administrator on 2017/9/29.
 */
class MusicList: DynamicUI() {
    companion object {
        private val TAG:String = MusicList::class.java.getSimpleName()
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
            this.exitSelf()
        }
    }

    override fun onShow(reshow: Boolean) {

    }

    override fun onHide() {

    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if( event.keyCode == KeyEvent.KEYCODE_BACK ){
            Root.getInstance().findUI(Content::class.java)?.exitTopUI()
            return true
        }

        return super.onKeyEvent(event)
    }
}