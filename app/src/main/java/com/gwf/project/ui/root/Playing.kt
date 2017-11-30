package com.gwf.project.ui.root

import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import com.gwf.project.R
import com.gwf.project.ui.Root
import com.gwf.project.util.ui.DynamicUI
import kotlinx.android.synthetic.main.title_bar.view.*


/**
 * Created by Administrator on 2017/9/29.
 */
class Playing: DynamicUI() {
    companion object {
        private val TAG:String = Playing::class.java.getSimpleName()
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
            this.exitSelf(R.anim.push_top_in,R.anim.push_bootom_out)
        }
    }

    override fun onShow(reshow: Boolean) {

    }

    override fun onHide() {

    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if( event.keyCode == KeyEvent.KEYCODE_BACK ){
            Root.getInstance().exitTopUI(R.anim.push_top_in,R.anim.push_bootom_out)
            return true
        }

        return super.onKeyEvent(event)
    }
}