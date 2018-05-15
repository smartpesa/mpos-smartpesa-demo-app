package com.smartpesa.smartpesa;

import com.smartpesa.smartpesa.network.Endpoint;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import smartpesa.sdk.ServiceManager;

@Module
public class ApplicationModule {
    private final SmartPesaApplication mApp;

    public ApplicationModule(SmartPesaApplication app) {
        this.mApp = app;
    }

    @Provides
    @Singleton
    Context provideApplicationContext() {
        return mApp;
    }

    @Provides
    @Singleton
    Application provideApplication() {
        return mApp;
    }

    @Provides
    @Singleton
    Resources provideResources(Application application) {
        return application.getResources();
    }

    @Provides
    @Singleton
    ServiceManager provideServiceManager(Application application, @Endpoint String endpoint) {
        return ServiceManager.get(application);
    }

}
