package ru.garretech.garred.doramatv.flow.profile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import android.text.Spannable
import android.text.Spanned
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.yandex.mobile.ads.banner.AdSize
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import kotlinx.android.synthetic.main.activity_about_application.*
import ru.garretech.garred.doramatv.BuildConfig
import ru.garretech.garred.doramatv.R
import ru.garretech.garred.doramatv.Settings

class AboutApplicationActivity : AppCompatActivity() {

    val mAdMobView: BannerAdView by lazy { BannerAdView(this) }
    private var mAdRequest: AdRequest? = null


    private val mBannerAdListener = object : BannerAdEventListener {
        override fun onLeftApplication() {
            //TODO("Not yet implemented")
        }

        override fun onReturnedToApplication() {
            //TODO("Not yet implemented")
        }

        override fun onImpression(p0: ImpressionData?) = Unit


        override fun onAdLoaded() {
            mAdMobView.visibility = View.VISIBLE
        }

        override fun onAdFailedToLoad(p0: AdRequestError) {
            //TODO("Not yet implemented")
        }

        override fun onAdClicked() = Unit

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
        mAdMobView.setAdUnitId(Settings.block_id())
        mAdMobView.setAdSize(AdSize.BANNER_300x250)
        mAdMobView.setBannerAdEventListener(mBannerAdListener)

        mAdRequest = AdRequest.Builder().build()

        val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        layoutParams.gravity = Gravity.CENTER_HORIZONTAL
        aboutAppLayout.addView(mAdMobView, layoutParams)
    }

    private fun refreshBannerAd() {
        mAdMobView.visibility = View.INVISIBLE
        mAdRequest?.let { mAdMobView.loadAd(it) }
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
