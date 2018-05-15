package com.smartpesa.smartpesa.flavour;

import com.smartpesa.smartpesa.activity.payment.AllPaymentsProgressActivity;
import com.smartpesa.smartpesa.fragment.payment.AbstractPaymentFragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import javax.inject.Inject;

public class PaymentHandler {

    @Inject
    public PaymentHandler() {

    }

    @Nullable
    public void onButtonClick(Bundle bundle, Activity context) {
        Intent paymentIntent = new Intent(context, AllPaymentsProgressActivity.class);
        paymentIntent.putExtras(bundle);
        context.startActivityForResult(paymentIntent, AbstractPaymentFragment.REQUEST_CODE);
    }

}
