package com.smartpesa.smartpesa.fragment.result;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.models.ParcelableTransactionResponse;
import com.smartpesa.smartpesa.models.SmartPesaTransactionType;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import butterknife.Bind;
import smartpesa.sdk.SmartPesa;

public class ApprovedFragment extends AbstractResultFragment {

    private static final String KEY_VERIFICATION = ApprovedFragment.class.getName() + ".verification";

    @Bind(R.id.receipt_APPROVED_BTN) Button receiptBTN;

    public static ApprovedFragment newInstance(ParcelableTransactionResponse transactionResponse, SmartPesa.Verification verification) {
        ApprovedFragment f = new ApprovedFragment();
        Bundle b = new Bundle();
        putArguments(b, transactionResponse.getTransaction(), transactionResponse.getAmount(), transactionResponse);
        b.putSerializable(KEY_VERIFICATION, verification);
        f.setArguments(b);
        return f;
    }

    private SmartPesa.Verification mVerification;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mVerification = (SmartPesa.Verification) getArguments().getSerializable(KEY_VERIFICATION);
    }

    @Override
    protected int getColorResId() {
        return R.color.pesa_green;
    }

    @Override
    protected int getStatusIconResId() {
        return R.drawable.ic_done_white_24dp;
    }

    @Override
    protected void handleResult() {
        if (mVerification == SmartPesa.Verification.SIGNATURE) {
            receiptBTN.setText(R.string.signature);
            receiptBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showSignatureDialog();
                }
            });
        } else if (mVerification == SmartPesa.Verification.RECEIPT) {
            receiptBTN.setText(R.string.receipt);
            receiptBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showReceipt();
                }
            });
        }

        if (transactionType == SmartPesaTransactionType.BALANCE_INQUIRY) {
            setTitleMessage(getString(R.string.inquiry_success));
        } else {
            setTitleMessage(getString(R.string.transaction_successful));
        }
    }

    @Override
    protected int getButtonLayoutRes() {
        return R.layout.layout_result_approved;
    }

    private void showReceipt() {
        mResultFragmentListener.showReceipt(new Bundle());
    }

    private void showSignatureDialog() {
        mResultFragmentListener.showSignature(mTransactionResponse, new Bundle());
    }
}
