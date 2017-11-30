package com.gwf.project.util.ui

import android.content.Context
import android.content.res.Configuration
import android.os.Handler
import android.os.Message
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ViewAnimator
import java.util.*
import kotlin.properties.Delegates

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
abstract class DynamicUI {
    companion object {
        val TAG: String = DynamicUI::class.java.getSimpleName()

        private var DEBUG:Boolean = true

        fun log(msg:String) {
            if( DEBUG )
                Log.i(TAG,msg)
        }
    }

    private var mParentUI:DynamicUI? = null

    private data class Vector2<N,C>(var className:N, var childUI:C)

    //Application Context
    protected var mContext: Context by Delegates.notNull()

    //
    private val MSG_ID_RUNNABLE = 1
    private var mHandler: Handler? = null

    //The container view(in *.xml) which associated with the current StaticUI
    protected var mContainer: ViewGroup? = null

    //A container view who will contains(or switch) children StaticUI in current StaticUI (mContainerView)
    private var mChildUIContainer: ViewGroup? = null

    private var mChildUIViewAnimator: ViewAnimator? = null

    private var mDefaultEnterInAnimation: Animation? = null
    private var mDefaultEnterOutAnimation: Animation? = null

    private var mDefaultExitInAnimation: Animation? = null
    private var mDefaultExitOutAnimation: Animation? = null

    /*
     * Saved the child StaticUI object be registered,which will be switched
     * by the childKey registered.
     */
    private val mChildStack = Stack< Vector2<String/*className*/,out DynamicUI/*childUI*/> >()

    private var mIsAttached = false

    /*
     * This ... you know.
     */
    var mIsInitialized = false

    constructor()

    constructor(context: Context, isRoot: Boolean = false) {
        setContext(context,isRoot)
    }

    private fun setContext(context:Context, isRoot: Boolean = false){
        mContext = context
        mIsInitialized = false

        /*
    	 * The Root DynamicUI must be initialized immediately
    	 */
        if ( isRoot ) {
            initialize()

            mIsAttached = true
            onShow(false)
        }
    }

    fun setDefaultEnterAnimation(inAnimRes: Int, outAnimRes: Int) {
        mDefaultEnterInAnimation = AnimationUtils.loadAnimation(mContext, inAnimRes)
        mDefaultEnterOutAnimation = AnimationUtils.loadAnimation(mContext, outAnimRes)
    }

    fun setDefaultEnterAnimation( inAnim: Animation?, outAnim: Animation?) {
        mDefaultEnterInAnimation = inAnim
        mDefaultEnterOutAnimation = outAnim
    }

    fun setDefaultExitAnimation(inAnimRes: Int, outAnimRes: Int) {
        mDefaultExitInAnimation = AnimationUtils.loadAnimation(mContext, inAnimRes)
        mDefaultExitOutAnimation = AnimationUtils.loadAnimation(mContext, outAnimRes)
    }

    fun setDefaultExitAnimation( inAnim: Animation?, outAnim: Animation?) {
        mDefaultExitInAnimation = inAnim
        mDefaultExitOutAnimation = outAnim
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
    open/*abstract*/ fun onFinalize(){

    }

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
        log("Call initialize on ${javaClass.simpleName}")

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
            /*if (mChildUIMap.size > 0) {
                for (child in mChildUIMap.values) {
                    if ( child != null ) {
                        with(child) {
                            mIsAttached = false
                            dispatchOnHide()
                            dispatchRelease()
                        }
                    }
                }
            }*/
            for (child in mChildStack) {
                if ( child != null ) {
                    var uiClassName = child.className
                    log("Call dispatchRelease on $uiClassName")
                    var ui = child.childUI
                    if( ui != null ) {
                        with(ui) {
                            mIsAttached = false
                            dispatchOnHide()
                            dispatchRelease()
                        }
                    }
                }
            }
            mChildStack.clear()

            mChildUIViewAnimator?.removeAllViews()

            //mCurChildKey = null
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun dispatchRelease() {
        /*if (mCurChildKey != null && mCurChildKey != "") {
            val curShow = mChildUIMap[mCurChildKey!!]
            if (curShow != null) {
                curShow.dispatchRelease()

                mChildUIViewAnimator?.removeView(curShow.getContainer())
            }
        }*/

        log("Call dispatchRelease on ${javaClass.simpleName}")
        try {
            val curShowUIVector2 = mChildStack.peek()
            if (curShowUIVector2 != null) {
                //var uiKey = curShowUIVector2.className
                var ui = curShowUIVector2.childUI
                if (ui != null) {
                    ui.dispatchRelease()

                    mChildUIViewAnimator?.removeView(ui.getContainer())
                }
            }
        }catch(e:EmptyStackException){

        }catch(e:Exception){
            e.printStackTrace()
        }

        mHandler?.removeMessages(MSG_ID_RUNNABLE)
        mHandler = null

        onFinalize()

        mChildStack.clear()

        try {
            mChildUIViewAnimator?.removeAllViews()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        mIsInitialized = false
        //mCurChildKey = null

        mContainer = null
        mChildUIContainer = null
        mChildUIViewAnimator = null
    }

    private fun dispatchOnShow(reshow: Boolean) {
        /*if (mCurChildKey != null && mCurChildKey != "") {
            val curShow = mChildUIMap[mCurChildKey!!]
            curShow?.dispatchOnShow(reshow)
        }*/

        log("Call dispatchOnShow on ${javaClass.simpleName}  reshow=$reshow")
        try {
            val curShowUIVector2 = mChildStack.peek()
            if (curShowUIVector2 != null) {
                //var uiKey = curShowUIVector2.className
                var curShowUI = curShowUIVector2.childUI
                curShowUI?.dispatchOnShow(reshow)
            }
        }catch(e:EmptyStackException){

        }catch(e:Exception){
            e.printStackTrace()
        }

        onShow(reshow)
    }

    private fun dispatchOnHide() {
        /*if (mCurChildKey != null && mCurChildKey != "") {
            val curShow = mChildUIMap[mCurChildKey!!]
            curShow?.dispatchOnHide()
        }*/

        log("Call dispatchOnHide on ${javaClass.simpleName}")
        try {
            val curShowUIVector2 = mChildStack.peek()
            if (curShowUIVector2 != null) {
                //var uiKey = curShowUIVector2.className
                var curShowUI = curShowUIVector2.childUI
                curShowUI?.dispatchOnHide()
            }
        }catch(e:EmptyStackException){

        }catch(e:Exception){
            e.printStackTrace()
        }

        onHide()
    }

    fun dispatchOnPause() {
        /*if (mCurChildKey != null && mCurChildKey != "") {
            val curShow = mChildUIMap[mCurChildKey!!]
            curShow?.dispatchOnPause()
        }*/

        log("Call dispatchOnPause on ${javaClass.simpleName}")
        try {
            val curShowUIVector2 = mChildStack.peek()
            if (curShowUIVector2 != null) {
                //var uiKey = curShowUIVector2.className
                var curShowUI = curShowUIVector2.childUI
                curShowUI?.dispatchOnPause()
            }
        }catch(e:EmptyStackException){

        }catch(e:Exception){
            e.printStackTrace()
        }

        onPause()
    }

    fun dispatchOnResume() {
        /*if (mCurChildKey != null && mCurChildKey != "") {
            val curShow = mChildUIMap[mCurChildKey!!]
            curShow?.dispatchOnResume()
        }*/

        log("Call dispatchOnResume on ${javaClass.simpleName}")
        try {
            val curShowUIVector2 = mChildStack.peek()
            if (curShowUIVector2 != null) {
                //var uiKey = curShowUIVector2.className
                var curShowUI = curShowUIVector2.childUI
                curShowUI?.dispatchOnResume()
            }
        }catch(e:EmptyStackException){

        }catch(e:Exception){
            e.printStackTrace()
        }

        onResume()
    }

    fun dispatchOrientationChanged(newConfig: Configuration) {
        /*if (mChildUIMap.size > 0) {
            for (childKey in mChildUIMap.keys) {
                if( childKey != null ) {
                    val child = mChildUIMap[childKey]
                    if (child != null && child is DynamicUI) {
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
        }*/
    }

    /* Dispatch the KeyEvent form the Root StaticUI to the child StaticUI.
     * return true: if current StaticUI handled the KeyEvent.
     * return false: if current StaticUI not handled the KeyEvent.
     */
    fun dispatchKeyEvent(event: KeyEvent): Boolean {
        /*if (mCurChildKey != null && mCurChildKey != "") {
            val curShow = mChildUIMap[mCurChildKey!!]
            if (curShow != null) {
                if ( curShow.dispatchKeyEvent(event) )
                    return true
            }
        }*/

        log("Call dispatchKeyEvent on ${javaClass.simpleName}")
        try {
            val curShowUIVector2 = mChildStack.peek()
            if (curShowUIVector2 != null) {
                //var uiKey = curShowUIVector2.className
                var curShowUI = curShowUIVector2.childUI
                if (curShowUI != null && curShowUI.dispatchKeyEvent(event))
                    return true
            }
        }catch(e:EmptyStackException){

        }catch(e:Exception){
            e.printStackTrace()
        }

        return onKeyEvent(event)
    }

    fun <T> findUI(clazz:Class<T>) : DynamicUI? {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw RuntimeException(TAG + ": findUI(...) must be called in main thread.")

            return null
        }

        try {
            val curShowUIVector2 = mChildStack.peek()
            if (curShowUIVector2 != null) {
                var uiClassName = curShowUIVector2.className
                if( uiClassName != null && uiClassName == clazz.simpleName ){
                    return curShowUIVector2.childUI
                }else
                    return curShowUIVector2.childUI.findUI(clazz)
            }
        }catch(e:EmptyStackException){

        }catch(e:Exception){
            e.printStackTrace()
        }

        return null
    }


    fun <T> enterUI(clazz:Class<T>,inAnimRes: Int = -1, outAnimRes: Int = -1){
        var inAnimation: Animation? = null
        if (inAnimRes > 0)
            inAnimation = AnimationUtils.loadAnimation(mContext, inAnimRes)
        var outAnimation: Animation? = null
        if (outAnimRes > 0)
            outAnimation = AnimationUtils.loadAnimation(mContext, outAnimRes)

        enterUIInner(clazz,inAnimation, outAnimation)
    }

    private fun <T> enterUIInner(clazz:Class<T>,inAnim: Animation? = null, outAnim: Animation? = null){
        //安全性检查1
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw RuntimeException(TAG + ": showUI(...) must be called in main thread.")
        }

        //安全性检查2
        if (mChildUIViewAnimator == null) {
            throw RuntimeException(TAG + ": mChildUIContainer was null.")
        }

        //判断指定UI's clazz是否在当前Parent中已经显示
        var stack = mChildStack.filter {
            it.className == clazz.simpleName
        }
        if( !stack?.isEmpty() )
            throw RuntimeException(TAG + ": The UI[${clazz.simpleName}] is showing in current parent.")
        /*for( uiVector2 in mChildStack ){
            if( uiVector2?.className == clazz.simpleName ){
                throw RuntimeException(TAG + ": The UI[${clazz.simpleName}] is showing in current parent.")
            }
        }*/

        var previousUi:DynamicUI? = null

        try {
            var uiVector2 = mChildStack.peek()
            previousUi = uiVector2?.childUI
        }catch(e:EmptyStackException){

        }catch(e:Exception){
            e.printStackTrace()
        }

        var newUi:DynamicUI = clazz.newInstance() as DynamicUI
        if( newUi != null ) {
            newUi.setContext(mContext, false)
            newUi.mParentUI = this //Set Parent UI
            mChildStack.push( Vector2(clazz.simpleName, newUi))

            var reshow = true
            if( !newUi.isInitialized() ) {
                newUi.initialize()
                reshow = false
            }

            mChildUIViewAnimator!!.addView(newUi!!.getContainer(), mChildUIViewAnimator!!.childCount, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

            var inAnimation:Animation? = inAnim
            var outAnimation:Animation? = outAnim

            if (inAnimation == null)
                inAnimation = mDefaultEnterInAnimation

            if (inAnimation != null) {
                mChildUIViewAnimator?.inAnimation = inAnimation
            } else
                mChildUIViewAnimator?.inAnimation = null

            //Show Out
            if (outAnimation == null)
                outAnimation = mDefaultEnterOutAnimation

            if (outAnimation != null)
                mChildUIViewAnimator?.outAnimation = outAnimation
            else
                mChildUIViewAnimator?.outAnimation = null

            mChildUIViewAnimator!!.showNext()

            newUi.mIsAttached = true

            //previousUi: On Hide.    Not remove but in stack
            previousUi?.dispatchOnHide()

            //New UI: On Show
            newUi.dispatchOnShow(reshow)
        }
    }

    fun exitSelf(inAnimRes: Int = -1, outAnimRes: Int = -1){
        var inAnimation: Animation? = null
        if (inAnimRes > 0)
            inAnimation = AnimationUtils.loadAnimation(mContext, inAnimRes)
        var outAnimation: Animation? = null
        if (outAnimRes > 0)
            outAnimation = AnimationUtils.loadAnimation(mContext, outAnimRes)

        exitSelfInner(inAnimation, outAnimation)
    }

    private fun exitSelfInner(inAnim: Animation? = null, outAnim: Animation? = null){
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw RuntimeException(TAG + ": showUI(...) must be called in main thread.")
        }

        log("Call exitSelf on ${javaClass.simpleName}")
        //首先验证当前UI时候顶端窗口
        val curShowUIVector2 = mParentUI?.mChildStack!!.peek()
        if (curShowUIVector2 != null) {
            if( curShowUIVector2.className == this.javaClass.simpleName ){
                mParentUI?.exitTopUIInner(inAnim,outAnim) //在父UI上调用
            }else{
                throw RuntimeException(TAG + ": this UI isn't the top UI.")
            }
        }
    }

    fun exitTopUI(inAnimRes: Int = -1, outAnimRes: Int = -1){
        var inAnimation: Animation? = null
        if (inAnimRes > 0)
            inAnimation = AnimationUtils.loadAnimation(mContext, inAnimRes)
        var outAnimation: Animation? = null
        if (outAnimRes > 0)
            outAnimation = AnimationUtils.loadAnimation(mContext, outAnimRes)

        exitTopUIInner(inAnimation, outAnimation)
    }

    private fun exitTopUIInner(inAnim: Animation? = null, outAnim: Animation? = null){
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw RuntimeException(TAG + ": showUI(...) must be called in main thread.")
        }

        if (mChildUIViewAnimator == null) {
            //Exception(TAG + ": mChildUIContainer was null.").printStackTrace()
            throw RuntimeException(TAG + ": mChildUIContainer was null.")
        }

        try {
            val toExitUIVector2 = mChildStack.pop()
            val toShowUIVector2 = mChildStack.peek()
            if (toExitUIVector2 != null) {

                var inAnimation:Animation? = inAnim
                var outAnimation:Animation? = outAnim

                //Show In
                if (inAnimation == null)
                    inAnimation = mDefaultExitInAnimation

                if (inAnimation != null) {
                    mChildUIViewAnimator?.inAnimation = inAnimation
                } else
                    mChildUIViewAnimator?.inAnimation = null

                //Show Out
                if (outAnimation == null)
                    outAnimation = mDefaultExitOutAnimation

                if (outAnimation != null)
                    mChildUIViewAnimator?.outAnimation = outAnimation
                else
                    mChildUIViewAnimator?.outAnimation = null

                mChildUIViewAnimator?.showPrevious()

                toExitUIVector2.childUI.dispatchOnHide()

                val previousContainer = toExitUIVector2.childUI!!.getContainer()

                toExitUIVector2.childUI.dispatchRelease()

                toExitUIVector2.childUI.mIsAttached = false//

                if ( outAnimation == null && previousContainer != null) {
                    mChildUIViewAnimator!!.removeView(previousContainer)
                }

                //显示前一个UI
                toShowUIVector2?.childUI?.dispatchOnShow( true )
            }
        }catch(e:EmptyStackException){

        }catch(e:Exception){
            e.printStackTrace()
        }
    }

    fun dumpUIArch(){
        log("UIArch:  ${javaClass.simpleName} *")
        if( !mChildStack.empty() ){
            dumpUIArchInner( this,"",true)
        }
    }

    private fun dumpUIArchInner(ui:DynamicUI,space:String,isTop:Boolean){
        var count = ui.mChildStack.size
        for( i in ui.mChildStack.indices ){
            var uiVector2 = ui.mChildStack[i]
            if( uiVector2?.childUI != null ){
                if( i == count -1 && isTop )
                    log("UIArch:  $space|--- ${uiVector2.childUI.javaClass.simpleName} *")
                else
                    log("UIArch:  $space|--- ${uiVector2.childUI.javaClass.simpleName}")
                if( !uiVector2.childUI.mChildStack.empty() ) {
                    dumpUIArchInner(uiVector2.childUI, space + "   ", (i == count -1 && isTop) )
                }
            }
        }
    }

    fun getContainer(): ViewGroup? = mContainer

    fun getChildUIContainer(): ViewGroup? = mChildUIContainer

    fun getContext(): Context? = mContext

    fun getCurrentChild(): DynamicUI? = mChildStack.peek()?.childUI ?: null

    fun isInitialized(): Boolean = mIsInitialized

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