package com.smartpesa.smartpesa.fragment.result;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.fragment.BaseFragment;
import com.smartpesa.smartpesa.models.ParcelableGoCoinPayment;
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

public class GoCoinApprovedFragment extends BaseFragment {

    public static final String IS_SUCCESS = "isSuccess";
    public static final String PARCELABLE_RESPONSE = "parcelableResponse";
    public static final String ERROR_MESSAGE = "errorMessage";

    @Bind(R.id.amount_APPROVED_TV) TextView amountTv;
    @Bind(R.id.cryptoNameTv) TextView cryptoNameTv;
    @Bind(R.id.cryptoPriceTv) TextView cryptoPriceTv;
    @Bind(R.id.statusTv) TextView statusTv;
    @Bind(R.id.completeBtn) Button completeBtn;
    @Bind(R.id.payment_status_approved_tv) TextView titleMessage;
    @Bind(R.id.approvedRL) RelativeLayout approvedRl;
    @Bind(R.id.message_TV) TextView message_TV;
    @Bind(R.id.status_APPROVED_IV) ImageView statusIv;

    boolean isSuccess;
    ParcelableTransactionResponse parcelableTransactionResponse;
    String errorMessage;

    @Nullable public MoneyUtils mMoneyUtils;

    public static GoCoinApprovedFragment newInstance(boolean success, String errorMessage, ParcelableTransactionResponse transactionResponse) {
        GoCoinApprovedFragment fragment = new GoCoinApprovedFragment();
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
        View view = inflater.inflate(R.layout.fragment_gocoin_approved, container, false);
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

            ParcelableGoCoinPayment goCoinPayment = parcelableTransactionResponse.getGoCoinPayment();

            if (goCoinPayment != null) {

                String cryptoName = goCoinPayment.getCryptoCurrencyName();

                if (cryptoName != null) {
                    cryptoNameTv.setText(cryptoName);
                }

                BigDecimal crytoPrice = goCoinPayment.getCryptoPrice();

                if (crytoPrice != null) {
                    cryptoPriceTv.setText(crytoPrice.toString());
                }

                String status = goCoinPayment.getStatus();

                if (status != null) {
                    statusTv.setText(status);
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
