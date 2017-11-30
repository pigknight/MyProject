package com.gwf.project.ui.root.content

import android.content.Context
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.album_list.view.*
import kotlinx.android.synthetic.main.title_bar.view.*
import com.gwf.project.R
import com.gwf.project.util.ui.DynamicUI
import com.gwf.project.ui.Root
import com.gwf.project.ui.root.Content


/**
 * Created by Administrator on 2017/9/29.
 */
class AlbumList:DynamicUI() {
    companion object {
        private val TAG:String = AlbumList::class.java.getSimpleName()
    }

    override fun onCreateContainer(): ViewGroup {
        return View.inflate(mContext, R.layout.album_list, null) as ViewGroup
    }

    override fun onCreateChildUIContainer(): ViewGroup? {
        return null
    }

    override fun onInitialize() {
        mContainer?.title?.text = "Album List"

        mContainer?.back?.setOnClickListener {
            this.exitSelf()
        }

        mContainer?.album_1?.setOnClickListener{
            Root.getInstance().findUI(Content::class.java)?.enterUI(MusicList::class.java)
        }

        mContainer?.album_2?.setOnClickListener{
            Root.getInstance().findUI(Content::class.java)?.enterUI(MusicList::class.java)
        }

        mContainer?.album_3?.setOnClickListener{
            Root.getInstance().findUI(Content::class.java)?.enterUI(MusicList::class.java)
        }

        mContainer?.album_4?.setOnClickListener{
            Root.getInstance().findUI(Content::class.java)?.enterUI(MusicList::class.java)
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