package com.github.gretaand.surfacewaterqualityapp;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import com.github.gretaand.surfacewaterqualityapp.utils.Limit;
import com.github.gretaand.surfacewaterqualityapp.utils.Result;
import com.github.gretaand.surfacewaterqualityapp.utils.VolleySingleton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LimitActivity extends BaseActivity {

    // characteristic name for pollutant result clicked on
    private String mCharacteristicName;

    // media type for result (water or biological tissue)
    private String mActivityMediaName;

    // station id (primary key)
    private int mStationId;

    // result converted measure value unit code
    private String mConvertedMeasureUnitCode;

    // results measured value unit code
    private String mMeasureUnitCode;

    // limit info for result
    private Limit mLimit;

    // progress bar - displayed while gathering and processing data
    private ProgressBar mProgressBar;

    // measure value for limit
    private double mLimitValue;

    // view for limit container
    private View mContainerView;

    private Snackbar mSnackbar;

    // constants for graph styling
    private final float DP_FOR_PADDING = 20;
    private final float DP_FOR_LABEL_SPACE = 10;
    private final float DP_FOR_LINE_THICKNESS = 2;

    // volley timeout in ms
    private final int VOLLEY_TIMEOUT = 5000;

    // map to add headers to the request for security purposes
    private final HashMap<String, String> params = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_limit_and_historical_graph);

        // set toolbar to act as action bar
        initToolbar(R.id.limit_toolbar);

        // Retrieve the intent that started activity (click on result)
        Intent intent = getIntent();
        Result result = Parcels.unwrap(intent.getParcelableExtra("result"));

        // initialize variables based on result object passed from the result activity
        mCharacteristicName = result.getCharacteristicName();
        mActivityMediaName = result.getActivityMediaName();
        mStationId = result.getStationPrimaryKey();
        mConvertedMeasureUnitCode = result.getConvertedMeasureUnitCode();
        mMeasureUnitCode = result.getMeasureUnitCode();

        // initialize progress bar
        mProgressBar = findViewById(R.id.limitProgressBar);

        // initialize view for container for Snackbar
        mContainerView = findViewById(R.id.limit_activity_container);

        // add authorization token to params for adding to request headers
        params.put("auth-token", getString(R.string.auth_token));
    }

    /**
     * When activity is started, get limit associated with the result clicked
     */
    public void onStart() {
        super.onStart();
        try {
            getLimit();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets limit information from REST web service for result clicked on by user
     *
     * @throws UnsupportedEncodingException UnsupportedEncodingException
     */
    private void getLimit() throws UnsupportedEncodingException {

        String encodedCharacteristicName = URLEncoder.encode(mCharacteristicName, "UTF-8");
        String encodedActivityMediaName = URLEncoder.encode(mActivityMediaName, "UTF-8");
        String url = "https://waterrestservice.azurewebsites.net/waterRestService/rest/water/" +
                "limit?characteristicName=" + encodedCharacteristicName + "&mediaName=" +
                encodedActivityMediaName;

        JsonArrayRequest request = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {

                // set the limit view
                setLimitView(response);

                // Now get the historical results for the result clicked and display
                try {
                    getResults();
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }

        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMessage = getVolleyErrorMessage(error);
                showSnackbar(errorMessage);
            }

        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("auth-token", params.get("auth-token"));
                return headers;
            }
        };

        // set volley timeout and retry policy.  Default times out too quickly.
        request.setRetryPolicy(new DefaultRetryPolicy(
                VOLLEY_TIMEOUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Access the RequestQueue through your singleton class.
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    /**
     * set limit view text, based on limit availability, and display
     */
    @TargetApi(Build.VERSION_CODES.N)
    private void setLimitView(JSONArray response) {

        // if there is a limit, get the limit info from the response and display
        if (response.length() > 0) try {
            JSONObject object = response.getJSONObject(0);
            Gson gson = new GsonBuilder().create();
            mLimit = gson.fromJson(object.toString(), Limit.class);

                /* if the limit doesn't have a set limit value, display an empty view with
                 * "No recommended limit." */
            if (mLimit.getLimitValue() == null) {
                LinearLayout noLimitLayout = findViewById(R.id.noLimitLayout);
                noLimitLayout.setVisibility(View.VISIBLE);
            } else { // otherwise, populate limit data and display

                // set characteristic name for the limit
                TextView limitCharacteristicNameTextView = findViewById(R.id.limitCharacteristicName);
                limitCharacteristicNameTextView.setText(mLimit.getCharacteristicName());

                // set limit and unit code
                TextView limitValueAndUnitCodeTextView = findViewById(
                        R.id.limitValueAndUnitCode);
                String limitAndUnitCode;
                if (mLimit.getLimitValue() != null) {
                    limitAndUnitCode = mLimit.getLimitValue() + " " + mLimit.getLimitUnit();
                } else {
                    limitAndUnitCode = "None";
                }
                limitValueAndUnitCodeTextView.setText(limitAndUnitCode);

                // set text for link
                TextView limitLinkTextView = findViewById(R.id.limitLink);
                limitLinkTextView.setText(mLimit.getPollutantInfoLink());

                // set layout to visible
                LinearLayout limitLayout = findViewById(R.id.limitLayout);
                limitLayout.setVisibility(View.VISIBLE);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        else {
            /* Display an empty view with "No recommended limit." if there is no limit
             * associated with the result */
            LinearLayout noLimitLayout = findViewById(R.id.noLimitLayout);
            noLimitLayout.setVisibility(View.VISIBLE);
        }

        // set graph title to visible
        TextView graphTitleTextView = findViewById(R.id.graphTitle);
        graphTitleTextView.setVisibility(View.VISIBLE);
    }

    /**
     * gets all the historical results for a given parameter that have the same unit of measure
     * and plots them on a graph
     */
    private void getResults() throws UnsupportedEncodingException {

        // encode the query parameters to avoid issues when there is an unsafe character (e.g. #)
        String encodedCharacteristicName = URLEncoder.encode(mCharacteristicName, "UTF-8");

        // Create query URL
        String url = "https://waterrestservice.azurewebsites.net/waterRestService/rest/water/historicalResults?" +
                "stationId=" + mStationId + "&" + "characteristicName=" + encodedCharacteristicName;


        JsonArrayRequest request = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {

                List<Result> results = new ArrayList<>();
                // Gson creates objects from json
                Gson gson = new GsonBuilder().create();
                // get TextView that contains the message that there are no historical results
                TextView noGraphTextView = findViewById(R.id.noGraph);

                if (response.length() > 0) {
                    // loop through the json array and create a list of results
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject object = response.getJSONObject(i);
                            Result result = gson.fromJson(object.toString(), Result.class);
                            results.add(result);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    /* loop through all the results and two lists, one with all the appropriate result
                     * data points to plot and one with the limit data points to plot */
                    ArrayList<DataPoint> resultsDataPoints = new ArrayList<>();
                    ArrayList<DataPoint> limitDataPoints = new ArrayList<>();
                    for (Result result : results) {
                        // get the activityStartDate as the x value
                        Date activityStartDate = result.getActivityStartDate();

                        /* check if the result has a converted measure value. If so, if the unit code
                         * matches the clicked result's unit code, create a data point */
                        if (result.getConvertedMeasureUnitCode() != null &&
                                result.getConvertedMeasureUnitCode().equalsIgnoreCase(mConvertedMeasureUnitCode)) {
                            resultsDataPoints.add(new DataPoint(activityStartDate, result.getConvertedMeasureValue()));
                        } else {
                            /* check if the measured value is a number.  If so, if the unit code
                             * matches the clicked result's unit code, create a data point. */
                            boolean resultMeasureIsDouble = result.checkIfMeasureValueIsDouble();
                            if (resultMeasureIsDouble
                                    && result.getMeasureUnitCode().equalsIgnoreCase(mMeasureUnitCode)) {
                                resultsDataPoints.add(new DataPoint(activityStartDate,
                                        result.getMeasureValue()));
                            }

                            /* if the measured value is not a number and a detection condition exists
                             * which is "not detected", "present below quantification limit" or "trace"
                             * create a data point with the y-value set to 0. */
                            String resultDetectionCondition = result.getDetectionCondition();
                            if (!resultMeasureIsDouble && resultDetectionCondition != null) {

                                if (resultDetectionCondition.toLowerCase().contains("not detected")
                                        || resultDetectionCondition.toLowerCase().contains("present below quantification limit")
                                        || resultDetectionCondition.toLowerCase().contains("trace")) {
                                    resultsDataPoints.add(new DataPoint(activityStartDate, 0));
                                }
                            }
                            /* otherwise, check if the measure value string contains "non-detect" or
                             * "not detected".  If so, create a data point with the y-value set
                             * as 0. */
                            else {
                                String resultValueString = result.getMeasureValueString();
                                if (resultValueString.toLowerCase().contains("non-detect")
                                        || resultValueString.toLowerCase().contains("not detected")) {
                                    resultsDataPoints.add(new DataPoint(activityStartDate, 0));
                                }
                            }
                        }

                        // if there is a limit, add the limit data points
                        if (mLimit != null && mLimit.getLimitUnit() != null) {
                            mLimitValue = mLimit.getLimitValue();
                            if (mLimitValue != 0) {
                                limitDataPoints.add(new DataPoint(activityStartDate, mLimitValue));
                            }
                        }
                    }

                    /* if the number of data points is one or all of the results are from the same
                     * date, display a bar graph instead of a line graph */
                    DataPoint[] resultsDataPointsArray = resultsDataPoints.toArray(
                            new DataPoint[resultsDataPoints.size()]);
                    if (resultsDataPoints.size() == 1
                            || !resultsDataPoints.isEmpty() && resultsDataPoints.get(0).getX() ==
                            resultsDataPoints.get(resultsDataPoints.size() - 1).getX()) {

                        int resultsDataPointsSize = resultsDataPoints.size();

                        // if there is only one result, bar graph the point
                        BarGraphSeries<DataPoint> barSeries = new BarGraphSeries<>(resultsDataPointsArray);
                        double maxY = barSeries.getHighestValueY();
                        BarGraphSeries<DataPoint> barSeriesToGraph;
                        if (resultsDataPointsSize == 1) {
                            barSeriesToGraph = new BarGraphSeries<>(resultsDataPointsArray);
                            showBarGraph(barSeriesToGraph, limitDataPoints);
                        }
                        /* if there is more than one data points, plot the point with the highest y value.
                         * If we don't select for the highest value and instead plot all, the
                         * values for all points will be drawn on the map */
                        else {
                            DataPoint maxDataPoint = new DataPoint(barSeries.getHighestValueX(), maxY);
                            DataPoint[] maxDataPointArray = {maxDataPoint};
                            barSeriesToGraph = new BarGraphSeries<>(maxDataPointArray);
                            showBarGraph(barSeriesToGraph, limitDataPoints);
                            TextView multiplePointsBarGraphView =
                                    findViewById(R.id.barGraphWithMultiplePointsMessage);
                            multiplePointsBarGraphView.setVisibility(View.VISIBLE);
                        }
                    }
                    /* if there is more than one result to display, create a line graph and add data
                     * points for results and limit, if there is one. */
                    else if (resultsDataPoints.size() > 1) {
                        // create an series for the historical results
                        LineGraphSeries<DataPoint> lineSeries = new LineGraphSeries<>(resultsDataPointsArray);
                        showLineGraph(lineSeries, limitDataPoints);
                    }
                    /* when no results to display, show the message that there is no historical data
                     * to graph, but some may be available that don't have same unit or aren't in number
                     * format */
                    else {
                        noGraphTextView.setText(R.string.no_historical_results_to_graph);
                        noGraphTextView.setVisibility(View.VISIBLE);
                    }
                } else { // when there are no objects in the response, set the message to no results
                    noGraphTextView.setText(R.string.no_historical_results);
                    noGraphTextView.setVisibility(View.VISIBLE);
                }

                // set progress bar to invisible
                mProgressBar.setVisibility(View.INVISIBLE);

                // set text for limit notes under the graph
                if (mLimit != null) {
                    LinearLayout limitNotesLayout = findViewById(R.id.limitNotesLayout);
                    TextView limitNotesTextView = findViewById(R.id.limitNotes);
                    String notes = mLimit.getNotes();
                    if (notes != null) {
                        limitNotesTextView.setText(getHtmlText(mLimit.getNotes()));
                        limitNotesLayout.setVisibility(View.VISIBLE);
                    }
                }
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


        // set volley timeout and retry policy.  Default times out too quickly.
        request.setRetryPolicy(new DefaultRetryPolicy(
                VOLLEY_TIMEOUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Access the RequestQueue through your singleton class.
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    /**
     * shows bar graph for historical results
     *
     * @param barSeries data point/s for the bar graph
     * @param limitDataPoints list of data points for the limit
     */
    private void showBarGraph(BarGraphSeries barSeries,
                      ArrayList<DataPoint> limitDataPoints) {

        // create a graph
        GraphView graph = findViewById(R.id.graph);
        graph.addSeries(barSeries);

        /* set manual x bounds to have nice steps. Set the min and max x values
         * manually as the date before and the date after the date with data */
        Date resultDate = new Date((long) barSeries.getHighestValueX());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(resultDate);
        calendar.add(Calendar.DATE, -1);
        Date minDate = calendar.getTime();
        calendar.add(Calendar.DATE, 2);
        Date maxDate = calendar.getTime();
        graph.getViewport().setMinX(minDate.getTime());
        graph.getViewport().setMaxX(maxDate.getTime());
        graph.getViewport().setXAxisBoundsManual(true);

        // if there is a limit, add a line to the graph
        if (!limitDataPoints.isEmpty()) {
            // add DataPoint for the min and max dates on the graph
            limitDataPoints.add(new DataPoint(minDate, mLimitValue));
            limitDataPoints.add(new DataPoint(maxDate, mLimitValue));
            // create an array from the ArrayList of points
            DataPoint[] limitDataPointsArray = limitDataPoints.toArray(
                    new DataPoint[limitDataPoints.size()]);
            // create a series
            LineGraphSeries<DataPoint> limitSeries = new LineGraphSeries<>(limitDataPointsArray);
            // set the color to red
            limitSeries.setColor(Color.RED);
            // set line thickness
            limitSeries.setThickness((int) getPixels(DP_FOR_LINE_THICKNESS));
            // add the series to the graph
            graph.addSeries(limitSeries);
        }

        // if there are negative values we need to set min and max y values differently
        double minY = graph.getViewport().getMinY(true);
        if (minY < 0) {
            graph.getViewport().setMinY(minY);
            graph.getViewport().setMaxY(0);
        } else {
            graph.getViewport().setMinY(0);
            graph.getViewport().setMaxY(graph.getViewport().getMaxY(true));
        }
        graph.getViewport().setYAxisBoundsManual(true);

        // set date label formatter
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(LimitActivity.this));
        graph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 1 since only one data point

        // set the text for the y-axis
        if (mConvertedMeasureUnitCode != null) {
            graph.getGridLabelRenderer().setVerticalAxisTitle(mConvertedMeasureUnitCode);
        } else {
            graph.getGridLabelRenderer().setVerticalAxisTitle(mMeasureUnitCode);
        }

        /* as we use dates as labels, the human rounding to nice readable numbers
        * is not necessary */
        graph.getGridLabelRenderer().setHumanRounding(false);

        // adjust the angle horizontal label so that it's readable
        graph.getGridLabelRenderer().setHorizontalLabelsAngle(90);

        // add some other styling
        graph.getGridLabelRenderer().setPadding((int) getPixels(DP_FOR_PADDING));
        graph.getGridLabelRenderer().setLabelsSpace((int) getPixels(DP_FOR_LABEL_SPACE));
        barSeries.setDrawValuesOnTop(true);
        barSeries.setValuesOnTopColor(Color.BLACK);
        barSeries.setValuesOnTopSize(getPixels(20));
        barSeries.setSpacing(50);

        // set the graph and graph note to visible
        graph.setVisibility(View.VISIBLE);

    }

    /**
     * shows line graph for historical results
     *
     * @param lineSeries series of data points for the line
     * @param limitDataPoints list of data points for the limit
     */
    private void showLineGraph(LineGraphSeries<DataPoint> lineSeries,
                       ArrayList<DataPoint> limitDataPoints) {

        // create a graph
        GraphView graph = findViewById(R.id.graph);

        // make the data points visible on the graph
        lineSeries.setDrawDataPoints(true);

        // set date label formatter
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(LimitActivity.this));
        graph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 3 because of the space

        // set the text for the y-axis
        if (mConvertedMeasureUnitCode != null) {
            graph.getGridLabelRenderer().setVerticalAxisTitle(mConvertedMeasureUnitCode);
        } else {
            graph.getGridLabelRenderer().setVerticalAxisTitle(mMeasureUnitCode);
        }

        /* set manual x bounds to have nice steps. Get min and max dates, and set
         * the min and max x values manually */
        Date minDate = new Date((long) lineSeries.getLowestValueX());
        Date maxDate = new Date((long) lineSeries.getHighestValueX());
        graph.getViewport().setMinX(minDate.getTime());
        graph.getViewport().setMaxX(maxDate.getTime());
        graph.getViewport().setXAxisBoundsManual(true);

        // enable scaling and scrolling
        //graph.getViewport().setScalable(true);
        //graph.getViewport().setScalableY(true);

        /* as we use dates as labels, the human rounding to nice readable numbers
         * is not necessary */
        graph.getGridLabelRenderer().setHumanRounding(false);

        // adjust the angle horizontal label so that it's readable
        graph.getGridLabelRenderer().setHorizontalLabelsAngle(90);

        // add some other styling
        graph.getGridLabelRenderer().setPadding((int) getPixels(DP_FOR_PADDING));
        graph.getGridLabelRenderer().setLabelsSpace((int) getPixels(DP_FOR_LABEL_SPACE));
        lineSeries.setThickness((int) getPixels(DP_FOR_LINE_THICKNESS));
        float DP_FOR_POINT_RADIUS = 4;
        lineSeries.setDataPointsRadius(getPixels(DP_FOR_POINT_RADIUS));

        // add the series to the graph
        graph.addSeries(lineSeries);

        // if there is a limit, add the data points to the graph
        if (!limitDataPoints.isEmpty()) {

            // create an array from the ArrayList of points
            DataPoint[] limitDataPointsArray = limitDataPoints.toArray(
                    new DataPoint[limitDataPoints.size()]);
            // create a series
            LineGraphSeries<DataPoint> limitSeries = new LineGraphSeries<>(limitDataPointsArray);
            // add the series to the graph
            graph.addSeries(limitSeries);
            // set the color to red
            limitSeries.setColor(Color.RED);
            limitSeries.setThickness((int) getPixels(DP_FOR_LINE_THICKNESS));
        }

        // set the graph and graph note to visible
        graph.setVisibility(View.VISIBLE);
    }

    private float getPixels(float dp) {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float scale = metrics.density;
        return dp * scale + 0.5f;
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

                try {
                    getLimit();
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        mSnackbar.show();
    }

}
