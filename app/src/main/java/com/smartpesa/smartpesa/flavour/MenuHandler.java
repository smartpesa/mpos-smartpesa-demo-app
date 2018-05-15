package com.smartpesa.smartpesa.flavour;

import com.smartpesa.smartpesa.fragment.AboutFragment;
import com.smartpesa.smartpesa.fragment.HomeFragment;
import com.smartpesa.smartpesa.fragment.InquiryFragment;
import com.smartpesa.smartpesa.fragment.LoyaltyInquiryFragment;
import com.smartpesa.smartpesa.fragment.MerchantInfoFragment;
import com.smartpesa.smartpesa.fragment.OperatorsFragment;
import com.smartpesa.smartpesa.fragment.ServicesFragment;
import com.smartpesa.smartpesa.fragment.SettingsFragment;
import com.smartpesa.smartpesa.fragment.TransferFundsFragment;
import com.smartpesa.smartpesa.fragment.history.LastTransactionFragment;
import com.smartpesa.smartpesa.fragment.history.PastHistoryFragment;
import com.smartpesa.smartpesa.fragment.history.StatisticsFragment;
import com.smartpesa.smartpesa.fragment.payment.AliPayPaymentFragment;
import com.smartpesa.smartpesa.fragment.payment.CashBackPaymentFragment;
import com.smartpesa.smartpesa.fragment.payment.CashWithdrawalPaymentFragment;
import com.smartpesa.smartpesa.fragment.payment.GoCoinPaymentFragment;
import com.smartpesa.smartpesa.fragment.payment.MasterCardPaymentFragment;
import com.smartpesa.smartpesa.fragment.payment.RefundPaymentFragment;
import com.smartpesa.smartpesa.fragment.payment.SalePaymentFragment;
import com.smartpesa.smartpesa.models.SmartPesaTransactionType;
import com.smartpesa.smartpesa.persistence.DefaultFromAccount;
import com.smartpesa.smartpesa.persistence.DefaultToAccount;
import com.smartpesa.smartpesa.persistence.MerchantModule;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import javax.inject.Inject;

import timber.log.Timber;

public class MenuHandler {

    protected final int mDefFromAccount;
    protected final int mDefToAccount;

    @Inject
    public MenuHandler(@DefaultFromAccount int defFromAccount, @DefaultToAccount int defToAccount) {
        mDefFromAccount = defFromAccount;
        mDefToAccount = defToAccount;
    }

    @Nullable
    public Fragment fragmentForMenuWithId(int identifier) {
        Fragment fragment = null;
        switch (identifier) {
            case MerchantModule.MENU_ID_SALE:
                Timber.i("User clicks on Sale");
                fragment = SalePaymentFragment.newInstance(SmartPesaTransactionType.SALE, mDefFromAccount, mDefToAccount);
                break;
            case MerchantModule.MENU_ID_HISTORY:
                Timber.i("User clicks on History");
                fragment = new PastHistoryFragment();
                break;
            case MerchantModule.MENU_ID_REFUND:
                Timber.i("User clicks on Refund");
                fragment = RefundPaymentFragment.newInstance(SmartPesaTransactionType.REFUND, mDefFromAccount, mDefToAccount);
                break;
            case MerchantModule.MENU_ID_CASH_BACK:
                Timber.i("User clicks on CashBack");
                fragment = CashBackPaymentFragment.newInstance(SmartPesaTransactionType.CASH_BACK, mDefFromAccount, mDefToAccount);
                break;
            case MerchantModule.MENU_ID_BALANCE_INQUIRY:
                Timber.i("User clicks on Balance Inquiry");
                fragment = new InquiryFragment();
                break;
            case MerchantModule.MENU_ID_WITHDRAWAL:
                Timber.i("User clicks on Cash Withdrawal");
                fragment = CashWithdrawalPaymentFragment.newInstance(SmartPesaTransactionType.WITHDRAWAL, mDefFromAccount, mDefToAccount);
                break;
            case MerchantModule.MENU_ID_FUND_TRANSFER:
                Timber.i("User clicks on Fund Transfer");
                fragment = new TransferFundsFragment();
                break;
            case MerchantModule.MENU_ID_BILL_PAYMENT:
                Timber.i("User clicks on Bill Payment");
                fragment = new ServicesFragment();
                break;
            case MerchantModule.MENU_ID_OPERATORS:
                Timber.i("User clicks on Operators Module");
                fragment = new OperatorsFragment();
                break;
            case MerchantModule.MENU_ID_HOME:
                Timber.i("User clicks on Home Module");
                fragment = new HomeFragment();
                break;
            case MerchantModule.MENU_ID_SETTINGS:
                Timber.i("User clicks on Settings Module");
                fragment = new SettingsFragment();
                break;
            case MerchantModule.MENU_ID_ABOUT:
                Timber.i("User clicks on About Module");
                fragment = AboutFragment.newInstance(false);
                break;
            case MerchantModule.MENU_ID_LAST_TRANSACTION:
                Timber.i("User clicks on Last Transaction Module");
                fragment = new LastTransactionFragment();
                break;
            case MerchantModule.MENU_ID_STATISTICS:
                Timber.i("User clicks on Statistics Module");
                fragment = new StatisticsFragment();
                break;
            case MerchantModule.MENU_ID_DUMMY_MERCHANT_INFO:
                Timber.i("User clicks on Merchant info Module");
                fragment = new MerchantInfoFragment();
                break;
            case MerchantModule.MENU_ID_LOYALTY_INQUIRY:
                Timber.i("User clicks on Loyalty Query Module");
                fragment = new LoyaltyInquiryFragment();
                break;
            case MerchantModule.MENU_ID_MASTERPASS:
                Timber.i("User clicks on Masterpass QR");
                fragment = MasterCardPaymentFragment.newInstance(SmartPesaTransactionType.SALE, mDefFromAccount, mDefToAccount);
                break;
            case MerchantModule.MENU_ID_ALIPAY:
                Timber.i("User clicks on AliPay");
                fragment = AliPayPaymentFragment.newInstance(SmartPesaTransactionType.PAYMENT, mDefFromAccount, mDefToAccount);
                break;
            case MerchantModule.MENU_ID_CRYPTO:
                Timber.i("User clicks on Crypto");
                fragment = GoCoinPaymentFragment.newInstance(SmartPesaTransactionType.PAYMENT, mDefFromAccount, mDefToAccount);
                break;
            default:
                Timber.e("Invalid menu item with identifier: %d", identifier);
                break;
        }
        return fragment;
    }

}
