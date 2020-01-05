package org.soaringforecast.rasp.satellite.noaa;

import androidx.lifecycle.ViewModelProviders;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.soaringforecast.rasp.R;
import org.soaringforecast.rasp.app.AppPreferences;
import org.soaringforecast.rasp.databinding.NoaaSatelliteImageBinding;
import org.soaringforecast.rasp.repository.AppRepository;
import org.soaringforecast.rasp.satellite.SatelliteImageDownloader;
import org.soaringforecast.rasp.satellite.data.SatelliteImage;

import org.cache2k.Cache;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

public class NoaaSatelliteImageFragment extends DaggerFragment {

    private NoaaSatelliteImageBinding binding;
    private NoaaSatelliteViewModel satelliteViewModel;

    @Inject
    public SatelliteImageDownloader satelliteImageDownloader;

    @Inject
    public Cache<String, SatelliteImage> satelliteImageCache;

    @Inject
    public AppPreferences appPreferences;

    @Inject
    public AppRepository appRepository;
    private int lastRegionPosition = -1;
    private int lastImageTypePosition = -1;


    public static NoaaSatelliteImageFragment newInstance() {
        return new NoaaSatelliteImageFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        satelliteViewModel = ViewModelProviders.of(this).get(NoaaSatelliteViewModel.class);
    }


    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.satellite_image_fragment_noaa, container, false);
        satelliteViewModel.setAppPreferences(appPreferences)
                .setAppRepository(appRepository)
                .setSatelliteImageCache(satelliteImageCache)
                .setSatelliteImageDownloader(satelliteImageDownloader);
        binding.setLifecycleOwner(this);
        binding.setViewModel(satelliteViewModel);
        setup();
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        //set title
        getActivity().setTitle(R.string.noaa_satellite);

    }

    private void setup() {
        satelliteViewModel.setup();

        satelliteViewModel.getRegionPosition().observe(this, regionPosition -> {
                    // Don't try loading images until after spinner position intially set
                    if (lastRegionPosition != -1 && regionPosition != lastRegionPosition) {
                        satelliteViewModel.setSelectedSatelliteRegion(regionPosition);
                    }
                    lastRegionPosition = regionPosition;
                }
        );

        satelliteViewModel.getImageTypePosition().observe(this, imageTypePosition -> {
                    // Don't try loading images until after spinner position initially set
                    if (lastImageTypePosition != -1 && imageTypePosition != lastImageTypePosition) {
                        satelliteViewModel.setSelectedSatelliteImageType(imageTypePosition);
                    }
                    lastImageTypePosition = imageTypePosition;

                }
        );

        //TODO is there a way have image objected in viewmodel?
        satelliteViewModel.getSatelliteBitmap().observe(this, satelliteBitmap -> {
            binding.satelliteImageImageView.setImageBitmap(satelliteBitmap);
        });

    }

}
