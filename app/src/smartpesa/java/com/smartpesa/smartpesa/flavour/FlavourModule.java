package com.smartpesa.smartpesa.flavour;

import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collections;
import java.util.List;

import dagger.Module;
import dagger.Provides;
import smartpesa.sdk.models.merchant.VerifiedMerchantInfo;

@Module
public class FlavourModule {

    @Provides
    @FlavourSpecific
    @FlavourScope
    @NonNull
    public List<IDrawerItem> providesFlavourSpecificMenuItems(Context context, @Nullable VerifiedMerchantInfo verifyMerchantInfo) {
        return Collections.emptyList();
    }

    @Provides
    @FlavourSpecific
    @FlavourScope
    public MenuHandler providesFlavourSpecificMenuHandler(SmartPesaMenuHandler menuHandler) {
        return menuHandler;
    }

    @Provides
    @FlavourSpecific
    @FlavourScope
    public PaymentHandler providePaymentHandler(SmartPesaPaymentHandler paymentHandler) {
        return paymentHandler;
    }
}
