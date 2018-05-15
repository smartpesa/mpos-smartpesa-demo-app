package com.smartpesa.smartpesa.flavour;

import com.smartpesa.smartpesa.activity.TipsAndTaxesActivity;
import com.smartpesa.smartpesa.models.SmartPesaTransactionType;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import javax.inject.Inject;

public class SmartPesaPaymentHandler extends PaymentHandler {

    @Inject
    public SmartPesaPaymentHandler() {
        super();
    }

    @Nullable
    public void onButtonClick(Bundle bundle, Activity context) {
        SmartPesaTransactionType transaction = SmartPesaTransactionType.fromEnumId(bundle.getInt("transactionType"));
        if (transaction.equals(SmartPesaTransactionType.SALE)) {
            Intent paymentIntent = new Intent(context, TipsAndTaxesActivity.class);
            paymentIntent.putExtras(bundle);
            context.startActivity(paymentIntent);
        }else{
            super.onButtonClick(bundle, context);
        }
    }
}
