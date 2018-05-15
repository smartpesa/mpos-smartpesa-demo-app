package com.smartpesa.smartpesa.fragment.payment;

import com.smartpesa.smartpesa.models.SmartPesaTransactionType;

import android.os.Bundle;

public class BillPaymentFragment extends AbstractPaymentFragment {

    private static final String BUNDLE_KEY_BILL_DETAILS = BillPaymentFragment.class.getName() + ".billPaymentDetails";

    String billPaymentDetails;

    public static BillPaymentFragment newInstance(SmartPesaTransactionType transactionType, int fromAccount, int toAccount, String billPaymentDetails) {
        BillPaymentFragment fragment = new BillPaymentFragment();
        Bundle paymentBundle = new Bundle();
        paymentBundle.putInt(BUNDLE_KEY_TRANSACTION_TYPE, transactionType.getEnumId());
        paymentBundle.putInt(BUNDLE_KEY_FROM_ACCOUNT, fromAccount);
        paymentBundle.putInt(BUNDLE_KEY_TO_ACCOUNT, toAccount);
        paymentBundle.putString(BUNDLE_KEY_BILL_DETAILS, billPaymentDetails);
        fragment.setArguments(paymentBundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
        billPaymentDetails = getArguments().getString(BUNDLE_KEY_BILL_DETAILS);
    }

    @Override
    protected void onBuildPaymentDescription(Bundle paymentBundle) {
        paymentBundle.putString("description", billPaymentDetails);
    }
}
