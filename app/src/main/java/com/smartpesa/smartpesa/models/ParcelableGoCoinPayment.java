package com.smartpesa.smartpesa.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import smartpesa.sdk.models.transaction.CryptoPayment;

public class ParcelableGoCoinPayment implements Parcelable {

    private final String result;
    private final UUID goCoinId;
    private final BigDecimal btcSpotRate;
    private final UUID crytoCurrencyId;
    private final String cryptoCurrencyName;
    private final BigDecimal cryptoPrice;
    private final int confirmationsRequired;
    private final String paymentAddress;
    private final String status;
    private final Date updatedAt;
    private final BigDecimal usdSpotRate;
    private final UUID paymentGoCoinId;

    public ParcelableGoCoinPayment(CryptoPayment goCoinPayment) {

        UUID nullPointer = UUID.fromString("00000000-0000-0000-0000-000000000000");

        result = goCoinPayment.getResult();

        if (goCoinPayment.getGoCoinId() != null) {
            goCoinId = goCoinPayment.getGoCoinId();
        } else {
            goCoinId = nullPointer;
        }

        btcSpotRate = goCoinPayment.getBtcSpotRate();

        if (goCoinPayment.getCurrency() != null && goCoinPayment.getCurrency().getCurrencyId() != null) {
            crytoCurrencyId = goCoinPayment.getCurrency().getCurrencyId();
        } else {
            crytoCurrencyId = nullPointer;
        }

        if (goCoinPayment.getCurrency() != null) {
            cryptoCurrencyName = goCoinPayment.getCurrency().getCurrencyName();
        } else {
            cryptoCurrencyName = "";
        }

        cryptoPrice = goCoinPayment.getAmount();
        confirmationsRequired = goCoinPayment.getConfirmationsRequired();
        paymentAddress = goCoinPayment.getPaymentAddress();
        status = goCoinPayment.getStatus();
        updatedAt = goCoinPayment.getUpdatedAt();
        usdSpotRate = goCoinPayment.getUsdSpotRate();

        if (goCoinPayment.getPaymentGoCoinId() != null) {
            paymentGoCoinId = goCoinPayment.getPaymentGoCoinId();
        } else {
            paymentGoCoinId = nullPointer;
        }
    }

    public ParcelableGoCoinPayment(Parcel in) {
        result = in.readString();
        goCoinId = UUID.fromString(in.readString());
        btcSpotRate = new BigDecimal(in.readDouble());
        crytoCurrencyId = UUID.fromString(in.readString());
        cryptoCurrencyName = in.readString();
        cryptoPrice = new BigDecimal(in.readDouble());
        confirmationsRequired = in.readInt();
        paymentAddress = in.readString();
        status = in.readString();
        updatedAt = new Date(in.readLong());
        usdSpotRate = new BigDecimal(in.readDouble());
        paymentGoCoinId = UUID.fromString(in.readString());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (result != null) {
            dest.writeString(result);
        }
        if (goCoinId != null) {
            dest.writeString(goCoinId.toString());
        }
        if (btcSpotRate != null) {
            dest.writeDouble(btcSpotRate.doubleValue());
        }
        if (crytoCurrencyId != null) {
            dest.writeString(crytoCurrencyId.toString());
        }
        dest.writeString(cryptoCurrencyName);
        if (cryptoPrice != null) {
            dest.writeDouble(cryptoPrice.doubleValue());
        }
        dest.writeInt(confirmationsRequired);
        dest.writeString(paymentAddress);
        dest.writeString(status);
        if (updatedAt != null) {
            dest.writeLong(updatedAt.getTime());
        }
        if (usdSpotRate != null) {
            dest.writeDouble(usdSpotRate.doubleValue());
        }
        if (paymentGoCoinId != null) {
            dest.writeString(paymentGoCoinId.toString());
        }
    }

    public static final Creator CREATOR = new Creator() {
        public ParcelableGoCoinPayment createFromParcel(Parcel in) {
            return new ParcelableGoCoinPayment(in);
        }

        public ParcelableGoCoinPayment[] newArray(int size) {
            return new ParcelableGoCoinPayment[size];
        }
    };

    public String getResult() {
        return result;
    }

    public UUID getGoCoinId() {
        return goCoinId;
    }

    public BigDecimal getBtcSpotRate() {
        return btcSpotRate;
    }

    public UUID getCrytoCurrencyId() {
        return crytoCurrencyId;
    }

    public String getCryptoCurrencyName() {
        return cryptoCurrencyName;
    }

    public BigDecimal getCryptoPrice() {
        return cryptoPrice;
    }

    public int getConfirmationsRequired() {
        return confirmationsRequired;
    }

    public String getPaymentAddress() {
        return paymentAddress;
    }

    public String getStatus() {
        return status;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public BigDecimal getUsdSpotRate() {
        return usdSpotRate;
    }

    public UUID getPaymentGoCoinId() {
        return paymentGoCoinId;
    }
}