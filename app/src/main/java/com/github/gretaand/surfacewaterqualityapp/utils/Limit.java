package com.github.gretaand.surfacewaterqualityapp.utils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Stores limit information
 * https://www.epa.gov/wqc/national-recommended-water-quality-criteria-human-health-criteria-table
 *
 * @author greta
 */
public class Limit {

    @SerializedName("characteristicName")
    @Expose
    private String characteristicName;
    @SerializedName("limitValue")
    @Expose
    private Double limitValue;
    @SerializedName("limitUnit")
    @Expose
    private String limitUnit;
    @SerializedName("pollutantInfoLink")
    @Expose
    private String pollutantInfoLink;
    @SerializedName("notes")
    @Expose
    private String notes;

    /**
     *
     * @return characteristic name
     */
    public String getCharacteristicName() {
        return this.characteristicName;
    }

    /**
     *
     * @return limit value
     */
    public Double getLimitValue() {
        return this.limitValue;
    }

    /**
     *
     * @return limit unit
     */
    public String getLimitUnit() {
        return this.limitUnit;
    }

    /**
     *
     * @return link to more info
     */
    public String getPollutantInfoLink() {
        return this.pollutantInfoLink;
    }

    /**
     *
     * @return notes about the pollutant or limit info
     */
    public String getNotes() {
        return this.notes;
    }
}
