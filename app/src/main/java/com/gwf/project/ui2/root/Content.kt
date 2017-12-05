package com.gwf.project.ui2.root

import android.view.View
import android.view.ViewGroup
import com.gwf.project.R
import com.gwf.project.fragmentui.FragmentUI
import com.gwf.project.ui2.Root
import com.gwf.project.ui2.root.content.Category
import kotlinx.android.synthetic.main.content.view.*

/**
 * Created by gwf on 17-12-1.
 */
class Content : FragmentUI() {
    companion object {
        private val TAG: String = Content::class.java.getSimpleName()
    }

    override fun onCreateContainer(): ViewGroup {
        return View.inflate(mContext, R.layout.content, null) as ViewGroup
    }

    override fun onCreateChildUIContainer(): ViewGroup? {
        return mContainer!!.findViewById(R.id.child_ui_container) as ViewGroup
    }

    override fun onInitialize() {
        mContainer?.play_control_bar?.setOnClickListener {
            //Root.getInstance().enterUI(Playing::class.java, R.anim.push_bottom_in, R.anim.push_top_out)
        }

        //Root.getInstance().findUI(Content::class.java)?.enterUI(Category::class.java)
        enterUI(Category::class.java)
    }

    override fun onShow() {

    }

    override fun onHide() {

    }
}