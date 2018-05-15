package com.smartpesa.smartpesa.activity;

import com.smartpesa.smartpesa.activity.payment.AbstractPaymentProgressActivity;
import com.smartpesa.smartpesa.models.SmartPesaTransactionType;

import android.os.Bundle;
import android.text.TextUtils;

import java.util.HashMap;

import smartpesa.sdk.SmartPesa;
import smartpesa.sdk.models.transaction.Transaction;

public class SmartPesaPaymentProgressActivity extends AbstractPaymentProgressActivity {

    double tips, tax;
    String taxId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tips = this.getIntent().getDoubleExtra("tip", 0.00);
        tax = this.getIntent().getDoubleExtra("tax", 0.00);
        taxId = this.getIntent().getStringExtra("tax_id");
    }

    @Override
    public void buildConfig(HashMap<String, Object> config) {
        double taxDouble = mMoneyUtils.parseBigDecimal(tax).doubleValue();
        double tipDouble = mMoneyUtils.parseBigDecimal(tips).doubleValue();
        config.put("tax", taxDouble);
        config.put("tip", tipDouble);
        config.put("tax_type", taxId);
        if (!TextUtils.isEmpty(description)) {
            config.put("description", description);
        }
    }

    @Override
    public void doPostProcessing(Transaction resultTlvResponse, SmartPesa.Verification transactionVerification, SmartPesaTransactionType transaction) {

    }
}
