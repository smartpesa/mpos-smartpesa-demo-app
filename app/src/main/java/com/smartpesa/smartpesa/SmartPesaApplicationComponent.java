package com.smartpesa.smartpesa;

import com.smartpesa.smartpesa.activity.MainActivityComponent;
import com.smartpesa.smartpesa.activity.MainActivityModule;
import com.smartpesa.smartpesa.activity.SplashActivity;
import com.smartpesa.smartpesa.activity.payment.IntentPaymentProgressActivity;
import com.smartpesa.smartpesa.helpers.PreferenceHelper;
import com.smartpesa.smartpesa.network.Endpoint;
import com.smartpesa.smartpesa.network.NetworkFlavorModule;
import com.smartpesa.smartpesa.network.NetworkModule;
import com.smartpesa.smartpesa.network.VersionEndpoint;
import com.smartpesa.smartpesa.persistence.MerchantComponent;
import com.smartpesa.smartpesa.persistence.MerchantModule;
import com.smartpesa.smartpesa.persistence.PersistenceModule;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Component;
import dagger.Lazy;
import smartpesa.sdk.ServiceManager;

@Singleton
@Component(modules = {ApplicationModule.class,
        PersistenceModule.class,
        NetworkModule.class,
        NetworkFlavorModule.class})
public interface SmartPesaApplicationComponent {

    Context provideApplicationContext();

    Lazy<ServiceManager> serviceManager();

    PreferenceHelper preferenceHelper();

    @Endpoint String serverEndpoint();

    @VersionEndpoint String versionEndpoint();

    void inject(SplashActivity splashActivity);

    void inject(IntentPaymentProgressActivity intentPaymentProgressActivity);

    MerchantComponent plus(MerchantModule merchantModule);

    MainActivityComponent plus(MainActivityModule mainActivityModule);
}
