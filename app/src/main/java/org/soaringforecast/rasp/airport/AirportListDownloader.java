package org.soaringforecast.rasp.airport;

import org.soaringforecast.rasp.repository.Airport;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.retrofit.AirportListDownloadApi;
import org.soaringforecast.rasp.retrofit.AirportListRetrofit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Single;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import timber.log.Timber;


/**
 * Download the airport list
 * Only process US airports
 * Read file and process in Room database
 */
public class AirportListDownloader {

    private final OkHttpClient okHttpClient;
    private final AppRepository appRepository;

    @Inject
    public AirportListDownloader(OkHttpClient okHttpClient, AppRepository appRepository) {
        this.okHttpClient = okHttpClient;
        this.appRepository = appRepository;
    }


    public Single<Integer>  downloadAirportsToDB() {
        return Completable.fromSingle(appRepository.deleteAllAirports()).andThen(getDownloadAirportListObservable(okHttpClient).flatMapCompletable(responseBody ->
                saveAirportListToDB(responseBody)).andThen(appRepository.getCountOfAirports()));
    }

    private Single<ResponseBody> getDownloadAirportListObservable(OkHttpClient okHttpClient) {
        Retrofit retrofit = new AirportListRetrofit(okHttpClient).getRetrofit();
        AirportListDownloadApi downloadService = retrofit.create(AirportListDownloadApi.class);
        return downloadService.downloadAirportList();
    }

    private Completable saveAirportListToDB(ResponseBody responseBody) {
        return new Completable() {
            @Override
            protected void subscribeActual(CompletableObserver s) {
                Airport airport;
                String airportLine;
                int linesRead = 0;
                int usAirports = 0;
                BufferedReader reader = null;
                try {
                    Timber.d("File Size= %1$s" , responseBody.contentLength());
                    reader = new BufferedReader(new InputStreamReader(responseBody.byteStream()));
                    airportLine = reader.readLine();
                    while (airportLine != null && !airportLine.isEmpty()) {
                        linesRead++;
                        if (linesRead == 2 || airportLine.contains("US-")) {
                            airport = Airport.createAirportFromCSVDetail(airportLine);
                            if (airport != null && airport.getIdent() != null) {
                                appRepository.insertAirport(airport);
                            }
                            usAirports++;
                        }
                        airportLine = reader.readLine();
                    }
                    Timber.d("Lines read: %1$d   Lines written %2$d", linesRead, usAirports);
                    Timber.d("File saved to DB successfully!");
                    s.onComplete();
                } catch (IOException e) {
                    e.printStackTrace();
                    Timber.d("Failed to save the file!");
                    s.onError(e);
                } finally {
                    if (reader != null) try {reader.close();} catch (IOException ignored){}
                }
            }
        };
    }

    private Single<Integer> getCountOfAirports() {
        return appRepository.getCountOfAirports();
    }

}
