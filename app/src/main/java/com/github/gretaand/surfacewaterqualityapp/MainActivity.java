package com.github.gretaand.surfacewaterqualityapp;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import com.github.gretaand.surfacewaterqualityapp.fragments.PermissionsDeniedDialogFragment;
import com.github.gretaand.surfacewaterqualityapp.fragments.PermissionsDialogFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import org.parceler.Parcels;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

/**
 * Main activity starts app with splash screen, asks for permissions and provides location data
 * to maps
 *
 * @author greta
 */
public class MainActivity extends FragmentActivity implements
        PermissionsDialogFragment.PermissionsDialogListener,
        PermissionsDeniedDialogFragment.PermissionsDeniedDialogListener {

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private View mMainView;
    private Location mLastLocation;
    private double mLatitude;
    private double mLongitude;

    /**
     * Provides the entry point to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;
    private LatLng mLatLngLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMainView = findViewById(R.id.main_activity_container);

        // initialize listener for when the view is attached to the window
        mMainView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {

            @Override
            public void onViewAttachedToWindow(View view) {

                // create animation object for fading in logo on splash screen
                Animation fadeInAnimation = new AlphaAnimation(0.0f, 1.0f);
                fadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        // do nothing
                    }

                    // request permissions or, if permission already granted, open map intent
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (checkPermissions()) {
                            requestPermissions();
                        } else {
                            startMapWithLocation();
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                        // do nothing
                    }
                });

                // start animation
                fadeInAnimation.setDuration(2000);
                mMainView.startAnimation(fadeInAnimation);

            }

            @Override
            public void onViewDetachedFromWindow(View view) {
                // do nothing
            }
        });

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Fabric.with(this, new Crashlytics());
    }

    @Override
    public void onRestart() {
        super.onRestart();
        if (checkPermissions()) {
            requestPermissions();
        }
        else {
            startMapWithLocation();
        }
    }

    /**
     * The dialog fragment receives a reference to this Activity through the
     * Fragment.onAttach() callback defined by the PermissionsDialogFragment.PermissionsDialogListener
     * interface. The PermissionsDialog is displayed if the initial request
     * for location permissions is denied. Method is called if user clicks "ok" button to give
     * location permission.
     *
     */
    @Override
    public void onPermissionsDialogPositiveClick() {
        startLocationPermissionRequest();
    }

    /**
     * The dialog fragment receives a reference to this Activity through the
     * Fragment.onAttach() callback defined by the PermissionsDialogFragment.PermissionsDialogListener
     * interface. The PermissionsDialog is displayed if the initial request
     * for location permissions is denied. Method is called if user clicks "Continue without
     * location" button to continue without all core functions of the app.
     *
     */
    @Override
    public void onPermissionsDialogNegativeClick() {
        // User touched the dialog's negative button; start map
        startMapWithoutLocation();
    }

    /**
     * The dialog fragment receives a reference to this Activity through the
     * Fragment.onAttach() callback defined by the PermissionsDeniedDialogFragment.PermissionsDialogListener
     * interface. The PermissionsDeniedDialog is displayed if the request for location permissions
     * is denied and if the user clicks the "do not ask again" option. Method is called if user
     * clicks "continue without location" button to continue without all core functions of
     * the app.
     *
     */
    @Override
    public void onPermissionsDeniedDialogNegativeClick() {
        startMapWithoutLocation();
    }

    /**
     * The dialog fragment receives a reference to this Activity through the
     * Fragment.onAttach() callback defined by the PermissionsDeniedDialogFragment.PermissionsDialogListener
     * interface. The PermissionsDeniedDialog is displayed if the request for location permissions
     * is denied and if the user clicks the "do not ask again" option. Method is called if user
     * clicks "settings" button to give location permission.
     */
    @Override
    public void onPermissionsDeniedDialogPositiveClick() {
        Intent intent = new Intent();
        intent.setAction(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",
                BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState != PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Starts permission request by prompting permission request
     */
    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }

    /**
     * determines if user has already been asked for permission and shows rationale
     * for permissions to the user if needed
     */
    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION);

        /* Provide an additional rationale to the user. This would happen if the user denied the
         * request previously, but didn't check the "Don't ask again" checkbox. */
        if (shouldProvideRationale) {
            // Create an instance of the dialog fragment and show it
            DialogFragment dialog = new PermissionsDialogFragment();
            dialog.show(getFragmentManager(), "PermissionsDialogFragment");
        } else {
            /* Request permission. It's possible this can be auto answered if device policy
             * sets the permission in a given state or the user denied the permission
             * previously and checked "Never ask again". */
            startLocationPermissionRequest();
        }
    }

    /**
     * Callback method for the result from requesting permissions - invoked for every call on
     * requestPermissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            /* If user interaction was interrupted, the permission request is cancelled and you
             * receive empty arrays. */

            // Permission granted. Start map
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startMapWithLocation();
            }
            // Permission denied.
            else {
                boolean shouldProvideRationale =
                        ActivityCompat.shouldShowRequestPermissionRationale(this,
                                android.Manifest.permission.ACCESS_FINE_LOCATION);

                /* Provide an additional rationale to the user. This would happen if the user
                 * denied the request previously, but didn't check the "Don't ask again"
                 * checkbox. */
                if (shouldProvideRationale) {
                    // Create an instance of the dialog fragment and show it
                    DialogFragment dialog = new PermissionsDialogFragment();
                    dialog.show(getFragmentManager(), "PermissionsDialogFragment");
                }
                /* Here, if permissions are denied and user clicked "never ask again" or the
                 * device policy is to reject location permissions, display a dialog prompting the
                 * user to update the app setting to allow locations or to continue to the app
                 * without all features provided with location data */
                else {
                    DialogFragment dialog = new PermissionsDeniedDialogFragment();
                    dialog.show(getFragmentManager(), "PermissionsDeniedDialogFragment");
                }
            }
        }
    }


    /**
     * Provides a simple way of getting a device's location and is well suited for
     * applications that do not require a fine-grained location and that do not need location
     * updates. Gets the best and most recent location currently available, which may be null
     * in rare cases when a location is not available.
     * <p>
     * Note: this method should be called after location permission has been granted.
     */
    @SuppressLint("MissingPermission")
    private void startMapWithLocation() {

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            mLastLocation = location;
                            mLatitude = mLastLocation.getLatitude();
                            mLongitude = mLastLocation.getLongitude();
                            mLatLngLocation = new LatLng(mLatitude, mLongitude);
                            Intent mapIntent = new Intent(MainActivity.this, MapsActivity.class);
                            mapIntent.putExtra("location", Parcels.wrap(mLatLngLocation));
                            startActivity(mapIntent);
                            finish();
                        } else {
                            showSnackbar(getString(R.string.no_location_detected));
                        }
                    }
                });
    }

    private void startMapWithoutLocation() {
        Intent mapIntent = new Intent(MainActivity.this, MapsActivity.class);
        mLatLngLocation = new LatLng(47.60357, -122.3295);
        mapIntent.putExtra("location", Parcels.wrap(mLatLngLocation));
        startActivity(mapIntent);
        finish();
    }

    /**
     * Shows a {@link Snackbar} using {@code text}.
     *
     * @param text The Snackbar text.
     */
    private void showSnackbar(final String text) {
        View container = findViewById(R.id.main_activity_container);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }
}
