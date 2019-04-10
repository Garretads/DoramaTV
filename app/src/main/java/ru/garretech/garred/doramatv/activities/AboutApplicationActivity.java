package ru.garretech.garred.doramatv.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.Spanned;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.garretech.garred.doramatv.BuildConfig;
import ru.garretech.garred.doramatv.R;

public class AboutApplicationActivity extends AppCompatActivity {
    @BindView(R.id.version_text) TextView versionTextView;
    @BindView(R.id.nick_link) TextView nickLink;
    @BindView(R.id.toolbar_actionbar) Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_application);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.about_application));

        versionTextView.setText(getText(R.string.version_label) + " " + BuildConfig.VERSION_NAME);



    }

    final Spannable revertSpanned(Spanned stext) {
        Object[] spans = stext.getSpans(0, stext.length(), Object.class);
        Spannable ret = Spannable.Factory.getInstance().newSpannable(stext.toString());
        if (spans != null && spans.length > 0) {
            for(int i = spans.length - 1; i >= 0; --i) {
                ret.setSpan(spans[i], stext.getSpanStart(spans[i]), stext.getSpanEnd(spans[i]), stext.getSpanFlags(spans[i]));
            }
        }

        return ret;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
