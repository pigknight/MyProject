package com.gwf.project.fragmentui

import android.app.Activity
import android.app.Fragment
import android.app.FragmentManager
import android.content.Context
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.util.*
import kotlin.properties.Delegates
import java.nio.file.Files.size
import java.nio.file.Files.size





/**
 * Created by gwf on 17-11-30.
 */
abstract class FragmentUI : Fragment() {
    companion object {
        val TAG: String = FragmentUI::class.java.getSimpleName()
    }

    protected var DEBUG_INFO: Boolean = false
    protected var DEBUG_ERR: Boolean = false

    private var mIsRoot: Boolean = false

    //Application Context
    protected var mContext: Context by Delegates.notNull()

    protected var mContainer: ViewGroup by Delegates.notNull()

    private var mChildUIContainer: ViewGroup? = null

    private var mParentUI: FragmentUI? = null

    private var mChildUIMap: HashMap<String,FragmentUI>? = null

    interface OnBackListener {
        fun onBack(obj:Any?)
    }

    private var mBackListener:OnBackListener? = null

    init{

    }

    //constructor()

    fun set(context: Context, isRoot: Boolean = false) {
        mContext = context
        mIsRoot = isRoot

        mContainer = onCreateContainer()
        if( mContainer != null ) {
            mChildUIContainer = onCreateChildUIContainer()
            if( mChildUIContainer != null )
                mChildUIMap = HashMap<String,FragmentUI>()
        }
    }

    fun setOnBackListener(listener:OnBackListener){
        mBackListener = listener
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        if( mContainer != null ) {
            return mContainer
        }else
            return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()

        onInitialize()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onDetach() {
        super.onDetach()

        mParentUI?.mChildUIMap?.remove(this.javaClass.simpleName)
    }

    fun <T> findUI(clazz: Class<T>): FragmentUI? {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw RuntimeException(TAG + ": findUI(...) must be called in main thread.")

            return null
        }

        var ui:FragmentUI? = null
        if( mChildUIMap != null ){
            ui = mChildUIMap?.get(clazz.simpleName)
        }

        return ui
    }

    fun <T> enterUI(clazz: Class<T>, inAnimRes: Int = -1, outAnimRes: Int = -1) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw RuntimeException(TAG + ": findUI(...) must be called in main thread.")

            return
        }

        if( mChildUIContainer != null ) {
            var newUi: FragmentUI = clazz.newInstance() as FragmentUI
            if (newUi != null) {
                newUi.set(mContext, false)
                newUi.mParentUI = this //Set Parent UI
                mChildUIMap?.put(clazz.simpleName,newUi)


                var fm: FragmentManager? = null
                if( mIsRoot )
                    fm = (mContext as Activity).getFragmentManager()
                else
                    fm = this.getChildFragmentManager()

                if( fm != null ) {
                    val tx = fm.beginTransaction()
                    tx.add(mChildUIContainer!!.id, newUi, clazz.simpleName)
                    //tx.addToBackStack(null)
                    tx.commit()
                }
            }
        }
    }

    abstract fun onCreateContainer(): ViewGroup

    abstract fun onCreateChildUIContainer(): ViewGroup?

    abstract fun onInitialize()

    abstract fun onShow()

    abstract fun onHide()


    fun dispatchBack(): Boolean {
        logInfo("Call dispatchKeyEvent on ${javaClass.simpleName}")
        var fm: FragmentManager? = null
        if( mIsRoot )
            fm = (mContext as Activity).getFragmentManager()
        else
            fm = this.getChildFragmentManager()

        if( fm != null ) {
            val fragments = fm.fragments ?: return false

            for (i in fragments.indices.reversed()) {
                val child = fragments[i]

                if (isFragmentBackHandled(child)) {
                    return true
                }
            }

            if (fm.backStackEntryCount > 0) {
                fm.popBackStack()
                return true
            }
        }
        return false
    }

    /**
     * 判断Fragment是否处理了Back键
     *
     * @return 如果处理了back键则返回 **true**
     */
    fun isFragmentBackHandled(fragment: Fragment?): Boolean {
        return (fragment != null
                && fragment.isVisible
                && fragment.userVisibleHint //for ViewPager
                )//&& fragment is FragmentBackHandler
                //&& (fragment as FragmentBackHandler).onBackPressed())
    }

    //=======================================================
    protected fun logInfo(msg: String) {
        if (DEBUG_INFO)
            Log.i(TAG, msg)
    }

    protected fun logErr(msg: String) {
        if (DEBUG_ERR)
            Log.e(TAG, msg)
    }
}