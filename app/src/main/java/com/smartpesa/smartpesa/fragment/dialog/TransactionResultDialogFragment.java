package com.smartpesa.smartpesa.fragment.dialog;

import com.smartpesa.intent.result.TransactionException;
import com.smartpesa.intent.result.TransactionResult;
import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.helpers.MoneyFormatHelper;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.math.BigDecimal;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import smartpesa.sdk.ServiceManager;
import smartpesa.sdk.models.merchant.VerifiedMerchantInfo;

public class TransactionResultDialogFragment extends AppCompatDialogFragment {

    private static final String KEY_SUCCESS = "is_success";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_CAPTION = "caption";
    private static final String KEY_AMOUNT = "amount";
    @Bind(R.id.transaction_result_amount)
    TextView mAmount;
    @Bind(R.id.transaction_result_message)
    TextView mMessage;
    @Bind(R.id.transaction_result_caption)
    TextView mCaption;
    @Bind(R.id.transaction_status_icon)
    CircleImageView mIcon;
    @Bind(R.id.positive_button)
    Button mPositiveButton;
    @Nullable private OnPositiveButtonListener mOnPositiveButtonListener;

    @NonNull
    private static TransactionResultDialogFragment newInstance(Bundle args) {
        TransactionResultDialogFragment fragment = new TransactionResultDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static TransactionResultDialogFragment newInstance(TransactionResult transactionResult) {
        String message = transactionResult.reference();
        String caption = transactionResult.responseDescription();

        Bundle args = createBundle(true, transactionResult.amount(), message, caption);

        return newInstance(args);
    }

    public static TransactionResultDialogFragment newInstance(TransactionException exception, BigDecimal transactAmount) {
        return newInstance(exception, transactAmount, null);
    }

    public static TransactionResultDialogFragment newInstance(TransactionException error, BigDecimal amount, @Nullable TransactionResult transactionResult) {
        String message = error.getMessage();
        String caption = error.getReason();
        if (transactionResult != null) {
            message = transactionResult.reference();
            caption = transactionResult.responseDescription();
            amount = transactionResult.amount();
        }

        Bundle args = createBundle(false, amount, message, caption);

        return newInstance(args);
    }

    @NonNull
    private static Bundle createBundle(boolean success, BigDecimal amount, String message, String caption) {
        Bundle args = new Bundle();
        args.putBoolean(KEY_SUCCESS, success);
        args.putSerializable(KEY_AMOUNT, amount);
        args.putString(KEY_MESSAGE, message);
        args.putString(KEY_CAPTION, caption);
        return args;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.AppTheme_Dialog_NoTitle);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_transaction_result, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        mPositiveButton.setVisibility(View.VISIBLE);
        mPositiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPositiveButtonClicked();
            }
        });
        extractArgs();
    }

    private void onPositiveButtonClicked() {
        dismiss();
        if (mOnPositiveButtonListener != null) {
            mOnPositiveButtonListener.onPositiveButtonClicked(getDialog());
        }
    }

    private void extractArgs() {
        Bundle bundle = getArguments();
        boolean isSuccess = bundle.getBoolean(KEY_SUCCESS);
        BigDecimal amount = (BigDecimal) bundle.getSerializable(KEY_AMOUNT);
        String message = bundle.getString(KEY_MESSAGE);
        String caption = bundle.getString(KEY_CAPTION);

        if (isSuccess) {
            mIcon.setImageResource(R.drawable.ic_checkmark_light_64dp);
            mIcon.setFillColorResource(R.color.pesa_green);
        } else {
            mIcon.setImageResource(R.drawable.ic_cross_light_64dp);
            mIcon.setFillColorResource(R.color.pesa_red);
        }
        VerifiedMerchantInfo cachedMerchant = ServiceManager.get(getContext()).getCachedMerchant();
        MoneyFormatHelper moneyFormatHelper = MoneyFormatHelper.getInstance(cachedMerchant != null ? cachedMerchant.getCurrency() : null);
        mAmount.setText(moneyFormatHelper.format(amount));
        mMessage.setText(message);
        mCaption.setText(caption);
    }

    public void setOnPositiveButtonListener(OnPositiveButtonListener onPositiveButtonListener) {
        mOnPositiveButtonListener = onPositiveButtonListener;
    }

    public interface OnPositiveButtonListener {
        void onPositiveButtonClicked(DialogInterface dialog);
    }
}