package ru.garretech.garred.doramatv.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.text.Spannable
import android.text.Spanned
import android.widget.TextView

import butterknife.BindView
import butterknife.ButterKnife
import ru.garretech.garred.doramatv.BuildConfig
import ru.garretech.garred.doramatv.R

class AboutApplicationActivity : AppCompatActivity() {
    @BindView(R.id.version_text)
    internal var versionTextView: TextView? = null
    @BindView(R.id.nick_link)
    internal var nickLink: TextView? = null
    @BindView(R.id.toolbar_actionbar)
    internal var toolbar: Toolbar? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_application)
        ButterKnife.bind(this)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = getString(R.string.about_application)

        versionTextView!!.text = getText(R.string.version_label).toString() + " " + BuildConfig.VERSION_NAME


    }

    internal fun revertSpanned(stext: Spanned): Spannable {
        val spans = stext.getSpans<Any>(0, stext.length, Any::class.java)
        val ret = Spannable.Factory.getInstance().newSpannable(stext.toString())
        if (spans != null && spans.size > 0) {
            for (i in spans.indices.reversed()) {
                ret.setSpan(spans[i], stext.getSpanStart(spans[i]), stext.getSpanEnd(spans[i]), stext.getSpanFlags(spans[i]))
            }
        }

        return ret
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}
