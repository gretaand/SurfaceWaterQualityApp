package com.github.gretaand.surfacewaterqualityapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Location;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;

import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import com.github.gretaand.surfacewaterqualityapp.utils.MarkerItem;
import com.github.gretaand.surfacewaterqualityapp.utils.Station;
import com.github.gretaand.surfacewaterqualityapp.utils.VolleySingleton;
import com.github.gretaand.surfacewaterqualityapp.views.ClusterMarkerView;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity containing google map with makers for each station with results
 *
 * @author greta
 */
public class MapsActivity extends BaseActivity implements
        GoogleMap.OnMyLocationButtonClickListener,
        OnMapReadyCallback, GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnCameraIdleListener,
        ClusterManager.OnClusterClickListener<MarkerItem>,
        ClusterManager.OnClusterItemClickListener<MarkerItem> {

    // list of stations returned for area bound by the map
    private List<Station> mStations;

    // TextView for message if there are too many stations or the area is too large
    private TextView mMapMessageTextView;

    // View for maps container
    private View mContainerView;

    // location lat and long info
    private LatLng mLatLngLocation;

    // lat and long coordinates for the area bounded by the map
    private double mLatNortheast;
    private double mLongNortheast;
    private double mLatSouthwest;
    private double mLongSouthwest;

    // max number of stations to display in order to avoid long user wait times
    private final int MAX_STATION_SIZE = 250;

    private GoogleMap mMap;
    private ProgressBar mSpinner;
    private ClusterManager<MarkerItem> mClusterManager;
    private Snackbar mSnackbar;

    private PlaceAutocompleteFragment mAutocompleteFragment;
    private int mAutocompleteFragmentHeight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // set toolbar to act as action bar
        initToolbar(R.id.maps_toolbar);

        // Obtain the Google SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // initialize the progress bar spinner and set to visible
        mSpinner = findViewById(R.id.mapProgressBar);
        mSpinner.setVisibility(View.VISIBLE);

        /* initialize the no stations (displayed when there are not stations in the map area) and
         * too many stations TextViews (displayed when MAX_STATION_SIZE is exceeded) */
        mMapMessageTextView = findViewById(R.id.mapMessage);

        // initialize container view for Snackbar
        mContainerView = findViewById(R.id.maps_activity_container);

        // get last location info from main activity
        Intent intent = getIntent();
        mLatLngLocation = Parcels.unwrap(intent.getParcelableExtra("location"));

        // initiate places autocomplete fragment
        mAutocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        //noinspection ConstantConditions
        mAutocompleteFragment.getView().setBackgroundColor(Color.WHITE);
        mAutocompleteFragment.getView().measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        mAutocompleteFragmentHeight = mAutocompleteFragment.getView().getMeasuredHeight()
                + (int) getResources().getDimension(R.dimen.place_autocomplete_button_padding);
    }

    /**
     * When activity is started, get results associated with the selected station
     */
    public void onStart() {
        super.onStart();
        try {
            getStations();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Manipulates the map once available. This callback is triggered when the map is ready to be
     * used. This is where we can add markers, add listeners or move the camera. If Google Play
     * services is not installed on the device, the user will be prompted to install it inside the
     * SupportMapFragment. This method will only be triggered once the user has installed Google
     * Play services and returned to the app.
     *
     * @param googleMap the map
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // set padding so that search box doesn't cover the my location button
        mMap.setPadding(0, mAutocompleteFragmentHeight, 0, 0);

        // Move the camera on the last location of the phone
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatLngLocation, 10));

        // set listener for map movement started
        mMap.setOnCameraMoveStartedListener(MapsActivity.this);

        /* Enable the my location layer so that my location button appears at the top right hand
         * corner of the map */
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
        // set the location button click listener
        mMap.setOnMyLocationButtonClickListener(this);

        // create cluster manager to cluster markers on map
        mClusterManager = new ClusterManager<>(this, mMap);

        /* add cluster renderer and set click listeners for the cluster markers and individual
         * station markers */
        final CustomClusterRenderer renderer = new CustomClusterRenderer();
        mClusterManager.setRenderer(renderer);
        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterItemClickListener(this);
        mClusterManager.setAnimation(false);

        /* set the on camera idle listener, this is where look up new stations based on the new
         * location of camera */
        mMap.setOnCameraIdleListener(this);

        // set the marker click listener to the cluster manager
        mMap.setOnMarkerClickListener(mClusterManager);

        // set the places autocomplete listener
        mAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                LatLng selectedLocation = place.getLatLng();
                // Move the camera on the selected location
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLocation,15 ));
                try {
                    getStations();
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onError(Status status) {
                showSnackbarWithoutAction(status.getStatusMessage());
            }
        });

        // add zoom controls and remove map toolbar (quick access to google maps app)
        UiSettings settings = mMap.getUiSettings();
        settings.setAllGesturesEnabled(true);
        settings.setZoomControlsEnabled(true);
        settings.setMapToolbarEnabled(false);
    }

    /**
     * Customizes the the look or rendered markers and clusters
     */
    public class CustomClusterRenderer extends DefaultClusterRenderer<MarkerItem> {

        CustomClusterRenderer() {
            super(getApplicationContext(), mMap, mClusterManager);
        }

        /**
         * Draws the customized markers (e.g. red, green, yellow) before a cluster item is rendered
         *
         * @param item          the cluster item (individual marker)
         * @param markerOptions the marker options object that is used to customize the marker icon
         */
        @Override
        protected void onBeforeClusterItemRendered(MarkerItem item, MarkerOptions markerOptions) {

            // Inflate the cluster marker view, assign the text (number of marker items)
            LayoutInflater inflater = LayoutInflater.from(MapsActivity.this);
            @SuppressLint("InflateParams")
            View markerView = inflater.inflate(R.layout.marker, null);
            TextView markerTextView = markerView.findViewById(R.id.marker);

            // get the drawable resource for the appropriate warning color
            int stationResultsWarningLevel = item.getStation().getWarningLevel();
            int drawable;
            if (stationResultsWarningLevel == 4) {                  // very high
                drawable = R.drawable.ic_place_red;
            } else if (stationResultsWarningLevel == 3) {           // high
                drawable = R.drawable.ic_place_orange;
            } else if (stationResultsWarningLevel == 2) {           // moderate
                drawable = R.drawable.ic_place_yellow;
            } else if (stationResultsWarningLevel == 1) {           // low
                drawable = R.drawable.ic_place_green;
            } else {                                                // none
                drawable = R.drawable.ic_place_light_gray;
            }

            // set the drawable as the TextView background
            markerTextView.setBackgroundResource(drawable);
            // measure the width and height of the view
            markerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            // Assign the position of the view
            markerView.layout(0, 0, markerView.getMeasuredWidth(),
                    markerView.getMeasuredHeight());
            // create bitmap to hold the pixels
            Bitmap bitmap = Bitmap.createBitmap(markerView.getMeasuredWidth(),
                    markerView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            // create canvas to host the draw calls (writing to the bitmap)
            Canvas canvas = new Canvas(bitmap);

            // render the view to the canvas
            markerView.draw(canvas);
            // set the bitmap as the marker icon
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
        }

        /**
         * Assigns the text and custom marker icon/color to the cluster marker before it is rendered
         *
         * @param cluster       the cluster contained in the cluster marker
         * @param markerOptions marker options assigned to the cluster marker
         */
        @Override
        public void onBeforeClusterRendered(Cluster<MarkerItem> cluster,
                                            MarkerOptions markerOptions) {

            /* get the makers and the number that is assigned to the cluster (number of markers
             * in cluster) */
            Collection<MarkerItem> markerItems = cluster.getItems();
            int bucket = getBucket(cluster);
            String clusterText = getClusterText(bucket);

            // loop through markers to determine number of each warning level in cluster
            SparseIntArray clusterWarningLevelMap = new SparseIntArray();
            for (MarkerItem marker : markerItems) {
                int markerWarningLevel = marker.getStation().getWarningLevel();
                int key;
                int countWarningLevel;
                if (markerWarningLevel > 0) {
                    key = markerWarningLevel;
                    countWarningLevel = clusterWarningLevelMap.get(key,
                            0) + 1;
                }
                // if no warning_level (warning level < 0), assign as -1 to keep track of all
                else {
                    key = -1;
                    countWarningLevel = clusterWarningLevelMap.get(key, 0) + 1;
                }
                clusterWarningLevelMap.put(key, countWarningLevel);
            }



            // Inflate the cluster marker view, assign the text (number of marker items)
            LayoutInflater inflater = LayoutInflater.from(MapsActivity.this);
            @SuppressLint("InflateParams")
            View markerClusterView = inflater.inflate(R.layout.marker_cluster_custom, null);
            ClusterMarkerView clusterView = markerClusterView.findViewById(R.id.clusterMarker);
            // get arc angles so markers can become pie charts for different colored markers
            clusterView.setSweepAngles(clusterWarningLevelMap, markerItems.size());

            // assign marker number in cluster
            TextView markerClusterTextView = markerClusterView.findViewById(R.id.clusterMarkerText);
            markerClusterTextView.setText(clusterText);

            // measure the width and height of the view
            markerClusterView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            // Assign the position of the view
            markerClusterView.layout(0, 0, markerClusterView.getMeasuredWidth(),
                    markerClusterView.getMeasuredHeight());
            // create bitmap to hold the pixels
            Bitmap bitmap = Bitmap.createBitmap(markerClusterView.getMeasuredWidth(),
                    markerClusterView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            // create canvas to host the draw calls (writing to the bitmap)
            Canvas canvas = new Canvas(bitmap);
            // render the view to the canvas
            markerClusterView.draw(canvas);
            // set the bitmap as the marker icon
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
        }

        /**
         * Sets the min size of cluster that should be rendered
         *
         * @param cluster cluster in question
         * @return true if cluster should be rendered
         */
        @Override
        public boolean shouldRenderAsCluster(Cluster cluster) {
            return cluster.getSize() > 1;
        }

    }

    /**
     * Zooms into the cluster when clicked
     *
     * @param cluster cluster that has been clicked on
     * @return true
     */
    @Override
    public boolean onClusterClick(Cluster<MarkerItem> cluster) {

        // Create the builder to collect all essential cluster items for the bounds.
        LatLngBounds.Builder builder = LatLngBounds.builder();
        for (MarkerItem item : cluster.getItems()) {
            builder.include(item.getPosition());
        }

        // Get the LatLngBounds
        final LatLngBounds bounds = builder.build();
        // Animate camera to the bounds
        try {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * Creates a new intent if a marker is clicked on
     *
     * @param marker marker that has been clicked on
     * @return false
     */
    @Override
    public boolean onClusterItemClick(MarkerItem marker) {
        // Find the current station that was clicked on
        Station currentStation = marker.getStation();

        /* Create new intent to open up ResultsActivity, which displays results from
         * selected station */
        if (currentStation != null) {
            Intent intent = new Intent(this, ResultsActivity.class);
            // Add the selected station monitoring location ID and name to the intent
            intent.putExtra("stationId", currentStation.getId());
            intent.putExtra("locationId", currentStation.getLocationId());
            intent.putExtra("stationName", currentStation.getLocationName());
            intent.putExtra("stationLocationType", currentStation.getLocationType());
            startActivity(intent);
        }
        return false;
    }

    /**
     * Callback is triggered when the map my location button is pushed at the top right hand corner of
     * the map. The map is returned back to the last location
     */
    @Override
    public boolean onMyLocationButtonClick() {
        // Move the camera on the last location of the phone
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLatLngLocation, 10));
        return false;
    }

    /**
     * Callback is triggered when map camera movement goes idle.  When this happens, we get the new
     * lat and long of the screen, set the progress circle as visible and call the method to get all
     * the new stations
     */
    @Override
    public void onCameraIdle() {
        LatLngBounds latLngBoundsScreen = mMap.getProjection().getVisibleRegion().latLngBounds;

        mSpinner.setVisibility(View.VISIBLE);

        // get lat and long for northeast and southwest corner of screen
        mLatNortheast = latLngBoundsScreen.northeast.latitude;
        mLongNortheast = latLngBoundsScreen.northeast.longitude;
        mLatSouthwest = latLngBoundsScreen.southwest.latitude;
        mLongSouthwest = latLngBoundsScreen.southwest.longitude;

        // create location points for the northeast and southwest corners of screen
        Location northeastLocation = new Location("");
        northeastLocation.setLatitude(mLatNortheast);
        northeastLocation.setLongitude(mLongNortheast);
        Location southwestLocation = new Location("");
        southwestLocation.setLatitude(mLatSouthwest);
        southwestLocation.setLongitude(mLongSouthwest);

        /* if the corners are too far apart, display message that the search are is too large and
         * remove spinner */
        float distance = northeastLocation.distanceTo(southwestLocation);
        int MAX_DISTANCE = 500000;     // in meters
        if (distance > MAX_DISTANCE) {
            mMapMessageTextView.setText(R.string.too_big_area);
            mMapMessageTextView.setVisibility(View.VISIBLE);
            mSpinner.setVisibility(View.INVISIBLE);
        } else { // if the area is small enough, search for stations
            try {
                getStations();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Callback is triggered when the map camera movement is started, so we set the TextView that
     * shows message that there are no stations as invisible
     *
     * @param i reason
     */
    @Override
    public void onCameraMoveStarted(int i) {
        mMapMessageTextView.setVisibility(View.INVISIBLE);
    }

    /**
     * Retrieves stations for the map markers/clusters from the REST web service
     */
    private void getStations() throws UnsupportedEncodingException {

        //  encode the query parameters to avoid issues when there is an unsafe character (e.g. #)
        String encodedLatNortheast = URLEncoder.encode(Double.toString(mLatNortheast), "UTF-8");
        String encodedLongNortheast = URLEncoder.encode(Double.toString(mLongNortheast), "UTF-8");
        String encodedLatSouthwest = URLEncoder.encode(Double.toString(mLatSouthwest), "UTF-8");
        String encodedLongSouthwest = URLEncoder.encode(Double.toString(mLongSouthwest), "UTF-8");

        // create query URL
        //String url = "http://10.0.2.2:8080/restService/rest/water/stations?"
        String url = "https://waterrestservice.azurewebsites.net/waterRestService/rest/water/stations?"
                + "latitudeNortheast=" + encodedLatNortheast + "&longitudeNortheast="
                + encodedLongNortheast + "&latitudeSouthwest=" + encodedLatSouthwest
                + "&longitudeSouthwest=" + encodedLongSouthwest;

        final HashMap<String, String> params = new HashMap<>();
        params.put("auth-token", getString(R.string.auth_token));

        // create Volley request
        JsonArrayRequest request = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {

            /* when the volley response occurs, create list of stations from the Json provided by
            the REST service */
            @Override
            public void onResponse(JSONArray response) {

                // set the no stations text view and too many stations TextView to invisible to start
                mMapMessageTextView.setVisibility(View.INVISIBLE);

                mStations = new ArrayList<>();

                // create GSON object to convert from Json to station objects
                Gson gson = new GsonBuilder().create();

                /* if the response isn't empty, convert the Json into a list of station objects
                 * (only include stations that have results available */
                if (response.length() > 0) {

                    // loop through the json array and create a list of stations
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject object = response.getJSONObject(i);
                            Station station = gson.fromJson(object.toString(), Station.class);

                            mStations.add(station);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    /* if the number of stations is small enough, create a marker item for each station,
                     * add each item to the cluster manager, and cluster the items */
                    if (mStations.size() < MAX_STATION_SIZE) {
                        mClusterManager.clearItems();
                        HashMap<Double, Double> latLngs = new HashMap<>();
                        for (Station station : mStations) {

                            // adjust lat a bit for stations that have the same lat and long
                            double latitude = station.getLatitude();
                            double longitude = station.getLongitude();
                            boolean containsLat = latLngs.containsKey(latitude);
                            if (containsLat && latLngs.get(latitude) == longitude) {
                                latitude += 0.0001;
                                while (latLngs.containsKey(latitude)) {
                                    latLngs.put(latitude, longitude);
                                    latitude += 0.0001;
                                }
                            } else {
                                latLngs.put(latitude, longitude);
                            }

                            // create markers items and add to clusters
                            MarkerItem markerItem = new MarkerItem(latitude,
                                    longitude, station);
                            mClusterManager.addItem(markerItem);
                            mClusterManager.cluster();
                        }
                    }
                    // if there are too many stations, display the message that there are too many stations
                    else {
                        mMapMessageTextView.setText(R.string.too_many_stations_message);
                        mMapMessageTextView.setVisibility(View.VISIBLE);
                    }

                    // if there are no stations with results available, show the no stations message
                    if (mStations.size() == 0) {
                        mMapMessageTextView.setText(R.string.no_stations_message);
                        mMapMessageTextView.setVisibility(View.VISIBLE);
                    }
                }
                /* if there response doesn't include any stations, show the no stations message and
                 * remove the progress spinner */
                else {
                    mMapMessageTextView.setText(R.string.no_stations_message);
                    mMapMessageTextView.setVisibility(View.VISIBLE);
                }

                /* remove the spinner once either stations, no stations message or too many stations
                 * message is displayed */
                mSpinner.setVisibility(View.INVISIBLE);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMessage = getVolleyErrorMessage(error);
                showSnackbarWithAction(errorMessage);
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

        int VOLLEY_TIMEOUT = 5000;
        request.setRetryPolicy(new DefaultRetryPolicy(
                VOLLEY_TIMEOUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        /* Access the RequestQueue through your singleton class, set a tag for the request (arbitrary),
         * cancel all previous requests (this is done so that the system is not overwhelmed when a
         * lot of zooming in and out happening), and add the new request to the queue. */
        request.setTag("request");
        VolleySingleton.getInstance(this).getRequestQueue().cancelAll("request");
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    /**
     * Shows a {@link Snackbar} using {@code text}.
     *
     * @param text The Snackbar text.
     */
    private void showSnackbarWithAction(final String text) {

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
                    getStations();
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        mSnackbar.show();
    }

    /**
     * Shows a {@link Snackbar} using {@code text}.
     *
     * @param text The Snackbar text.
     */
    private void showSnackbarWithoutAction(final String text) {

        if (mContainerView != null && mSnackbar == null) {
            mSnackbar = Snackbar.make(mContainerView, text, Snackbar.LENGTH_INDEFINITE);
        }
        mSnackbar.setAction(R.string.ok, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // dismiss old Snackbar and create new one so that it can be shown again if needed
                mSnackbar.dismiss();
            }
        });
        mSnackbar.show();
    }

}

