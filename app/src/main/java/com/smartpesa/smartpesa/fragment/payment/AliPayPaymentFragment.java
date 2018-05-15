package com.smartpesa.smartpesa.fragment.payment;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.activity.AliPayQRScanActivity;
import com.smartpesa.smartpesa.activity.payment.AliPayPaymentProgressActivity;
import com.smartpesa.smartpesa.helpers.UIHelper;
import com.smartpesa.smartpesa.models.SmartPesaTransactionType;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class AliPayPaymentFragment extends AbstractPaymentFragment {

    private static final int ALIPAY_SCAN = 0;
    private static final int ALIPAY_DISPLAY = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public static AliPayPaymentFragment newInstance(SmartPesaTransactionType transactionType, int fromAccount, int toAccount) {
        AliPayPaymentFragment fragment = new AliPayPaymentFragment();
        Bundle paymentBundle = new Bundle();
        paymentBundle.putInt(BUNDLE_KEY_TRANSACTION_TYPE, transactionType.getEnumId());
        paymentBundle.putInt(BUNDLE_KEY_FROM_ACCOUNT, fromAccount);
        paymentBundle.putInt(BUNDLE_KEY_TO_ACCOUNT, toAccount);
        fragment.setArguments(paymentBundle);
        return fragment;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        continueBTN.setVisibility(View.INVISIBLE);
        alipayButtonsLl.setVisibility(View.VISIBLE);

        alipayPayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aliPayPayment(ALIPAY_DISPLAY);
            }
        });

        alipayScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aliPayPayment(ALIPAY_SCAN);
            }
        });
    }

    private void aliPayPayment(int alipayType) {
        String value = amountPaymentET.getText().toString();

        double cashBackAmount = getCashbackAmount();
        double amount = mMoneyUtils.parseBigDecimal(value).doubleValue();
        //check if amount is 0.00
        if (amount != 0.00) {
            //check for internet connectivity
            if (UIHelper.isOnline(mContext)) {

                Bundle paymentBundle = new Bundle();
                paymentBundle.putDouble("amount", amount);
                paymentBundle.putDouble("cashBackAmount", cashBackAmount);
                paymentBundle.putInt("transactionType", SmartPesaTransactionType.SALE.getEnumId());
                paymentBundle.putInt("fromAccount", mFromAccount);
                paymentBundle.putInt("toAccount", mToAccount);

                onBuildPaymentDescription(paymentBundle);

                if (alipayType == ALIPAY_SCAN) {

                    Intent intent = new Intent(getActivity(), AliPayQRScanActivity.class);
                    intent.putExtra("bundle", paymentBundle);
                    startActivity(intent);

                } else if (alipayType == ALIPAY_DISPLAY) {

                    paymentBundle.putBoolean("isScan", false);
                    Intent paymentIntent = new Intent(getActivity(), AliPayPaymentProgressActivity.class);
                    paymentIntent.putExtras(paymentBundle);
                    startActivity(paymentIntent);

                }

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
            actionBar.setTitle(R.string.title_alipay);
        }
    }
}
