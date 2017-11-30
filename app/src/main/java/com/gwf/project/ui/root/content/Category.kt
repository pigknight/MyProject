package com.gwf.project.ui.root.content

import android.support.v4.view.ViewPager
import android.view.ViewGroup
import com.gwf.project.util.ui.DynamicUI
import android.view.View
import com.gwf.project.R
import com.gwf.project.ui.Root
import com.gwf.project.ui.root.Content
import com.gwf.project.util.ui.adapter.ViewPagerAdapter
import kotlinx.android.synthetic.main.category.view.*
import kotlinx.android.synthetic.main.category_local.view.*
import kotlinx.android.synthetic.main.category_network.view.*
import kotlinx.android.synthetic.main.category_favorite.view.*
import java.util.ArrayList

/**
 * Created by Administrator on 2017/9/29.
 */
class Category:DynamicUI() {
    companion object {
        private val TAG:String = Category::class.java.getSimpleName()
    }

    private var mLayoutLocal:View? = null
    private var mLayoutNetwork:View? = null
    private var mLayoutFavorite:View? = null

    //private var mPager: ViewPager? = null//页卡内容
    private var mTabPageList: ArrayList<View>? = null // Tab页面列表

    private var mSelectedTabView: View? = null

    private fun mOnTabClickListener(v:View) {
        if (mSelectedTabView === v)
            return
        when (v) {
            mContainer?.tab_local -> {
                mContainer?.viewpager?.setCurrentItem(0,false)
            }
            mContainer?.tab_network ->{
                mContainer?.viewpager?.setCurrentItem(1,false)
            }
            mContainer?.tab_favorite -> {
                mContainer?.viewpager?.setCurrentItem(2,false)
            }
        }

        if (mSelectedTabView != null)
            mSelectedTabView!!.isSelected = false
        v.isSelected = true
        mSelectedTabView = v
    }

    override fun onCreateContainer(): ViewGroup {
        return View.inflate(mContext, R.layout.category, null) as ViewGroup
    }

    override fun onCreateChildUIContainer(): ViewGroup? {
        return null
    }

    override fun onInitialize() {
        setDefaultEnterAnimation(R.anim.push_right_in, R.anim.push_left_out)
        setDefaultExitAnimation(R.anim.push_left_in, R.anim.push_right_out)

        mContainer?.tab_local?.setOnClickListener{
            mOnTabClickListener(it)
        }
        mContainer?.tab_network?.setOnClickListener{
            mOnTabClickListener(it)
        }
        mContainer?.tab_favorite?.setOnClickListener{
            mOnTabClickListener(it)
        }

        mLayoutLocal = View.inflate(mContext, R.layout.category_local, null)
        mLayoutNetwork = View.inflate(mContext, R.layout.category_network, null)
        mLayoutFavorite = View.inflate(mContext, R.layout.category_favorite, null)

        mTabPageList = ArrayList<View>()

        mTabPageList?.add(mLayoutLocal!!)
        mTabPageList?.add(mLayoutNetwork!!)
        mTabPageList?.add(mLayoutFavorite!!)

        mContainer?.viewpager?.adapter = ViewPagerAdapter(mTabPageList!!)
        mContainer?.viewpager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(arg0: Int) {

            }

            override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {

            }

            override fun onPageSelected(index: Int) {
                when (index) {
                    0 -> mContainer?.tab_local?.performClick()
                    1 -> mContainer?.tab_network?.performClick()
                    2 -> mContainer?.tab_favorite?.performClick()
                }
            }

        })

        mLayoutLocal?.btn_view_local_album?.setOnClickListener{
            Root.getInstance().findUI(Content::class.java)?.enterUI(AlbumList::class.java)
        }

        mLayoutNetwork?.btn_view_network_album?.setOnClickListener{
            Root.getInstance().findUI(Content::class.java)?.enterUI(AlbumList::class.java)
        }

        mLayoutFavorite?.btn_view_favorite_album?.setOnClickListener{
            Root.getInstance().findUI(Content::class.java)?.enterUI(AlbumList::class.java)
        }

        mContainer?.tab_local?.performClick()
    }

    override fun onShow(reshow: Boolean) {

    }

    override fun onHide() {

    }
}