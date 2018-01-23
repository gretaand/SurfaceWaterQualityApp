package com.github.gretaand.surfacewaterqualityapp;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

/**
 * Displays about info when selected from toolbar menu
 *
 * @author greta
 */
public class AboutActivity extends BaseActivity {

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // set toolbar to act as action bar
        initToolbar(R.id.about_toolbar);

        // add text to about TextView with clickable links
        TextView licensesTextView = findViewById(R.id.about);
        Spanned licensesText = getHtmlText(getString(R.string.about_info));
        licensesTextView.setText(licensesText);
        licensesTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
