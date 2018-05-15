package com.smartpesa.smartpesa.fragment.payment;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.activity.MainActivity;
import com.smartpesa.smartpesa.fragment.MasterPassFragment;
import com.smartpesa.smartpesa.helpers.UIHelper;
import com.smartpesa.smartpesa.models.SmartPesaTransactionType;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;

public class MasterCardPaymentFragment extends AbstractPaymentFragment {

    public static MasterCardPaymentFragment newInstance(SmartPesaTransactionType transaction, int fromAccount, int toAccount) {
        MasterCardPaymentFragment fragment = new MasterCardPaymentFragment();
        Bundle paymentBundle = new Bundle();
        paymentBundle.putInt(BUNDLE_KEY_TRANSACTION_TYPE, transaction.getEnumId());
        paymentBundle.putInt(BUNDLE_KEY_FROM_ACCOUNT, fromAccount);
        paymentBundle.putInt(BUNDLE_KEY_TO_ACCOUNT, toAccount);
        fragment.setArguments(paymentBundle);
        return fragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_payment, menu);
    }

    @Override
    public void processPayment() {

        String value = amountPaymentET.getText().toString();
        String currency = mVerifyMerchantInfo.getCurrency().getCurrencySymbol();
        double amount = mMoneyUtils.parseBigDecimal(value).doubleValue();
        //check if amount is 0.00
        if (amount != 0.00) {
            //check for internet connectivity
            if (UIHelper.isOnline(mContext)) {
                Fragment fragment = MasterPassFragment.newInstance(value, currency);
                MainActivity.getComponent((MainActivity) getActivity())
                        .provideNavigator()
                        .pushFragment(fragment);
            } else {
                UIHelper.showErrorDialog(mContext, getResources().getString(R.string.app_name), getResources().getString(R.string.internet_not_connected));
            }
        } else {
            UIHelper.showToast(mContext, mContext.getResources().getString(R.string.please_enter_amount));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        android.support.v7.app.ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_masterpass);
        }
    }

}
