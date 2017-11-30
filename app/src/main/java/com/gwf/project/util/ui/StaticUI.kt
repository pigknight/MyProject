package com.gwf.project.util.ui

import android.content.Context
import android.content.res.Configuration
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ViewAnimator
import java.util.HashMap

/**
 * Created by Administrator on 2017/9/28.
 */

/**
 * --------------------------------Usage---------------------------------------
 * 1. Each StaticUI which having a separate logic function must be derived from the base class 'StaticUI'.
 * 2. The sub-StaticUI must be rewrite all abstract methods of the Base StaticUI.
 * 3. Define and instantiate a StaticUI subclass as root StaticUI. Each application can have one or more root StaticUI(derived from the base class 'StaticUI' too).
 * 5. After you define and instantiate a StaticUI subclass,call registerChildUI(,,) to register it on the current StaticUI.
 * 6. Call switchChildUI(,,) to switch the child StaticUI of the current StaticUI(parent StaticUI).
 * 7. Call dispatchKeyEvent(event) to dispatch KeyEvent to the root StaticUI.
 * 8. Before the application exit, call dispatchFinalize() to release all.(There is not necessary)
 */

/**
 * The base class for the StaticUI class which is a logic function unit.
 */
abstract class StaticUI {
    private val TAG = StaticUI::class.java!!.getSimpleName()

    //Application Context
    protected var mContext: Context? = null

    //
    private val MSG_ID_RUNNABLE = 1
    private var mHandler: Handler? = null

    private var mPreviousUI: StaticUI? = null
    private var mNextUI: StaticUI? = null

    //The container view(in *.xml) which associated with the current StaticUI
    protected var mContainer: ViewGroup? = null

    protected var mTempContainer: ViewGroup? = null

    //A container view who will contains(or switch) children StaticUI in current StaticUI (mContainerView)
    private var mChildUIContainer: ViewGroup? = null

    private var mChildUIViewAnimator: ViewAnimator? = null

    private var mDefaultInAnimation: Animation? = null
    private var mDefaultOutAnimation: Animation? = null

    /*
     * Saved the child StaticUI object be registered,which will be switched
     * by the childKey registered.
     */
    private val mChildUIMap = HashMap<String, StaticUI>()

    private var mIsAttached = false

    /*
     * This ... you know.
     */
    var mIsInitialized = false
    /*
     * The childKey which corresponding the child StaticUI who is showing
     */
    var mCurChildKey: String? = null

    /*constructor(context: Context) : this(context,false){

    }*/

    constructor(context: Context, isRoot: Boolean = false) {
        mContext = context
        mIsInitialized = false

        /*
    	 * The Root StaticUI must be initialized immediately
    	 */
        if ( isRoot ) {
            initialize()

            mIsAttached = true
            onShow(false)
        }
    }

    fun setDefaultAnimation(inAnimRes: Int, outAnimRes: Int) {
        mDefaultInAnimation = AnimationUtils.loadAnimation(mContext, inAnimRes)
        mDefaultOutAnimation = AnimationUtils.loadAnimation(mContext, outAnimRes)
    }

    fun setDefaultAnimation( inAnim: Animation?, outAnim: Animation?) {
        mDefaultInAnimation = inAnim
        mDefaultOutAnimation = outAnim
    }


    /*
     * This is called when the StaticUI is Initializing.
     * You must implement this method and return the
     * Container View for this StaticUI.
     */
    abstract fun onCreateContainer(): ViewGroup

    /*
     * This is called when the StaticUI is Initializing.
     * You must implement this method and return a
     * Subclass of the ViewGroup which will contain the child StaticUI.
     */
    abstract fun onCreateChildUIContainer(): ViewGroup?

    /*
     * This is called when the StaticUI is Initializing.
     * You can do some task to initialize the StaticUI
     */
    abstract fun onInitialize()

    /*
     * This is called while the StaticUI is no longer in use.
     * You can release the StaticUI(layout) itself.
     */
    abstract fun onFinalize()

    /*
     * This is called while the StaticUI is Initialized and prepare
     * to show.
     * You can prepare the default data for the StaticUI to showing.
     * Or you can set the default focus
     */
    abstract fun onShow(reshow: Boolean)

    /*
     * This is called while the StaticUI is hide.
     */
    abstract fun onHide()

    /*
     * Activity.onPause();
     */
    open fun onPause(){

    }

    /*
     * Activity.onResume();
     */
    open fun onResume(){

    }

    /*
     * This is called while receive Key Event
     */
    open fun onKeyEvent(event: KeyEvent): Boolean{
        return false
    }

    /*
     * Configuration.ORIENTATION_PORTRAIT
     * Or
     * Configuration.ORIENTATION_LANDSCAPE
     */
    open fun isOrientationDependent(): Boolean{
        return false
    }

    private fun initialize() {
        mTempContainer = null
        mContainer = onCreateContainer()
        mChildUIContainer = onCreateChildUIContainer()
        if (mChildUIContainer != null) {

            mChildUIContainer!!.removeAllViews()

            if (mChildUIContainer is ViewAnimator) {
                mChildUIViewAnimator = mChildUIContainer as ViewAnimator?
            } else {
                mChildUIViewAnimator = ViewAnimator(mContext)
                val lp = mChildUIContainer!!.layoutParams
                if (lp != null)
                    mChildUIContainer!!.addView(mChildUIViewAnimator, ViewGroup.LayoutParams(lp.width, lp.height))
                else
                    mChildUIContainer!!.addView(mChildUIViewAnimator,
                            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            }
        }

        mHandler = object : Handler() {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    MSG_ID_RUNNABLE -> {
                        val r = msg.obj as Runnable
                        r?.run()
                    }
                }
            }
        }

        onInitialize()

        mIsInitialized = true
    }

    fun removeAllChildUI() {
        try {
            if (mChildUIMap.size > 0) {
                for (child in mChildUIMap.values) {
                    if ( child != null ) {
                        with(child) {
                            mIsAttached = false
                            dispatchOnHide()
                            dispatchRelease()
                        }
                    }
                }
            }

            mChildUIViewAnimator?.removeAllViews()

            mCurChildKey = null
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun dispatchRelease() {
        if (mCurChildKey != null && mCurChildKey != "") {
            val curShow = mChildUIMap[mCurChildKey!!]
            if (curShow != null) {
                curShow.dispatchRelease()

                mChildUIViewAnimator?.removeView(curShow.getContainer())
            }
        }

        mHandler?.removeMessages(MSG_ID_RUNNABLE)
        mHandler = null

        onFinalize()

        try {
            mChildUIViewAnimator?.removeAllViews()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        mIsInitialized = false
        mCurChildKey = null

        mContainer = null
        mChildUIContainer = null
        mChildUIViewAnimator = null
    }

    private fun dispatchOnShow(reshow: Boolean) {
        if (mCurChildKey != null && mCurChildKey != "") {
            val curShow = mChildUIMap[mCurChildKey!!]
            curShow?.dispatchOnShow(reshow)
        }

        onShow(reshow)
    }

    private fun dispatchOnHide() {
        if (mCurChildKey != null && mCurChildKey != "") {
            val curShow = mChildUIMap[mCurChildKey!!]
            curShow?.dispatchOnHide()
        }

        onHide()
    }

    fun dispatchOnPause() {
        if (mCurChildKey != null && mCurChildKey != "") {
            val curShow = mChildUIMap[mCurChildKey!!]
            curShow?.dispatchOnPause()
        }

        onPause()
    }

    fun dispatchOnResume() {
        if (mCurChildKey != null && mCurChildKey != "") {
            val curShow = mChildUIMap[mCurChildKey!!]
            curShow?.dispatchOnResume()
        }

        onResume()
    }

    /* Dispatch the KeyEvent form the Root StaticUI to the child StaticUI.
     * return true: if current StaticUI handled the KeyEvent.
     * return false: if current StaticUI not handled the KeyEvent.
     */
    fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (mCurChildKey != null && mCurChildKey != "") {
            val curShow = mChildUIMap[mCurChildKey!!]
            if (curShow != null) {
                if ( curShow.dispatchKeyEvent(event) )
                    return true
            }
        }

        return onKeyEvent(event)
    }

    fun getContainer(): ViewGroup? {
        return mContainer
    }

    fun getChildUIContainer(): ViewGroup? {
        return mChildUIContainer
    }

    /*
     * Register the child StaticUI
     * childUI: the children logic & function class
     * childKey: the key about the child StaticUI,Must be Unique under the current StaticUI
     */
    fun registerChildUI(childUI: StaticUI?, childKey: String?) {
        if (childUI != null && childKey != null) {
            if (mChildUIMap.containsKey(childKey)) {
                mChildUIMap.remove(childKey)
            }

            mChildUIMap.put(childKey, childUI)
        } else {
            throw RuntimeException(TAG + ": register failed! childUI or key is null!")
        }
    }

    /*
     * Switch the child StaticUI
     * childKey:  the key about the child StaticUI
     * releaseOld: After switch to new child StaticUI,release current StaticUI.
     */
    fun switchChildUI(childKey: String, releaseOld: Boolean) {
        switchChildUI(childKey, releaseOld, null, null)
    }

    fun switchChildUI(childKey: String, releaseOld: Boolean, inAnimRes: Int, outAnimRes: Int) {
        var inAnimation: Animation? = null
        if (inAnimRes > 0)
            inAnimation = AnimationUtils.loadAnimation(mContext, inAnimRes)
        var outAnimation: Animation? = null
        if (outAnimRes > 0)
            outAnimation = AnimationUtils.loadAnimation(mContext, outAnimRes)

        switchChildUI(childKey, releaseOld, inAnimation, outAnimation)
    }

    fun switchChildUI(childKey: String, releaseOld: Boolean, inAnimRes: Int, outAnimRes: Int, addToFront: Boolean) {
        var inAnimation: Animation? = null
        if (inAnimRes > 0)
            inAnimation = AnimationUtils.loadAnimation(mContext, inAnimRes)
        var outAnimation: Animation? = null
        if (outAnimRes > 0)
            outAnimation = AnimationUtils.loadAnimation(mContext, outAnimRes)

        switchChildUI(childKey, releaseOld, inAnimation, outAnimation, addToFront)
    }

    fun switchChildUI(childKey: String, releaseOld: Boolean, inAnimation: Animation?, outAnimation: Animation?) {
        switchChildUI(childKey, releaseOld, inAnimation, outAnimation, false)
    }

    fun switchChildUI(childKey: String, releaseOld: Boolean, inAnimation: Animation?, outAnimation: Animation?, addToFront: Boolean) {
        switchChildUI(childKey, releaseOld, inAnimation, outAnimation, addToFront, false)
    }

    private fun switchChildUI(childKey: String?, releaseOld: Boolean, inAnimation: Animation?, outAnimation: Animation?, addToFront: Boolean, forceSwitch: Boolean): Boolean {
        var inAnimation = inAnimation
        var outAnimation = outAnimation
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw RuntimeException(TAG + ": switchChildUI(...) must be called in main thread.")
        }

        if (mChildUIViewAnimator == null) {
            Exception(TAG + ": mChildUIContainer was null.").printStackTrace()
            return false//throw new RuntimeException(TAG + ": mChildUIContainer was null.");
        }

        if (childKey == null && mCurChildKey != null ) {
            val currentUI = mChildUIMap[mCurChildKey!!]
            if (currentUI != null) {
                currentUI.dispatchOnHide()
                currentUI.dispatchRelease()
            }

            return false
        }

        if (mCurChildKey != null && mCurChildKey == childKey && !forceSwitch)
            return false

        if (mPreviousUI != null) {
            val container = mPreviousUI!!.getContainer()
            if (container != null)
                mChildUIViewAnimator!!.removeView(mPreviousUI!!.getContainer())
            else {
                if (mPreviousUI!!.mTempContainer != null) { //针对上次切换使用addToFront=true的调用,mPreviousUI.getContainer()==null,检查mTempContainer
                    mChildUIViewAnimator!!.removeView(mPreviousUI!!.mTempContainer)

                    mPreviousUI!!.mTempContainer = null
                }
            }
            mPreviousUI = null
        }

        if (mCurChildKey != null && mCurChildKey != childKey) {
            mPreviousUI = mChildUIMap[mCurChildKey!!]

            mPreviousUI!!.mTempContainer = mPreviousUI!!.getContainer()
        }

        mNextUI = mChildUIMap[childKey]
        if (mNextUI != null) {
            var reshow = true
            if (!mNextUI!!.isInitialized()) {
                mNextUI!!.initialize()
                reshow = false
            }

            if (addToFront) {
                mChildUIViewAnimator!!.addView(mNextUI!!.getContainer(), 0, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            } else {
                mChildUIViewAnimator!!.addView(mNextUI!!.getContainer(), mChildUIViewAnimator!!.childCount, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            }

            //Show In
            if (inAnimation == null)
                inAnimation = mDefaultInAnimation

            if (inAnimation != null) {
                mChildUIViewAnimator!!.inAnimation = inAnimation
            } else
                mChildUIViewAnimator!!.inAnimation = null

            //Show Out
            if (outAnimation == null)
                outAnimation = mDefaultOutAnimation

            if (outAnimation != null)
                mChildUIViewAnimator!!.outAnimation = outAnimation
            else
                mChildUIViewAnimator!!.outAnimation = null

            //Out
            if (mPreviousUI != null) {
                mPreviousUI!!.dispatchOnHide()

                val previousContainer = mPreviousUI!!.getContainer()

                if (releaseOld)
                    mPreviousUI!!.dispatchRelease()

                mPreviousUI!!.mIsAttached = false//

                if (outAnimation == null && previousContainer != null) {
                    mChildUIViewAnimator!!.removeView(previousContainer)
                    mPreviousUI!!.mTempContainer = null
                    mPreviousUI = null
                }
            }

            //==========================================
            mCurChildKey = childKey

            mChildUIViewAnimator!!.showNext()
            //===============================================

            mNextUI!!.mIsAttached = true//

            //In
            mNextUI!!.dispatchOnShow(reshow)
            mNextUI = null

            return true
        } else {
            mPreviousUI = null

            return false
        }
    }

    fun dispatchOrientationChanged(newConfig: Configuration) {
        if (mChildUIMap.size > 0) {
            for (childKey in mChildUIMap.keys) {
                if( childKey != null ) {
                    val child = mChildUIMap[childKey]
                    if (child != null && child is StaticUI) {
                        if (child.isOrientationDependent()) {
                            //release old
                            child.onHide()

                            mHandler?.removeMessages(MSG_ID_RUNNABLE)
                            mHandler = null

                            child.onFinalize()

                            //create new
                            child.initialize()
                        }
                        child.dispatchOrientationChanged(newConfig)
                    }
                }
            }
        }

        if (mCurChildKey != null) {
            val curChild = mChildUIMap[mCurChildKey!!]
            if (curChild != null && curChild.isOrientationDependent()) {
                this.switchChildUI(this.mCurChildKey, false, mChildUIViewAnimator!!.inAnimation, mChildUIViewAnimator!!.outAnimation, false, true)
            }
        }
    }

    fun getCurrentChildKey(): String? {
        return mCurChildKey
    }

    fun getContext(): Context? {
        return mContext
    }

    fun getCurrentChild(): StaticUI? {
        return if (mCurChildKey != null) {
            mChildUIMap[mCurChildKey!!]
        } else null
    }

    fun isInitialized(): Boolean {
        return mIsInitialized
    }

    fun isShowing(): Boolean {
        return if (mContainer != null)
            mIsAttached == true && mContainer!!.visibility == View.VISIBLE && mContainer!!.windowVisibility == View.VISIBLE
        else
            false
    }

    fun run(r: Runnable) {
        mHandler?.sendMessage(mHandler?.obtainMessage(MSG_ID_RUNNABLE, r))
    }

    fun runDelayed(r: Runnable, delayMillis: Long) {
        mHandler?.sendMessageDelayed(mHandler?.obtainMessage(MSG_ID_RUNNABLE, r), delayMillis)
    }
}