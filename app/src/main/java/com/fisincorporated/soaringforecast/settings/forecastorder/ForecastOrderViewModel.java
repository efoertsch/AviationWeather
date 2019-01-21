package com.fisincorporated.soaringforecast.settings.forecastorder;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.fisincorporated.soaringforecast.app.AppPreferences;
import com.fisincorporated.soaringforecast.repository.AppRepository;
import com.fisincorporated.soaringforecast.soaring.json.Forecast;
import com.fisincorporated.soaringforecast.soaring.json.Forecasts;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class ForecastOrderViewModel extends ViewModel implements  ForecastOrderAdapter.NewForecastListListener {

    private MutableLiveData<List<Forecast>> orderedForecasts;
    private AppRepository appRepository;
    private AppPreferences appPreferences;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public ForecastOrderViewModel setRepositoryAndPreferences(AppRepository appRepository, AppPreferences appPreferences) {
        this.appRepository = appRepository;
        this.appPreferences = appPreferences;
        return this;
    }

    public MutableLiveData<List<Forecast>> getOrderedForecasts() {
        if (orderedForecasts == null) {
            orderedForecasts = new MutableLiveData<>();
            listOrderedForecasts();
        }
        return orderedForecasts;
    }

    /**
     * First try to get forecast list from appPreferences, and if nothing, get default list from appRepository
     */
    public void listOrderedForecasts() {
        Disposable disposable = appPreferences.getOrderedForecastList().flatMap(new Function<Forecasts, Observable<Forecasts>>() {
            @Override
            public Observable<Forecasts> apply(Forecasts orderedForecasts) {
                if (orderedForecasts != null && orderedForecasts.getForecasts() != null && orderedForecasts.getForecasts().size() > 0) {
                    return Observable.just(orderedForecasts);
                } else {
                    return appRepository.getForecasts().toObservable();
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(orderedForecasts -> {
                            this.orderedForecasts.setValue(orderedForecasts.getForecasts());
                        }
                );
        compositeDisposable.add(disposable);
    }

    @Override
    public void newForecastOrder(List<Forecast> orderedForecasts) {
        Forecasts forecasts = new Forecasts();
        forecasts.setForecasts(orderedForecasts);
        appPreferences.setOrderedForecastList(forecasts);
    }


    @Override
    public void onCleared() {
        compositeDisposable.dispose();
        super.onCleared();
    }

}
