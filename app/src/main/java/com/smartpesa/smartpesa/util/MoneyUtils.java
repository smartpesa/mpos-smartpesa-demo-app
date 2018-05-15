package com.smartpesa.smartpesa.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;

import smartpesa.sdk.models.currency.Currency;
import timber.log.Timber;

public class MoneyUtils {

    private final DecimalFormat mDecimalFormat;
    private final Currency mCurrency;

    public MoneyUtils(Currency currency) {
        mCurrency = currency;
        StringBuilder sb = new StringBuilder("0");
        if (currency.getCurrencyDecimals() > 0) {
            sb.append(".");
            for (int i = 0; i < currency.getCurrencyDecimals(); i++) {
                sb.append("0");
            }
        }
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        //TODO improve this
        decimalFormatSymbols.setDecimalSeparator('.');
        decimalFormatSymbols.setGroupingSeparator(',');
        this.mDecimalFormat = new DecimalFormat(sb.toString(), decimalFormatSymbols);
    }

    @Nullable
    public String getZero() {
        return format(0.00);
    }

    @Nullable
    public String stripGroupingSeparator(@NonNull String money) {
        try {
            Number number = mDecimalFormat.parse(money);
            mDecimalFormat.setGroupingUsed(false);
            return mDecimalFormat.format(number);
        } catch (ParseException e) {
            e.printStackTrace();
            Timber.e(e, "Failed to strip grouping value: %s", money);
            return null;
        }
    }

    @Nullable
    public String format(double amount) {
        mDecimalFormat.setGroupingUsed(true);
        return mDecimalFormat.format(amount);
    }

    @Nullable
    public String format(BigDecimal bigDecimal) {
        mDecimalFormat.setGroupingUsed(true);
        return mDecimalFormat.format(bigDecimal.setScale(mDecimalFormat.getMinimumFractionDigits(), RoundingMode.HALF_EVEN));
    }

    @Nullable
    public String formatWithoutGroupingSeparator(double value) {
        mDecimalFormat.setGroupingUsed(false);
        return mDecimalFormat.format(value);
    }

    @Nullable
    public BigDecimal parseBigDecimal(double value) {
        return new BigDecimal(value).setScale(mDecimalFormat.getMinimumFractionDigits(), RoundingMode.HALF_EVEN);
    }

    @Nullable
    public String stringify(double value) {
        return formatWithoutGroupingSeparator(value).replace(String.valueOf(mDecimalFormat.getDecimalFormatSymbols().getDecimalSeparator()), "");
    }

    @Nullable
    public BigDecimal parseBigDecimal(@NonNull String valueString) {
        mDecimalFormat.setParseBigDecimal(true);
        mDecimalFormat.setGroupingUsed(true);
        try {
            return ((BigDecimal) mDecimalFormat.parse(valueString)).setScale(mDecimalFormat.getMinimumFractionDigits(), RoundingMode.HALF_EVEN);
        } catch (ParseException e) {
            e.printStackTrace();
            Timber.e(e, "Failed to parse BigDecimal value: %s", valueString);
            return null;
        }
    }

    @Nullable
    public String padding(@NonNull String value) {
        if (TextUtils.isEmpty(value)) {
            value = "0";
        }
        char separator = mDecimalFormat.getDecimalFormatSymbols().getDecimalSeparator();
        int precision = mCurrency.getCurrencyDecimals();
        if (precision > 0) {
            int length = value.length();
            if (length <= precision) {
                StringBuilder valueBuilder = new StringBuilder("0");
                valueBuilder.append(separator);
                valueBuilder.append(String.format("%0" + precision + "d", Integer.parseInt(value)));
                value = valueBuilder.toString();
            } else {
                String firstHalf = value.substring(0, value.length() - precision);
                String secondHalf = value.substring(value.length() - precision, value.length());
                value = firstHalf + separator + secondHalf;
            }
        }
        return value;

    }
}
