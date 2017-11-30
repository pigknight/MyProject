package com.gwf.project.ui.root

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.gwf.project.MainActivity
import com.gwf.project.R
import com.gwf.project.ui.Root
import com.gwf.project.ui.root.content.MusicList
import com.gwf.project.util.ui.DynamicUI
import kotlinx.android.synthetic.main.content.view.*

/**
 * Created by Administrator on 2017/9/29.
 */
class Content:DynamicUI() {
    companion object {
        private val TAG:String = Content::class.java.getSimpleName()
    }

    override fun onCreateContainer(): ViewGroup{
        return View.inflate(mContext, R.layout.content, null) as ViewGroup
    }

    override fun onCreateChildUIContainer(): ViewGroup?{
        return mContainer!!.findViewById(R.id.child_ui_container) as ViewGroup
    }

    override fun onInitialize(){
        setDefaultEnterAnimation(R.anim.push_right_in, R.anim.push_left_out)
        setDefaultExitAnimation(R.anim.push_left_in, R.anim.push_right_out)

        mContainer?.play_control_bar?.setOnClickListener{
            Root.getInstance().enterUI(Playing::class.java,R.anim.push_bottom_in,R.anim.push_top_out)
        }
    }

    override fun onShow(reshow: Boolean){

    }

    override fun onHide(){

    }
}