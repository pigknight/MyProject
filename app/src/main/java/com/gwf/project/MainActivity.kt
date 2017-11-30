package com.gwf.project

import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.gwf.project.ui.Root
import com.gwf.project.ui.root.Content
import com.gwf.project.ui.root.content.Category

class MainActivity : AppCompatActivity() {
    private val TAG = MainActivity::class.java.getSimpleName()

    private var mMainContainer: ViewGroup? = null

    private var mWaitForSecondKey = false
    private val MSG_ID_DELAYED_TO_WAIT_SECOND_BACK_KEY = 1

    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                MSG_ID_DELAYED_TO_WAIT_SECOND_BACK_KEY -> {
                    this.removeMessages(MSG_ID_DELAYED_TO_WAIT_SECOND_BACK_KEY)
                    mWaitForSecondKey = false
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMainContainer = View.inflate(this, R.layout.activity_main, null) as ViewGroup
        setContentView(mMainContainer)

        initUI()

        // Example of a call to a native method
        /*val tv = findViewById(R.id.sample_text) as TextView
        tv.text = stringFromJNI()*/
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()

        Root.getInstance().dispatchOnPause()
    }

    override fun onResume() {
        super.onPause()

        Root.getInstance().dispatchOnResume()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {

        Root.getInstance().dispatchOrientationChanged(newConfig)

        super.onConfigurationChanged(newConfig)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (Root.getInstance().dispatchKeyEvent(event))
                return true

            if (!mWaitForSecondKey) {
                mWaitForSecondKey = true

                Toast.makeText(this, "Press again to exit!", Toast.LENGTH_SHORT).show()

                mHandler.removeMessages(MSG_ID_DELAYED_TO_WAIT_SECOND_BACK_KEY)
                mHandler.sendEmptyMessageDelayed(MSG_ID_DELAYED_TO_WAIT_SECOND_BACK_KEY, 2000)
            } else
                finish()

            return true
        }

        return super.onKeyDown(keyCode, event)
    }

    //==============================================================================
    fun getMainContainer(): ViewGroup {
        return mMainContainer!!
    }
    //==============================================================================

    private fun initUI() {
        //Init
        Root.initInstance(this)
        //Root.getInstance().setDefaultEnterAnimation(R.anim.push_right_in, R.anim.push_left_out)
        //Root.getInstance().setDefaultExitAnimation(R.anim.push_left_in, R.anim.push_right_out)

        Root.getInstance().enterUI(Content::class.java)
        Root.getInstance().findUI(Content::class.java)?.enterUI(Category::class.java)
        //Root.getInstance().findUI(Category::class.java)?.showUI(CategoryLocal::class.java)

        //Register
        //Root.getInstance().re
    }

    //================================Native===================================Start
    companion object {
        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String
    //================================Native===================================End
}
