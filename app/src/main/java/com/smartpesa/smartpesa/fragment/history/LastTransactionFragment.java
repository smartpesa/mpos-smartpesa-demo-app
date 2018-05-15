package com.smartpesa.smartpesa.fragment.history;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.models.ParcelableTransactionResponse;
import com.smartpesa.smartpesa.models.SmartPesaTransactionType;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.ArrayAdapter;

import java.util.HashMap;
import java.util.List;

import smartpesa.sdk.SmartPesa;
import smartpesa.sdk.models.transaction.Balance;
import smartpesa.sdk.models.transaction.Transaction;
import smartpesa.sdk.models.transaction.TransactionResult;

public class LastTransactionFragment extends AbstractTransactionFragment {

    @Override
    protected ParcelableTransactionResponse getTransactionTLVResponse() {
        Transaction resultTlvResponse = serviceManager.get().getLastTransactionDetails();
        if (resultTlvResponse != null) {
            TransactionResult transactionResponse = resultTlvResponse.getTransactionResult();
            return transactionResponse != null ? new ParcelableTransactionResponse(transactionResponse) : null;
        } else {
            return null;
        }
    }

    @Override
    protected void handleReceiptPrint() {
        final SmartPesa.ReceiptFormat[] receiptFormats = {
                SmartPesa.ReceiptFormat.CUSTOMER,
                SmartPesa.ReceiptFormat.MERCHANT,
                SmartPesa.ReceiptFormat.BANK};
        new AlertDialog.Builder(getActivity())
                .setTitle(mContext.getString(R.string.select_receipt_format))
                .setAdapter(
                        new ArrayAdapter<SmartPesa.ReceiptFormat>(
                                getActivity(),
                                android.R.layout.simple_list_item_1,
                                receiptFormats
                        ),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SmartPesa.ReceiptFormat receiptFormat = receiptFormats[which];
                                fetchReceiptAndPrint(transactionID, receiptFormat);
                            }
                        })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    @Override
    protected void onBuildReceiptConfig(HashMap<String, Object> config) {
        Transaction resultTlvResponse = serviceManager.get().getLastTransactionDetails();
        if (resultTlvResponse != null
                && resultTlvResponse.getExtraData() != null
                && getTransactionTLVResponse().getTransaction() != SmartPesaTransactionType.CASH_DEPOSIT
                && resultTlvResponse.getBalances() != null
                && resultTlvResponse.getBalances().size() > 0) {
            List<Balance> balances = resultTlvResponse.getBalances();
            for (int i = 0; i < balances.size(); i++) {
                Balance balance = balances.get(i);
                if (balance.getAmountType() == SmartPesa.AmountType.AVAILABLE_BALANCE) {
                    config.put("available_balance", balance.getAmount());
                } else if (balance.getAmountType() == SmartPesa.AmountType.LEDGER_BALANCE) {
                    config.put("ledger_balance", balance.getAmount());
                }
            }
        }
    }
}
