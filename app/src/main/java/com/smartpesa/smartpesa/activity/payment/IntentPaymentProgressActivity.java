package com.smartpesa.smartpesa.activity.payment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.hanks.htextview.HTextView;
import com.skyfishjy.library.RippleBackground;
import com.smartpesa.intent.SpConnect;
import com.smartpesa.intent.TransactionArgument;
import com.smartpesa.intent.result.TransactionError;
import com.smartpesa.intent.result.TransactionException;
import com.smartpesa.intent.result.TransactionResult;
import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.activity.SplashActivity;
import com.smartpesa.smartpesa.activity.base.BaseActivity;
import com.smartpesa.smartpesa.fragment.dialog.BluetoothDialogFragment;
import com.smartpesa.smartpesa.fragment.dialog.TerminalDialogFragment;
import com.smartpesa.smartpesa.fragment.result.IntentResultFragment;
import com.smartpesa.smartpesa.helpers.Converter;
import com.smartpesa.smartpesa.helpers.PreferredTerminalUtils;
import com.smartpesa.smartpesa.helpers.UIHelper;
import com.smartpesa.smartpesa.models.ParcelableTransactionResponse;
import com.smartpesa.smartpesa.models.SmartPesaTransactionType;
import com.smartpesa.smartpesa.util.MoneyUtils;
import com.wang.avi.AVLoadingIndicatorView;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.Lazy;
import smartpesa.sdk.ServiceManager;
import smartpesa.sdk.SmartPesa;
import smartpesa.sdk.core.error.SpException;
import smartpesa.sdk.devices.SpTerminal;
import smartpesa.sdk.error.SpCardTransactionException;
import smartpesa.sdk.error.SpSessionException;
import smartpesa.sdk.error.SpTransactionException;
import smartpesa.sdk.interfaces.TransactionCallback;
import smartpesa.sdk.interfaces.TransactionData;
import smartpesa.sdk.models.currency.Currency;
import smartpesa.sdk.models.loyalty.Loyalty;
import smartpesa.sdk.models.loyalty.LoyaltyTransaction;
import smartpesa.sdk.models.merchant.TransactionType;
import smartpesa.sdk.models.merchant.VerifiedMerchantInfo;
import smartpesa.sdk.models.merchant.VerifyMerchantCallback;
import smartpesa.sdk.models.transaction.Balance;
import smartpesa.sdk.models.transaction.Card;
import smartpesa.sdk.models.transaction.Transaction;
import smartpesa.sdk.models.version.GetVersionCallback;
import smartpesa.sdk.models.version.Version;
import smartpesa.sdk.scanner.TerminalScanningCallback;
import timber.log.Timber;

public class IntentPaymentProgressActivity extends BaseActivity  {


    private static final int REQUEST_PERMISSION_BT_TRANSACTION = 8111;
    @BindView(R.id.progress_TV) protected TextView progressTV;
    @BindView(R.id.amount_Process_TV) protected TextView amountProcessTV;
    @BindView(R.id.transationTypeTV) protected TextView transactionTypeTV;
    @BindView(R.id.rippleBG) protected RippleBackground rippleBackground;
    @BindView(R.id.progress_IV) protected ImageView progressIV;
    @BindView(R.id.creditCardIV) protected ImageView topCreditCardIv;
    @BindView(R.id.bottomCreditCardIv) protected ImageView bottomCreditCardIv;
    @BindView(R.id.container_body) protected FrameLayout fragmentContainer;
    @BindView(R.id.try_again) protected Button tryAgainBtn;
    @BindView(R.id.goBackBtn) protected Button goBackBtn;
    @BindView(R.id.failBtnLl) protected LinearLayout failBtnLl;
    @BindView(R.id.bluetoothConnection) protected FrameLayout bluetoothConnection;
    @BindView(R.id.swipeOrInsert) protected FrameLayout swipeOrInsertFl;
    @BindView(R.id.confirmInputFl) protected FrameLayout confirmInputFl;
    @BindView(R.id.inputFl) protected FrameLayout pinInputFl;
    @BindView(R.id.transactionProcessingFl) protected FrameLayout transactionFl;
    @BindView(R.id.indicator1) protected AVLoadingIndicatorView iv1;
    @BindView(R.id.indicator2) protected AVLoadingIndicatorView iv2;
    @BindView(R.id.indicator3) protected AVLoadingIndicatorView iv3;
    @BindView(R.id.indicator4) protected AVLoadingIndicatorView iv4;
    @BindView(R.id.indicator5) protected AVLoadingIndicatorView iv5;
    @BindView(R.id.indicator6) protected AVLoadingIndicatorView iv6;
    @BindView(R.id.sendToBankIndicator) protected AVLoadingIndicatorView sendToBankIndicator;
    @BindView(R.id.receiveFromBankIndicator) protected AVLoadingIndicatorView receiveFromBankIndicator;
    @BindView(R.id.receiveFromBankSlowIndicator) protected AVLoadingIndicatorView receiveFromBankSlowIndicator;
    @BindView(R.id.timerText) protected HTextView timerText;
    @BindView(R.id.timerLabel) protected TextView timerLabelTv;

    protected Context mContext;
    @Inject
    Lazy<ServiceManager> serviceManager;
    protected boolean lockBackBTN = false;
    public BigDecimal amount;
    protected FragmentManager fragmentManager;
    protected int fromAccount, toAccount;
    public SmartPesaTransactionType transactionType;
    protected static final String BLUETOOTH_FRAGMENT_TAG = "bluetooth";
    @Nullable public MoneyUtils mMoneyUtils;
    PreferredTerminalUtils mPreferredTerminalUtils;
    public boolean isRetry;
    public final boolean[] transactionFinished = {false};
    public final boolean[] bluetoothDisconnected = {false};
    boolean chipOnly, isTimerRunning;
    private AVLoadingIndicatorView[] IMGS = {iv1, iv3, iv3, iv4, iv5, iv6};
    Thread timerThread;
    double tips;
    String externalReference;

    private TransactionArgument mTransactionArgument;
    String merchantCode, operatorCode, opertorPin;
    EditText merchantIdEt, operatorCodeEt, loginPinEt;
    ProgressDialog mProgressDialog;
    Intent requestIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_progress);
        ButterKnife.bind(this);
        isTimerRunning = false;

        mContext = IntentPaymentProgressActivity.this;
        SmartPesaApplication.component(this).inject(this);
        serviceManager = SmartPesaApplication.component(mContext).serviceManager();
        fragmentManager = getSupportFragmentManager();

        if (getMerchantComponent() == null) {
            askUserToLogin();
        } else {
            //call the UI components
            getIntentValues();
        }

    }

    private void askUserToLogin() {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(R.string.login_title)
                .customView(R.layout.dialog_intent_login, true)
                .positiveText(R.string.login_title)
                .negativeText(android.R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (isActivityDestroyed()) return;

                        merchantCode = merchantIdEt.getText().toString();
                        operatorCode = operatorCodeEt.getText().toString();
                        opertorPin = loginPinEt.getText().toString();

                        if (TextUtils.isEmpty(merchantCode) || TextUtils.isEmpty(operatorCode) || TextUtils.isEmpty(opertorPin)) {
                            UIHelper.showToast(IntentPaymentProgressActivity.this, "Enter valid credentials and try again.");
                        } else {
                            if (isActivityDestroyed()) return;
                            performGetVersion();
                        }
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (isActivityDestroyed()) return;
                        UIHelper.showToast(IntentPaymentProgressActivity.this, "Cannot proceed without login");
                        finish();
                    }
                })
                .build();


        merchantIdEt = (EditText) dialog.getCustomView().findViewById(R.id.merchant_id_tv);
        operatorCodeEt = (EditText) dialog.getCustomView().findViewById(R.id.operator_code_tv);
        loginPinEt = (EditText) dialog.getCustomView().findViewById(R.id.login_pin_tv);

        if (!TextUtils.isEmpty(merchantIdEt.getText().toString())) {
            loginPinEt.requestFocus();
        }

        dialog.show();
    }

    private void performGetVersion() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(R.string.app_name);
        mProgressDialog.setMessage("Please wait, verifying merchant..");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        serviceManager.get().getVersion(new GetVersionCallback() {
            @Override
            public void onSuccess(Version version) {
                if (isActivityDestroyed()) return;
                performLogin();
            }

            @Override
            public void onError(SpException exception) {
                if (isActivityDestroyed()) return;
                mProgressDialog.dismiss();
                UIHelper.showToast(IntentPaymentProgressActivity.this, exception.getMessage());
                finish();
            }
        });
    }

    private void performLogin() {
        //makes the verifyMerchant call in the SDK
        serviceManager.get().verifyMerchant(merchantCode, operatorCode, opertorPin, new VerifyMerchantCallback() {
            @Override
            public void onSuccess(VerifiedMerchantInfo verifiedMerchantInfo) {
                if (isActivityDestroyed()) return;
                mProgressDialog.dismiss();
                UIHelper.showToast(mContext, "Please wait, processing...");

                // init merchant module after successful login
                ((SmartPesaApplication) getApplication()).createMerchantComponent();
                getIntentValues();
            }

            @Override
            public void onError(SpException exception) {
                if (isActivityDestroyed()) return;
                mProgressDialog.dismiss();

                if (exception instanceof SpSessionException) {
                    //show the user that session expired
                    UIHelper.showToast(mContext, getString(R.string.session_expired));

                    //finish this activity and start the splash screen
                    finish();
                    startActivity(new Intent(mContext, SplashActivity.class));
                } else {
                    UIHelper.showToast(IntentPaymentProgressActivity.this, exception.getMessage());
                    finish();
                }
            }
        });
    }

    protected void getIntentValues() {

            requestIntent = getIntent();

            if (requestIntent.getBooleanExtra(SpConnect.KEY_GET_LAST_TRANSACTION, false)) {
                Transaction lastTransaction = serviceManager.get().getLastTransactionDetails();
                if (lastTransaction != null && lastTransaction.getTransactionResult() != null) {
                    TransactionResult result = Converter.from(lastTransaction.getTransactionResult());
                    Intent intent = new Intent();
                    SpConnect.populateResultIntent(requestIntent, intent, result);
                    setResult(RESULT_OK, intent);
                } else {
                    setResult(RESULT_CANCELED, null);
                }
                finish();
            } else {

                if (requestIntent == null) {
                    showTransactionError(amount, null, getString(R.string.error_argument_invalid));
                    return;
                }

                mTransactionArgument = SpConnect.parseTransactionArgumentData(requestIntent);

                if (mTransactionArgument == null) {
                    showTransactionError(amount, null, getString(R.string.error_argument_invalid));
                    return;
                }

                transactionType = Converter.from(mTransactionArgument.transactionType());

                if (transactionType == null) {
                    showTransactionError(amount, null, getString(R.string.transaction_null));
                    return;
                }

                if (!transactionType.equals(SmartPesaTransactionType.SALE)) {
                    showTransactionError(amount, null, getString(R.string.transaction_not_support));
                    return;
                }

                amount = mTransactionArgument.amount();

                if (amount == null) {
                    showTransactionError(amount, null, getString(R.string.amount_null));
                    return;
                }

                BigDecimal tip = mTransactionArgument.tip();

                if (getMerchantComponent() != null) {
                    fromAccount = getMerchantComponent().provideDefaultFromAccount();
                    toAccount = getMerchantComponent().provideDefaultToAccount();
                } else {
                    fromAccount = SmartPesa.AccountType.DEFAULT.getEnumId();
                    toAccount = SmartPesa.AccountType.DEFAULT.getEnumId();
                }

                externalReference = mTransactionArgument.externalReference();

                if (tip != null) {
                    tips = tip.doubleValue();
                }

                startTransaction();
            }
    }

    public void startTransaction() {

        mMoneyUtils = getMerchantComponent().provideMoneyUtils();

        tryAgainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRetry = true;
                startTerminalScan();
            }
        });

        goBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isActivityDestroyed()) return;
                finish();
            }
        });

        IMGS[0] = iv1;
        IMGS[1] = iv2;
        IMGS[2] = iv3;
        IMGS[3] = iv4;
        IMGS[4] = iv5;
        IMGS[5] = iv6;

        mPreferredTerminalUtils = new PreferredTerminalUtils(IntentPaymentProgressActivity.this, getMerchantComponent().provideMerchant().getMerchantCode(),
                getMerchantComponent().provideMerchant().getOperatorCode());

        transactionTypeTV.setText(UIHelper.getTitleFromTransactionType(mContext, transactionType));

        if (getMerchantComponent() != null && mMoneyUtils != null) {
            String currencySymbol = getMerchantComponent().provideMerchant().getCurrency().getCurrencySymbol();
            amountProcessTV.setText(currencySymbol + " " + mMoneyUtils.format(amount));
            startTerminalScan();
        } else {
            showTransactionError(amount, null, getString(R.string.session_expired));
        }
    }

    public void startTerminalScan() {

        transactionFinished[0] = false;
        bluetoothDisconnected[0] = false;

        rippleBackground.setVisibility(View.VISIBLE);
        rippleBackground.startRippleAnimation();
        bluetoothConnection.setVisibility(View.VISIBLE);

        progressTV.setText(getString(R.string.sp__enabling_bluetooth));
        progressIV.setVisibility(View.INVISIBLE);
        failBtnLl.setVisibility(View.INVISIBLE);
        swipeOrInsertFl.setVisibility(View.INVISIBLE);
        confirmInputFl.setVisibility(View.INVISIBLE);
        pinInputFl.setVisibility(View.INVISIBLE);

        sendToBankIndicator.setVisibility(View.INVISIBLE);
        receiveFromBankIndicator.setVisibility(View.INVISIBLE);
        receiveFromBankSlowIndicator.setVisibility(View.INVISIBLE);
        if (timerThread != null && timerThread.isAlive()) {
            timerThread.interrupt();
        }
        timerLabelTv.setVisibility(View.GONE);
        timerText.setVisibility(View.GONE);
        timerText.animateText("");
        isTimerRunning = false;

        //lock the back button
        lockBackBTN = true;
        chipOnly = false;

        String currencySymbol = getMerchantComponent().provideMerchant().getCurrency().getCurrencySymbol();
        //set the amount
        amountProcessTV.setText(currencySymbol + " " + mMoneyUtils.format(amount));
        transactionTypeTV.setText(UIHelper.getTitleFromTransactionType(mContext, transactionType));

        final boolean[] processCalled = {false};
        final boolean[] listDisplayed = {false};

        serviceManager.get().scanTerminal(new TerminalScanningCallback() {
            @Override
            public void onDeviceListRefresh(Collection<SpTerminal> collection) {
                if (isActivityDestroyed()) return;

                if (!listDisplayed[0]) {

                    SpTerminal spTerminal = mPreferredTerminalUtils.matches(collection);

                    if (spTerminal != null && !isRetry && !processCalled[0]) {

                        processCalled[0] = true;

                        processPayment(spTerminal);

                        if (serviceManager != null) {
                            serviceManager.get().stopScan();
                        }

                    } else {
                        displayBluetoothDevice(collection);
                        listDisplayed[0] = true;
                    }
                } else if (!processCalled[0]) {
                    displayBluetoothDevice(collection);
                }
            }

            @Override
            public void onScanStopped() {
                if (isActivityDestroyed()) return;

            }

            @Override
            public void onScanTimeout() {
                if (isActivityDestroyed()) return;

            }

            @Override
            public void onEnablingBluetooth(String s) {
                if (isActivityDestroyed()) return;
                progressTV.setText(s);

            }

            @Override
            public void onBluetoothPermissionDenied(String[] strings) {
                if (isActivityDestroyed()) return;
                ActivityCompat.requestPermissions(IntentPaymentProgressActivity.this,
                        strings,
                        REQUEST_PERMISSION_BT_TRANSACTION);
            }
        }, this);
    }

    //transaction with the SDK
    public void processPayment(SpTerminal terminal) {

        SmartPesa.AccountType fromAccountType = SmartPesa.AccountType.fromEnumId(fromAccount);
        SmartPesa.AccountType toAccountType = SmartPesa.AccountType.fromEnumId(toAccount);

        SmartPesa.TransactionParam param = buildTransactionParam(terminal, fromAccountType, toAccountType);

        if (param != null) {

            serviceManager.get().performTransaction(param, new TransactionCallback() {
                @Override
                public void onProgressTextUpdate(String s) {
                    if (isActivityDestroyed()) return;
                    Timber.d("onProgressTextUpdate %s", s);
                    progressTV.setText(s);
                }

                @Override
                public void onDeviceConnected(SpTerminal spTerminal) {
                    if (isActivityDestroyed()) return;
                    progressTV.setText("Connected to mPOS device\n (" + spTerminal.getName() + ")");
                }

                @Override
                public void onDeviceDisconnected(SpTerminal spTerminal) {
                    if (isActivityDestroyed()) return;

                    if (!transactionFinished[0]) {
                        rippleBackground.setVisibility(View.INVISIBLE);
                        bluetoothConnection.setVisibility(View.INVISIBLE);
                        progressIV.setVisibility(View.VISIBLE);
                        progressIV.setImageDrawable(getResources().getDrawable(R.drawable.ic_bluetooth_disconnected_icon));
                        throwUserOutside(getString(R.string.bluetooth_disconnected_help));
                        YoYo.with(Techniques.Shake)
                                .duration(1500)
                                .playOn(findViewById(R.id.progress_IV));
                        lockBackBTN = false;
                        bluetoothDisconnected[0] = true;
                        failBtnLl.setVisibility(View.VISIBLE);

                        swipeOrInsertFl.setVisibility(View.INVISIBLE);
                        sendToBankIndicator.setVisibility(View.INVISIBLE);
                        receiveFromBankIndicator.setVisibility(View.INVISIBLE);
                        receiveFromBankSlowIndicator.setVisibility(View.INVISIBLE);
                        if (timerThread != null && timerThread.isAlive()) {
                            timerThread.interrupt();
                        }
                        timerLabelTv.setVisibility(View.GONE);
                        timerText.setVisibility(View.GONE);
                        timerText.animateText("");
                        isTimerRunning = false;
                    }
                }

                @Override
                public void onBatteryStatus(SmartPesa.BatteryStatus batteryStatus) {
                    if (isActivityDestroyed()) return;
                    progressTV.setText(getText(R.string.battery_low));
                }

                @Override
                public void onShowSelectApplicationPrompt(List<String> list) {
                    if (isActivityDestroyed()) return;

                    if (list != null && !list.isEmpty() && list.size() != 0) {
                        new MaterialDialog.Builder(IntentPaymentProgressActivity.this)
                                .title(R.string.select_application)
                                .items(list)
                                .itemsCallback(new MaterialDialog.ListCallback() {
                                    @Override
                                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                        serviceManager.get().selectApplication(which);
                                    }
                                })
                                .show();
                    }
                }

                @Override
                public void onShowSelectTIDPrompt(List<String> list) {

                }

                @Override
                public void onWaitingForCard(String s, SmartPesa.CardMode cardMode) {
                    if (isActivityDestroyed()) return;

                    if (!chipOnly) {
                        rippleBackground.setVisibility(View.INVISIBLE);
                        bluetoothConnection.setVisibility(View.INVISIBLE);
                        progressIV.setVisibility(View.INVISIBLE);
                        progressTV.setText(s);
                        swipeOrInsertFl.setVisibility(View.VISIBLE);
                        animateCardL2R(false);
                    }
                }

                @Override
                public void onShowInsertChipAlertPrompt() {
                    if (isActivityDestroyed()) return;

                    //ask user to insert chip card
                    chipOnly = true;
                    rippleBackground.setVisibility(View.INVISIBLE);
                    bluetoothConnection.setVisibility(View.INVISIBLE);
                    progressIV.setVisibility(View.INVISIBLE);
                    progressTV.setText(R.string.sp__please_insert_card);
                    swipeOrInsertFl.setVisibility(View.VISIBLE);
                    animateCardL2R(true);
                }

                @Override
                public void onReadCard(Card card) {
                    if (isActivityDestroyed()) return;

                    //change the UI
                    rippleBackground.setVisibility(View.INVISIBLE);
                    bluetoothConnection.setVisibility(View.INVISIBLE);
                    swipeOrInsertFl.setVisibility(View.INVISIBLE);
                    confirmInputFl.setVisibility(View.INVISIBLE);
                    pinInputFl.setVisibility(View.INVISIBLE);
                    progressIV.setVisibility(View.INVISIBLE);

                    showTransactionProgress();
                }

                @Override
                public void onShowPinAlertPrompt(int i) {
                    if (isActivityDestroyed()) return;

                    progressTV.setText(R.string.sp__enter_pin);
                    confirmInputFl.setVisibility(View.INVISIBLE);
                    pinInputFl.setVisibility(View.VISIBLE);
                    rippleBackground.setVisibility(View.INVISIBLE);
                    bluetoothConnection.setVisibility(View.INVISIBLE);
                    transactionFl.setVisibility(View.INVISIBLE);
                    swipeOrInsertFl.setVisibility(View.INVISIBLE);

                    animatePinPad();
                }

                @Override
                public void onPinEntered(int i) {
                    if (isActivityDestroyed()) return;

                    //change the UI
                    rippleBackground.setVisibility(View.INVISIBLE);
                    bluetoothConnection.setVisibility(View.INVISIBLE);
                    swipeOrInsertFl.setVisibility(View.INVISIBLE);
                    confirmInputFl.setVisibility(View.INVISIBLE);
                    pinInputFl.setVisibility(View.INVISIBLE);
                    progressIV.setVisibility(View.INVISIBLE);

                    showTransactionProgress();
                }

                @Override
                public void onShowInputPrompt() {
                    if (isActivityDestroyed()) return;

                    progressTV.setText(R.string.input_account_number_hint);
                    rippleBackground.setVisibility(View.INVISIBLE);
                    bluetoothConnection.setVisibility(View.INVISIBLE);
                    transactionFl.setVisibility(View.INVISIBLE);
                    confirmInputFl.setVisibility(View.INVISIBLE);
                    pinInputFl.setVisibility(View.VISIBLE);
                    swipeOrInsertFl.setVisibility(View.INVISIBLE);
                    animatePinPad();
                }

                @Override
                public void onReturnInputStatus(final SmartPesa.InputStatus inputStatus, @Nullable final String s) {
                    if (isActivityDestroyed()) return;

                    if (inputStatus == SmartPesa.InputStatus.ENTERED) {
                        if (!TextUtils.isEmpty(s)) {
                            int stringLength = s.length();
                            String masked = s;
                            if (stringLength > 4) {
                                masked = UIHelper.maskString(s, "X", 4);
                            }

                            UIHelper.showMessageDialogWithTitleTwoButtonCallback(
                                    mContext,
                                    mContext.getString(R.string.app_name),
                                    mContext.getString(R.string.confirm_input, masked),
                                    mContext.getString(R.string.yes),
                                    mContext.getString(R.string.no),
                                    new MaterialDialog.ButtonCallback() {
                                        @Override
                                        public void onPositive(MaterialDialog dialog) {
                                            super.onPositive(dialog);
                                            serviceManager.get().confirmInput(s);
                                        }

                                        @Override
                                        public void onNegative(MaterialDialog dialog) {
                                            super.onNegative(dialog);
                                            serviceManager.get().confirmInput(null);
                                        }
                                    }

                            );
                        } else {
                            serviceManager.get().confirmInput(null);
                        }
                    }
                }

                @Override
                public void onShowConfirmAmountPrompt() {
                    if (isActivityDestroyed()) return;

                    rippleBackground.setVisibility(View.INVISIBLE);
                    bluetoothConnection.setVisibility(View.INVISIBLE);
                    swipeOrInsertFl.setVisibility(View.INVISIBLE);
                    transactionFl.setVisibility(View.INVISIBLE);
                    progressTV.setText("Please confirm amount");
                    confirmInputFl.setVisibility(View.VISIBLE);
                    pinInputFl.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAmountConfirmed(boolean b) {
                    if (isActivityDestroyed()) return;
                    rippleBackground.setVisibility(View.INVISIBLE);
                    bluetoothConnection.setVisibility(View.INVISIBLE);
                    progressIV.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onTransactionFinished(TransactionType transactionType, boolean b, @Nullable Transaction transaction, @Nullable SmartPesa.Verification verification, @Nullable SpCardTransactionException e) {
                    if (isActivityDestroyed()) return;
                    if (bluetoothDisconnected[0] == false) {
                        progressTV.setVisibility(View.INVISIBLE);

                        transactionFinished[0] = true;
                        lockBackBTN = false;

                        rippleBackground.setVisibility(View.INVISIBLE);
                        bluetoothConnection.setVisibility(View.INVISIBLE);
                        swipeOrInsertFl.setVisibility(View.INVISIBLE);
                        transactionFl.setVisibility(View.INVISIBLE);
                        confirmInputFl.setVisibility(View.INVISIBLE);
                        pinInputFl.setVisibility(View.INVISIBLE);
                        failBtnLl.setVisibility(View.INVISIBLE);
                    }
                }

                @Override
                public void onTransactionApproved(TransactionData transactionData) {
                    if (isActivityDestroyed()) return;
                    if (bluetoothDisconnected[0] == false) {
                            final Intent intent = new Intent();
                            com.smartpesa.intent.result.TransactionResult result = null;

                            // Extract transaction result from SDK and convert to Parcelable Result
                            if (transactionData != null && transactionData.getTransaction() != null && transactionData.getTransaction().getTransactionResult() != null) {
                                result = Converter.from(transactionData.getTransaction().getTransactionResult());
                            }

                            ParcelableTransactionResponse parcelableTransactionResponse = null;

                            if (transactionData != null && transactionData.getTransaction() != null && transactionData.getTransaction().getTransactionResult() != null) {
                                parcelableTransactionResponse = new ParcelableTransactionResponse(transactionData.getTransaction().getTransactionResult());
                            }

                            //populate without signature
                            final int activityResult = RESULT_OK;
                            // Set activity result
                            SpConnect.populateResultIntent(requestIntent, intent, result);
                            setResult(activityResult, intent);
                            showTransactionSuccess(amount, parcelableTransactionResponse);
                    }
                }

                @Override
                public void onTransactionDeclined(SpTransactionException e, TransactionData transactionData) {
                    if (isActivityDestroyed()) return;
                    if (bluetoothDisconnected[0] == false) {
                            //handle transaction error
                            final Intent intent = new Intent();
                            com.smartpesa.intent.result.TransactionResult result = null;

                            // Extract transaction result from SDK and convert to Parcelable Result
                            if (transactionData != null && transactionData.getTransaction() != null && transactionData.getTransaction().getTransactionResult() != null) {
                                result = Converter.from(transactionData.getTransaction().getTransactionResult());
                            }

                            ParcelableTransactionResponse parcelableTransactionResponse = null;

                            if (transactionData != null && transactionData.getTransaction() != null && transactionData.getTransaction().getTransactionResult() != null) {
                                parcelableTransactionResponse = new ParcelableTransactionResponse(transactionData.getTransaction().getTransactionResult());
                            }

                            // Attach error to the intent
                            TransactionException error = Converter.from(e);
                            TransactionError transactionError = TransactionError.builder()
                                    .transactionException(error)
                                    .transactionResult(result)
                                    .build();

                            final int[] activityResult = {RESULT_CANCELED};
                            // Set activity result
                            SpConnect.populateErrorIntent(requestIntent, intent, transactionError);
                            setResult(activityResult[0], intent);
                            showTransactionError(amount, parcelableTransactionResponse, e.getMessage());
                    }
                }


                @Override
                public void onError(SpException exception) {
                    if (isActivityDestroyed()) return;
                        if (bluetoothDisconnected[0] == false) {
                                transactionFinished[0] = true;

                                Intent intent = new Intent();

                                TransactionError transactionError = TransactionError.builder()
                                        .transactionException(Converter.from(exception))
                                        .transactionResult(null)
                                        .build();

                                SpConnect.populateErrorIntent(requestIntent, intent, transactionError);
                                setResult(RESULT_CANCELED, intent);
                                showTransactionError(amount, null, exception.getMessage());
                        }
                }

                @Override
                public void onStartPostProcessing(String s, Transaction transaction) {

                }

                @Override
                public void onReturnLoyaltyBalance(Loyalty loyalty) {

                }

                @Override
                public void onShowLoyaltyRedeemablePrompt(LoyaltyTransaction loyaltyTransaction) {

                }

                @Override
                public void onLoyaltyCancelled() {

                }

                @Override
                public void onLoyaltyApplied(LoyaltyTransaction loyaltyTransaction) {

                }

                @Override
                public void onShowConfirmFeePrompt(TransactionType.FeeChargeType feeChargeType, Currency currency, BigDecimal feeAmount, BigDecimal finalAmount) {
                    if(isActivityDestroyed()) return;

                    String total = currency.getCurrencySymbol()+" "+mMoneyUtils.format(finalAmount);

                    UIHelper.showMessageDialogWithTitleTwoButtonCallback(IntentPaymentProgressActivity.this,
                            getString(R.string.app_name),
                            "Total amount is " + total,
                            "Accept",
                            "Decline",
                            new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    super.onPositive(dialog);
                                    serviceManager.get().confirmFee(true);
                                }

                                @Override
                                public void onNegative(MaterialDialog dialog) {
                                    super.onNegative(dialog);
                                    serviceManager.get().confirmFee(false);
                                }
                            });
                }

                @Override
                public void onRequestForInput() {

                }

                @Override
                public void onShowBalance(Balance balance) {

                }

                @Override
                public void onShowPinPass(String s) {

                }
            }, this);

        } else {
            showTransactionError(amount, null, "Transaction type cannot be null");
            return;
        }
    }

    public SmartPesa.TransactionParam buildTransactionParam(SpTerminal terminal, SmartPesa.AccountType fromAccountType, SmartPesa.AccountType toAccountType) {

        if (transactionType != null) {
            SmartPesa.TerminalTransactionParam.Builder builder = SmartPesa.TransactionParam.newBuilder(terminal)
                    .transactionType(transactionType.getEnumId())
                    .terminal(terminal)
                    .amount(amount)
                    .from(fromAccountType)
                    .to(toAccountType);

            if (tips != 0.00) {
                builder.tip(mMoneyUtils.parseBigDecimal(tips));
            }

            if (!TextUtils.isEmpty(externalReference)) {
                builder.externalReference(externalReference);
            }

            SmartPesa.TransactionParam param = builder.build();

            return param;
        } else {
            return null;
        }
    }

    private void showTransactionSuccess(BigDecimal amount, ParcelableTransactionResponse transactionResponse) {
        if (!isActivityDestroyed()) {
            Fragment fragment = IntentResultFragment.newInstance(true, amount, transactionResponse, null);
            if (fragmentManager != null) {
                fragmentManager.beginTransaction().replace(R.id.container_body, fragment).commit();
                fragmentContainer.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showTransactionError(BigDecimal amount, ParcelableTransactionResponse transactionResponse, String errorMessage) {
        if (!isActivityDestroyed()) {
            Fragment fragment = IntentResultFragment.newInstance(false, amount, transactionResponse, errorMessage);
            if (fragmentManager != null) {
                fragmentManager.beginTransaction().replace(R.id.container_body, fragment).commit();
                fragmentContainer.setVisibility(View.VISIBLE);
            }
        }
    }

    //UI for transaction animation
    public void showTransactionProgress() {
        if (isActivityDestroyed()) return;

        transactionFl.setVisibility(View.VISIBLE);
        sendToBankIndicator.setVisibility(View.VISIBLE);
        receiveFromBankIndicator.setVisibility(View.INVISIBLE);
        receiveFromBankSlowIndicator.setVisibility(View.INVISIBLE);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendToBankIndicator.setVisibility(View.INVISIBLE);
                receiveFromBankIndicator.setVisibility(View.VISIBLE);
                receiveFromBankSlowIndicator.setVisibility(View.INVISIBLE);
            }
        }, 4000);

        final Handler handler1 = new Handler();
        handler1.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isTimerRunning == false) {
                    isTimerRunning = true;
                    sendToBankIndicator.setVisibility(View.INVISIBLE);
                    receiveFromBankIndicator.setVisibility(View.INVISIBLE);
                    receiveFromBankSlowIndicator.setVisibility(View.VISIBLE);

                    final int[] startTime = {75};

                    timerThread = new Thread() {
                        @Override
                        public void run() {
                            try {
                                while (!isInterrupted()) {
                                    Thread.sleep(1000);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (startTime[0] == 0) {
                                                timerLabelTv.setVisibility(View.GONE);
                                                timerText.setVisibility(View.GONE);
                                            } else {
                                                timerText.setVisibility(View.VISIBLE);
                                                timerLabelTv.setVisibility(View.VISIBLE);
                                                timerText.animateText("" + startTime[0]);
                                                startTime[0] = startTime[0] - 1;
                                            }
                                        }
                                    });
                                }
                            } catch (InterruptedException e) {
                            }
                        }
                    };
                    timerThread.start();
                }
            }
        }, 15000);
    }

    public void animateCardL2R(final boolean isChip) {
        if (isActivityDestroyed()) return;

        if (isChip) {
            topCreditCardIv.setVisibility(View.INVISIBLE);
            animateCardB2T(isChip);
        } else {
            TranslateAnimation animation = new TranslateAnimation(0.0f, 500.0f,
                    0.0f, 0.0f);
            animation.setDuration(2000);
            animation.setRepeatMode(2);
            animation.setFillAfter(false);
            animation.setRepeatCount(0);

            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    topCreditCardIv.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    topCreditCardIv.setVisibility(View.GONE);
                    animateCardB2T(isChip);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            topCreditCardIv.startAnimation(animation);
        }
    }

    public void animatePinPad() {
        if (isActivityDestroyed()) return;

        Random random = new Random();
        final int rndIndex = random.nextInt(IMGS.length);
        IMGS[rndIndex].setVisibility(View.VISIBLE);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                IMGS[rndIndex].setVisibility(View.INVISIBLE);
                animatePinPad();
            }
        }, 1000);
    }

    private void animateCardB2T(final boolean isChipAlone) {
        if (isActivityDestroyed()) return;

        TranslateAnimation tAnimation = new TranslateAnimation(0, 0, 0, -100);
        tAnimation.setDuration(2000);
        tAnimation.setRepeatCount(0);
        tAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        tAnimation.setFillAfter(false);
        tAnimation.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                bottomCreditCardIv.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                bottomCreditCardIv.setVisibility(View.GONE);
                animateCardL2R(isChipAlone);
            }
        });

        bottomCreditCardIv.setAnimation(tAnimation);

    }

    //bluetooth device list display
    public void displayBluetoothDevice(Collection<SpTerminal> devices) {
        if (isActivityDestroyed()) return;

        TerminalDialogFragment dialog;
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(BLUETOOTH_FRAGMENT_TAG);
        if (fragment == null) {
            dialog = new TerminalDialogFragment();
            if (!isActivityPaused()) {
                dialog.show(getSupportFragmentManager(), BLUETOOTH_FRAGMENT_TAG);
            } else {
                return;
            }
        } else {
            dialog = (TerminalDialogFragment) fragment;
        }
        dialog.setCancelable(false);
        dialog.setSelectedListener(new TerminalSelectedListenerImpl());
        dialog.updateDevices(devices);
    }

    //on device selected from list
    protected class TerminalSelectedListenerImpl implements BluetoothDialogFragment.TerminalSelectedListener<SpTerminal> {
        @Override
        public void onSelected(SpTerminal device) {
            processPayment(device);
            if (serviceManager != null) {
                serviceManager.get().stopScan();
            }
        }

        @Override
        public void onCancelled() {

        }
    }

    protected void throwUserOutside(String helpText) {
        progressTV.setText(helpText);
    }

    @Override
    public void onBackPressed() {
        if (!lockBackBTN) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceManager != null) {
            serviceManager.get().stopScan();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION_BT_TRANSACTION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (isActivityDestroyed()) return;
                            startTerminalScan();
                        }
                    }, 300);
                } else {
                    Toast.makeText(this, R.string.bt_permission_denied, Toast.LENGTH_SHORT).show();
                    if (isActivityDestroyed()) return;
                    finish();
                }
        }
    }
}
