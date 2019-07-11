package org.soaringforecast.rasp.soaring.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

// Load  from raw/forecast_options_json.json
public class Forecast {

    @SerializedName("forecast_name")
    @Expose
    private String forecastName;   // "wstar_bsratio"
    @SerializedName("forecast_type")
    @Expose
    private String forecastType;    //"" (primary type always displayed), "full" (display if all types to be displayed), "comment" (header)
    @SerializedName("forecast_name_display")
    @Expose
    private String forecastNameDisplay;    //"Thermal Updraft Velocity & B/S Ratio"
    @SerializedName("forecast_description")
    @Expose
    private String forecastDescription;   //"A composite plot displaying the Thermal Updraft Velocity ...."
    @SerializedName("forecast_category")
    @Expose
    private String forecastCategory;   //"Thermal", "Wind",

    public String getForecastName() {
        return forecastName;
    }

    public void setForecastName(String forecastName) {
        this.forecastName = forecastName;
    }

    public String getForecastType() {
        return forecastType;
    }

    public void setForecastType(String forecastType) {
        this.forecastType = forecastType;
    }

    public String getForecastNameDisplay() {
        return forecastNameDisplay;
    }

    public void setForecastNameDisplay(String forecastNameDisplay) {
        this.forecastNameDisplay = forecastNameDisplay;
    }

    public String getForecastDescription() {
        return forecastDescription;
    }

    public void setForecastDescription(String forecastDescription) {
        this.forecastDescription = forecastDescription;
    }

    public String getForecastCategory() {
        return forecastCategory;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Forecast) {
            Forecast c = (Forecast) obj;
            return c.forecastName.equals(forecastName);
        }

        return false;
    }

    public String toString(){
        return forecastNameDisplay;
    }

}