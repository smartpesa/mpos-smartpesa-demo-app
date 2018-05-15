package com.smartpesa.smartpesa.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import smartpesa.sdk.SmartPesa;
import smartpesa.sdk.models.transaction.AlipayPayment;
import smartpesa.sdk.models.transaction.CardPayment;
import smartpesa.sdk.models.transaction.CryptoPayment;
import smartpesa.sdk.models.transaction.Payment;
import smartpesa.sdk.models.transaction.TransactionResult;

public class ParcelableTransactionResponse implements Parcelable {

    private final UUID transactionId;
    private final String transactionReference;
    private final Date transactionDatetime;
    private final String description;
    private final BigDecimal amount;
    private final UUID currencyId;
    private final int responseCode;
    private final String notificationEmail;
    private final String notificationPhone;
    private final String currencyName;
    private final String currencySymbol;
    private final String responseDescription;
    private final String systemTraceAuditNumber;
    private final String transactionDescription;
    private final String settledStatus;
    private final SmartPesa.SettlementCode settlementCode;
    private final String authorisationResponseCode;
    private final String authorisationResponse;
    private SmartPesaTransactionType transaction;
    private final boolean reversed;
    private final String retrievalReferenceNumber;
    private final Payment.Type paymentType;
    private final ParcelableCardPayment cardPayment;
    private final ParcelableAliPayPayment alipayPayment;
    private final ParcelableGoCoinPayment goCoinPayment;

    public ParcelableTransactionResponse(TransactionResult transactionResponse) {
        UUID nullPointer = UUID.fromString("00000000-0000-0000-0000-000000000000");

        if (transactionResponse.getTransactionId() != null) {
            transactionId = transactionResponse.getTransactionId();
        } else {
            transactionId = nullPointer;
        }

        transactionReference = transactionResponse.getTransactionReference();
        transactionDatetime = transactionResponse.getTransactionDatetime();
        description = transactionResponse.getTransactionDescription();
        amount = transactionResponse.getAmount();
        currencyId = transactionResponse.getCurrencyId();
        responseCode = transactionResponse.getResponseCode();
        notificationEmail = transactionResponse.getNotificationEmail();
        notificationPhone = transactionResponse.getNotificationPhone();
        currencyName = transactionResponse.getCurrencyName();
        currencySymbol = transactionResponse.getCurrencySymbol();
        responseDescription = transactionResponse.getResponseDescription();
        systemTraceAuditNumber = transactionResponse.getSystemTraceAuditNumber();
        transactionDescription = transactionResponse.getTransactionDescription();
        settledStatus = transactionResponse.getSettledStatus();
        settlementCode = transactionResponse.getSettlementCode();
        authorisationResponseCode = transactionResponse.getAuthorisationResponseCode();
        authorisationResponse = transactionResponse.getAuthorisationResponse();
        reversed = transactionResponse.isReversed();
        retrievalReferenceNumber = transactionResponse.getRetrievalReferenceNumber();
        try {
            transaction = SmartPesaTransactionType.fromEnumId(transactionResponse.getTransactionTypeEnumId());
        } catch (Exception e) {
            e.printStackTrace();
            transaction = null;
        }

        if (transactionResponse.getPayment() != null) {

            if (transactionResponse.getPayment().getPaymentType() != null) {

                paymentType = transactionResponse.getPayment().getPaymentType();

                if (paymentType.equals(Payment.Type.CARD)) {
                    cardPayment = new ParcelableCardPayment((CardPayment) transactionResponse.getPayment());
                } else {
                    cardPayment = null;
                }

                if (paymentType.equals(Payment.Type.ALIPAY)) {
                    alipayPayment = new ParcelableAliPayPayment((AlipayPayment) transactionResponse.getPayment());
                } else {
                    alipayPayment = null;
                }

                if (paymentType.equals(Payment.Type.CRYPTO)) {
                    goCoinPayment = new ParcelableGoCoinPayment((CryptoPayment) transactionResponse.getPayment());
                } else {
                    goCoinPayment = null;
                }

            } else {
                paymentType = null;
                cardPayment = null;
                alipayPayment = null;
                goCoinPayment = null;
            }

        } else {
            paymentType = null;
            cardPayment = null;
            alipayPayment = null;
            goCoinPayment = null;
        }
    }

    public ParcelableTransactionResponse(Parcel in) {
        transactionId = UUID.fromString(in.readString());
        transactionReference = in.readString();
        transactionDatetime = new Date(in.readLong());
        description = in.readString();
        amount = new BigDecimal(in.readDouble());
        currencyId = UUID.fromString(in.readString());
        responseCode = in.readInt();
        notificationEmail = in.readString();
        notificationPhone = in.readString();
        currencyName = in.readString();
        currencySymbol = in.readString();
        responseDescription = in.readString();
        systemTraceAuditNumber = in.readString();
        transactionDescription = in.readString();
        settledStatus = in.readString();
        settlementCode = SmartPesa.SettlementCode.fromEnumId(in.readInt());
        authorisationResponseCode = in.readString();
        authorisationResponse = in.readString();
        reversed = in.readByte() != 0;
        retrievalReferenceNumber = in.readString();
        transaction = SmartPesaTransactionType.fromEnumId(in.readInt());
        paymentType = Payment.Type.fromCode(in.readInt());
        cardPayment = in.readParcelable(ParcelableCardPayment.class.getClassLoader());
        alipayPayment = in.readParcelable(ParcelableAliPayPayment.class.getClassLoader());
        goCoinPayment = in.readParcelable(ParcelableGoCoinPayment.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(transactionId.toString());
        dest.writeString(transactionReference);
        dest.writeLong(transactionDatetime.getTime());
        dest.writeString(description);
        dest.writeDouble(amount.doubleValue());
        dest.writeString(currencyId.toString());
        dest.writeInt(responseCode);
        dest.writeString(notificationEmail);
        dest.writeString(notificationPhone);
        dest.writeString(currencyName);
        dest.writeString(currencySymbol);
        dest.writeString(responseDescription);
        dest.writeString(systemTraceAuditNumber);
        dest.writeString(transactionDescription);
        dest.writeString(settledStatus);
        dest.writeInt(settlementCode.getEnumId());
        dest.writeString(authorisationResponseCode);
        dest.writeString(authorisationResponse);
        dest.writeByte((byte) (reversed ? 1 : 0));
        dest.writeString(retrievalReferenceNumber);
        dest.writeInt(transaction.getEnumId());
        dest.writeInt(paymentType.code);
        dest.writeParcelable(cardPayment, flags);
        dest.writeParcelable(alipayPayment, flags);
        dest.writeParcelable(goCoinPayment, flags);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public ParcelableTransactionResponse createFromParcel(Parcel in) {
            return new ParcelableTransactionResponse(in);
        }

        public ParcelableTransactionResponse[] newArray(int size) {
            return new ParcelableTransactionResponse[size];
        }
    };

    public UUID getTransactionId() {
        return transactionId;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public Date getTransactionDatetime() {
        return transactionDatetime;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public UUID getCurrencyId() {
        return currencyId;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getNotificationEmail() {
        return notificationEmail;
    }

    public String getNotificationPhone() {
        return notificationPhone;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public String getResponseDescription() {
        return responseDescription;
    }

    public String getSystemTraceAuditNumber() {
        return systemTraceAuditNumber;
    }

    public String getTransactionDescription() {
        return transactionDescription;
    }

    public String getSettledStatus() {
        return settledStatus;
    }

    public SmartPesa.SettlementCode getSettlementCode() {
        return settlementCode;
    }

    public String getAuthorisationResponseCode() {
        return authorisationResponseCode;
    }

    public String getAuthorisationResponse() {
        return authorisationResponse;
    }

    public boolean isReversed() {
        return reversed;
    }

    public String getRetrievalReferenceNumber() {
        return retrievalReferenceNumber;
    }

    @Nullable
    public SmartPesaTransactionType getTransaction() {
        return transaction;
    }

    public void setTransaction(SmartPesaTransactionType transaction) {
        this.transaction = transaction;
    }

    public ParcelableCardPayment getCardPayment() {
        return cardPayment;
    }

    public ParcelableAliPayPayment getAlipayPayment() {
        return alipayPayment;
    }

    public Payment.Type getPaymentType() {
        return paymentType;
    }

    public ParcelableGoCoinPayment getGoCoinPayment() {
        return goCoinPayment;
    }
}