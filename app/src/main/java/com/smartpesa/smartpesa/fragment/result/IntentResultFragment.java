package com.smartpesa.smartpesa.fragment.result;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.fragment.BaseFragment;
import com.smartpesa.smartpesa.models.ParcelableTransactionResponse;
import com.smartpesa.smartpesa.util.DateUtils;
import com.smartpesa.smartpesa.util.MoneyUtils;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import smartpesa.sdk.models.merchant.VerifiedMerchantInfo;

public class IntentResultFragment extends BaseFragment {

    public static final String KEY_AMOUNT = IntentResultFragment.class.getName() + "amount";
    public static final String KEY_DATA = IntentResultFragment.class.getName() + "transactionData";
    public static final String KEY_IS_SUCCESS = IntentResultFragment.class.getName() + "isTransactionSuccess";
    public static final String KEY_ERROR_MESSAGE = IntentResultFragment.class.getName() + "errorMessage";

    @Bind(R.id.approvedRL) RelativeLayout approvedRL;
    @Bind(R.id.status_APPROVED_IV) ImageView approvedIV;
    @Bind(R.id.payment_status_approved_tv) TextView paymentStatusTV;
    @Bind(R.id.amount_APPROVED_TV) TextView amountTV;
    @Bind(R.id.dateLabel_APPROVED_TV) TextView dateLabelTV;
    @Bind(R.id.date_APPROVED_TV) TextView dateTV;
    @Bind(R.id.timeLabel_APPROVED_TV) TextView timeLabelTV;
    @Bind(R.id.time_APPROVED_TV) TextView timeTV;
    @Bind(R.id.cardLabel_APPROVED_TV) TextView cardLabelTV;
    @Bind(R.id.cardNumber_APPROVED_TV) TextView cardNumTV;
    @Bind(R.id.expiryLabel_APPROVED_TV) TextView expiryLabelTV;
    @Bind(R.id.expiry_APPROVED_TV) TextView expiryTV;
    @Bind(R.id.referenceLabel_APPROVED_TV) TextView referenceLabelTV;
    @Bind(R.id.reference_APPROVED_TV) TextView referenceTV;
    @Bind(R.id.message_TV) TextView messageTV;
    @Bind(R.id.closeBtn) Button closeBtn;
    @Bind(R.id.timerText) TextView timerTextTv;

    @Nullable protected ParcelableTransactionResponse mTransactionResponse = null;
    protected BigDecimal mAmount = null;
    boolean isTransactionSuccess;
    String errorMessage = null;
    @Nullable public MoneyUtils mMoneyUtils;
    VerifiedMerchantInfo mVerifyMerchantInfo;

    public static IntentResultFragment newInstance(boolean isTransactionSuccess, @Nullable BigDecimal amount, @Nullable ParcelableTransactionResponse transactionResponse, @Nullable String errorMessage) {
        IntentResultFragment fragment = new IntentResultFragment();
        Bundle resultBundle = new Bundle();
        resultBundle.putBoolean(KEY_IS_SUCCESS, isTransactionSuccess);

        if (amount != null) {
            resultBundle.putDouble(KEY_AMOUNT, amount.doubleValue());
        }

        if (transactionResponse != null) {
            resultBundle.putParcelable(KEY_DATA, transactionResponse);
        }

        if (errorMessage != null) {
            resultBundle.putString(KEY_ERROR_MESSAGE, errorMessage);
        }

        fragment.setArguments(resultBundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getMerchantComponent(getActivity()) == null) {
            logoutUser();
            return;
        }

        mMoneyUtils = SmartPesaApplication.merchantComponent(getActivity()).provideMoneyUtils();
        mVerifyMerchantInfo = getMerchantComponent(getActivity()).provideMerchant();

        if (getArguments().containsKey(KEY_DATA)) {
            mTransactionResponse = getArguments().getParcelable(KEY_DATA);
        }

        if (getArguments().containsKey(KEY_AMOUNT)) {
            mAmount = mMoneyUtils.parseBigDecimal(getArguments().getDouble(KEY_AMOUNT));
        }

        if (getArguments().containsKey(KEY_ERROR_MESSAGE)) {
            errorMessage = getArguments().getString(KEY_ERROR_MESSAGE);
        }

        if (getArguments().containsKey(KEY_IS_SUCCESS)) {
            isTransactionSuccess = getArguments().getBoolean(KEY_IS_SUCCESS);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_intent_result, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (isTransactionSuccess) {
            approvedIV.setImageResource(R.drawable.ic_done_white_24dp);
            approvedRL.setBackgroundColor(getResources().getColor(R.color.pesa_green));
        } else {
            approvedIV.setImageResource(R.drawable.ic_clear_white_24dp);
            approvedRL.setBackgroundColor(getResources().getColor(R.color.pesa_red));
        }

        //set the amount in the amount field
        if (mAmount == null) {
            amountTV.setVisibility(View.GONE);
        } else {
            amountTV.setText(mVerifyMerchantInfo.getCurrency().getCurrencySymbol() + " " + mMoneyUtils.format(mAmount));
        }

        if (mTransactionResponse == null) {
            hideOtherViews();
        } else {
            displayTransaction();
        }

        if (errorMessage != null) {
            messageTV.setText(errorMessage);
            messageTV.setVisibility(View.VISIBLE);
        }

        final CountDownTimer downTimer = new CountDownTimer(3000, 1000) {
            public void onTick(long millisUntilFinished) {
                if (getActivity() != null) {
                    timerTextTv.setText(getString(R.string.redirecting) + String.format(" %d seconds",
                            TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
                }
            }

            public void onFinish() {
                if (getActivity() != null) {
                    timerTextTv.setText("");
                    getActivity().finish();
                }
            }
        }.start();

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    downTimer.cancel();
                    getActivity().finish();
                }
            }
        });
    }

    private void hideOtherViews() {

        dateTV.setVisibility(View.INVISIBLE);
        dateLabelTV.setVisibility(View.INVISIBLE);
        timeTV.setVisibility(View.INVISIBLE);
        timeLabelTV.setVisibility(View.INVISIBLE);
        cardNumTV.setVisibility(View.INVISIBLE);
        cardLabelTV.setVisibility(View.INVISIBLE);
        expiryLabelTV.setVisibility(View.INVISIBLE);
        expiryTV.setVisibility(View.INVISIBLE);
        referenceTV.setVisibility(View.INVISIBLE);
        referenceLabelTV.setVisibility(View.INVISIBLE);

    }

    public void displayTransaction() {

        BigDecimal amount = mTransactionResponse.getAmount();
        Date dateTime = mTransactionResponse.getTransactionDatetime();
        String time = DateUtils.format(dateTime, DateUtils.TIME_FORMAT_HH_MM_SS);
        String date = DateUtils.format(dateTime, DateUtils.DATE_FORMAT_DD_MM_YYYY);
        String cardNumber = mTransactionResponse.getCardPayment().getCardNumber();
        String referenceNumber = mTransactionResponse.getTransactionReference();
        String currencySymbol = mTransactionResponse.getCurrencySymbol();
        String message = mTransactionResponse.getResponseDescription();

        if (message != null) {
            messageTV.setText(message);
        }

        if (amount == null) {
            amountTV.setVisibility(View.INVISIBLE);
        } else {
            amountTV.setText(currencySymbol + " " + mMoneyUtils.format(amount));
        }

        if (date != null) {
            dateTV.setText(date);
        }

        if (time != null) {
            timeTV.setText(time);
        }

        if (cardNumber != null) {
            cardNumTV.setText(cardNumber);
        }

        if (referenceNumber != null) {
            referenceTV.setText(referenceNumber);
        }

    }
}
