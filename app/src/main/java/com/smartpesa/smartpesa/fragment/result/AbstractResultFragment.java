package com.smartpesa.smartpesa.fragment.result;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.fragment.BaseFragment;
import com.smartpesa.smartpesa.models.ParcelableTransactionResponse;
import com.smartpesa.smartpesa.models.SmartPesaTransactionType;
import com.smartpesa.smartpesa.util.DateUtils;
import com.smartpesa.smartpesa.util.MoneyUtils;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import smartpesa.sdk.models.merchant.VerifiedMerchantInfo;

public abstract class AbstractResultFragment extends BaseFragment {

    public static final String KEY_TRANSACTION_TYPE = AbstractResultFragment.class.getName() + "transactionType";
    public static final String KEY_AMOUNT = AbstractResultFragment.class.getName() + "amount";
    public static final String KEY_DATA = AbstractResultFragment.class.getName() + "transactionData";

    protected static void putArguments(Bundle bundle, SmartPesaTransactionType transactionType, BigDecimal amount, @Nullable ParcelableTransactionResponse transactionResponse) {
        bundle.putInt(KEY_TRANSACTION_TYPE, transactionType.getEnumId());
        bundle.putDouble(KEY_AMOUNT, amount.doubleValue());
        bundle.putParcelable(KEY_DATA, transactionResponse);
    }

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
    public
    @Bind(R.id.referenceLabel_APPROVED_TV) TextView referenceLabelTV;
    public
    @Bind(R.id.reference_APPROVED_TV) TextView referenceTV;
    @Bind(R.id.message_TV) TextView messageTV;

    Context mContext;
    @Nullable protected ParcelableTransactionResponse mTransactionResponse;
    public SmartPesaTransactionType transactionType;
    protected BigDecimal mAmount;

    @Nullable public MoneyUtils mMoneyUtils;

    //listener to switch fragment and show receipt
    public interface ResultFragmentListener {
        void showReceipt(Bundle bundle);

        void goToHistory();

        void retry();

        void showSignature(ParcelableTransactionResponse transactionResponse, Bundle bundle);
    }

    public ResultFragmentListener mResultFragmentListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mResultFragmentListener = (ResultFragmentListener) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getMerchantComponent(getActivity()) == null) {
            logoutUser();
            return;
        }

        if (getArguments().containsKey(KEY_DATA)) {
            mTransactionResponse = getArguments().getParcelable(KEY_DATA);
        }

        mMoneyUtils = SmartPesaApplication.merchantComponent(getActivity()).provideMoneyUtils();

        transactionType = SmartPesaTransactionType.fromEnumId(getArguments().getInt(KEY_TRANSACTION_TYPE));
        mAmount = mMoneyUtils.parseBigDecimal(getArguments().getDouble(KEY_AMOUNT));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_approved, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeComponents(view);

        approvedIV.setImageResource(getStatusIconResId());
        approvedRL.setBackgroundColor(getResources().getColor(getColorResId()));

        //set the amount in the amount field
        if (transactionType.equals(SmartPesaTransactionType.BALANCE_INQUIRY) || mAmount == null) {
            amountTV.setVisibility(View.GONE);
        } else {
            VerifiedMerchantInfo mVerifyMerchantInfo = getMerchantComponent(getActivity()).provideMerchant();
            if (mVerifyMerchantInfo == null) {
                logoutUser();
                return;
            } else {
                String currencySymbol = mVerifyMerchantInfo.getCurrency().getCurrencySymbol();
                amountTV.setText(currencySymbol + " " + mMoneyUtils.format(mAmount));
            }
        }

        handleResult();

        if (mTransactionResponse == null) {
            hideOtherViews();
        } else {
            displayTransaction();
        }
    }

    protected abstract
    @ColorRes
    int getColorResId();

    protected abstract
    @DrawableRes
    int getStatusIconResId();

    protected abstract void handleResult();

    protected void setTitleMessage(String title) {
        paymentStatusTV.setText(title);
    }

    protected void setDescription(String description) {
        messageTV.setText(description);
        messageTV.setVisibility(View.VISIBLE);
    }

    //hide other views when failure occurs
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

    //display the transaction details
    public void displayTransaction() {

        BigDecimal amount = mTransactionResponse.getAmount();
        Date dateTime = mTransactionResponse.getTransactionDatetime();
        String time = DateUtils.format(dateTime, DateUtils.TIME_FORMAT_HH_MM_SS);
        String date = DateUtils.format(dateTime, DateUtils.DATE_FORMAT_DD_MM_YYYY);
        String cardNumber = mTransactionResponse.getCardPayment().getCardNumber();
        String referenceNumber = mTransactionResponse.getTransactionReference();
        String currencySymbol = mTransactionResponse.getCurrencySymbol();

        if (transactionType.equals(SmartPesaTransactionType.BALANCE_INQUIRY)) {
            amountTV.setVisibility(View.INVISIBLE);
        } else {
            amountTV.setText(currencySymbol + " " + mMoneyUtils.format(amount));
        }

        dateTV.setText(date);
        timeTV.setText(time);
        cardNumTV.setText(cardNumber);
        referenceTV.setText(referenceNumber);
    }

    //initialize the UI components
    private void initializeComponents(View view) {

        ViewStub stub = (ViewStub) view.findViewById(R.id.button_stub);
        stub.setLayoutResource(getButtonLayoutRes());
        stub.inflate();
        ButterKnife.bind(this, view);
        mContext = getActivity();
    }

    protected abstract
    @LayoutRes
    int getButtonLayoutRes();

    public interface Retryable {
        void canRetry();
    }

}
