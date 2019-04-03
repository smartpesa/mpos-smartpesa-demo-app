package com.smartpesa.smartpesa;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.smartpesa.smartpesa.flavour.DaggerFlavourComponent;
import com.smartpesa.smartpesa.flavour.FlavourComponent;
import com.smartpesa.smartpesa.flavour.FlavourModule;
import com.smartpesa.smartpesa.persistence.MerchantComponent;
import com.smartpesa.smartpesa.persistence.MerchantModule;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDexApplication;

import io.fabric.sdk.android.Fabric;
import okhttp3.HttpUrl;
import smartpesa.sdk.ServiceManager;
import smartpesa.sdk.ServiceManagerConfig;
import smartpesa.sdk.network.NetworkSettings;
import smartpesa.sdk.ota.OtaManager;
import smartpesa.sdk.ota.OtaManagerConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class SmartPesaApplication extends MultiDexApplication {
    public static final String FONTS_SMART_PESA_REGULAR_TTF = "fonts/SmartPesa-Regular.ttf";
    private static FlavourComponent sFlavourComponent;
    private SmartPesaApplicationComponent mComponent;
    private MerchantComponent mMerchantComponent;

    public static SmartPesaApplicationComponent component(Context context) {
        return ((SmartPesaApplication) context.getApplicationContext()).mComponent;
    }

    @Nullable
    public static MerchantComponent merchantComponent(Context context) {
        return ((SmartPesaApplication) context.getApplicationContext()).mMerchantComponent;
    }

    public static FlavourComponent flavourComponent(Context context) {
        if (sFlavourComponent == null) {
            sFlavourComponent = DaggerFlavourComponent.builder()
                    .flavourModule(new FlavourModule())
                    .merchantComponent(merchantComponent(context))
                    .build();
        }
        return sFlavourComponent;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Set up Crashlytics, disabled for debug builds
        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();
        Fabric.with(this, crashlyticsKit);

        mComponent = DaggerSmartPesaApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();

        TimberUtils.initTimber();

        // Init Calligraphy
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath(FONTS_SMART_PESA_REGULAR_TTF)
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        ServiceManagerConfig config;

        config = new ServiceManagerConfig.Builder(getApplicationContext())
                .networkSettings(new NetworkSettings.Builder()
                        .url(new HttpUrl.Builder()
                                .host("demo.smartpesa.com")
                                .scheme("http")
                                .build())
                        .build())
                .build();

        try {
            ServiceManager.init(config);
        } catch (Exception e) {
            e.printStackTrace();
        }

        OtaManagerConfig otaConfig = new OtaManagerConfig.Builder(getApplicationContext())
                .setNetworkSettings(new NetworkSettings.Builder()
                        .url(new HttpUrl.Builder()
                                .host("demo.smartpesa.com")
                                .scheme("http")
                                .build())
                        .build())
                .build();

        OtaManager.init(otaConfig);

        if (mComponent.serviceManager().get().getCachedMerchant() != null) {
            createMerchantComponent();
        }
    }

    public void createMerchantComponent() {
        this.mMerchantComponent = mComponent.plus(new MerchantModule());
        //Recreate flavour component as well
        sFlavourComponent = DaggerFlavourComponent.builder()
                .flavourModule(new FlavourModule())
                .merchantComponent(this.mMerchantComponent)
                .build();
    }

    public void releaseMerchant() {
        this.mMerchantComponent = null;
    }
}
