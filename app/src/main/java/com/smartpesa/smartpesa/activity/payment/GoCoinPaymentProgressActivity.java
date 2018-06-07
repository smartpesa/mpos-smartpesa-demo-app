package com.smartpesa.smartpesa.activity.payment;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.activity.base.BaseActivity;
import com.smartpesa.smartpesa.fragment.result.GoCoinApprovedFragment;
import com.smartpesa.smartpesa.fragment.result.UnknownResultFragment;
import com.smartpesa.smartpesa.models.ParcelableTransactionResponse;
import com.smartpesa.smartpesa.models.SmartPesaTransactionType;
import com.smartpesa.smartpesa.util.MoneyUtils;
import com.smartpesa.smartpesa.util.constants.SPConstants;
import com.wang.avi.AVLoadingIndicatorView;

import net.glxn.qrgen.android.QRCode;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.math.BigDecimal;

import butterknife.Bind;
import butterknife.ButterKnife;
import dagger.Lazy;
import smartpesa.sdk.ServiceManager;
import smartpesa.sdk.SmartPesa;
import smartpesa.sdk.core.error.SpException;
import smartpesa.sdk.error.SpCardTransactionException;
import smartpesa.sdk.error.SpSessionException;
import smartpesa.sdk.error.SpTransactionException;
import smartpesa.sdk.interfaces.QRTransactionCallback;
import smartpesa.sdk.interfaces.TransactionData;
import smartpesa.sdk.models.currency.Currency;
import smartpesa.sdk.models.merchant.TransactionType;
import smartpesa.sdk.models.transaction.QrPayment;
import smartpesa.sdk.models.transaction.Transaction;

public class GoCoinPaymentProgressActivity extends BaseActivity {

    @Bind(R.id.amount_Process_TV) protected TextView amountTv;
    @Bind(R.id.progress_TV) protected TextView progressTv;
    @Bind(R.id.transationTypeTV) protected TextView transactionTypeTv;
    @Bind(R.id.transactionAnimation) protected AVLoadingIndicatorView transactionAnimation;
    @Bind(R.id.qrImageView) protected ImageView qrImageView;
    @Bind(R.id.container_body) protected FrameLayout fragmentContainer;
    @Bind(R.id.aliPaylogo) protected ImageView aliPayLogo;

    @Nullable public MoneyUtils mMoneyUtils;
    public BigDecimal amount;
    public SmartPesaTransactionType transactionType;
    protected int fromAccount, toAccount;
    public String qrScannedData;
    public boolean isScan;
    public Lazy<ServiceManager> serviceManager;
    protected FragmentManager fragmentManager;
    Currency crytoCurrency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alipay_payment_progress);

        ButterKnife.bind(this);

        if (getMerchantComponent() == null) {
            logoutUser();
            return;
        }

        serviceManager = SmartPesaApplication.component(this).serviceManager();
        mMoneyUtils = getMerchantComponent().provideMoneyUtils();
        fragmentManager = getSupportFragmentManager();

        aliPayLogo.setImageResource(R.drawable.ic_crypto_menu);

        amount = new BigDecimal(this.getIntent().getDoubleExtra(SPConstants.AMOUNT, 0.00));
        transactionType = SmartPesaTransactionType.fromEnumId(this.getIntent().getIntExtra(SPConstants.TRANSACTION_TYPE, -1));
        fromAccount = this.getIntent().getIntExtra(SPConstants.FROM_ACCOUNT, 0);
        toAccount = this.getIntent().getIntExtra(SPConstants.TO_ACCOUNT, 0);
        qrScannedData = this.getIntent().getStringExtra(SPConstants.GO_COIN_SCANNED_QR);
        isScan = this.getIntent().getBooleanExtra(SPConstants.IS_GO_COIN_SCAN, false);
        crytoCurrency = (Currency) this.getIntent().getSerializableExtra(SPConstants.TRANSACTION_CURRENCY);

        String currency = "";
        if (getMerchantComponent().provideMerchant() != null) {
            currency = getMerchantComponent().provideMerchant().getCurrency().getCurrencySymbol();
        } else {
            logoutUser();
            return;
        }

        if (mMoneyUtils != null) {
            amountTv.setText(currency + " " + mMoneyUtils.format(amount));
        } else {
            logoutUser();
            return;
        }

        if (isScan) {
            transactionTypeTv.setText("Crypto Scan");
        } else {
            transactionTypeTv.setText("Crypto Payment");
        }

        processPayment();
    }

    public void processPayment() {

        SmartPesa.QrMode qrMode;

        transactionAnimation.show();

        if (isScan) {
            qrMode = SmartPesa.QrMode.SCAN;
        } else {
            qrMode = SmartPesa.QrMode.DISPLAY;
        }

        SmartPesa.QrTransactionParam.Builder builder = SmartPesa.TransactionParam.newBuilder(SmartPesa.QrPaymentProvider.CRYPTO)
                .transactionType(transactionType.getEnumId())
                .amount(this.amount)
                .currency(getMerchantComponent().provideMerchant().getCurrency())
                .qrMode(qrMode);

        if (crytoCurrency != null) {
            builder.cryptoCurrency(crytoCurrency);
        }

        if (isScan) {
            builder.qrScannedData(qrScannedData);
        }

        SmartPesa.TransactionParam param = builder.build();

        serviceManager.get().performTransaction(param, new QRTransactionCallback() {
            @Override
            public void onRequestDisplayQrCode(SmartPesa.QrPaymentProvider qrPaymentProvider, QrPayment qrPayment) {

                BigDecimal amount = qrPayment.getAmount();
                String currencySymbol = qrPayment.getCurrency().getCurrencySymbol();

                if (amount != null && currencySymbol != null) {

                    if (isActivityDestroyed()) return;
                    amountTv.setText(amount + " " + currencySymbol);

                }

                String qrString = qrPayment.getQrCodeString();
                if (!TextUtils.isEmpty(qrString)) {

                    qrString = qrString.replaceAll("^\"|\"$", "");

                    Bitmap myBitmap = QRCode.from(qrString).bitmap();
                    if (myBitmap != null) {
                        qrImageView.setImageBitmap(myBitmap);
                        transactionAnimation.hide();
                    }

                }
            }

            @Override
            public void onProgressTextUpdate(String s) {
                if (isActivityDestroyed()) return;
                progressTv.setText(s);
            }

            @Override
            public void onTransactionApproved(TransactionData transactionData) {
                if (isActivityDestroyed()) return;
                transactionAnimation.hide();

                Fragment fragment = null;

                if (transactionData != null && transactionData.getTransaction() != null && transactionData.getTransaction().getTransactionResult() != null) {
                    ParcelableTransactionResponse transactionResponse = new ParcelableTransactionResponse(transactionData.getTransaction().getTransactionResult());
                    fragment = GoCoinApprovedFragment.newInstance(true, null, transactionResponse);
                } else {
                    fragment = GoCoinApprovedFragment.newInstance(true, "The transaction result is empty", null);
                }

                showFragment(fragment);

            }

            @Override
            public void onTransactionDeclined(SpTransactionException e, TransactionData transactionData) {
                if (isActivityDestroyed()) return;
                transactionAnimation.hide();

                Fragment fragment = null;

                if (transactionData != null && transactionData.getTransaction() != null && transactionData.getTransaction().getTransactionResult() != null) {
                    ParcelableTransactionResponse transactionResponse = new ParcelableTransactionResponse(transactionData.getTransaction().getTransactionResult());
                    fragment = GoCoinApprovedFragment.newInstance(false, null, transactionResponse);
                } else {
                    if (e != null) {
                        fragment = GoCoinApprovedFragment.newInstance(false, e.getMessage(), null);
                    } else {
                        fragment = GoCoinApprovedFragment.newInstance(false, "Unknow transaction exception", null);
                    }
                }

                showFragment(fragment);
            }

            @Override
            public void onError(SpException exception) {
                if (isActivityDestroyed()) return;
                transactionAnimation.hide();

                Fragment fragment = null;

                if (exception != null) {
                    String failureReason = exception.getMessage();

                    if (exception instanceof SpSessionException) {
                        fragment = UnknownResultFragment.newInstance(exception, transactionType, amount, null);
                    } else {
                        fragment = GoCoinApprovedFragment.newInstance(false, failureReason, null);
                    }
                } else {
                    fragment = GoCoinApprovedFragment.newInstance(false, "Unknow transaction exception", null);
                }

                showFragment(fragment);
            }

            @Override
            public void onTransactionFinished(TransactionType transactionType, boolean b, @Nullable Transaction transaction, @Nullable SmartPesa.Verification verification, @Nullable SpCardTransactionException e) {

            }
        });
    }

    public void showFragment(Fragment fragment) {
        if (isActivityDestroyed()) return;

        if (fragment != null && fragmentManager != null) {
            fragmentManager.beginTransaction().replace(R.id.container_body, fragment).commitAllowingStateLoss();
            fragmentContainer.setVisibility(View.VISIBLE);
        }
    }
}
