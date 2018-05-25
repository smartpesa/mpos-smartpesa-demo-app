package com.smartpesa.smartpesa.fragment.payment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.activity.AliPayQRScanActivity;
import com.smartpesa.smartpesa.helpers.UIHelper;
import com.smartpesa.smartpesa.models.SmartPesaTransactionType;
import com.smartpesa.smartpesa.util.SmallCalculator;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.HashMap;

import butterknife.Bind;
import smartpesa.sdk.core.error.SpException;
import smartpesa.sdk.models.ExtraDataContainer;
import smartpesa.sdk.models.provider.GetProviderExtraDataCallback;

public class CryptoAtmPaymentFragment extends AbstractPaymentFragment {

    public static final String ACTION = "action";
    public static final String RATE = "rate";
    public static final String RATE1 = "rate";
    public static final String BTCUSD = "btcusd";
    public static final String BIT_STAMP = "BitStamp";
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

    public static CryptoAtmPaymentFragment newInstance(SmartPesaTransactionType transactionType, int fromAccount, int toAccount) {
        CryptoAtmPaymentFragment fragment = new CryptoAtmPaymentFragment();
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
        cashBackLabelTV.setText(R.string.margin);
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

        final double cashBackAmount = getCashbackAmount();
        final double amount = mMoneyUtils.parseBigDecimal(value).doubleValue();
        //check if amount is 0.00
        if (amount != 0.00) {
            //check for internet connectivity
            if (UIHelper.isOnline(mContext)) {

                final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setMessage("Processing transaction...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                HashMap<String, Object> hashMap = new HashMap<>();

                hashMap.put(ACTION, RATE);
                hashMap.put(RATE1, BTCUSD);

                serviceManager.get().getProviderExtraData(BIT_STAMP, hashMap, new GetProviderExtraDataCallback() {
                    @Override
                    public void onSuccess(ExtraDataContainer extraDataContainer) {
                        if (null == getActivity()) return;

                        progressDialog.dismiss();
                        UIHelper.log(extraDataContainer.raw);

                        processBTCData(extraDataContainer, amount, cashBackAmount);
                    }

                    @Override
                    public void onError(SpException e) {
                        if (null == getActivity()) return;

                        progressDialog.dismiss();
                        UIHelper.showMessageDialog(getActivity(), e.getMessage());
                    }
                });

            } else {
                UIHelper.showErrorDialog(mContext, getResources().getString(R.string.app_name), getResources().getString(R.string.internet_not_connected));
            }
        } else {
            UIHelper.showToast(mContext, mContext.getResources().getString(R.string.please_enter_amount));
        }
    }

    private void processBTCData(ExtraDataContainer extraDataContainer, final double amount, final double cashBackAmount) {
        if (extraDataContainer != null) {

            String rawData = extraDataContainer.raw;

            if (!TextUtils.isEmpty(rawData)) {

                try {
                    JSONObject jsonObject = new JSONObject(rawData);

                    if (jsonObject != null) {

                        if (jsonObject.has("ask")) {

                            String ask = jsonObject.getString("ask");

                            if (!TextUtils.isEmpty(ask)) {

                                BigDecimal askAmount = mMoneyUtils.parseBigDecimal(ask);

                                double totalAmount = amount + cashBackAmount;

                                double btc =  totalAmount / askAmount.doubleValue();

                                UIHelper.showMessageDialogWithTitleTwoButtonCallback(getActivity(),
                                        getString(R.string.app_name),
                                        "Bitcoin price $" + ask +
                                                "\n" + "You will buy " + String.format("%.8f", btc) + " BTC",
                                        "Continue",
                                        "Cancel", new MaterialDialog.ButtonCallback() {
                                            @Override
                                            public void onPositive(MaterialDialog dialog) {
                                                super.onPositive(dialog);
                                                startScanActivity(cashBackAmount, amount);
                                            }

                                            @Override
                                            public void onNegative(MaterialDialog dialog) {
                                                super.onNegative(dialog);
                                            }
                                        });
                            } else {
                                showBTCFailed();
                            }

                        } else {
                            showBTCFailed();
                        }

                    } else {
                        showBTCFailed();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    showBTCFailed();
                }

            } else {
                showBTCFailed();
            }

        } else {
            showBTCFailed();
        }
    }

    private void showBTCFailed() {
        UIHelper.showErrorDialog(getActivity(),
                getString(R.string.app_name),
                "Unable to get Crypto price, please try again later or contact support");
    }

    private void startScanActivity(double cashBackAmount, double amount) {
        Bundle paymentBundle = new Bundle();
        paymentBundle.putDouble("amount", amount);
        paymentBundle.putDouble("cashBackAmount", cashBackAmount);
        paymentBundle.putInt("transactionType", transactionType.getEnumId());
        paymentBundle.putInt("fromAccount", mFromAccount);
        paymentBundle.putInt("toAccount", mToAccount);
        paymentBundle.putBoolean("crypto", true);

        Intent intent = new Intent(getActivity(), AliPayQRScanActivity.class);
        intent.putExtra("bundle", paymentBundle);
        startActivity(intent);
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