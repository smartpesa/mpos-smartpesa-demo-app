package com.smartpesa.smartpesa.fragment.payment;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.models.SmartPesaTransactionType;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

public class CashWithdrawalPaymentFragment extends AbstractPaymentFragment {

    public static CashWithdrawalPaymentFragment newInstance(SmartPesaTransactionType transactionType, int fromAccount, int toAccount) {
        CashWithdrawalPaymentFragment fragment = new CashWithdrawalPaymentFragment();
        Bundle paymentBundle = new Bundle();
        paymentBundle.putInt(BUNDLE_KEY_TRANSACTION_TYPE, transactionType.getEnumId());
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
}
