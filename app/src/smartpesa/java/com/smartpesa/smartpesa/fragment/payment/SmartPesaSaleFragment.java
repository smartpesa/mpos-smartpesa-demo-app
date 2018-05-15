package com.smartpesa.smartpesa.fragment.payment;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.helpers.UIHelper;
import com.smartpesa.smartpesa.models.SmartPesaTransactionType;
import com.smartpesa.smartpesa.util.SmallCalculator;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.math.BigDecimal;

import butterknife.Bind;

public class SmartPesaSaleFragment extends AbstractPaymentFragment {

    boolean cashBackSelected = false;
    @Bind(R.id.cashBackRL) RelativeLayout cashBackRL;
    @Bind(R.id.cashBackLabelTV) TextView cashBackLabelTV;
    @Bind(R.id.amountLabel_PAYMENT_TV) TextView amountLabelTV;

    @Bind(R.id.two_percent_btn) Button twoPercentBtn;
    @Bind(R.id.four_percent_btn) Button fourPercentBtn;
    @Bind(R.id.six_percent_btn) Button sixPercentBtn;
    @Bind(R.id.eight_percent_btn) Button eightPercentBtn;

    @Bind(R.id.calc_linear_layout) LinearLayout calculationLayout;
    @Bind(R.id.percent_linear_layout) LinearLayout percentLayout;

    private String mCashBackStringAmount;
    private SmallCalculator mCashBackCalculator;

    public static SmartPesaSaleFragment newInstance(SmartPesaTransactionType transactionType, int fromAccount, int toAccount) {
        SmartPesaSaleFragment fragment = new SmartPesaSaleFragment();
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
        twoPercentBtn.setOnClickListener(this);
        fourPercentBtn.setOnClickListener(this);
        sixPercentBtn.setOnClickListener(this);
        eightPercentBtn.setOnClickListener(this);
        cashBackLabelTV.setText(R.string.tips_title);
        continueBTN.setText(R.string.next);
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
            case R.id.two_percent_btn:
                calculatePercent(new BigDecimal(2));
                break;
            case R.id.four_percent_btn:
                calculatePercent(new BigDecimal(4));
                break;
            case R.id.six_percent_btn:
                calculatePercent(new BigDecimal(6));
                break;
            case R.id.eight_percent_btn:
                calculatePercent(new BigDecimal(8));
                break;
            default:
                super.onClick(v);
        }
    }

    public void calculatePercent(BigDecimal percent) {
        //the amount from the display
        String amountDisplay = super.getCurrentDisplayValue();
        //convert to big decimal for calculation
        BigDecimal amt = mMoneyUtils.parseBigDecimal(amountDisplay);
        //check if that amount is zero
        if (!amt.equals(BigDecimal.ZERO)) {
            //perform percentage calculation
            amt = amt.multiply(percent);
            amt = amt.divide(new BigDecimal(100));
            //show amount in display
            showFormattedAmount(mMoneyUtils.format(amt));
        }
    }

    public void processPayment() {

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
                paymentBundle.putInt("transactionType", transactionType.getEnumId());
                paymentBundle.putInt("fromAccount", mFromAccount);
                paymentBundle.putInt("toAccount", mToAccount);
                onBuildPaymentDescription(paymentBundle);

                if (mPaymentHandler != null) {
                    mPaymentHandler.onButtonClick(paymentBundle, getActivity());
                }

            } else {
                UIHelper.showErrorDialog(mContext, getResources().getString(R.string.app_name), getResources().getString(R.string.internet_not_connected));
            }
        } else {
            UIHelper.showToast(mContext, mContext.getResources().getString(R.string.please_enter_amount));
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
        calculationLayout.setVisibility(View.GONE);
        percentLayout.setVisibility(View.VISIBLE);
        cashBackSelected = true;
    }

    private void switchToAmount() {
        cashBackSelectedRL.setVisibility(View.INVISIBLE);
        amountSelectedRL.setVisibility(View.VISIBLE);
        calculationLayout.setVisibility(View.VISIBLE);
        percentLayout.setVisibility(View.GONE);
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