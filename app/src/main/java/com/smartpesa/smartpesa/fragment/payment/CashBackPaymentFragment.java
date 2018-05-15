package com.smartpesa.smartpesa.fragment.payment;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.models.SmartPesaTransactionType;
import com.smartpesa.smartpesa.util.SmallCalculator;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.RelativeLayout;

import butterknife.Bind;

public class CashBackPaymentFragment extends AbstractPaymentFragment {

    boolean cashBackSelected = false;
    @Bind(R.id.cashBackRL) RelativeLayout cashBackRL;
    private String mCashBackStringAmount;
    private SmallCalculator mCashBackCalculator;

    public static CashBackPaymentFragment newInstance(SmartPesaTransactionType transactionType, int fromAccount, int toAccount) {
        CashBackPaymentFragment fragment = new CashBackPaymentFragment();
        Bundle paymentBundle = new Bundle();
        paymentBundle.putInt(BUNDLE_KEY_TRANSACTION_TYPE, transactionType.getEnumId());
        paymentBundle.putInt(BUNDLE_KEY_FROM_ACCOUNT, fromAccount);
        paymentBundle.putInt(BUNDLE_KEY_TO_ACCOUNT, toAccount);
        fragment.setArguments(paymentBundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCashBackCalculator = new SmallCalculator();
        mCashBackStringAmount = "";
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_payment, menu);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cashBackSelectedRL.setVisibility(View.INVISIBLE);
        amountSelectedRL.setVisibility(View.VISIBLE);
        amountRL.setOnClickListener(this);
        cashBackRL.setOnClickListener(this);
        cashBackRL.setVisibility(View.VISIBLE);
        amountPaymentET.setOnClickListener(this);
        cashBackAmountET.setOnClickListener(this);
    }

    @Override
    protected void backSpace() {
        if (cashBackSelected) {
            if (mCashBackStringAmount.length() != 0) {
                mCashBackStringAmount = doBackspace(mCashBackStringAmount);
                displayCurrentStringAmount();
            }
        } else {
            super.backSpace();
        }
    }

    @Override
    protected void appendDigit(String digit) {
        if (cashBackSelected) {
            if (mCashBackStringAmount.length() < MAX_DIGIT_LENGTH) {
                mCashBackStringAmount = doAppend(mCashBackStringAmount, digit);
                displayCurrentStringAmount();
            }
        } else {
            super.appendDigit(digit);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.amount_PAYMENT_RL:
                switchToAmount();
                break;
            case R.id.cashBackRL:
                switchToCashBack();
                break;
            case R.id.cashBack_ET:
                switchToCashBack();
                break;
            case R.id.amount_PAYMENT_ET:
                switchToAmount();
                break;
            default:
                super.onClick(v);
        }
    }

    @Override
    protected double getCashbackAmount() {
        String cashBackValue = cashBackAmountET.getText().toString();
        cashBackValue = mMoneyUtils.stripGroupingSeparator(cashBackValue);
        return Double.valueOf(cashBackValue);
    }

    private void switchToCashBack() {
        cashBackSelectedRL.setVisibility(View.VISIBLE);
        amountSelectedRL.setVisibility(View.INVISIBLE);
        cashBackSelected = true;
    }

    private void switchToAmount() {
        cashBackSelectedRL.setVisibility(View.INVISIBLE);
        amountSelectedRL.setVisibility(View.VISIBLE);
        cashBackSelected = false;
    }

    @Override
    protected void showFormattedAmount(String amount) {
        if (cashBackSelected) {
            cashBackAmountET.setText(amount);
        } else {
            super.showFormattedAmount(amount);
        }
    }

    @Override
    public SmallCalculator getCalculator() {
        return cashBackSelected ? mCashBackCalculator : super.getCalculator();
    }

    @Override
    protected void resetAmount() {
        if (cashBackSelected) {
            mCashBackStringAmount = "";
        } else {
            super.resetAmount();
        }
    }

    @NonNull
    @Override
    protected String getCurrentDisplayValue() {
        return cashBackSelected ? cashBackAmountET.getText().toString() : super.getCurrentDisplayValue();
    }

    @Override
    protected void displayCurrentStringAmount() {
        if (cashBackSelected) {
            showFormattedAmount(formatCurrent(mCashBackStringAmount));
        } else {
            super.displayCurrentStringAmount();
        }
    }
}
