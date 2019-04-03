package com.smartpesa.smartpesa.fragment.result;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.models.ParcelableTransactionResponse;
import com.smartpesa.smartpesa.models.SmartPesaTransactionType;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;

import java.math.BigDecimal;

import butterknife.BindView;
import smartpesa.sdk.core.error.SpException;
import smartpesa.sdk.error.SpNetworkException;
import smartpesa.sdk.error.SpSessionException;

public class UnknownResultFragment extends AbstractResultFragment {
    public static final String KEY_NETWORK_ERROR = UnknownResultFragment.class.getName() + ".networkError";

    @BindView(R.id.go_to_history) Button goToHistoryBtn;

    public static UnknownResultFragment newInstance(@NonNull SpException mNetwork, SmartPesaTransactionType transactionType, BigDecimal amount, ParcelableTransactionResponse transactionResponse) {
        UnknownResultFragment f = new UnknownResultFragment();
        Bundle b = new Bundle();
        putArguments(b, transactionType, amount, transactionResponse);
        b.putSerializable(KEY_NETWORK_ERROR, mNetwork);
        f.setArguments(b);
        return f;
    }

    private SpException mNetworkError;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNetworkError = (SpException) getArguments().getSerializable(KEY_NETWORK_ERROR);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        goToHistoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mResultFragmentListener.goToHistory();
            }
        });
    }

    @Override
    protected int getColorResId() {
        return R.color.pesa_yellow;
    }

    @Override
    protected int getStatusIconResId() {
        return R.drawable.ic_disconnected_white;
    }

    @Override
    protected void handleResult() {
        setTitleMessage(getString(R.string.connection_dropped));
        String message = "";
        if (mNetworkError instanceof SpNetworkException || mNetworkError instanceof SpSessionException) {
            message = getActivity().getString(R.string.transaction_submitted_help);
        }
        setDescription(message);
    }

    @Override
    protected int getButtonLayoutRes() {
        return R.layout.layout_result_unknown;
    }
}
