package com.github.gretaand.surfacewaterqualityapp.utils;

import android.graphics.Color;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.Date;

/**
 * POJO to store result information
 * http://www.waterqualitydata.us/Result/search?
 *
 * @author greta
 */
@Parcel
public class Result {

    @SerializedName("stationPrimaryKey")
    @Expose
    int stationPrimaryKey;
    @SerializedName("activityStartDate")
    @Expose
    Date activityStartDate;
    @SerializedName("activityMediaName")
    @Expose
    String activityMediaName;
    @SerializedName("characteristicName")
    @Expose
    String characteristicName;
    @SerializedName("detectionCondition")
    @Expose
    String detectionCondition;
    @SerializedName("measureValueString")
    @Expose
    String measureValueString;
    @SerializedName("measureUnitCode")
    @Expose
    String measureUnitCode;
    @SerializedName("convertedMeasureValue")
    @Expose
    double convertedMeasureValue;
    @SerializedName("convertedMeasureUnitCode")
    @Expose
    String convertedMeasureUnitCode;
    @SerializedName("warningLevel")
    @Expose
    int warningLevel;

    public Result() {}

    /**
     *
     * @return result station's id
     */
    public int getStationPrimaryKey() {
        return this.stationPrimaryKey;
    }

    /**
     *
     * @return test date
     */
    public Date getActivityStartDate() {
        return this.activityStartDate;
    }

    /**
     *
     * @return media tested (e.g. water, biological tissue)
     */
    public String getActivityMediaName() {
        return this.activityMediaName;
    }

    /**
     *
     * @return characteristic name
     */
    public String getCharacteristicName() {
        return this.characteristicName;
    }

    /**
     *
     * @return detection condition
     */
    public String getDetectionCondition() {
        return this.detectionCondition;
    }

    /**
     *
     * @return measured value as a string
     */
    public String getMeasureValueString() {
        return this.measureValueString;
    }

    /**
     *
     * @return the measured value as a number; used to compare limit and measure values
     */
    public double getMeasureValue() {
        return Double.parseDouble(this.measureValueString);
    }

    /**
     *
     * @return measure unit code
     */
    public String getMeasureUnitCode() {
        return this.measureUnitCode;
    }

    /**
     * returns the converted measured value (converted to units of limit)
     * as a number; used to compare limit and measure values
     */
    public Double getConvertedMeasureValue() {
        return this.convertedMeasureValue;
    }

    /**
     *
     * @return converted measure unit code
     */
    public String getConvertedMeasureUnitCode() {
        return this.convertedMeasureUnitCode;
    }

    /**
     *
     * @return warning level
     */
    public int getWarningLevel(){
        return this.warningLevel;
    }

    /**
     *
     * @param warningLevel warning level
     * @return color associated with warning level
     */
    public int getWarningColor(int warningLevel) {
        if (warningLevel == 4) {           // very high
            return Color.RED;
        }
        else if (warningLevel == 3) {       // high
            return android.graphics.Color.rgb(255, 165, 0);
        }
        else if (warningLevel == 2) {       // moderate
            return Color.YELLOW;
        }
        else if (warningLevel == 1) {       // low
            return Color.GREEN;
        }
        else {
            return Color.LTGRAY;            // none
        }
    }

    /**
     *
     * @param warningLevel warning level
     * @return text associated with warning level
     */
    public String getWarningText(int warningLevel) {
        if (warningLevel == 4) {            // very high
            return "X";
        }
        else if (warningLevel == 3) {       // high
            return "!!";
        }
        else if (warningLevel == 2) {       // moderate
            return "!";
        }
        else if (warningLevel == 1) {       // low
            return "OK";
        }
        else {
            return "?";                     // none
        }
    }

    /**
     *
     * @return true if the measured value is a number since many are text
     */
    public boolean checkIfMeasureValueIsDouble() {
        try{
            Double.parseDouble(measureValueString);
        }
        catch(Exception e) {
            return false;
        }
        return true;
    }
}
