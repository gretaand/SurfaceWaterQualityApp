package com.github.gretaand.surfacewaterqualityapp.utils;

import com.github.gretaand.surfacewaterqualityapp.utils.Station;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Stores info for markers that can be used in marker clusters
 *
 * @author greta
 */

public class MarkerItem implements ClusterItem {
    private final LatLng mPosition;
    private final Station mStation;

    public MarkerItem(double lat, double lng, Station station) {
        mPosition = new LatLng(lat, lng);
        mStation = station;
    }

    /**
     *
     * @return position of the marker
     */
    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    /**
     *
     * @return null (no title)
     */
    @Override
    public String getTitle() {
        return null;
    }

    /**
     *
     * @return null (no snippet)
     */
    @Override
    public String getSnippet() {
        return null;
    }

    /**
     *
     * @return station
     */
    public Station getStation() {
        return mStation;
    }
}