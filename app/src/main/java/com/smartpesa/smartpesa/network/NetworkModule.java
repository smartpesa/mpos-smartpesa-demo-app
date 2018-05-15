package com.smartpesa.smartpesa.network;

import com.smartpesa.smartpesa.R;

import android.content.res.Resources;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class NetworkModule {

    @Provides
    @VersionEndpoint
    @Singleton
    String provideVersionEndpoint(@Endpoint String endPoint, Resources resources) {
        return resources.getString(R.string.version_endpoint, endPoint);
    }
}
