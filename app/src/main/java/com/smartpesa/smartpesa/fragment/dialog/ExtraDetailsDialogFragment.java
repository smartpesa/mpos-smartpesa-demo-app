
package com.smartpesa.smartpesa.fragment.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.helpers.UIHelper;
import com.smartpesa.smartpesa.models.ParcelableTransactionResponse;
import com.smartpesa.smartpesa.util.MoneyUtils;

import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import dagger.Lazy;
import smartpesa.sdk.ServiceManager;
import smartpesa.sdk.core.error.SpException;
import smartpesa.sdk.error.SpSessionException;
import smartpesa.sdk.models.transaction.GetSignatureCallback;
import smartpesa.sdk.models.transaction.GetTransactionDetailCallback;
import smartpesa.sdk.models.transaction.TransactionDetail;

public class ExtraDetailsDialogFragment extends BaseDialogFragment {

    @Bind(R.id.cardLabelTv) TextView cardLabelTv;
    @Bind(R.id.cardTv) TextView cardTv;
    @Bind(R.id.merchantIdLabelTv) TextView merchantIdLabelTv;
    @Bind(R.id.merchantIdTv) TextView merchantIdTv;
    @Bind(R.id.terminalIdLabelTv) TextView terminalIdLabelTv;
    @Bind(R.id.terminalIdTv) TextView terminalIdTv;
    @Bind(R.id.fromAccountLabelTv) TextView fromAccountLabelTv;
    @Bind(R.id.fromAccountTv) TextView fromAccountTv;
    @Bind(R.id.toAccountLabelTv) TextView toAccountLabelTv;
    @Bind(R.id.toAccountTv) TextView toAccountTv;
    @Bind(R.id.taxLabelTv) TextView taxLabelTv;
    @Bind(R.id.taxTv) TextView taxTv;
    @Bind(R.id.tipLabelTv) TextView tipLabelTv;
    @Bind(R.id.tipTv) TextView tipTv;
    @Bind(R.id.operatorNameLabelTv) TextView operatorNameLabelTv;
    @Bind(R.id.operatorNameTv) TextView operatorNameTv;
    @Bind(R.id.deviceSerialNumberLabelTv) TextView deviceSerialNumberLabelTv;
    @Bind(R.id.deviceSerialNumberTv) TextView deviceSerialNumberTv;
    @Bind(R.id.cvmResultLabelTv) TextView cvmResultLabelTv;
    @Bind(R.id.cvmResultTv) TextView cvmResultTv;
    @Bind(R.id.appIdLabelTv) TextView appIdLabelTv;
    @Bind(R.id.appIdTv) TextView appIdTv;
    @Bind(R.id.tvrLabelTv) TextView tvrLabelTv;
    @Bind(R.id.tvrTv) TextView tvrTv;
    @Bind(R.id.appCryptLabelTv) TextView appCryptLabelTv;
    @Bind(R.id.appCryptTv) TextView appCryptTv;
    @Bind(R.id.destinationBankLabelTv) TextView destinationBankLabelTv;
    @Bind(R.id.destinationBankTv) TextView destinationBankTv;
    @Bind(R.id.destinationAccountNumberLabelTv) TextView destinationAccountNumberLabelTv;
    @Bind(R.id.destinationAccountNumberTv) TextView destinationAccountNumberTv;
    @Bind(R.id.billerNameLabelTv) TextView billerNameLabelTv;
    @Bind(R.id.billerNameTv) TextView billerNameTv;
    @Bind(R.id.billerCodeLabelTv) TextView billerCodeLabelTv;
    @Bind(R.id.billerCodeTv) TextView billerCodeTv;
    @Bind(R.id.ipAddressLabelTv) TextView ipAddressLabelTv;
    @Bind(R.id.ipAddressTv) TextView ipAddressTv;
    @Bind(R.id.signatureLabelTv) TextView signatureLabelTv;
    @Bind(R.id.signatureIv) ImageView signatureIv;
    @Bind(R.id.signatureNotAvailableTv) TextView signatureNotAvailableTv;

    @Bind(R.id.okBtn) Button okBtn;
    @Bind(R.id.extraDetailsProgessBar) ProgressBar progressBar;

    @Nullable public MoneyUtils mMoneyUtils;
    protected Lazy<ServiceManager> serviceManager;
    ParcelableTransactionResponse transactionResponse;
    UUID transactionId;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = new Dialog(getActivity(),
                R.style.Theme_CustomDialog);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_extra_details, null);
        ButterKnife.bind(this, view);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);
        // Creating Full Screen
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        serviceManager = SmartPesaApplication.component(getActivity()).serviceManager();
        mMoneyUtils = SmartPesaApplication.merchantComponent(getActivity()).provideMoneyUtils();
        transactionId = UUID.fromString(getArguments().getString("transactionId"));
        transactionResponse = getArguments().getParcelable("transactionResult");

        getTransactionExtraDetails(transactionId);

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    private void getTransactionExtraDetails(UUID transactionID) {
        progressBar.setVisibility(View.VISIBLE);
        serviceManager.get().getTransactionDetail(transactionID, new GetTransactionDetailCallback() {
            @Override
            public void onSuccess(TransactionDetail transactionExtraDetail) {
                if (null == getActivity()) return;
                progressBar.setVisibility(View.GONE);
                setUpValues(transactionExtraDetail);
            }

            @Override
            public void onError(SpException exception) {
                if (null == getActivity()) return;
                if (exception instanceof SpSessionException) {
                    progressBar.setVisibility(View.GONE);
                    UIHelper.showToast(getActivity(), "Session timeout, please login again");
                } else {
                    progressBar.setVisibility(View.GONE);
                    UIHelper.showErrorDialog(getActivity(), getString(R.string.app_name), exception.getMessage());
                }
            }
        });
    }

    private void setUpValues(TransactionDetail transactionExtraDetail) {

        if (transactionExtraDetail != null) {

            if (transactionExtraDetail.getTransactionId() != null) {

                if (!TextUtils.isEmpty(transactionExtraDetail.getMerchantCode())) {
                    merchantIdTv.setText(transactionExtraDetail.getMerchantCode());
                    merchantIdTv.setVisibility(View.VISIBLE);
                    merchantIdLabelTv.setVisibility(View.VISIBLE);
                }

                if (transactionExtraDetail.getSalesTax() != null) {
                    taxTv.setText(mMoneyUtils.format(transactionExtraDetail.getSalesTax()));
                    taxTv.setVisibility(View.VISIBLE);
                    taxLabelTv.setVisibility(View.VISIBLE);
                }

                if (transactionExtraDetail.getTip() != null) {
                    tipTv.setText(mMoneyUtils.format(transactionExtraDetail.getTip()));
                    tipTv.setVisibility(View.VISIBLE);
                    tipLabelTv.setVisibility(View.VISIBLE);
                }

                if (!TextUtils.isEmpty(transactionExtraDetail.getOperatorName())) {
                    operatorNameTv.setText(transactionExtraDetail.getOperatorName());
                    operatorNameLabelTv.setVisibility(View.VISIBLE);
                    operatorNameTv.setVisibility(View.VISIBLE);
                }

                if (transactionExtraDetail.getDeviceId() != null) {
                    if (!TextUtils.isEmpty(transactionExtraDetail.getSerialNumber())) {
                        deviceSerialNumberTv.setText(transactionExtraDetail.getSerialNumber());
                        deviceSerialNumberLabelTv.setVisibility(View.VISIBLE);
                        deviceSerialNumberTv.setVisibility(View.VISIBLE);
                    }
                }

                //// TODO: 12/3/18  
//                if (!TextUtils.isEmpty(transactionExtraDetail.getTvr())) {
//                    tvrTv.setText(transactionExtraDetail.getTvr());
//                    tvrTv.setVisibility(View.VISIBLE);
//                    tvrLabelTv.setVisibility(View.VISIBLE);
//                }

                if (!TextUtils.isEmpty(transactionExtraDetail.getBillingOrganisationName())) {
                    billerNameTv.setText(transactionExtraDetail.getBillingOrganisationName());
                    billerNameTv.setVisibility(View.VISIBLE);
                    billerNameLabelTv.setVisibility(View.VISIBLE);
                }

                if (!TextUtils.isEmpty(transactionExtraDetail.getBillerCode())) {
                    billerCodeTv.setText(transactionExtraDetail.getBillerCode());
                    billerCodeTv.setVisibility(View.VISIBLE);
                    billerCodeLabelTv.setVisibility(View.VISIBLE);
                }

                if (!TextUtils.isEmpty(transactionExtraDetail.getIpAddress())) {
                    ipAddressTv.setText(transactionExtraDetail.getIpAddress());
                    ipAddressTv.setVisibility(View.VISIBLE);
                    ipAddressLabelTv.setVisibility(View.VISIBLE);
                }

                if (!TextUtils.isEmpty(transactionExtraDetail.getAcquiringInstitutionName())) {
                    destinationBankTv.setText(transactionExtraDetail.getAcquiringInstitutionName());
                    destinationBankTv.setVisibility(View.VISIBLE);
                    destinationBankLabelTv.setVisibility(View.VISIBLE);
                }

                if (!TextUtils.isEmpty(transactionExtraDetail.getToAccount())) {
                    destinationAccountNumberTv.setText(transactionExtraDetail.getToAccount());
                    destinationAccountNumberTv.setVisibility(View.VISIBLE);
                    destinationAccountNumberLabelTv.setVisibility(View.VISIBLE);
                }
            }
        }

        if (transactionResponse != null) {

            if (transactionResponse.getCardPayment() != null) {
                if (!TextUtils.isEmpty(transactionResponse.getCardPayment().getTerminalId())) {
                    terminalIdTv.setText(transactionResponse.getCardPayment().getTerminalId());
                    terminalIdTv.setVisibility(View.VISIBLE);
                    terminalIdLabelTv.setVisibility(View.VISIBLE);
                }

                if (transactionResponse.getCardPayment().getFromAccount() != null) {
                    fromAccountTv.setText(transactionResponse.getCardPayment().getFromAccount().toString());
                    fromAccountTv.setVisibility(View.VISIBLE);
                    fromAccountLabelTv.setVisibility(View.VISIBLE);
                }

                if (!TextUtils.isEmpty(transactionResponse.getCardPayment().getCardName())) {
                    cardTv.setText(transactionResponse.getCardPayment().getCardName());
                    cardTv.setVisibility(View.VISIBLE);
                    cardLabelTv.setVisibility(View.VISIBLE);
                }

                if (transactionResponse.getCardPayment().getToAccount() != null) {
                    toAccountTv.setText(transactionResponse.getCardPayment().getToAccount().toString());
                    toAccountTv.setVisibility(View.VISIBLE);
                    toAccountLabelTv.setVisibility(View.VISIBLE);
                }

                if (!TextUtils.isEmpty(transactionResponse.getCardPayment().getCvmResult())) {
                    cvmResultTv.setText(transactionResponse.getCardPayment().getCvmResult());
                    cvmResultTv.setVisibility(View.VISIBLE);
                    cvmResultLabelTv.setVisibility(View.VISIBLE);
                }

                if (!TextUtils.isEmpty(transactionResponse.getCardPayment().getTc_hash())) {
                    appCryptTv.setText(transactionResponse.getCardPayment().getTc_hash());
                    appCryptTv.setVisibility(View.VISIBLE);
                    appCryptLabelTv.setVisibility(View.VISIBLE);
                }

                if (!TextUtils.isEmpty(transactionResponse.getCardPayment().getCvmDescription()) && transactionResponse.getCardPayment().getCvmDescription().equals("Signature")) {
                    showSignature();
                }
            }
        }
    }

    private void showSignature() {

        serviceManager.get().getSignature(transactionId, new GetSignatureCallback() {

            @Override
            public void onSuccess(Bitmap bitmap) {
                if (null == getActivity()) return;

                if (bitmap != null) {
                    signatureIv.setImageBitmap(bitmap);
                    signatureLabelTv.setVisibility(View.VISIBLE);
                    signatureIv.setVisibility(View.VISIBLE);
                    signatureLabelTv.setVisibility(View.VISIBLE);
                } else {
                    signatureNotAvailableTv.setVisibility(View.VISIBLE);
                    signatureLabelTv.setVisibility(View.VISIBLE);
                    signatureIv.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(SpException exception) {
                if (null == getActivity()) return;
                signatureNotAvailableTv.setVisibility(View.GONE);
                signatureLabelTv.setVisibility(View.GONE);
                signatureIv.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        SmartPesaApplication.component(getActivity()).serviceManager().get().stopScan();
    }
}