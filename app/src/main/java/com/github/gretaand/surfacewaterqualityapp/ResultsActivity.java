package com.github.gretaand.surfacewaterqualityapp;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.github.gretaand.surfacewaterqualityapp.adapters.ResultAdapter;
import com.github.gretaand.surfacewaterqualityapp.utils.Result;
import com.github.gretaand.surfacewaterqualityapp.utils.VolleySingleton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Retrieves and displays information about the station clicked and most recent results for each
 * pollutant for that station
 *
 * @author greta
 */
public class ResultsActivity extends BaseActivity {

    // station id for the station clicked
    private int mStationId;

    // adapter that takes a list of results as input
    private ResultAdapter mResultAdapter;

    // view for results container
    private View mContainerView;

    private ProgressBar mProgressBar;
    private Snackbar mSnackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        // set toolbar to act as action bar
        initToolbar(R.id.results_toolbar);

        // Retrieve the intent that started activity (click on station)
        Intent intent = getIntent();
        mStationId = intent.getIntExtra("stationId", -99);
        String locationId = intent.getStringExtra("locationId");
        String stationName = intent.getStringExtra("stationName");
        String locationType = intent.getStringExtra("stationLocationType");

        // set station info in the in the view above the results ListView
        TextView stationNameTextView = findViewById(R.id.stationName);
        stationNameTextView.setText(stationName);
        TextView stationIdTextView = findViewById(R.id.stationId);
        stationIdTextView.setText(locationId);
        TextView stationLocationTypeTextView = findViewById(R.id.locationType);
        stationLocationTypeTextView.setText(locationType);

        // initialize progress bar
        mProgressBar = findViewById(R.id.resultsProgressBar);

        // initialize container view for Snackbar
        mContainerView = findViewById(R.id.results_activity_container);
    }

    /**
     * When activity is started, get results associated with the selected station
     */
    public void onStart() {
        super.onStart();
        getResults();
    }

    private void getResults() {

        // create query URL
        String url = "https://waterrestservice.azurewebsites.net/waterRestService/rest/water/results?stationId=" +
                mStationId;

        final HashMap<String, String> params = new HashMap<>();
        params.put("auth-token", getString(R.string.auth_token));

        JsonArrayRequest request = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {

                // update view for the results
                setResultsView(response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMessage = getVolleyErrorMessage(error);
                showSnackbar(errorMessage);
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("auth-token", params.get("auth-token"));
                return headers;
            }
        };

        int VOLLEY_TIMEOUT = 5000;  // in ms
        request.setRetryPolicy(new DefaultRetryPolicy(
                VOLLEY_TIMEOUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Access the RequestQueue through your singleton class.
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void setResultsView(JSONArray response) {

        List<Result> results = new ArrayList<>();
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
        for (int i = 0; i < response.length(); i++) {
            try {
                JSONObject object = response.getJSONObject(i);
                Result result = gson.fromJson(object.toString(), Result.class);
                results.add(result);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        // Find ListView in activity_results.xml;
        ListView resultsListView = findViewById(R.id.list_results);
        // Create an adapter that takes a list of selected results as input
        mResultAdapter = new ResultAdapter(ResultsActivity.this, new ArrayList<Result>());
        // Add results to the adapter
        mResultAdapter.addAll(results);
        // Set the adapter on the ListView so the list can be populated to the UI
        resultsListView.setAdapter(mResultAdapter);

        // Set an item click listener on the ListView, which selects the result to view info about
        resultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Find the current result that was clicked on
                Result currentResult = mResultAdapter.getItem(position);
                /* Create new intent to open up LimitsActivity, which displays limit and notes from
                 * selected result */
                Intent intent = new Intent(ResultsActivity.this, LimitActivity.class);
                // Add the selected station monitoring location ID, and characteristicName to the intent
                intent.putExtra("result", Parcels.wrap(currentResult));
                // Start activity
                startActivity(intent);
            }

        });

        // set progress bar to invisible
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    /**
     * Shows a {@link Snackbar} using {@code text}.
     *
     * @param text The Snackbar text.
     */
    private void showSnackbar(final String text) {

        if (mContainerView != null && mSnackbar == null) {
            mSnackbar = Snackbar.make(mContainerView, text, Snackbar.LENGTH_INDEFINITE);
        }
        mSnackbar.setAction(R.string.try_again, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // dismiss old Snackbar and create new one so that it can be shown again if needed
                mSnackbar.dismiss();
                mSnackbar = Snackbar.make(mContainerView, text, Snackbar.LENGTH_INDEFINITE);

                getResults();
            }
        });
        mSnackbar.show();
    }
}


