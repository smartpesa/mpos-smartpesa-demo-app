package com.smartpesa.smartpesa.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.math.BigDecimal;

import smartpesa.sdk.models.transaction.AlipayPayment;

public class ParcelableAliPayPayment implements Parcelable {

    private final String action;
    private final String resultCode;
    private final String tradeNo;
    private final String buyerId;
    private final String notifyId;
    private final BigDecimal forexRate;
    private final String buyerIdentityCode;
    private final String sellerId;
    private final BigDecimal transactionAmountInYuan;

    public ParcelableAliPayPayment(AlipayPayment alipayPayment) {

        action = alipayPayment.getAction();
        resultCode = alipayPayment.getResultCode();
        tradeNo = alipayPayment.getTradeNo();
        buyerId = alipayPayment.getBuyerId();
        notifyId = alipayPayment.getNotifyId();
        forexRate = alipayPayment.getForexRate();
        buyerIdentityCode = alipayPayment.getBuyerIdentityCode();
        sellerId = alipayPayment.getSellerId();
        transactionAmountInYuan = alipayPayment.getTransAmountInChineseYuan();
    }

    public ParcelableAliPayPayment(Parcel in) {
        action = in.readString();
        resultCode = in.readString();
        tradeNo = in.readString();
        buyerId = in.readString();
        notifyId = in.readString();
        forexRate = new BigDecimal(in.readDouble());
        buyerIdentityCode = in.readString();
        sellerId = in.readString();
        transactionAmountInYuan = new BigDecimal(in.readDouble());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(action);
        dest.writeString(resultCode);
        dest.writeString(tradeNo);
        dest.writeString(buyerId);
        dest.writeString(notifyId);
        if (forexRate != null) {
            dest.writeDouble(forexRate.doubleValue());
        }
        dest.writeString(buyerIdentityCode);
        dest.writeString(sellerId);
        if (transactionAmountInYuan != null) {
            dest.writeDouble(transactionAmountInYuan.doubleValue());
        }
    }

    public static final Creator CREATOR = new Creator() {
        public ParcelableAliPayPayment createFromParcel(Parcel in) {
            return new ParcelableAliPayPayment(in);
        }

        public ParcelableAliPayPayment[] newArray(int size) {
            return new ParcelableAliPayPayment[size];
        }
    };

    public String getAction() {
        return action;
    }

    public String getResultCode() {
        return resultCode;
    }

    public String getTradeNo() {
        return tradeNo;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public String getNotifyId() {
        return notifyId;
    }

    public BigDecimal getForexRate() {
        return forexRate;
    }

    public String getBuyerIdentityCode() {
        return buyerIdentityCode;
    }

    public String getSellerId() {
        return sellerId;
    }

    public BigDecimal getTransactionAmountInYuan() {
        return transactionAmountInYuan;
    }
}