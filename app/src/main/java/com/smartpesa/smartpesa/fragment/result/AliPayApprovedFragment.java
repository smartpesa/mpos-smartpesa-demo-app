package com.smartpesa.smartpesa.fragment.result;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.fragment.BaseFragment;
import com.smartpesa.smartpesa.models.ParcelableAliPayPayment;
import com.smartpesa.smartpesa.models.ParcelableTransactionResponse;
import com.smartpesa.smartpesa.util.MoneyUtils;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.math.BigDecimal;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AliPayApprovedFragment extends BaseFragment {

    public static final String IS_SUCCESS = "isSuccess";
    public static final String PARCELABLE_RESPONSE = "parcelableResponse";
    public static final String ERROR_MESSAGE = "errorMessage";

    @Bind(R.id.amount_APPROVED_TV) TextView amountTv;
    @Bind(R.id.buyerIdTv) TextView buyerIdTv;
    @Bind(R.id.sellerTv) TextView sellerTv;
    @Bind(R.id.resultCodeTv) TextView resultCodeTv;
    @Bind(R.id.amountInYuanTv) TextView amountInYuanTv;
    @Bind(R.id.completeBtn) Button completeBtn;
    @Bind(R.id.payment_status_approved_tv) TextView titleMessage;
    @Bind(R.id.approvedRL) RelativeLayout approvedRl;
    @Bind(R.id.message_TV) TextView message_TV;
    @Bind(R.id.status_APPROVED_IV) ImageView statusIv;
    @Bind(R.id.actionTv) TextView actionTv;
    @Bind(R.id.tradeNoTv) TextView tradeNoTv;
    @Bind(R.id.notifyIdTv) TextView notifyIdTv;
    @Bind(R.id.forexRateTv) TextView forexRateTv;
    @Bind(R.id.buyerIdentityCodeTv) TextView buyerIdentityCodeTv;

    boolean isSuccess;
    ParcelableTransactionResponse parcelableTransactionResponse;
    String errorMessage;

    @Nullable public MoneyUtils mMoneyUtils;

    public static AliPayApprovedFragment newInstance(boolean success, String errorMessage, ParcelableTransactionResponse transactionResponse) {
        AliPayApprovedFragment fragment = new AliPayApprovedFragment();
        Bundle paymentBundle = new Bundle();
        paymentBundle.putBoolean(IS_SUCCESS, success);
        paymentBundle.putString(ERROR_MESSAGE, errorMessage);
        paymentBundle.putParcelable(PARCELABLE_RESPONSE, transactionResponse);
        fragment.setArguments(paymentBundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mMoneyUtils = SmartPesaApplication.merchantComponent(getActivity()).provideMoneyUtils();

        isSuccess = getArguments().getBoolean(IS_SUCCESS);
        parcelableTransactionResponse = getArguments().getParcelable(PARCELABLE_RESPONSE);
        errorMessage = getArguments().getString(ERROR_MESSAGE);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_alipay_approved, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!isSuccess) {
            titleMessage.setText(R.string.transaction_failed);
            approvedRl.setBackgroundColor(getResources().getColor(R.color.pesa_red));
            statusIv.setImageResource(R.drawable.ic_clear_white_24dp);
        }

        if (parcelableTransactionResponse != null) {

            String message = parcelableTransactionResponse.getResponseDescription();
            message_TV.setText(message);
            message_TV.setVisibility(View.VISIBLE);

            BigDecimal amount = parcelableTransactionResponse.getAmount();
            String currencySymbol = parcelableTransactionResponse.getCurrencySymbol();

            if (amount != null && currencySymbol != null && mMoneyUtils != null) {
                amountTv.setText(currencySymbol + " " + mMoneyUtils.format(amount));
            }

            ParcelableAliPayPayment alipayPayment = parcelableTransactionResponse.getAlipayPayment();

            if (alipayPayment != null) {

                String resultCode = alipayPayment.getResultCode();

                if (resultCode != null) {
                    resultCodeTv.setText(resultCode);
                }

                BigDecimal amountInYuan = alipayPayment.getTransactionAmountInYuan();

                if (amountInYuan != null && mMoneyUtils != null) {
                    amountInYuanTv.setText(mMoneyUtils.format(amountInYuan));
                }

                String buyerId = alipayPayment.getBuyerId();

                if (buyerId != null) {
                    buyerIdTv.setText(buyerId);
                }

                String sellerId = alipayPayment.getSellerId();

                if (sellerId != null) {
                    sellerTv.setText(sellerId);
                }

                String action = alipayPayment.getAction();

                if (action != null) {
                    actionTv.setText(action);
                }

                String tradeNo = alipayPayment.getTradeNo();

                if (tradeNo != null) {
                    tradeNoTv.setText(tradeNo);
                }

                String notifyId = alipayPayment.getNotifyId();

                if (notifyId != null) {
                    notifyIdTv.setText(notifyId);
                }

                BigDecimal forexRate = alipayPayment.getForexRate();

                if (forexRate != null && mMoneyUtils != null) {
                    forexRateTv.setText(mMoneyUtils.format(forexRate));
                }

                String buyerIdentityCode = alipayPayment.getBuyerIdentityCode();

                if (buyerIdentityCode != null) {
                    buyerIdentityCodeTv.setText(buyerIdentityCode);
                }
            }
        } else {
            if (errorMessage != null) {
                message_TV.setText(errorMessage);
                message_TV.setVisibility(View.VISIBLE);
            }
        }

        completeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

    }
}
