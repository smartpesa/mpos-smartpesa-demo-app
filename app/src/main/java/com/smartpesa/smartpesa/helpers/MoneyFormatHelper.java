package com.smartpesa.smartpesa.helpers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import smartpesa.sdk.models.currency.Currency;

public class MoneyFormatHelper {

    private static final int DEFAULT_CURRENCY_DECIMALS = 2;
    private static MoneyFormatHelper sInstance;
    private final String mCurrencySymbol;
    private final int mCurrencyDecimals;
    private final DecimalFormat mDecimalFormat;

    private MoneyFormatHelper(String symbol, int currencyDecimals) {
        mCurrencySymbol = symbol;
        this.mCurrencyDecimals = currencyDecimals;

        StringBuilder sb = new StringBuilder(mCurrencySymbol + " 0");
        if (mCurrencyDecimals > 0) {
            sb.append(".");
            for (int i = 0; i < mCurrencyDecimals; i++) {
                sb.append("0");
            }
        }
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        //TODO improve this
        decimalFormatSymbols.setDecimalSeparator('.');
        decimalFormatSymbols.setGroupingSeparator(',');
        mDecimalFormat = new DecimalFormat(sb.toString(), decimalFormatSymbols);
        mDecimalFormat.setGroupingUsed(true);
    }

    @NonNull
    public static MoneyFormatHelper getInstance(@Nullable Currency currency) {
        int decimals = currency == null ? DEFAULT_CURRENCY_DECIMALS : currency.getCurrencyDecimals();
        String symbol = currency == null ? "" : currency.getCurrencySymbol() == null ? "" : currency.getCurrencySymbol();
        if (sInstance == null) {
            sInstance = new MoneyFormatHelper(symbol, decimals);
        } else {
            if (sInstance.mCurrencyDecimals != decimals || !symbol.equals(sInstance.mCurrencySymbol)) {
                sInstance = new MoneyFormatHelper(symbol, decimals);
            }
        }
        return sInstance;
    }

    public String format(BigDecimal amount) {
        return mDecimalFormat.format(amount.setScale(mDecimalFormat.getMinimumFractionDigits(), RoundingMode.HALF_EVEN));
    }
}