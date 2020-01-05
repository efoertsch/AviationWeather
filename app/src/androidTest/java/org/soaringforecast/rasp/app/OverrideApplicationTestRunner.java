package org.soaringforecast.rasp.app;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.test.runner.AndroidJUnitRunner;


/**
 * A customized AndroidJunitRunner that will use overridden Dagger module methods.
 * This class is put in gradle.build for testInstrumentationRunner
 * From https://artemzin.com/blog/how-to-mock-dependencies-in-unit-integration-and-functional-tests-dagger-robolectric-instrumentation/
 */
public class OverrideApplicationTestRunner extends AndroidJUnitRunner {
    @Override
    @NonNull
    public Application newApplication(@NonNull ClassLoader cl,
                                      @NonNull String className,
                                      @NonNull Context context)
            throws InstantiationException,
            IllegalAccessException,
            ClassNotFoundException {
        return Instrumentation.newApplication(SoaringWeatherApplicationTest.class, context);
    }
}
