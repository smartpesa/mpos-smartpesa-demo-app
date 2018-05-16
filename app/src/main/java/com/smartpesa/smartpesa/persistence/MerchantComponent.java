package com.smartpesa.smartpesa.persistence;

import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.smartpesa.smartpesa.helpers.MenuItem;
import com.smartpesa.smartpesa.util.MoneyUtils;

import android.content.Context;
import android.support.annotation.Nullable;

import java.util.List;

import dagger.Subcomponent;
import smartpesa.sdk.models.merchant.VerifiedMerchantInfo;

@MerchantScope
@Subcomponent(
        modules = {
                MerchantModule.class
        }
)
public interface MerchantComponent {

    Context provideScopedContext();

    @Nullable
    VerifiedMerchantInfo provideMerchant();

    List<MenuItem> getMenuItems();

    @DefaultFromAccount
    int provideDefaultFromAccount();

    @DefaultToAccount
    int provideDefaultToAccount();

    MoneyUtils provideMoneyUtils();

}
