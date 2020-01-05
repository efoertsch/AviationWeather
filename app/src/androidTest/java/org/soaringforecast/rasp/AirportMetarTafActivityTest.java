package org.soaringforecast.rasp;


import android.content.Context;
import android.content.Intent;
import androidx.test.espresso.ViewInteraction;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.soaringforecast.rasp.app.BaseTest;
import org.soaringforecast.rasp.soaring.forecast.ForecastDrawerActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import timber.log.Timber;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class AirportMetarTafActivityTest extends BaseTest {

    @Rule
    public ActivityTestRule<ForecastDrawerActivity> mActivityRule =
            new ActivityTestRule<>(ForecastDrawerActivity.class, true, false);

    @Before
    public void setup() {
        Timber.d("Setup called");
        super.setup();
        setSharedPreferenceAirportList(airportList);
    }

    @Test
    public void airportInfoHasMetarFields() {
        Context targetContext = getContext();
        Intent intent = new Intent(targetContext, ForecastDrawerActivity.class);

        mActivityRule.launchActivity(intent);

        String[] airports = airportList.split("\\s+");
        for (int i = 0; i < airports.length; ++i) {

            scrollToRecyclerPosition(R.id.airport_metar_taf_recycler_view, i);
            ViewInteraction viewInteraction = onView(withRecyclerView(R.id.airport_metar_taf_recycler_view).atPosition(i));
            // Check that metar layout exists
            viewInteraction.check(matches(hasDescendant(withId(R.id.airport_weather_include_metar))));
            // Check that metar field exists
            viewInteraction.check(matches(hasDescendant(withId(R.id.airport_station_raw_text))));
        }
    }

    @Test
    public void airportInfoHasTafFields() {
        Context targetContext = getContext();
        Intent intent = new Intent(targetContext, ForecastDrawerActivity.class);

        mActivityRule.launchActivity(intent);

        String[] airports = airportList.split("\\s+");
        for (int i = 0; i < airports.length; ++i) {

            scrollToRecyclerPosition(R.id.airport_metar_taf_recycler_view, i);
            ViewInteraction viewInteraction = onView(withRecyclerView(R.id.airport_metar_taf_recycler_view).atPosition(i));
            // Check that metar layout exists
            viewInteraction.check(matches(hasDescendant(withId(R.id.airport_weather_include_taf))));
            // Check that metar field exists
            viewInteraction.check(matches(hasDescendant(withId(R.id.airport_taf_raw_forecast))));
        }
    }

    @Test
    public void airportsAreDisplayedInOrder() {
        Context targetContext = getContext();
        Intent intent = new Intent(targetContext, ForecastDrawerActivity.class);

        mActivityRule.launchActivity(intent);

        String[] airports = airportList.split("\\s+");
        for (int i = 0; i < airports.length; ++i) {

            scrollToRecyclerPosition(R.id.airport_metar_taf_recycler_view, i);
            ViewInteraction viewInteraction = onView(withRecyclerView(R.id.airport_metar_taf_recycler_view).atPosition(i));

            // Check the row has the airport id field
            viewInteraction.check(matches(hasDescendant(withId(R.id.airport_station_id))));
            // Check that the current position has the correct airport id
            viewInteraction.check(matches(hasDescendant(withText(airports[i]))));
            // Check that metar layout exists
            viewInteraction.check(matches(hasDescendant(withId(R.id.airport_weather_include_metar))));
            // Check that metar field exists
            viewInteraction.check(matches(hasDescendant(withId(R.id.airport_station_raw_text))));
        }
    }

    @After
    public void after() {
        Timber.d("After Called");
    }

}
