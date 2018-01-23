package com.github.gretaand.surfacewaterqualityapp;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

/**
 * acts as a base activity for all shared methods/code between activities (e.g. toolbar)
 *
 * @author greta
 */

public class BaseActivity extends AppCompatActivity{

    /**
     * initiates toolbar
     *
     * @param toolbarResourceId resource id for toolbar to include
     */
    void initToolbar(int toolbarResourceId) {
        // set toolbar to act as action bar
        Toolbar myToolbar = findViewById(toolbarResourceId);
        setSupportActionBar(myToolbar);
    }

    /**
     * Toolbar menu items are inflated
     *
     * @param menu the options menu
     * @return must return true for the menu to be displayed
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    /**
     * Handles when an item on the toolbar overflow menu is clicked and open appropriate page
     *
     * @param item menu item selected
     * @return false to allow normal menu processing to proceed, true to consume it
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                // User chose the "About" item, show the about page
                Intent aboutIntent = new Intent(this, AboutActivity.class);
                startActivity(aboutIntent);
                break;
            case R.id.action_help:
                // User chose the "Help" action, show the help page
                Intent helpIntent = new Intent(this, HelpActivity.class);
                startActivity(helpIntent);
            default:
                /* If we got here, the user's action was not recognized. Invoke the superclass
                 * to handle it. */
                break;
        }
        return true;
    }

    /**
     * creates text with html markup for use with TextView
     *
     * @param string string
     * @return text
     */
    @SuppressWarnings("deprecation")
    Spanned getHtmlText(String string) {

        Spanned text;
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            text = Html.fromHtml(string, Html.FROM_HTML_MODE_LEGACY);
        }
        else {
            text = Html.fromHtml(string);
        }
        return text;
    }

    /**
     * Provides error message to be displayed in snackbar when volley fails
     *
     * @param error volley error
     * @return string message
     */
    String getVolleyErrorMessage(VolleyError error) {
        String errorMessage;
        if (error instanceof TimeoutError) {
            // This indicates that the request has timed out
            errorMessage = getString(R.string.volley_timeout);
        } else if (error instanceof NoConnectionError) {
            // This indicates that there is no connection
            errorMessage = getString(R.string.volley_no_connection);
        } else if (error instanceof AuthFailureError) {
            // Error indicating that there was an Authentication Failure while performing the request
            errorMessage = getString(R.string.volley_auth_failure);
        } else if (error instanceof ServerError) {
            //Indicates that the server responded with a error response
            errorMessage = getString(R.string.volley_server_error);
        } else if (error instanceof NetworkError) {
            //Indicates that there was network error while performing the request
            errorMessage = getString(R.string.volley_network_error);
        } else if (error instanceof ParseError) {
            // Indicates that the server response could not be parsed
            errorMessage = getString(R.string.volley_parse_error);
        } else {
            errorMessage = getString(R.string.volley_error);
        }
        return errorMessage;
    }
}
