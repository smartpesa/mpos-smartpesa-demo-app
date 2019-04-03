package com.smartpesa.smartpesa.fragment.payment;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.activity.crypto.CryptoInfoActivity;
import com.smartpesa.smartpesa.fragment.dialog.CurrencyDialogFragment;
import com.smartpesa.smartpesa.helpers.UIHelper;
import com.smartpesa.smartpesa.models.SmartPesaTransactionType;
import com.smartpesa.smartpesa.persistence.MerchantModule;
import com.smartpesa.smartpesa.util.SmallCalculator;
import com.smartpesa.smartpesa.util.constants.SPConstants;

import java.math.BigDecimal;

import butterknife.BindView;
import smartpesa.sdk.models.currency.Currency;

public class CryptoAtmPaymentFragment extends AbstractPaymentFragment {

    private static final String CURRENCY_DIALOG_TAG = "currencyDialog";
    private static final String BUNDLE_KEY_CRYPTO_TYPE = "cryptoType";

    boolean cashBackSelected = false;
    @BindView(R.id.cashBackRL) RelativeLayout cashBackRL;
    @BindView(R.id.cashBackLabelTV) TextView cashBackLabelTV;
    @BindView(R.id.amountLabel_PAYMENT_TV) TextView amountLabelTV;

    @BindView(R.id.two_percent_btn) Button twoPercentBtn;
    @BindView(R.id.four_percent_btn) Button fourPercentBtn;
    @BindView(R.id.six_percent_btn) Button sixPercentBtn;
    @BindView(R.id.eight_percent_btn) Button eightPercentBtn;

    @BindView(R.id.calc_linear_layout) LinearLayout calculationLayout;
    @BindView(R.id.percent_linear_layout) LinearLayout percentLayout;

    private String mCashBackStringAmount;
    private SmallCalculator mCashBackCalculator;
    int cryptoType;

    public static CryptoAtmPaymentFragment newInstance(SmartPesaTransactionType transactionType, int crytoType, int fromAccount, int toAccount) {
        CryptoAtmPaymentFragment fragment = new CryptoAtmPaymentFragment();
        Bundle paymentBundle = new Bundle();
        paymentBundle.putInt(BUNDLE_KEY_TRANSACTION_TYPE, transactionType.getEnumId());
        paymentBundle.putInt(BUNDLE_KEY_FROM_ACCOUNT, fromAccount);
        paymentBundle.putInt(BUNDLE_KEY_TO_ACCOUNT, toAccount);
        paymentBundle.putInt(BUNDLE_KEY_CRYPTO_TYPE, crytoType);
        fragment.setArguments(paymentBundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cryptoType = getArguments().getInt(BUNDLE_KEY_CRYPTO_TYPE);
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

        Drawable drawable = getResources().getDrawable(R.drawable.button_green);
        Drawable drawable2 = getResources().getDrawable(R.drawable.button_green);
        currencyLabelTV.setBackground(drawable);
        cashBackCurrencyLabelTV.setBackground(drawable2);

        currencyLabelTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAvailableCurrencies();
            }
        });

        cashBackCurrencyLabelTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAvailableCurrencies();
            }
        });

    }

    private void getAvailableCurrencies() {
        CurrencyDialogFragment currencyDialogFragment = new CurrencyDialogFragment();
        currencyDialogFragment.show(getActivity().getSupportFragmentManager(), CURRENCY_DIALOG_TAG);

        currencyDialogFragment.setSelectedListener(new CurrencyDialogFragment.CurrencySelectedListener<Currency>() {
            @Override
            public void onSelected(Currency currency) {
                transactionCurrency = currency;
                currencyLabelTV.setText(currency.getCurrencySymbol());
                cashBackCurrencyLabelTV.setText(currency.getCurrencySymbol());
                clearCalculation();
            }
        });
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

    @Override
    public void processPayment() {

        String value = amountPaymentET.getText().toString();

        double cashBackAmount = getCashbackAmount();
        double amount = mMoneyUtils.parseBigDecimal(value).doubleValue();
        //check if amount is 0.00
        if (amount != 0.00) {
            //check for internet connectivity
            if (UIHelper.isOnline(mContext)) {

                Bundle paymentBundle = new Bundle();
                paymentBundle.putDouble(SPConstants.AMOUNT, amount);
                paymentBundle.putDouble(SPConstants.TIPS, cashBackAmount);
                paymentBundle.putInt(SPConstants.TRANSACTION_TYPE, transactionType.getEnumId());
                paymentBundle.putInt(SPConstants.FROM_ACCOUNT, mFromAccount);
                paymentBundle.putInt(SPConstants.TO_ACCOUNT, mToAccount);
                paymentBundle.putBoolean(SPConstants.IS_CRYPTO, true);
                paymentBundle.putSerializable(SPConstants.TRANSACTION_CURRENCY, transactionCurrency);
                paymentBundle.putInt(SPConstants.CRYPTO_ATM_CRYPTO_TYPE, cryptoType);

                Intent intent = new Intent(getActivity(), CryptoInfoActivity.class);
                intent.putExtra(SPConstants.BUNDLE, paymentBundle);

                startActivity(intent);

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

    @Override
    public void onResume() {
        super.onResume();
        android.support.v7.app.ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            if (cryptoType == MerchantModule.MENU_ID_CRYPTO_ATM_BITCOIN) {
                actionBar.setTitle(R.string.buy_bitcoin);
            } else if (cryptoType == MerchantModule.MENU_ID_CRYPTO_ATM_LITECOIN){
                actionBar.setTitle(R.string.buy_litecoin);
            }

        }
    }
}