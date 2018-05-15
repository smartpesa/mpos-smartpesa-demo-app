package com.smartpesa.smartpesa.fragment.history;

import com.smartpesa.smartpesa.models.ParcelableTransactionResponse;
import com.smartpesa.smartpesa.models.SmartPesaTransactionType;

import android.os.Bundle;
import android.view.View;

import java.util.HashMap;

import smartpesa.sdk.SmartPesa;

public class HistoryTransactionFragment extends AbstractTransactionFragment {

    private static final String DATA = HistoryTransactionFragment.class.getName() + ".data";
    private ParcelableTransactionResponse mTransactionResponse;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getArguments();
        if (b != null) {
            mTransactionResponse = b.getParcelable(DATA);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getTransactionTLVResponse().getTransaction() != null && getTransactionTLVResponse().getTransaction() == SmartPesaTransactionType.BALANCE_INQUIRY) {
            printBTN.setVisibility(View.GONE);
        }
    }

    @Override
    protected ParcelableTransactionResponse getTransactionTLVResponse() {
        return mTransactionResponse;
    }

    @Override
    protected void handleReceiptPrint() {
        fetchReceiptAndPrint(transactionID, SmartPesa.ReceiptFormat.REPRINT);
    }

    @Override
    protected void onBuildReceiptConfig(HashMap<String, Object> config) {
        //Do nothing
    }

    public static HistoryTransactionFragment newInstance(ParcelableTransactionResponse transactionResponse) {
        HistoryTransactionFragment fragment = new HistoryTransactionFragment();
        Bundle b = new Bundle();
        b.putParcelable(DATA, transactionResponse);
        fragment.setArguments(b);
        return fragment;
    }
}
