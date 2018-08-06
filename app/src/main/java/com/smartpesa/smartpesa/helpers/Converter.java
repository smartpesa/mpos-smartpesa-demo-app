package com.smartpesa.smartpesa.helpers;

import com.smartpesa.intent.TransactionType;
import com.smartpesa.intent.result.Card;
import com.smartpesa.intent.result.Currency;
import com.smartpesa.intent.result.TransactionException;
import com.smartpesa.intent.result.TransactionResult;
import com.smartpesa.smartpesa.models.SmartPesaTransactionType;

import android.support.annotation.Nullable;

import smartpesa.sdk.core.error.SpException;
import smartpesa.sdk.models.transaction.CardPayment;

public class Converter {
    public static TransactionResult from(smartpesa.sdk.models.transaction.TransactionResult result) {
        Currency c = Currency.builder()
                .symbol(result.getCurrencySymbol())
                .name(result.getCurrencyName())
                .decimals(result.getCurrencyDecimals())
                .build();

        CardPayment cardPayment = (CardPayment) result.getPayment();

        Card cd = Card.builder()
                .pan(cardPayment.getCardNumber())
                .expiry(cardPayment.getCardExpiry())
                .holderName(cardPayment.getCardHolderName())
                .type(cardPayment.getCardType())
                .build();

        return TransactionResult.builder()
                .id(result.getTransactionId())
                .reference(result.getTransactionReference())
                .datetime(result.getTransactionDatetime())
                .amount(result.getAmount())
                .currency(c)
                .card(cd)
                .responseCode(result.getResponseCode())
                .responseDescription(result.getResponseDescription())
                .cvmDescription(cardPayment.getCvmDescription())
                .description(result.getTransactionDescription())
                .authorisationResponse(result.getAuthorisationResponse())
                .authorisationResponseCode(result.getAuthorisationResponseCode())
                .authorisationId(cardPayment.getAuthorisationId())
                .isReversed(result.isReversed())
                .type(from(SmartPesaTransactionType.fromEnumId(result.getTransactionTypeEnumId())))
                .build();
    }

    @Nullable
    public static TransactionType from(SmartPesaTransactionType type) {
        switch (type) {
            case SALE:
                return TransactionType.SALES;
            case REFUND:
                return TransactionType.REFUND;
            case VOID:
                return TransactionType.VOID;
            default:
                return null;
        }
    }

    @Nullable
    public static SmartPesaTransactionType from(TransactionType type) {
        switch (type) {
            case SALES:
                return SmartPesaTransactionType.SALE;
            case REFUND:
                return SmartPesaTransactionType.REFUND;
            case VOID:
                return SmartPesaTransactionType.VOID;
            default:
                return null;
        }
    }

    public static TransactionException from(SpException exception) {
        String reason = "";
//        if (exception instanceof SpVersionException) {
//            reason = ((SpVersionException) exception).getReason().toString();
//        } else if (exception instanceof SpNetworkException) {
//            reason = ((SpNetworkException) exception).getReason().toString();
//        } else if (exception instanceof SpSessionException) {
//            reason = "Session Expired.";
//        } else if (exception instanceof SpCardException) {
//            reason = ((SpCardException) exception).getReason().toString();
//        } else if (exception instanceof SpDeviceException) {
//            reason = ((SpDeviceException) exception).getReason().toString();
//        } else if (exception instanceof SpDeviceConnectionException) {
//            reason = ((SpDeviceConnectionException) exception).getReason().toString();
//        } else if (exception instanceof SpTransactionException) {
//            reason = ((SpTransactionException) exception).getReason().toString();
//        } else if (exception instanceof SpServerResponseException) {
//            reason = "Server Processing Failed.";
//        } else if (exception instanceof SpPinEntryException) {
//            reason = ((SpPinEntryException) exception).getReason().toString();
//        }
        return new TransactionException(exception, exception.getMessage(), reason, ""); //TODO
    }
}