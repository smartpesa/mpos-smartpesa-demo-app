package com.smartpesa.smartpesa.persistence;

import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.flavour.FlavourComponent;
import com.smartpesa.smartpesa.helpers.MenuItem;
import com.smartpesa.smartpesa.helpers.UIHelper;
import com.smartpesa.smartpesa.util.MoneyUtils;

import android.content.Context;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dagger.Module;
import dagger.Provides;
import smartpesa.sdk.ServiceManager;
import smartpesa.sdk.SmartPesa;
import smartpesa.sdk.models.merchant.VerifiedMerchantInfo;

@Module
public class MerchantModule {
    public static final int MENU_ID_SALE = 0;
    public static final int MENU_ID_HISTORY = 1;
    public static final int MENU_ID_REFUND = 2;
    public static final int MENU_ID_CASH_BACK = 3;
    public static final int MENU_ID_BALANCE_INQUIRY = 4;
    public static final int MENU_ID_WITHDRAWAL = 5;
    public static final int MENU_ID_FUND_TRANSFER = 6;
    public static final int MENU_ID_BILL_PAYMENT = 7;
    public static final int MENU_ID_OPERATORS = 8;
    public static final int MENU_ID_SETTINGS = 9;
    public static final int MENU_ID_ABOUT = 10;
    public static final int MENU_ID_LOGOUT = 11;
    public static final int MENU_ID_DUMMY_MERCHANT_INFO = 12;
    public static final int MENU_ID_LAST_TRANSACTION = 13;
    public static final int MENU_ID_STATISTICS = 14;
    public static final int MENU_ID_HOME = 15;
    public static final int MENU_ID_LOYALTY_INQUIRY = 16;
    public static final int MENU_ID_REPORTS = 17;
    public static final int MENU_ID_MASTERPASS = 18;
    public static final int MENU_ID_ALIPAY = 19;
    public static final int MENU_ID_WECHAT = 20;
    public static final int MENU_ID_CRYPTO = 21;
    public static final int MENU_ID_CRYPTO_ATM = 22;
    public static final int MENU_ID_CRYPTO_ATM_BITCOIN = 23;
    public static final int MENU_ID_CRYPTO_ATM_LITECOIN = 24;
    public static final int MENU_ID_ACTIVATE_SOFTPOS = 25;

    public MerchantModule() {
    }

    @Nullable
    @Provides
    @MerchantScope
    VerifiedMerchantInfo provideCurrentMerchant(ServiceManager serviceManager) {
        return serviceManager.getCachedMerchant();
    }

    @Provides
    public List<MenuItem> provideMenuList(@Nullable VerifiedMerchantInfo currentMerchant) {
        ArrayList<MenuItem> list = new ArrayList<>();

        MenuItem sale = new MenuItem(MENU_ID_SALE, R.drawable.ic_sale, R.string.title_payment);
        MenuItem refund = new MenuItem(MENU_ID_REFUND, R.drawable.ic_refund, R.string.title_refund);
        MenuItem cashBack = new MenuItem(MENU_ID_CASH_BACK, R.drawable.ic_local_atm_black_24dp, R.string.title_cashback);
        MenuItem queryLoyalty = new MenuItem(MENU_ID_LOYALTY_INQUIRY, R.drawable.ic_loyalty, R.string.title_loyalty_inquiry);
        MenuItem balanceInquiry = new MenuItem(MENU_ID_BALANCE_INQUIRY, R.drawable.ic_info_outline_black_24dp, R.string.title_inquiry);
        MenuItem cashout = new MenuItem(MENU_ID_WITHDRAWAL, R.drawable.ic_cashout, R.string.title_withdrawal);
        MenuItem transferFunds = new MenuItem(MENU_ID_FUND_TRANSFER, R.drawable.ic_transfer_funds, R.string.title_transfer_funds);
        MenuItem billPayment = new MenuItem(MENU_ID_BILL_PAYMENT, R.drawable.ic_directions_subway_black_24dp, R.string.title_services);
        MenuItem alipay = new MenuItem(MENU_ID_ALIPAY, R.drawable.ic_alipay, R.string.title_alipay);
        MenuItem weChat = new MenuItem(MENU_ID_WECHAT, R.drawable.ic_wechat, R.string.title_wechat);
        MenuItem cryptoPayment = new MenuItem(MENU_ID_CRYPTO, R.drawable.ic_crypto_menu, R.string.title_crypto);
        MenuItem cryptoATM = new MenuItem(MENU_ID_CRYPTO_ATM, R.drawable.ic_crypto_atm, R.string.title_crypto_atm);
        MenuItem credible = new MenuItem(MENU_ID_DUMMY_MERCHANT_INFO, R.drawable.menu_credible, R.string.title_credible);

        int[] permissions = currentMerchant.getMenuControl();

        if (permissions != null && permissions.length > 0) {
            for (int i = 0; i < permissions.length; i++) {
                int permission = permissions[i];
                if (permission == 1) {
                    list.add(sale);
                } else if (permission == 2) {
                    list.add(cryptoATM);
                } else if (permission == 3) {
                    list.add(cashout);
                } else if (permission == 4) {
                    list.add(cryptoPayment);
                } else if (permission == 5) {
                    list.add(billPayment);
                } else if (permission == 6) {
                    list.add(credible);
                } else if (permission == 7) {
                    list.add(refund);
                } else if (permission == 8) {
                    list.add(alipay);
                }  else if (permission == 9) {
                    list.add(cashBack);
                } else if (permission == 10) {
                    list.add(weChat);
                } else if (permission == 11) {
                    list.add(balanceInquiry);
                }
            }
        }

        return list;
    }

    @Provides
    @DefaultFromAccount
    int providesDefaultFromAccount(@Nullable VerifiedMerchantInfo currentMerchant) {
        if (currentMerchant != null) {
            return UIHelper.getAccountPositionFromEnum(currentMerchant.getPlatformPermissions().getDefaultFromAccountType());
        } else {
            return SmartPesa.AccountType.DEFAULT.getEnumId();
        }
    }

    @Provides
    @DefaultToAccount
    int providesDefaultToAccount(@Nullable VerifiedMerchantInfo currentMerchant) {
        if (currentMerchant != null) {
            return UIHelper.getAccountPositionFromEnum(currentMerchant.getPlatformPermissions().getDefaultToAccountType());
        } else {
            return SmartPesa.AccountType.DEFAULT.getEnumId();
        }
    }

    @Provides
    public MoneyUtils provideMoneyHandler(@Nullable VerifiedMerchantInfo merchantInfo) {
        return new MoneyUtils(merchantInfo.getCurrency());
    }
}
