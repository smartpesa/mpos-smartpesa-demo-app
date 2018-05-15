package com.smartpesa.smartpesa.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.UUID;

import smartpesa.sdk.models.currency.Currency;

public class ParcelableCurrency implements Parcelable {

    private final UUID currencyId;
    private final String currencyName;
    private final String currencySymbol;
    private final int currencyIso;
    private final int currencyDecimals;
    private final Currency.Type currencyType;

    public ParcelableCurrency(Currency currency) {
        UUID nullPointer = UUID.fromString("00000000-0000-0000-0000-000000000000");

        if (currency.getCurrencyId() != null) {
            currencyId = currency.getCurrencyId();
        } else {
            currencyId = nullPointer;
        }

        currencyName = currency.getCurrencyName();
        currencySymbol = currency.getCurrencySymbol();
        currencyIso = currency.getCurrencyIso4217();
        currencyDecimals = currency.getCurrencyDecimals();
        currencyType = currency.getCurrencyType();
    }

    public ParcelableCurrency(Parcel in) {
        currencyId = UUID.fromString(in.readString());
        currencyName = in.readString();
        currencySymbol = in.readString();
        currencyIso = in.readInt();
        currencyDecimals = in.readInt();
        currencyType = Currency.Type.fromString(in.readString());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(currencyId.toString());
        dest.writeString(currencyName);
        dest.writeString(currencySymbol);
        dest.writeInt(currencyIso);
        dest.writeInt(currencyDecimals);
        dest.writeString(currencyType.toString());
    }

    public static final Creator CREATOR = new Creator() {
        public ParcelableCurrency createFromParcel(Parcel in) {
            return new ParcelableCurrency(in);
        }

        public ParcelableCurrency[] newArray(int size) {
            return new ParcelableCurrency[size];
        }
    };

    public UUID getCurrencyId() {
        return currencyId;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public int getCurrencyIso() {
        return currencyIso;
    }

    public int getCurrencyDecimals() {
        return currencyDecimals;
    }

    public Currency.Type getCurrencyType() {
        return currencyType;
    }
}