package com.smartpesa.smartpesa.activity.payment;

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
import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.activity.SplashActivity;
import com.smartpesa.smartpesa.activity.base.BaseActivity;
import com.smartpesa.smartpesa.fragment.dialog.BluetoothDialogFragment;
import com.smartpesa.smartpesa.fragment.dialog.TerminalDialogFragment;
import com.smartpesa.smartpesa.fragment.dialog.TransactionResultDialogFragment;
import com.smartpesa.smartpesa.fragment.history.IntentLastTransactionFragment;
import com.smartpesa.smartpesa.fragment.result.FailedResultFragment;
import com.smartpesa.smartpesa.helpers.Converter;
import com.smartpesa.smartpesa.helpers.PreferenceHelper;
import com.smartpesa.smartpesa.helpers.PreferredTerminalUtils;
import com.smartpesa.smartpesa.helpers.UIHelper;
import com.smartpesa.smartpesa.models.SmartPesaTransactionType;
import com.smartpesa.smartpesa.util.DateUtils;
import com.smartpesa.smartpesa.util.MoneyUtils;
import com.smartpesa.smartpesa.util.constants.PreferenceConstants;
import com.wang.avi.AVLoadingIndicatorView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.inject.Inject;

import butterknife.Bind;
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

public class IntentPaymentProgressActivity extends BaseActivity {

    private static final int REQUEST_PERMISSION_BT_TRANSACTION = 8111;
    private static final String TIPS_DIALOG_TAG = "tipsDialog";
    @Bind(R.id.progress_TV) protected TextView progressTV;
    @Bind(R.id.amount_Process_TV) protected TextView amountProcessTV;
    @Bind(R.id.transationTypeTV) protected TextView transactionTypeTV;
    @Bind(R.id.rippleBG) protected RippleBackground rippleBackground;
    @Bind(R.id.progress_IV) protected ImageView progressIV;
    @Bind(R.id.creditCardIV) protected ImageView topCreditCardIv;
    @Bind(R.id.bottomCreditCardIv) protected ImageView bottomCreditCardIv;
    @Bind(R.id.container_body) protected FrameLayout fragmentContainer;
    @Bind(R.id.try_again) protected Button tryAgainBtn;
    @Bind(R.id.goBackBtn) protected Button goBackBtn;
    @Bind(R.id.failBtnLl) protected LinearLayout failBtnLl;
    @Bind(R.id.bluetoothConnection) protected FrameLayout bluetoothConnection;
    @Bind(R.id.swipeOrInsert) protected FrameLayout swipeOrInsertFl;
    @Bind(R.id.confirmInputFl) protected FrameLayout confirmInputFl;
    @Bind(R.id.inputFl) protected FrameLayout pinInputFl;
    @Bind(R.id.transactionProcessingFl) protected FrameLayout transactionFl;
    @Bind(R.id.indicator1) protected AVLoadingIndicatorView iv1;
    @Bind(R.id.indicator2) protected AVLoadingIndicatorView iv2;
    @Bind(R.id.indicator3) protected AVLoadingIndicatorView iv3;
    @Bind(R.id.indicator4) protected AVLoadingIndicatorView iv4;
    @Bind(R.id.indicator5) protected AVLoadingIndicatorView iv5;
    @Bind(R.id.indicator6) protected AVLoadingIndicatorView iv6;
    @Bind(R.id.sendToBankIndicator) protected AVLoadingIndicatorView sendToBankIndicator;
    @Bind(R.id.receiveFromBankIndicator) protected AVLoadingIndicatorView receiveFromBankIndicator;
    @Bind(R.id.receiveFromBankSlowIndicator) protected AVLoadingIndicatorView receiveFromBankSlowIndicator;
    @Bind(R.id.timerText) protected HTextView timerText;
    @Bind(R.id.timerLabel) protected TextView timerLabelTv;

    protected Context mContext;
    @Inject
    Lazy<ServiceManager> serviceManager;
    protected boolean lockBackBTN = false;
    public boolean transactionIsSuccessful;
    public String failureReason;
    public BigDecimal amount;
    protected FragmentManager fragmentManager;
    public String verification;
    public SmartPesa.Verification transactionVerification;
    protected double cashBackAmount;
    protected int fromAccount, toAccount;
    protected String description;
    public SmartPesaTransactionType transactionType;
    protected static final String BLUETOOTH_FRAGMENT_TAG = "bluetooth";
    @Nullable public MoneyUtils mMoneyUtils;
    PreferredTerminalUtils mPreferredTerminalUtils;
    public boolean isRetry;
    public final boolean[] transactionFinished = {false};
    public final boolean[] bluetoothDisconnected = {false};
    public final boolean[] bluetoothTimedOut = {false};
    boolean isActivityDestroyed, chipOnly, isTimerRunning;
    private AVLoadingIndicatorView[] IMGS = {iv1, iv3, iv3, iv4, iv5, iv6};
    Thread t;
    double tips, sales_tax, sales_tax2;
    String taxId;
    UUID instalmentUUID;
    private BigDecimal mConvenienceFee;

    private TransactionArgument mTransactionArgument;
    private static final String TRANSACTION_RESULT_DIALOG_TAG = "transaction_result";
    PreferenceHelper mPreferenceHelper;
    String merchantCode, operatorCode, opertorPin;
    EditText merchantIdEt, operatorCodeEt, loginPinEt;
    ProgressDialog mProgressDialog;
    Intent requestIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_progress);
        ButterKnife.bind(this);
        isActivityDestroyed = false;
        isTimerRunning = false;

        mContext = IntentPaymentProgressActivity.this;
        SmartPesaApplication.component(this).inject(this);
        serviceManager = SmartPesaApplication.component(mContext).serviceManager();
        fragmentManager = getSupportFragmentManager();

        if (getMerchantComponent() == null) {
            askUserToLogin();
        } else {
            startTransaction();
        }

    }

    private void startTransaction() {
        requestIntent = getIntent();
        if (requestIntent == null) {
            TransactionException e = new TransactionException(getString(R.string.error_argument_invalid), getString(R.string.error_argument_invalid_caption), "");
            showError(e);
            return;
        }

        mTransactionArgument = SpConnect.parseTransactionArgumentData(requestIntent);
        amount = mTransactionArgument.amount();
        transactionType = Converter.from(mTransactionArgument.transactionType());
        fromAccount = SmartPesa.AccountType.DEFAULT.getEnumId();
        toAccount = SmartPesa.AccountType.DEFAULT.getEnumId();
        cashBackAmount = 0.00;

        mMoneyUtils = getMerchantComponent().provideMoneyUtils();
        //set fonts here
        UIHelper fonts = new UIHelper(mContext);

        progressTV.setTypeface(fonts.regularFont);
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
                finish();
            }
        });
        //get the amount from the intent
        IMGS[0] = iv1;
        IMGS[1] = iv2;
        IMGS[2] = iv3;
        IMGS[3] = iv4;
        IMGS[4] = iv5;
        IMGS[5] = iv6;

        mPreferredTerminalUtils = new PreferredTerminalUtils(IntentPaymentProgressActivity.this, getMerchantComponent().provideMerchant().getMerchantCode(),
                getMerchantComponent().provideMerchant().getOperatorCode());

        transactionTypeTV.setText(UIHelper.getTitleFromTransactionType(mContext, transactionType));

        String currencySymbol = getMerchantComponent().provideMerchant().getCurrency().getCurrencySymbol();
        amountProcessTV.setText(currencySymbol + " " + mMoneyUtils.format(amount));

        if (transactionType.equals(SmartPesaTransactionType.BALANCE_INQUIRY)) {
            amountProcessTV.setVisibility(View.INVISIBLE);
        }

        //call the process payment once the activity is started
        startTerminalScan();
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
                        merchantCode = merchantIdEt.getText().toString();
                        operatorCode = operatorCodeEt.getText().toString();
                        opertorPin = loginPinEt.getText().toString();
                        if (TextUtils.isEmpty(merchantCode) || TextUtils.isEmpty(operatorCode) || TextUtils.isEmpty(opertorPin)) {
                            UIHelper.showToast(IntentPaymentProgressActivity.this, "Enter valid credentials and try again.");
                        } else {
                            performGetVersion();
                        }
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        UIHelper.showToast(IntentPaymentProgressActivity.this, "Cannot proceed without login");
                        finish();
                    }
                })
                .build();

        mPreferenceHelper = SmartPesaApplication.component(this).preferenceHelper();

        merchantIdEt = (EditText) dialog.getCustomView().findViewById(R.id.merchant_id_tv);
        operatorCodeEt = (EditText) dialog.getCustomView().findViewById(R.id.operator_code_tv);
        loginPinEt = (EditText) dialog.getCustomView().findViewById(R.id.login_pin_tv);

        merchantIdEt.setText(mPreferenceHelper.getString(PreferenceConstants.KEY_MERCHANT_CODE));
        operatorCodeEt.setText(mPreferenceHelper.getString(PreferenceConstants.KEY_OPERATOR_CODE));

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
                UIHelper.showToast(mContext, "Please wait, starting transaction...");
                mPreferenceHelper.putString(PreferenceConstants.KEY_MERCHANT_CODE, merchantCode);
                mPreferenceHelper.putString(PreferenceConstants.KEY_OPERATOR_CODE, operatorCode);

                //save the time logged in to Shared preference for UI version check
                Date d = new Date();
                String tempDate = DateUtils.format(d);
                mPreferenceHelper.putString(PreferenceConstants.KEY_LAST_LOGIN_TIME, tempDate);

                // init merchant module after successful login
                ((SmartPesaApplication) getApplication()).createMerchantComponent();
                startTransaction();
            }

            @Override
            public void onError(SpException exception) {
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

    public void startTerminalScan() {

        transactionFinished[0] = false;
        bluetoothDisconnected[0] = false;
        bluetoothTimedOut[0] = false;

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
        if (t != null && t.isAlive()) {
            t.interrupt();
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

        serviceManager.get().scanTerminal(new TerminalScanningCallback() {
            @Override
            public void onDeviceListRefresh(Collection<SpTerminal> collection) {
                if (isActivityDestroyed()) return;

                SpTerminal spTerminal = mPreferredTerminalUtils.matches(collection);
                if (spTerminal != null && !isRetry) {
                    processPayment(spTerminal);
                } else {
                    displayBluetoothDevice(collection);
                }
            }

            @Override
            public void onScanStopped() {
                if (isActivityDestroyed()) return;

                //stop the ripple animation
                rippleBackground.setVisibility(View.INVISIBLE);
                bluetoothConnection.setVisibility(View.INVISIBLE);
                swipeOrInsertFl.setVisibility(View.INVISIBLE);
                confirmInputFl.setVisibility(View.INVISIBLE);
                pinInputFl.setVisibility(View.INVISIBLE);

                sendToBankIndicator.setVisibility(View.INVISIBLE);
                receiveFromBankIndicator.setVisibility(View.INVISIBLE);
                receiveFromBankSlowIndicator.setVisibility(View.INVISIBLE);
                if (t != null && t.isAlive()) {
                    t.interrupt();
                }
                timerLabelTv.setVisibility(View.GONE);
                timerText.setVisibility(View.GONE);
                timerText.animateText("");
                isTimerRunning = false;

                swipeOrInsertFl.setVisibility(View.INVISIBLE);

                //hide the loading animation
                progressIV.setVisibility(View.INVISIBLE);
                throwUserOutside(getString(R.string.bluetooth_scan_stopped_help));
                lockBackBTN = false;
                failBtnLl.setVisibility(View.VISIBLE);
            }

            @Override
            public void onScanTimeout() {
                if (isActivityDestroyed()) return;
            }

            @Override
            public void onEnablingBluetooth(String s) {
                progressTV.setText(s);
            }

            @Override
            public void onBluetoothPermissionDenied(String[] permissions) {
                ActivityCompat.requestPermissions(IntentPaymentProgressActivity.this,
                        permissions,
                        REQUEST_PERMISSION_BT_TRANSACTION);
            }
        });
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
                            startTerminalScan();
                        }
                    }, 300);
                } else {
                    Toast.makeText(this, R.string.bt_permission_denied, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (serviceManager != null) {
            serviceManager.get().stopScan();
        }
    }

    public SmartPesa.TransactionParam buildTransactionParam(SpTerminal terminal, SmartPesa.AccountType fromAccountType, SmartPesa.AccountType toAccountType, HashMap<String, Object> config) {

        SmartPesaTransactionType transactType = Converter.from(mTransactionArgument.transactionType());

        SmartPesa.TerminalTransactionParam.Builder builder = SmartPesa.TransactionParam.newBuilder(terminal)
                .transactionType(transactType.getEnumId())
                .terminal(terminal)
                .amount(amount)
                .from(fromAccountType)
                .to(toAccountType)
                .extraParams(config)
                .cashBack(mMoneyUtils.parseBigDecimal(cashBackAmount));

        if (instalmentUUID != null) {
            builder.withInstalmentId(instalmentUUID);
        }

        if (sales_tax != 0.00) {
            builder.salesTaxInfo("IVA", mMoneyUtils.parseBigDecimal(sales_tax));
        }

        if (sales_tax2 != 0.00) {
            builder.salesTaxInfo2("IAC", mMoneyUtils.parseBigDecimal(sales_tax2));
        }

        if (tips != 0.00) {
            builder.tip(mMoneyUtils.parseBigDecimal(tips));
        }

        SmartPesa.TransactionParam param = builder.build();
        return param;
    }

    //initialise the transaction with the SDK
    public void processPayment(SpTerminal terminal) {

        SmartPesa.AccountType fromAccountType = SmartPesa.AccountType.fromEnumId(fromAccount);
        SmartPesa.AccountType toAccountType = SmartPesa.AccountType.fromEnumId(toAccount);
        final HashMap<String, Object> config = new HashMap<>();
        buildConfig(config);
        UIHelper.log(config.toString());

        SmartPesa.TransactionParam param = buildTransactionParam(terminal, fromAccountType, toAccountType, config);

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
                    if (t != null && t.isAlive()) {
                        t.interrupt();
                    }
                    timerLabelTv.setVisibility(View.GONE);
                    timerText.setVisibility(View.GONE);
                    timerText.animateText("");
                    isTimerRunning = false;
                }

                if (!transactionIsSuccessful) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Find approved fragment
                            Fragment fragmentByTag = getSupportFragmentManager().findFragmentById(R.id.container_body);
                            if (fragmentByTag != null && fragmentByTag instanceof FailedResultFragment) {
                                ((FailedResultFragment) fragmentByTag).canRetry();
                            }
                        }
                    }, 300);
                }
            }

            @Override
            public void onBatteryStatus(SmartPesa.BatteryStatus batteryStatus) {

            }

            @Override
            public void onShowSelectApplicationPrompt(List<String> list) {
                if (isActivityDestroyed) return;
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
            public void onWaitingForCard(String s, SmartPesa.CardMode cardMode) {
                if (isActivityDestroyed) return;
                if (!chipOnly) {
                    rippleBackground.setVisibility(View.INVISIBLE);
                    bluetoothConnection.setVisibility(View.INVISIBLE);
                    progressIV.setVisibility(View.INVISIBLE);
                    progressTV.setText(R.string.sp__please_swipe_or_insert);
                    swipeOrInsertFl.setVisibility(View.VISIBLE);
                    animateCardL2R(false);
                }
            }

            @Override
            public void onShowInsertChipAlertPrompt() {
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
                if (isActivityDestroyed) return;
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
            public void onShowPinAlertPrompt() {
                if (isActivityDestroyed) return;

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
            public void onPinEntered() {
                if (isActivityDestroyed) return;
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
                if (isActivityDestroyed) return;
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
                if (isActivityDestroyed) return;

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
                if (isActivityDestroyed) return;

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
                if (isActivityDestroyed) return;
                rippleBackground.setVisibility(View.INVISIBLE);
                bluetoothConnection.setVisibility(View.INVISIBLE);
                progressIV.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onTransactionFinished(TransactionType transactionType, boolean b, @Nullable Transaction transaction, @Nullable SmartPesa.Verification verification, @Nullable SpCardTransactionException e) {

            }

            @Override
            public void onTransactionApproved(TransactionData transactionData) {

//                if (isActivityDestroyed) return;
//
//                if (transactionData != null && transactionData.getTransaction() != null
//                        && transactionData.getTransaction().getTransactionResult() != null && transactionData.getTransaction().getTransactionResult().getPayment() != null) {
//
//                    TransactionResult transactionResult = transactionData.getTransaction().getTransactionResult();
//
//                    CardPayment cardPayment = (CardPayment) transactionResult.getPayment();
//
//                    if (cardPayment.getVerification() != null) {
//
//                        if (cardPayment.getVerification().equals(SmartPesa.Verification.SIGNATURE)) {
//                            FragmentManager fragmentManager = getSupportFragmentManager();
//                            GetSignatureDialogFragment signDialog = GetSignatureDialogFragment.newInstance(transactionResult.getTransactionId(), cardPayment.getCardHolderName());
//                            signDialog.setCancelable(false);
//                            signDialog.show(fragmentManager, "dialog_get_signature");
//
//                            fragmentManager.executePendingTransactions();
//                            final com.smartpesa.intent.result.TransactionResult finalResult = transactionResult;
//                            signDialog.getDialog().setOnDismissListener(new DialogInterface.OnDismissListener() {
//                                @Override
//                                public void onDismiss(DialogInterface dialogInterface) {
//                                    activityResult[0] = RESULT_OK;
//                                    tf[0] = TransactionResultDialogFragment.newInstance(finalResult);
//                                    // Set activity result
//                                    setResult(activityResult[0], intent);
//                                    showLastTransaction();
//
//                                    SpConnect.populateResultIntent(requestIntent, intent, finalResult);
//                                }
//                            });
//                        }
//
//                    }
//                }
            }

            @Override
            public void onTransactionDeclined(SpTransactionException e, TransactionData transactionData) {

                //// TODO: 12/3/18

            }

            //// TODO: 12/3/18

//            @Override
//            public void onTransactionFinished(TransactionType transactionType, boolean isSuccess, @Nullable Transaction transaction, @Nullable SmartPesa.Verification verification, @Nullable SpTransactionException exception) {
//                if (isActivityDestroyed) return;
//                progressTV.setVisibility(View.INVISIBLE);
//
//                transactionFinished[0] = true;
//                lockBackBTN = false;
//
//                rippleBackground.setVisibility(View.INVISIBLE);
//                bluetoothConnection.setVisibility(View.INVISIBLE);
//                swipeOrInsertFl.setVisibility(View.INVISIBLE);
//                transactionFl.setVisibility(View.INVISIBLE);
//                confirmInputFl.setVisibility(View.INVISIBLE);
//                pinInputFl.setVisibility(View.INVISIBLE);
//                failBtnLl.setVisibility(View.INVISIBLE);
//
//                // Prepare results
//                final Intent intent = new Intent();
//                com.smartpesa.intent.result.TransactionResult result = null;
//
//                // Extract transaction result from SDK and convert to Parcelable Result
//                if (transaction != null && transaction.getTransactionResult() != null && transaction.getTransactionResult().getTransactionId() != null) {
//                    result = Converter.from(transaction.getTransactionResult());
//                }
//
//                final TransactionResultDialogFragment[] tf = new TransactionResultDialogFragment[1];
//                final int[] activityResult = {RESULT_CANCELED};
//                if (isSuccess) {
//                    if (verification.equals(SmartPesa.Verification.SIGNATURE)) {
//                        UIHelper.log("SHOW SIGNATURE");
//
//
//                    } else {
//                        activityResult[0] = RESULT_OK;
//
//                        tf[0] = TransactionResultDialogFragment.newInstance(result);
//
//                        // Set activity result
//                        setResult(activityResult[0], intent);
//                        showLastTransaction();
//                        SpConnect.populateResultIntent(requestIntent, intent, result);
//                    }
//                } else {
//
//                    // Attach error to the intent
//                    TransactionException error = Converter.from(exception);
//                    TransactionError transactionError = TransactionError.builder()
//                            .transactionException(error)
//                            .transactionResult(result)
//                            .build();
//
//                    tf[0] = TransactionResultDialogFragment.newInstance(error, getFinalTransactedAmount(), result);
//                    // Set activity result
//                    setResult(activityResult[0], intent);
//                    showResultDialog(tf[0]);
//
//                    SpConnect.populateErrorIntent(requestIntent, intent, transactionError);
//                }
//            }

            @Override
            public void onError(SpException exception) {
                if (isActivityDestroyed) return;
                showError(Converter.from(exception));
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

            }

            @Override
            public void onRequestForInput() {

            }

            @Override
            public void onShowBalance(Balance balance) {

            }
        });
    }

    protected void showLastTransaction() {
        Fragment fragment = new IntentLastTransactionFragment();
        fragmentManager.beginTransaction().replace(R.id.container_body, fragment).commit();
        fragmentContainer.setVisibility(View.VISIBLE);
    }

    private void showResultDialog(TransactionResultDialogFragment tf) {
        tf.setOnPositiveButtonListener(mResultDialogListener);
        tf.setCancelable(false);
        tf.show(getSupportFragmentManager(), TRANSACTION_RESULT_DIALOG_TAG);
    }

    private TransactionResultDialogFragment.OnPositiveButtonListener mResultDialogListener = new TransactionResultDialogFragment.OnPositiveButtonListener() {
        @Override
        public void onPositiveButtonClicked(DialogInterface dialog) {
            finish();
        }
    };

    private BigDecimal getFinalTransactedAmount() {
        return amount;
    }

    public void showTransactionProgress() {
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

                    t = new Thread() {
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
                    t.start();
                }
            }
        }, 15000);
    }

    public void animateCardL2R(final boolean isChip) {
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
        UIHelper.log("" + isChipAlone);
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

    private void showError(TransactionException exception) {
        Intent intent = new Intent();

        TransactionError transactionError = TransactionError.builder()
                .transactionException(exception)
                .transactionResult(null)
                .build();

        SpConnect.populateErrorIntent(requestIntent, intent, transactionError);
        setResult(RESULT_CANCELED, intent);

        TransactionResultDialogFragment tf = TransactionResultDialogFragment.newInstance(exception, getFinalTransactedAmount());
        showResultDialog(tf);

        progressTV.setVisibility(View.INVISIBLE);
    }

    public void buildConfig(HashMap<String, Object> config) {
        if (transactionType.equals(SmartPesaTransactionType.REVERSAL)) {
            config.put("transaction_id", description);
        } else if (transactionType.equals(SmartPesaTransactionType.GENERAL_TRANSFER)) {
            config.put("aiic", description);
        } else if (transactionType.equals(SmartPesaTransactionType.PAYMENT)) {
            String billPaymentDetails = description;
            String[] parts = billPaymentDetails.split("~");
            String accountNo = parts[0];
            String billingOrgID = parts[1];
            config.put("billing_org", billingOrgID);
            config.put("account_no", accountNo);
        } else {
            if (!TextUtils.isEmpty(description)) {
                config.put("description", description);
            }
        }

        if (mConvenienceFee != null) {
            config.put("convenience_fee", mConvenienceFee);
        }
    }

    public void displayBluetoothDevice(Collection<SpTerminal> devices) {
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

    protected class TerminalSelectedListenerImpl implements BluetoothDialogFragment.TerminalSelectedListener<SpTerminal> {
        @Override
        public void onSelected(SpTerminal device) {
            processPayment(device);
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

    public void retry() {
        Fragment current = getSupportFragmentManager().findFragmentById(R.id.container_body);
        getSupportFragmentManager().beginTransaction().remove(current).commit();
        fragmentContainer.setVisibility(View.GONE);
        startTerminalScan();
    }
}
