package com.smartpesa.smartpesa.fragment.payment;

import com.smartpesa.smartpesa.models.SmartPesaTransactionType;

import android.os.Bundle;

public class FundTransferPaymentFragment extends AbstractPaymentFragment {

    public static final String BUNDLE_KEY_AIIC = FundTransferPaymentFragment.class.getName() + ".aiic";

    String aiic;

    public static FundTransferPaymentFragment newInstance(SmartPesaTransactionType transactionType, int fromAccount, int toAccount, String aiic) {
        FundTransferPaymentFragment fragment = new FundTransferPaymentFragment();
        Bundle paymentBundle = new Bundle();
        paymentBundle.putInt(BUNDLE_KEY_TRANSACTION_TYPE, transactionType.getEnumId());
        paymentBundle.putInt(BUNDLE_KEY_FROM_ACCOUNT, fromAccount);
        paymentBundle.putInt(BUNDLE_KEY_TO_ACCOUNT, toAccount);
        paymentBundle.putString(BUNDLE_KEY_AIIC, aiic);
        fragment.setArguments(paymentBundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        aiic = getArguments().getString(BUNDLE_KEY_AIIC);
    }

    @Override
    protected void onBuildPaymentDescription(Bundle paymentBundle) {
        paymentBundle.putString("description", aiic);
    }
}
