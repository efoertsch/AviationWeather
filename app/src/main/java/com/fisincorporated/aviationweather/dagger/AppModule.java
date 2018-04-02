package com.fisincorporated.aviationweather.dagger;

import android.content.res.Resources;

import com.fisincorporated.aviationweather.R;
import com.fisincorporated.aviationweather.app.AppPreferences;
import com.fisincorporated.aviationweather.app.WeatherApplication;
import com.fisincorporated.aviationweather.dagger.annotations.AviationWeatherGov;
import com.fisincorporated.aviationweather.dagger.annotations.SoaringForecast;
import com.fisincorporated.aviationweather.retrofit.AviationWeatherApi;
import com.fisincorporated.aviationweather.retrofit.AviationWeatherGovRetrofit;
import com.fisincorporated.aviationweather.retrofit.LoggingInterceptor;
import com.fisincorporated.aviationweather.retrofit.SoaringForecastApi;
import com.fisincorporated.aviationweather.retrofit.SoaringForecastRetrofit;
import com.fisincorporated.aviationweather.satellite.SatelliteImage;
import com.fisincorporated.aviationweather.satellite.SatelliteImageType;
import com.fisincorporated.aviationweather.satellite.SatelliteRegion;

import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Dispatcher;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

// TODO - Consider breaking up into separate modules - Android specific, Retrofit and Cache

@Module
public class AppModule {

    public static final String AIRPORT_PREFS = "AIRPORT_PREFS";

    @Provides
    @Named("app.shared.preferences.name")
    public String providesAppSharedPreferencesName() {
        return AIRPORT_PREFS;
    }

    private WeatherApplication application;

    // For unit testing only
    public AppModule(){}

    public AppModule(WeatherApplication application) {
        this.application = application;
    }

    @Provides
    @Singleton
    public AppPreferences provideAppPreferences() {
        return new AppPreferences(application);
    }

    @Provides
    @Singleton
    @AviationWeatherGov
    public Retrofit provideAviationWeatherGovRetrofit() {
        return new AviationWeatherGovRetrofit(getOkHttpClient(getInterceptor())).getRetrofit();
    }

    @Provides
    @Singleton
    public AviationWeatherApi providesAviationWeatherApi() {
        return provideAviationWeatherGovRetrofit().create(AviationWeatherApi.class);
    }

    @Provides
    @Singleton
    public Interceptor getInterceptor() {
        return new LoggingInterceptor();
    }


    @Provides
    @Singleton
    @SoaringForecast
    public Retrofit provideSoaringForecastRetrofit() {
        return new SoaringForecastRetrofit(getOkHttpClient(getInterceptor())).getRetrofit();
    }

    @Provides
    @Singleton
    public SoaringForecastApi providesSoaringForecastApi() {
        return provideSoaringForecastRetrofit().create(SoaringForecastApi.class);
    }

    @Provides
    @Singleton
    public Cache<String, SatelliteImage> provideCache() {
        return new Cache2kBuilder<String, SatelliteImage>() {}
                .name("Satellite Images Cache")
                .eternal(false)
                .expireAfterWrite(15, TimeUnit.MINUTES)    // expire/refresh after 15 minutes
                .entryCapacity(20)
                .build();
    }

    @Provides
    @Singleton
    public List<SatelliteRegion> provideSatelliteRegionArray() {
        ArrayList<SatelliteRegion> satelliteRegions = new ArrayList<>();
        Resources res = application.getResources();
        try {
            String[] regions = res.getStringArray(R.array.satellite_regions);
            if (regions != null) {
                for (int i = 0; i < regions.length; ++i) {
                    SatelliteRegion satelliteRegion = new SatelliteRegion(regions[i]);
                    satelliteRegions.add(satelliteRegion);
                }
            }
        } catch(Resources.NotFoundException nfe){}
        return satelliteRegions;
    }

    @Provides
    @Singleton
    public List<SatelliteImageType> provideSatelliteImageType() {
        ArrayList<SatelliteImageType> satelliteImageTypes = new ArrayList<>();
        Resources res = application.getResources();
        try {
            String[] imageTypes = res.getStringArray(R.array.satellite_image_types);
            if (imageTypes != null) {
                for (int i = 0; i < imageTypes.length; ++i) {
                    SatelliteImageType satelliteImageType = new SatelliteImageType(imageTypes[i]);
                    satelliteImageTypes.add(satelliteImageType);
                }
            }
        } catch(Resources.NotFoundException nfe){}
        return satelliteImageTypes;
    }

    @Provides
    @Singleton
    public OkHttpClient getOkHttpClient(Interceptor interceptor){
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(4);
        httpClient.dispatcher(dispatcher);
        httpClient.connectTimeout(30, TimeUnit.SECONDS);
        httpClient.readTimeout(30, TimeUnit.SECONDS);
        httpClient.addInterceptor(interceptor);
        return httpClient.build();
    }


}
