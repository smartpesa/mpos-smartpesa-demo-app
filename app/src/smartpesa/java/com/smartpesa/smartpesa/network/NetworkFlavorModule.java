package com.smartpesa.smartpesa.network;

import com.smartpesa.smartpesa.R;

import android.content.res.Resources;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class NetworkFlavorModule {

    @Provides
    @Endpoint
    @Singleton
    String provideEndpoint(Resources resources)
    {
        return resources.getString(R.string.app_url);
    }
}
