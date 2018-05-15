package com.smartpesa.smartpesa.persistence;

import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.flavour.FlavourComponent;
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
    public static final int MENU_ID_LAST_TRANSACTION = 22;
    public static final int MENU_ID_STATISTICS = 212;
    public static final int MENU_ID_HOME = 202;
    public static final int MENU_ID_LOYALTY_INQUIRY = 999;
    public static final int MENU_ID_REPORTS = 21212;
    public static final int MENU_ID_MASTERPASS = 23;
    public static final int MENU_ID_ALIPAY = 24;
    public static final int MENU_ID_WECHAT = 1111;
    public static final int MENU_ID_CRYPTO = 1101;

    public MerchantModule() {
    }

    @Nullable
    @Provides
    @MerchantScope
    VerifiedMerchantInfo provideCurrentMerchant(ServiceManager serviceManager) {
        return serviceManager.getCachedMerchant();
    }

    @Provides
    @MainMenuItems
    @MerchantScope
    List<IDrawerItem> providesMainDrawerItem(@Nullable VerifiedMerchantInfo currentMerchant, Context context) {
        UIHelper font = new UIHelper(context);

        PrimaryDrawerItem home = new PrimaryDrawerItem().withName(R.string.title_home).withIcon(R.drawable.ic_payment_black_24dp).withTypeface(font.boldFont).withIdentifier(MENU_ID_HOME);

        PrimaryDrawerItem sale = new PrimaryDrawerItem().withName(R.string.title_payment).withIcon(R.drawable.ic_payment_black_24dp).withTypeface(font.boldFont).withIdentifier(MENU_ID_SALE);
        PrimaryDrawerItem refund = new PrimaryDrawerItem().withName(R.string.title_refund).withIcon(R.drawable.ic_attach_money_black_24dp).withTypeface(font.boldFont).withIdentifier(MENU_ID_REFUND);
        PrimaryDrawerItem cashBack = new PrimaryDrawerItem().withName(R.string.title_cashback).withIcon(R.drawable.ic_local_atm_black_24dp).withTypeface(font.boldFont).withIdentifier(MENU_ID_CASH_BACK);
        PrimaryDrawerItem queryLoyalty = new PrimaryDrawerItem().withName(R.string.title_loyalty_inquiry).withIcon(R.drawable.ic_info_outline_black_24dp).withTypeface(font.boldFont).withIdentifier(MENU_ID_LOYALTY_INQUIRY);
        PrimaryDrawerItem balanceInquiry = new PrimaryDrawerItem().withName(R.string.title_inquiry).withIcon(R.drawable.ic_info_outline_black_24dp).withTypeface(font.boldFont).withIdentifier(MENU_ID_BALANCE_INQUIRY);
        PrimaryDrawerItem withdrawal = new PrimaryDrawerItem().withName(R.string.title_withdrawal).withIcon(R.drawable.ic_attach_money_black_24dp).withTypeface(font.boldFont).withIdentifier(MENU_ID_WITHDRAWAL);
        PrimaryDrawerItem transferFunds = new PrimaryDrawerItem().withName(R.string.title_transfer_funds).withIcon(R.drawable.ic_transfer_funds).withTypeface(font.boldFont).withIdentifier(MENU_ID_FUND_TRANSFER);
        PrimaryDrawerItem billPayment = new PrimaryDrawerItem().withName(R.string.title_services).withIcon(R.drawable.ic_directions_subway_black_24dp).withTypeface(font.boldFont).withIdentifier(MENU_ID_BILL_PAYMENT);
        PrimaryDrawerItem reports = new PrimaryDrawerItem().withName(R.string.title_reports).withIcon(R.drawable.ic_attach_money_black_24dp).withTypeface(font.boldFont).withIdentifier(MENU_ID_REPORTS);
        PrimaryDrawerItem masterPassQR = new PrimaryDrawerItem().withName(R.string.title_masterpass).withIcon(R.drawable.ic_attach_money_black_24dp).withTypeface(font.boldFont).withIdentifier(MENU_ID_MASTERPASS);
        PrimaryDrawerItem alipay = new PrimaryDrawerItem().withName(R.string.title_alipay).withIcon(R.drawable.ic_alipay).withTypeface(font.boldFont).withIdentifier(MENU_ID_ALIPAY);
        PrimaryDrawerItem weChat = new PrimaryDrawerItem().withName(R.string.title_wechat).withIcon(R.drawable.ic_wechat).withTypeface(font.boldFont).withIdentifier(MENU_ID_WECHAT);
        PrimaryDrawerItem crypto = new PrimaryDrawerItem().withName(R.string.title_crypto).withIcon(R.drawable.ic_crypto_menu).withTypeface(font.boldFont).withIdentifier(MENU_ID_CRYPTO);

        ArrayList<IDrawerItem> drawerItemList = new ArrayList<>();

        int[] permissions = currentMerchant.getMenuControl();

        if (permissions != null && permissions.length > 0) {
            for (int i = 0; i < permissions.length; i++) {
                int permission = permissions[i];
                if (permission == 1) {
                    drawerItemList.add(sale);
                } else if (permission == 2) {
                    drawerItemList.add(queryLoyalty);
                } else if (permission == 3) {
                    drawerItemList.add(refund);
                } else if (permission == 4) {
                    drawerItemList.add(billPayment);
                } else if (permission == 5) {
                    drawerItemList.add(cashBack);
                } else if (permission == 6) {
                    drawerItemList.add(balanceInquiry);
                } else if (permission == 7) {
                    drawerItemList.add(withdrawal);
                } else if (permission == 8) {
                    drawerItemList.add(transferFunds);
                } else if (permission == 9) {
                    drawerItemList.add(masterPassQR);
                } else if (permission == 10) {
                    drawerItemList.add(alipay);
                } else if (permission == 11) {
                    drawerItemList.add(crypto);
                } else if (permission == 12) {
                    drawerItemList.add(weChat);
                }
            }
        }

        FlavourComponent flavourComponent = SmartPesaApplication.flavourComponent(context);
        if (flavourComponent != null) {
            drawerItemList.addAll(flavourComponent.provideFlavourSpecificDrawerItem());
        }

        return drawerItemList;
    }

    @Provides
    @AllMenuItems
    @MerchantScope
    List<IDrawerItem> providesDrawerItemForCurrentMerchant(@Nullable VerifiedMerchantInfo currentMerchant, Context context, @MainMenuItems List<IDrawerItem> menuItems) {
        UIHelper font = new UIHelper(context);

        // Initialize array list with max possible values to avoid array resizing.
        ArrayList<IDrawerItem> drawerItemList = new ArrayList<>(15);

        PrimaryDrawerItem history = new PrimaryDrawerItem().withName(R.string.title_history).withIcon(R.drawable.ic_history_black_24dp).withTypeface(font.boldFont).withIdentifier(MENU_ID_HISTORY);
        PrimaryDrawerItem lastTransaction = new PrimaryDrawerItem().withName(R.string.last_transaction).withIcon(R.drawable.ic_history_black_24dp).withTypeface(font.boldFont).withIdentifier(MENU_ID_LAST_TRANSACTION);
        PrimaryDrawerItem statistics = new PrimaryDrawerItem().withName(R.string.statistics).withIcon(R.drawable.ic_history_black_24dp).withTypeface(font.boldFont).withIdentifier(MENU_ID_STATISTICS);

        drawerItemList.add(history);

        drawerItemList.add(new DividerDrawerItem());

        drawerItemList.addAll(menuItems);

        //show operators fragment only if he has permissions to manage operators.
        PrimaryDrawerItem operators = new PrimaryDrawerItem().withName(R.string.title_operators).withIcon(R.drawable.ic_person_black_24dp).withTypeface(font.boldFont).withIdentifier(MENU_ID_OPERATORS);
        PrimaryDrawerItem settings = new PrimaryDrawerItem().withName(R.string.title_settings).withIcon(R.drawable.ic_settings_black_24dp).withTypeface(font.boldFont).withIdentifier(MENU_ID_SETTINGS);
        PrimaryDrawerItem about = new PrimaryDrawerItem().withName(R.string.title_about).withIcon(R.drawable.ic_chat_bubble_outline_black_24dp).withTypeface(font.boldFont).withIdentifier(MENU_ID_ABOUT);
        PrimaryDrawerItem logout = new PrimaryDrawerItem().withName(R.string.title_logout).withIcon(R.drawable.ic_exit_to_app_black_24dp).withTypeface(font.boldFont).withIdentifier(MENU_ID_LOGOUT);

        drawerItemList.add(new DividerDrawerItem());
        drawerItemList.add(operators);
        drawerItemList.add(settings);
        drawerItemList.add(new DividerDrawerItem());
        drawerItemList.add(about);
        drawerItemList.add(logout);

        return drawerItemList;
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
