package org.soaringforecast.rasp.app;


import org.soaringforecast.rasp.dagger.AppModule;
import org.soaringforecast.rasp.dagger.DaggerApplicationComponent;

import dagger.android.AndroidInjector;
import dagger.android.support.DaggerApplication;
import okhttp3.Interceptor;
import retrofit.MockInterceptor;

//https://artemzin.com/blog/how-to-mock-dependencies-in-unit-integration-and-functional-tests-dagger-robolectric-instrumentation/
public class SoaringWeatherApplicationTest extends SoaringWeatherApplication {


    public static final String AIRPORT_PREFS_TEST = "SHARED_AIRPORT_FUNCTIONAL_TEST";

    @Override
    void checkIfAirportDownloadNeeded(){}

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        AppModule appModule = new AppModule(){
            @Override
            public String providesAppSharedPreferencesName() {
                return AIRPORT_PREFS_TEST;
            }

            @Override
            public Interceptor getInterceptor() {
                return new MockInterceptor();
            }
        };
        component = DaggerApplicationComponent.builder().application(this).appModule(new AppModule(this)).build();

        component.inject(this);
        return component;
    }

}
