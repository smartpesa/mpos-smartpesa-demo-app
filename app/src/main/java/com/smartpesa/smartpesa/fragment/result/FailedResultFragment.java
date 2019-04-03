package com.smartpesa.smartpesa.fragment.result;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.models.ParcelableTransactionResponse;
import com.smartpesa.smartpesa.models.SmartPesaTransactionType;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.math.BigDecimal;

import butterknife.BindView;

public class FailedResultFragment extends AbstractResultFragment implements AbstractResultFragment.Retryable {

    public static final String FAILURE_REASON = FailedResultFragment.class.getName() + ".failureReason";

    public static FailedResultFragment newInstance(String reason, SmartPesaTransactionType transactionType, BigDecimal amount, ParcelableTransactionResponse transactionResponse) {
        Bundle b = new Bundle();
        putArguments(b, transactionType, amount, transactionResponse);
        b.putString(FAILURE_REASON, reason);
        FailedResultFragment f = new FailedResultFragment();
        f.setArguments(b);
        return f;
    }

    @BindView(R.id.try_again_button) Button retryBtn;
    @BindView(R.id.help_btn) Button helpBtn;

    private String mFailureReason;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFailureReason = getArguments().getString(FAILURE_REASON);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected int getColorResId() {
        return R.color.pesa_red;
    }

    @Override
    protected int getStatusIconResId() {
        return R.drawable.ic_clear_white_24dp;
    }

    @Override
    protected void handleResult() {

        retryBtn.setEnabled(false);
        retryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mResultFragmentListener.retry();
            }
        });

        if (transactionType == SmartPesaTransactionType.BALANCE_INQUIRY) {
            setTitleMessage(getString(R.string.inquiry_failed));
        } else {
            setTitleMessage(getString(R.string.transaction_failed));
        }
        if (mTransactionResponse != null) {
            mFailureReason = mTransactionResponse.getResponseDescription();
        }

        setDescription(mFailureReason);
    }

    @Override
    protected int getButtonLayoutRes() {
        return R.layout.layout_result_failed;
    }

    @Override
    public void canRetry() {
        new CountDownTimer(5000, 1000) {
            public void onTick(long millisUntilFinished) {
                retryBtn.setText(mContext.getString(R.string.try_in, millisUntilFinished / 1000));
            }

            public void onFinish() {
                retryBtn.setText(R.string.try_again);
                retryBtn.setEnabled(true);
            }
        }.start();

    }
}
