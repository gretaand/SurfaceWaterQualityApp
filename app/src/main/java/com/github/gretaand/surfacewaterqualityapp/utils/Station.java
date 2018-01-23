package com.github.gretaand.surfacewaterqualityapp.utils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Stores station information
 * http://www.waterqualitydata.us/Station/search?
 *
 * @author greta
 */
public class Station {

    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("locationId")
    @Expose
    private String locationId;
    @SerializedName("locationName")
    @Expose
    private String locationName;
    @SerializedName("latitude")
    @Expose
    private double latitude;
    @SerializedName("longitude")
    @Expose
    private double longitude;
    @SerializedName("warningLevel")
    @Expose
    private int warningLevel;
    @SerializedName("locationType")
    @Expose
    private String locationType;

    /**
     *
     * @return id
     */
    public int getId() {
        return this.id;
    }

    /**
     *
     * @return location id
     */
    public String getLocationId() {
        return this.locationId;
    }

    /**
     *
     * @return location name
     */
    public String getLocationName() {
        return this.locationName;
    }

    /**
     *
     * @return latitude
     */
    public double getLatitude() {
        return this.latitude;
    }

    /**
     *
     * @return longitude
     */
    public double getLongitude() {
        return this.longitude;
    }

    /**
     *
     * @return location type
     */
    public String getLocationType() {
        return this.locationType;
    }

    /**
     *
     * @return warning level associated with most recent results for the station
     */
    public int getWarningLevel() {
        return this.warningLevel;
    }
}
