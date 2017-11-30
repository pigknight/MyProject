package com.gwf.project.util.ui.adapter

import android.os.Parcelable
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup

/**
 * Created by Administrator on 2017/10/13.
 */
class ViewPagerAdapter(var mListViews: List<View>) : PagerAdapter() {

    override fun destroyItem(parentView: ViewGroup, position: Int, data: Any) {
        (parentView as ViewPager).removeView(mListViews[position])
    }

    override fun finishUpdate(arg0: ViewGroup) {}

    override fun getCount(): Int {
        return mListViews.size
    }

    override fun instantiateItem(parentView: ViewGroup, position: Int): Any {
        (parentView as ViewPager).addView(mListViews[position], 0)
        return mListViews[position]
    }

    override fun isViewFromObject(arg0: View, arg1: Any): Boolean {
        return arg0 === arg1
    }

    override fun restoreState(arg0: Parcelable?, arg1: ClassLoader?) {}

    override fun saveState(): Parcelable? {
        return null
    }

    override fun startUpdate(parentView: ViewGroup) {}
}