package com.smartpesa.smartpesa.activity.payment;

import com.smartpesa.smartpesa.models.SmartPesaTransactionType;

import smartpesa.sdk.SmartPesa;
import smartpesa.sdk.models.transaction.Transaction;

public class AllPaymentsProgressActivity extends AbstractPaymentProgressActivity {

    @Override
    public void doPostProcessing(Transaction resultTlvResponse, SmartPesa.Verification transactionVerification, SmartPesaTransactionType transaction) {

    }
}
