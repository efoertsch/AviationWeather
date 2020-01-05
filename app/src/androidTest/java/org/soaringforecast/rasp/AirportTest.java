package org.soaringforecast.rasp;

import androidx.room.Room;
import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.soaringforecast.rasp.airport.AirportListDownloader;
import org.soaringforecast.rasp.repository.Airport;
import org.soaringforecast.rasp.repository.AppDatabase;
import org.soaringforecast.rasp.repository.AppRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import okhttp3.OkHttpClient;

import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class AirportTest {
    private AppRepository appRepository;
    private AppDatabase appDatabase;
    private OkHttpClient okHttpClient = new OkHttpClient();


    @Before
    public void createDb() {
        Context context = InstrumentationRegistry.getTargetContext();
        appDatabase = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        appRepository = AppRepository.getAppRepository(context, null, null, null, null, null,null);

    }

    @After
    public void closeDb() {
        appDatabase.close();
    }

    @Test
    public void saveOneAirportToDB() {
        Airport airport = Airport.getNewAirport();
        airport.setIdent("3B3");
        airport.setName("Sterling Airport");
        airport.setMunicipality("Sterling");
        airport.setState("MA");
        appRepository.insertAirport(airport);
        airport = appRepository.getAirport("3B3").blockingGet();
        assertNotNull(airport);
        List<Airport> airports = appRepository.findAirports("3B3").blockingGet();
        assert (airports.size() == 1);
        airports = appRepository.findAirports( "Sterling Airport").blockingGet();
        assert (airports.size() == 1);

        airports = appRepository.findAirports("Sterling").blockingGet();
        assert (airports.size() == 1);

    }

    @Test
    public void downloadAirportFileAndSaveToDB() {
         int count = appRepository.deleteAllAirports().blockingGet();
        AirportListDownloader airportListDownloader = new AirportListDownloader(okHttpClient, appRepository);
        count = airportListDownloader.downloadAirportsToDB().blockingGet();
        assert(count > 500);
    }
}
