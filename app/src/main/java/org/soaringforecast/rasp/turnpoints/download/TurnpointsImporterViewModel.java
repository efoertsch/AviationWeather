package org.soaringforecast.rasp.turnpoints.download;

import android.annotation.SuppressLint;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import android.os.Environment;

import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.repository.Turnpoint;
import org.soaringforecast.rasp.retrofit.JSONServerApi;
import org.soaringforecast.rasp.retrofit.TurnpointFileApi;
import org.soaringforecast.rasp.retrofit.TurnpointFileRetrofit;
import org.soaringforecast.rasp.turnpoints.json.TurnpointFile;
import org.soaringforecast.rasp.turnpoints.json.TurnpointRegion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import timber.log.Timber;

public class TurnpointsImporterViewModel extends ViewModel {

    private AppRepository appRepository;
    private AppPreferences appPreferences;
    private OkHttpClient okHttpClient;
    private JSONServerApi JSONServerApi;

    private MutableLiveData<List<TurnpointFile>> turnpointFiles = new MutableLiveData();
    private MutableLiveData<List<File>> cupFiles = new MutableLiveData();
    private CompositeDisposable compositeDisposable = new CompositeDisposable();


    public TurnpointsImporterViewModel setAppRepository(AppRepository appRepository) {
        this.appRepository = appRepository;
        return this;
    }

    public TurnpointsImporterViewModel setAppPreferences(AppPreferences appPreferences) {
        this.appPreferences = appPreferences;
        return this;
    }

    public TurnpointsImporterViewModel setOkHttpClient(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
        return this;
    }

    public TurnpointsImporterViewModel setGBSCJsonApi(JSONServerApi JSONServerApi){
        this.JSONServerApi = JSONServerApi;
        return this;
    }

    /**
     * Get list of .cup(SeeYou) files in the devices Downloads directory
     * @return
     */
    @SuppressLint("CheckResult")
    public LiveData<List<File>> getCupFiles() {

        Disposable disposable = appRepository.getDownloadedCupFileList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(downloadedCupFileList ->
                                cupFiles.setValue(downloadedCupFileList)
                        , Timber::e);
        compositeDisposable.add(disposable);
        return cupFiles;
    }

    @SuppressLint("CheckResult")
    // Get list of selected .cup(SeeYou) files based on JSON from soargbsc.com/soaringforecast/turnpoint_regions.json
    public LiveData<List<TurnpointFile>> getTurnpointFiles() {
        Disposable disposable = JSONServerApi.getTurnpointRegions()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(turnpointRegions -> {
                            String currentRegion =  appPreferences.getSoaringForecastRegion();
                            List<TurnpointRegion> turnpointRegionList = turnpointRegions.getTurnpointRegions();
                            for (TurnpointRegion turnpointRegion: turnpointRegionList){
                                if (turnpointRegion.getRegion().equals(currentRegion)){
                                    turnpointFiles.setValue(turnpointRegion.getTurnpointFiles());
                                    break;
                                }
                            }
                        }
                        , Timber::e);
        compositeDisposable.add(disposable);
        return turnpointFiles;
    }

    public Single<Integer> clearTurnpointDatabase() {
        return appRepository.deleteAllTurnpoints();
    }


    public Single<Integer> importTurnpointFileFromDownloadDirectory(final String fileName) {
        return new Single<Integer>() {
            @Override
            protected void subscribeActual(SingleObserver s) {
                try {
                    int numberTurnpoints = importTurnpointsFromDownloadDirectory(fileName);
                    s.onSuccess(numberTurnpoints);
                } catch (IOException e) {
                    s.onError(e);
                }
            }
        };
    }

    /**
     * @param fileName SeeYou cup file name in device Download directory
     * @return success if import went OK
     * @throws IOException
     */
    private int importTurnpointsFromDownloadDirectory(final String fileName) throws IOException {
        BufferedReader reader = null;
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String cupFilePath = path.getAbsoluteFile() + "/" + fileName;
        try {
            Timber.d("Turnpoint file: %1$s", fileName);
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(cupFilePath)));
            return saveTurnpoints(reader);
        } finally {
            if (reader != null) try {
                reader.close();
            } catch (IOException ignored) {
            }
        }
    }

    public Single<Integer> importTurnpointsFromUrl(String url) {
        return getDownloadTurnpointsObservable(okHttpClient, url)
                .flatMap(responseBody -> saveTurnpointsFromUrl(responseBody));
    }

   private Single<ResponseBody> getDownloadTurnpointsObservable(OkHttpClient okHttpClient, String url) {
        Retrofit retrofit = new TurnpointFileRetrofit(okHttpClient).getRetrofit();
        TurnpointFileApi downloadService = retrofit.create(TurnpointFileApi.class);
        return downloadService.getCupFile(url);
    }

    /**
     * @param responseBody
     * @return
     * @throws IOException
     */
    private Single<Integer> saveTurnpointsFromUrl(ResponseBody responseBody) {
        return new Single<Integer>() {
            @Override
            protected void subscribeActual(SingleObserver s) {
                BufferedReader reader = null;
                try {
                    Timber.d("File Size= %1$s", responseBody.contentLength());
                    reader = new BufferedReader(new InputStreamReader(responseBody.byteStream()));
                    int numberTurnpoints = saveTurnpoints(reader);
                    s.onSuccess(numberTurnpoints);
                } catch (IOException e) {
                    e.printStackTrace();
                    Timber.d("Failed to save the file!");
                    s.onError(e);
                } finally {
                    if (reader != null) try {
                        reader.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        };
    }

    private int saveTurnpoints(BufferedReader reader) throws IOException {
        int linesRead = 0;
        int numberTurnpoints = 0;
        String turnpointLine;
        Turnpoint turnpoint;
        turnpointLine = reader.readLine();
        while (turnpointLine != null && !turnpointLine.isEmpty()) {
            linesRead++;
            if (linesRead > 1) {
                turnpoint = Turnpoint.createTurnpointFromCSVDetail(turnpointLine);
                if (turnpoint != null) {
                    appRepository.insertTurnpointViaViaBackground(turnpoint);
                }
                numberTurnpoints++;
            }
            turnpointLine = reader.readLine();
        }
        Timber.d("Lines read: %1$d  Lines written %2$d", linesRead, numberTurnpoints);
        Timber.d("File saved to DB successfully!");
        return numberTurnpoints;
    }

    @Override
    public void onCleared() {
        compositeDisposable.dispose();
        super.onCleared();
    }

}
