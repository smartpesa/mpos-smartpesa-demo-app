package com.smartpesa.smartpesa.activity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import dagger.Lazy;
import smartpesa.sdk.ServiceManager;

public class LocateOperators extends AppCompatActivity {

    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    @Bind(R.id.mapview) MapView mMapView;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    Double lattitude, longitude;
    public Lazy<ServiceManager> serviceManager;
    ArrayList<LatLng> fakeLocations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locate_operators);
        ButterKnife.bind(this);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        mMapView.onCreate(mapViewBundle);

        //setup toolbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        serviceManager = SmartPesaApplication.component(this).serviceManager();

        updateLocationInfo();
        fakeLocations = new ArrayList<>();
        fakeLocations.add(new LatLng(1.280292, 103.8422893));
        fakeLocations.add(new LatLng(1.279511, 103.8400883));
        fakeLocations.add(new LatLng(1.276894, 103.8423633));
        fakeLocations.add(new LatLng(1.277055, 103.8403673));

        if (lattitude != null && longitude != null) {
            if (lattitude.doubleValue() != 0.0 && longitude.doubleValue() != 0.0) {
                mMapView.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        LatLng transactionLocation = new LatLng(lattitude, longitude);
                        googleMap.addMarker(new MarkerOptions().position(transactionLocation)
                                .title("My location"));
                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(transactionLocation)
                                .zoom(17).build();
                        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        showNearbyOperators(googleMap);
                        mMapView.setVisibility(View.VISIBLE);
                    }
                });
            } else {
                mMapView.setVisibility(View.GONE);
            }
        } else {
            mMapView.setVisibility(View.GONE);
        }
    }

    private void showNearbyOperators(GoogleMap googleMap) {
        for (int i = 0; i < fakeLocations.size(); i++) {
            googleMap.addMarker(new MarkerOptions().position(fakeLocations.get(i))
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher))
                    .title("SmartPesa operator"));
        }
    }

    private double[] getGPS() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = lm.getProviders(true);
        Location l = null;
        for (int i = providers.size() - 1; i >= 0; i--) {
            l = lm.getLastKnownLocation(providers.get(i));
            if (l != null) break;
        }
        double[] gps = new double[2];
        if (l != null) {
            gps[0] = l.getLatitude();
            gps[1] = l.getLongitude();
        }
        return gps;
    }

    private void updateLocationInfo() {
        double[] gps = getGPS();
        if (gps != null) {
            lattitude = gps[0];
            longitude = gps[1];
            if (lattitude != null && longitude != null) {
                if (lattitude != null && longitude != null) {
                    if (lattitude.doubleValue() != 0.0 && longitude.doubleValue() != 0.0) {
                        mMapView.getMapAsync(new OnMapReadyCallback() {
                            @Override
                            public void onMapReady(GoogleMap googleMap) {
                                LatLng transactionLocation = new LatLng(lattitude, longitude);
                                googleMap.addMarker(new MarkerOptions().position(transactionLocation)
                                        .title("My Location"));
                                CameraPosition cameraPosition = new CameraPosition.Builder()
                                        .target(transactionLocation)
                                        .zoom(17).build();
                                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                mMapView.setVisibility(View.VISIBLE);
                            }
                        });
                    } else {
                        mMapView.setVisibility(View.GONE);
                    }
                } else {
                    mMapView.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();

    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

}
