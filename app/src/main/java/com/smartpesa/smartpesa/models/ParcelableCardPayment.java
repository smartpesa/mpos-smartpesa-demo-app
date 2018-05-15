package com.smartpesa.smartpesa.models;

import android.os.Parcel;
import android.os.Parcelable;

import smartpesa.sdk.SmartPesa;
import smartpesa.sdk.models.transaction.CardPayment;

public class ParcelableCardPayment implements Parcelable {

    private final String terminalId;
    private final int method;
    private final String cardNumber;
    private final String cardType;
    private final String cardExpiry;
    private final String cardHolderName;
    private final String cardName;
    private final int cardIcon;
    private final String signatureId;
    private final String cvmResult;
    private final String cvmDescription;
    private final String tc_hash;
    private final SmartPesa.AccountType fromAccount;
    private final SmartPesa.AccountType toAccount;
    private final String approvalCode;

    public ParcelableCardPayment(CardPayment cardPayment) {
        terminalId = cardPayment.getTerminalId();
        method = cardPayment.getMethod().getEnumId();
        cardNumber = cardPayment.getCardNumber();
        cardType = cardPayment.getCardType();
        cardExpiry = cardPayment.getCardExpiry();
        cardHolderName = cardPayment.getCardHolderName();
        cardName = cardPayment.getCardName();
        cardIcon = cardPayment.getCardIconResId();
        signatureId = cardPayment.getSignatureId();
        cvmResult = cardPayment.getCvmResult();
        cvmDescription = cardPayment.getCvmDescription();
        tc_hash = cardPayment.getTcHash();
        fromAccount = cardPayment.getAccountFrom();
        toAccount = cardPayment.getAccountTo();
        approvalCode = cardPayment.getAuthorisationId();
    }

    public ParcelableCardPayment(Parcel in) {
        terminalId = in.readString();
        method = in.readInt();
        cardNumber = in.readString();
        cardType = in.readString();
        cardExpiry = in.readString();
        cardHolderName = in.readString();
        cardName = in.readString();
        cardIcon = in.readInt();
        signatureId = in.readString();
        cvmResult = in.readString();
        cvmDescription = in.readString();
        tc_hash = in.readString();
        fromAccount = SmartPesa.AccountType.fromEnumId(in.readInt());
        toAccount = SmartPesa.AccountType.fromEnumId(in.readInt());
        approvalCode = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(terminalId);
        dest.writeInt(method);
        dest.writeString(cardNumber);
        dest.writeString(cardType);
        dest.writeString(cardExpiry);
        dest.writeString(cardHolderName);
        dest.writeString(cardName);
        dest.writeInt(cardIcon);
        dest.writeString(signatureId);
        dest.writeString(cvmResult);
        dest.writeString(cvmDescription);
        dest.writeString(tc_hash);
        dest.writeInt(fromAccount.getEnumId());
        dest.writeInt(toAccount.getEnumId());
        dest.writeString(approvalCode);
    }

    public static final Creator CREATOR = new Creator() {
        public ParcelableCardPayment createFromParcel(Parcel in) {
            return new ParcelableCardPayment(in);
        }

        public ParcelableCardPayment[] newArray(int size) {
            return new ParcelableCardPayment[size];
        }
    };

    public String getTc_hash() {
        return tc_hash;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public int getMethod() {
        return method;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getCardType() {
        return cardType;
    }

    public String getCardExpiry() {
        return cardExpiry;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public String getCardName() {
        return cardName;
    }

    public int getCardIcon() {
        return cardIcon;
    }

    public String getSignatureId() {
        return signatureId;
    }

    public String getCvmResult() {
        return cvmResult;
    }

    public String getCvmDescription() {
        return cvmDescription;
    }

    public SmartPesa.AccountType getFromAccount() {
        return fromAccount;
    }

    public SmartPesa.AccountType getToAccount() {
        return toAccount;
    }

    public String getApprovalCode() {
        return approvalCode;
    }

}