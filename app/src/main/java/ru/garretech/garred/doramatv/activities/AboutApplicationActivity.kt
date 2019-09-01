package ru.garretech.garred.doramatv.activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.appcompat.widget.Toolbar
import android.text.Spannable
import android.text.Spanned
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.yandex.mobile.ads.*

import kotlinx.android.synthetic.main.activity_about_application.*
import kotlinx.android.synthetic.main.toolbar.*
import ru.garretech.garred.doramatv.BuildConfig
import ru.garretech.garred.doramatv.R
import ru.garretech.garred.doramatv.Settings

class AboutApplicationActivity : AppCompatActivity() {

    val mAdMobView: AdView by lazy { AdView(this) }
    private var mAdRequest: AdRequest? = null


    private val mBannerAdListener = object : AdEventListener {
        override fun onAdFailedToLoad(p0: AdRequestError) {

        }

        override fun onAdClosed() {

        }


        override fun onAdLeftApplication() {

        }

        override fun onAdLoaded() {
            mAdMobView.visibility = View.VISIBLE
        }

        override fun onAdOpened() {

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_application)


        initAdMobView()
        refreshBannerAd()
        setSupportActionBar(applicationAboutToolbar as Toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = getString(R.string.about_application)

        versionTextView.text = getText(R.string.version_label).toString() + " " + BuildConfig.VERSION_NAME


    }



    private fun initAdMobView() {
        mAdMobView.adSize = AdSize.flexibleSize()

        mAdMobView.blockId = Settings.BLOCK_ID1
        mAdMobView.adEventListener = mBannerAdListener

        mAdRequest = AdRequest.Builder().build()

        val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        layoutParams.gravity = Gravity.CENTER_HORIZONTAL
        aboutAppLayout.addView(mAdMobView, layoutParams)
    }

    private fun refreshBannerAd() {
        mAdMobView.visibility = View.INVISIBLE
        mAdMobView.loadAd(mAdRequest)
    }

    internal fun revertSpanned(stext: Spanned): Spannable {
        val spans = stext.getSpans(0, stext.length, Any::class.java)
        val ret = Spannable.Factory.getInstance().newSpannable(stext.toString())
        if (spans != null && spans.isNotEmpty()) {
            for (i in spans.indices.reversed()) {
                ret.setSpan(spans[i], stext.getSpanStart(spans[i]), stext.getSpanEnd(spans[i]), stext.getSpanFlags(spans[i]))
            }
        }

        return ret
    }


    override fun onDestroy() {
        mAdMobView.destroy()
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}
