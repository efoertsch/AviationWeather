package com.fisincorporated.aviationweather.soaring.json;

import com.fisincorporated.aviationweather.data.taf.Forecast;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Forecasts {

    @SerializedName("forecasts")
    @Expose
    private List<Forecast> forecasts = null;

    public List<Forecast> getForecasts() {
        return forecasts;
    }

    public void setForecasts(List<Forecast> forecasts) {
        this.forecasts = forecasts;
    }

}