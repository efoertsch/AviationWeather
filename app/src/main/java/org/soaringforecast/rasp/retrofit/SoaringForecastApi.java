package org.soaringforecast.rasp.retrofit;



import org.soaringforecast.rasp.soaring.json.ForecastModels;
import org.soaringforecast.rasp.soaring.json.Regions;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Calls to www.soargbsc.com/rasp
 */
public interface SoaringForecastApi {

    // Construct URL as BASE_URL + "current.json
    @GET()
    Single<Regions> getForecastDates(@Url String url);


    // Construct URL as BASE_URL + "NewEngland/2018-03-30/status.json"
    @GET
    Single<ForecastModels> getForecastModels(@Url String url);


}