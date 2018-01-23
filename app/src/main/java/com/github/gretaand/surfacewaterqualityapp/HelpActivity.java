package com.github.gretaand.surfacewaterqualityapp;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

/**
 * Displays help info when selected from toolbar menu
 *
 * @author greta
 */
public class HelpActivity extends BaseActivity {

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        // set toolbar to act as action bar
        initToolbar(R.id.help_toolbar);

        // add text to app info TextView with clickable links
        TextView appInfoTextView = findViewById(R.id.app_info);
        Spanned text = getHtmlText(getString(R.string.app_info));
        appInfoTextView.setText(text);
        appInfoTextView.setMovementMethod(LinkMovementMethod.getInstance());

        // add text to the view limits and historical results TextView
        TextView viewLimitsTextView = findViewById(R.id.view_limits_and_historical_results);
        Spanned text2 = getHtmlText(getString(R.string.view_limits_and_historical_results));
        viewLimitsTextView.setText(text2);
    }
}
