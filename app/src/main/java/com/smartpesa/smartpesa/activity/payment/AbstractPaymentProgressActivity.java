
package com.smartpesa.smartpesa.activity.payment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.hanks.htextview.HTextView;
import com.skyfishjy.library.RippleBackground;
import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.activity.MainActivity;
import com.smartpesa.smartpesa.activity.base.BaseActivity;
import com.smartpesa.smartpesa.fragment.LoyaltyFragment;
import com.smartpesa.smartpesa.fragment.dialog.BluetoothDialogFragment;
import com.smartpesa.smartpesa.fragment.dialog.GetSignatureDialogFragment;
import com.smartpesa.smartpesa.fragment.dialog.LoyaltyDialogFragment;
import com.smartpesa.smartpesa.fragment.dialog.TerminalDialogFragment;
import com.smartpesa.smartpesa.fragment.history.LastTransactionFragment;
import com.smartpesa.smartpesa.fragment.payment.AbstractPaymentFragment;
import com.smartpesa.smartpesa.fragment.result.AbstractResultFragment;
import com.smartpesa.smartpesa.fragment.result.ApprovedFragment;
import com.smartpesa.smartpesa.fragment.result.FailedResultFragment;
import com.smartpesa.smartpesa.fragment.result.UnknownResultFragment;
import com.smartpesa.smartpesa.helpers.PreferredTerminalUtils;
import com.smartpesa.smartpesa.helpers.UIHelper;
import com.smartpesa.smartpesa.models.ParcelableTransactionResponse;
import com.smartpesa.smartpesa.models.SmartPesaTransactionType;
import com.smartpesa.smartpesa.models.UIRedeemable;
import com.smartpesa.smartpesa.persistence.MerchantModule;
import com.smartpesa.smartpesa.util.MoneyUtils;
import com.smartpesa.smartpesa.util.constants.SPConstants;
import com.wang.avi.AVLoadingIndicatorView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;
import dagger.Lazy;
import smartpesa.sdk.ServiceManager;
import smartpesa.sdk.SmartPesa;
import smartpesa.sdk.core.error.SpException;
import smartpesa.sdk.devices.SpTerminal;
import smartpesa.sdk.error.SpCardTransactionException;
import smartpesa.sdk.error.SpNetworkException;
import smartpesa.sdk.error.SpSessionException;
import smartpesa.sdk.error.SpTransactionException;
import smartpesa.sdk.interfaces.TransactionCallback;
import smartpesa.sdk.interfaces.TransactionData;
import smartpesa.sdk.models.currency.Currency;
import smartpesa.sdk.models.loyalty.Loyalty;
import smartpesa.sdk.models.loyalty.LoyaltyTransaction;
import smartpesa.sdk.models.loyalty.Redeemable;
import smartpesa.sdk.models.merchant.TransactionType;
import smartpesa.sdk.models.transaction.Balance;
import smartpesa.sdk.models.transaction.Card;
import smartpesa.sdk.models.transaction.CardPayment;
import smartpesa.sdk.models.transaction.Transaction;
import smartpesa.sdk.models.transaction.TransactionResult;
import smartpesa.sdk.scanner.TerminalScanningCallback;
import timber.log.Timber;

public abstract class AbstractPaymentProgressActivity extends BaseActivity implements AbstractResultFragment.ResultFragmentListener, DialogInterface.OnDismissListener {

    private static final String LOYALTY_DIALOG = "loyalty";
    public static final String CRYPTO_ATM_BIT_COIN_TRADER = "CryptoATMBitCoinTrader";
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
    @Bind(R.id.bluetoothImage) protected ImageView bluetoothImage;

    protected Context mContext;
    public Lazy<ServiceManager> serviceManager;
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
    private BigDecimal mConvenienceFee;
    private Bundle mTransactionBundle;
    final boolean[] transactionFinished = {false};
    final boolean[] bluetoothDisconnected = {false};
    final boolean[] bluetoothTimedOut = {false};
    PreferredTerminalUtils mPreferredTerminalUtils;
    public boolean isRetry;
    List<Redeemable> mRedeemableList;
    boolean isActivityDestroyed, chipOnly, isTimerRunning;
    private AVLoadingIndicatorView[] IMGS = { iv1, iv3, iv3, iv4, iv5, iv6 };
    Thread t;
    Currency transactionCurrency;
    String cryptoAtmQRScannedText;
    String crytoAtmCryptoValue;
    int cryptoATMcryptoType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_progress);
        ButterKnife.bind(this);
        isActivityDestroyed = false;
        isTimerRunning = false;

        if (getMerchantComponent() == null) {
            logoutUser();
            return;
        }

        mMoneyUtils = getMerchantComponent().provideMoneyUtils();

        //get the amount from the intent
        amount = new BigDecimal(this.getIntent().getDoubleExtra(SPConstants.AMOUNT, 0.00));
        transactionType = SmartPesaTransactionType.fromEnumId(this.getIntent().getIntExtra(SPConstants.TRANSACTION_TYPE, -1));
        cashBackAmount = this.getIntent().getDoubleExtra(SPConstants.CASH_BACK_AMOUNT, 0.00);
        fromAccount = this.getIntent().getIntExtra(SPConstants.FROM_ACCOUNT, 0);
        toAccount = this.getIntent().getIntExtra(SPConstants.TO_ACCOUNT, 0);
        description = this.getIntent().getStringExtra(SPConstants.DESCRIPTION);
        transactionCurrency = (Currency) this.getIntent().getSerializableExtra(SPConstants.TRANSACTION_CURRENCY);
        cryptoAtmQRScannedText = this.getIntent().getStringExtra(SPConstants.CRYPTO_ATM_QR_TEXT);
        crytoAtmCryptoValue = this.getIntent().getStringExtra(SPConstants.CRYPTO_ATM_CRYPTO_VALUE);
        cryptoATMcryptoType = this.getIntent().getIntExtra(SPConstants.CRYPTO_ATM_CRYPTO_TYPE, -1);

        String f = this.getIntent().getStringExtra(AbstractPaymentFragment.KEY_CONVENIENCE_FEE);
        if (mMoneyUtils != null && f != null) {
            mConvenienceFee = mMoneyUtils.parseBigDecimal(f);
        }

        IMGS[0] = iv1;
        IMGS[1] = iv2;
        IMGS[2] = iv3;
        IMGS[3] = iv4;
        IMGS[4] = iv5;
        IMGS[5] = iv6;

        Timber.i("User doing a %s", transactionType.toString());

        mPreferredTerminalUtils = new PreferredTerminalUtils(AbstractPaymentProgressActivity.this, getMerchantComponent().provideMerchant().getMerchantCode(),
                getMerchantComponent().provideMerchant().getOperatorCode());

        //call the UI components
        initializeComponents();

        //call the process payment once the activity is started
        startTerminalScan();
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

        //set the amount
        if (transactionCurrency != null) {
            amountProcessTV.setText(transactionCurrency.getCurrencySymbol() + " " + mMoneyUtils.format(amount));
        } else {
            amountProcessTV.setText(getMerchantComponent().provideMerchant().getCurrency().getCurrencySymbol() + " " + mMoneyUtils.format(amount));
        }

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
                if (isActivityDestroyed) return;

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
                if (isActivityDestroyed) return;
            }

            @Override
            public void onEnablingBluetooth(String s) {

            }
            @Override
            public void onBluetoothPermissionDenied(String[] strings) {

            }
        }, AbstractPaymentProgressActivity.this);
    }

    //initialise the transaction with the SDK
    public void processPayment(SpTerminal terminal) {

        SmartPesa.AccountType fromAccountType = UIHelper.getAccountTypeFromInt(fromAccount);
        SmartPesa.AccountType toAccountType = UIHelper.getAccountTypeFromInt(toAccount);
        final HashMap<String, Object> config = new HashMap<>();
        buildConfig(config);

        SmartPesa.TerminalTransactionParam.Builder builder = SmartPesa.TransactionParam.newBuilder(terminal)
                .transactionType(transactionType.getEnumId())
                .terminal(terminal)
                .amount(this.amount)
                .cardMode(SmartPesa.CardMode.SWIPE_OR_INSERT_OR_TAP)
                .from(fromAccountType)
                .to(toAccountType)
                .extraParams(config)
                .cashBack(mMoneyUtils.parseBigDecimal(cashBackAmount));

        if (transactionCurrency != null) {
            builder.currency(transactionCurrency);
        }

        if (transactionType.equals(SmartPesaTransactionType.SALE)) {
            boolean withLoyalty = this.getIntent().getBooleanExtra(SPConstants.IS_WITH_LOYALTY, false);
            if (withLoyalty) {
                builder.withLoyalty();
            }
        }

        if (cryptoATMcryptoType != -1 && !TextUtils.isEmpty(cryptoAtmQRScannedText)) {

            HashMap<String, Object> postHash = new HashMap<>();

            postHash.put("qrcode", cryptoAtmQRScannedText);
            postHash.put("amount", new BigDecimal(crytoAtmCryptoValue));
            if (cryptoATMcryptoType == MerchantModule.MENU_ID_CRYPTO_ATM_BITCOIN) {
                postHash.put("crypto_currency_symbol", "BTC");

            } else if (cryptoATMcryptoType == MerchantModule.MENU_ID_CRYPTO_ATM_LITECOIN) {
                postHash.put("crypto_currency_symbol", "LTC");
            }

            builder.withPostProcessing(CRYPTO_ATM_BIT_COIN_TRADER, postHash);

        }

        SmartPesa.TransactionParam param = builder.build();

        serviceManager.get().performTransaction(param, new TransactionCallback() {
            @Override
            public void onProgressTextUpdate(String s) {
                if (isActivityDestroyed) return;
                Timber.d("onProgressTextUpdate %s", s);
                UIHelper.log(s);
                progressTV.setText(s);
            }

            @Override
            public void onTransactionFinished(TransactionType transactionType, boolean b, @Nullable Transaction transaction, @Nullable SmartPesa.Verification verification, @Nullable SpCardTransactionException e) {

            }

            @Override
            public void onDeviceConnected(SpTerminal spTerminal) {
                if (isActivityDestroyed) return;
                progressTV.setText("Connected to mPOS device\n ("+spTerminal.getName()+")");
            }

            @Override
            public void onDeviceDisconnected(SpTerminal spTerminal) {
                if (isActivityDestroyed) return;

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

                    if (!transactionType.equals(SmartPesaTransactionType.LOYALTY)) {
                        failBtnLl.setVisibility(View.VISIBLE);
                    }

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

            }

            @Override
            public void onWaitingForCard(String s, SmartPesa.CardMode cardMode) {
                if (isActivityDestroyed) return;
                if(!chipOnly) {
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

                if (!TextUtils.isEmpty(s)) {

                    if (inputStatus == SmartPesa.InputStatus.ENTERED) {

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
                    }
                } else {
                    serviceManager.get().confirmInput(null);
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
            public void onTransactionApproved(TransactionData transactionData) {
                if (isActivityDestroyed) return;
                transactionFl.setVisibility(View.INVISIBLE);
                if (bluetoothDisconnected[0] == false && bluetoothTimedOut[0] == false) {
                        transactionFinished[0] = true;
                        failBtnLl.setVisibility(View.INVISIBLE);
                        if (transactionData != null && transactionData.getTransaction() != null && transactionData.getTransaction().getTransactionResult() != null) {

                            CardPayment cardPayment = (CardPayment) transactionData.getTransaction().getTransactionResult().getPayment();
                            TransactionResult transactionResponse = transactionData.getTransaction().getTransactionResult();

                            Fragment fragment = null;

                            fragment = ApprovedFragment.newInstance(transactionResponse != null ? new ParcelableTransactionResponse(transactionResponse) : null, cardPayment.getVerification());

                            fragmentManager.beginTransaction().replace(R.id.container_body, fragment).commitAllowingStateLoss();
                            fragmentContainer.setVisibility(View.VISIBLE);
                            lockBackBTN = false;
                        }
                }

            }

            @Override
            public void onTransactionDeclined(SpTransactionException e, TransactionData transactionData) {
                if (isActivityDestroyed) return;
                transactionFl.setVisibility(View.INVISIBLE);
                if (bluetoothDisconnected[0] == false && bluetoothTimedOut[0] == false) {
                        transactionFinished[0] = true;
                        failBtnLl.setVisibility(View.INVISIBLE);
                        Fragment fragment = null;
                        if (transactionData != null && transactionData.getTransaction() != null && transactionData.getTransaction().getTransactionResult() != null) {

                            TransactionResult transactionResponse = transactionData.getTransaction().getTransactionResult();

                            fragment = FailedResultFragment.newInstance(failureReason, transactionType, amount, transactionResponse != null ? new ParcelableTransactionResponse(transactionResponse) : null);
                        } else {
                            failureReason = e.getMessage();
                            fragment = FailedResultFragment.newInstance(failureReason, transactionType, amount, null);
                        }

                        fragmentManager.beginTransaction().replace(R.id.container_body, fragment).commitAllowingStateLoss();
                        fragmentContainer.setVisibility(View.VISIBLE);
                        lockBackBTN = false;
                }
            }

            @Override
            public void onError(SpException exception) {
                if (isActivityDestroyed) return;
                Fragment fragment = null;
                if (bluetoothDisconnected[0] == false && bluetoothTimedOut[0] == false) {
                        transactionFinished[0] = true;
                        failBtnLl.setVisibility(View.INVISIBLE);
                        transactionFl.setVisibility(View.INVISIBLE);
                        //send result to fragment
                        transactionIsSuccessful = false;

                        if (exception != null) {
                            failureReason = exception.getMessage();
                        }

                        if (exception instanceof SpTransactionException) {
                            fragment = FailedResultFragment.newInstance(failureReason, transactionType, amount, null);
                        } else if (exception instanceof SpSessionException || exception instanceof SpNetworkException) {
                            fragment = UnknownResultFragment.newInstance(exception, transactionType, amount, null);
                        } else {
                            fragment = FailedResultFragment.newInstance(failureReason, transactionType, amount, null);
                        }
                        if (isActivityDestroyed) return;
                        fragmentManager.beginTransaction().replace(R.id.container_body, fragment).commitAllowingStateLoss();
                        fragmentContainer.setVisibility(View.VISIBLE);
                        lockBackBTN = false;
                }

            }

            @Override
            public void onStartPostProcessing(String providerName, Transaction transaction) {
                if (isActivityDestroyed) return;

            }

            @Override
            public void onReturnLoyaltyBalance(Loyalty loyalty) {
                if (isActivityDestroyed) return;
                showLoyaltyBalance(loyalty);
            }

            @Override
            public void onShowLoyaltyRedeemablePrompt(LoyaltyTransaction loyaltyTransaction) {
                if (isActivityDestroyed) return;
                showLoyaltySelectionDialog(loyaltyTransaction);
            }

            @Override
            public void onLoyaltyCancelled() {
                if (isActivityDestroyed) return;
                UIHelper.log("Loyalty cancelled");
            }

            @Override
            public void onLoyaltyApplied(LoyaltyTransaction loyaltyTransaction) {
                if (isActivityDestroyed) return;
                amountProcessTV.setText(mMoneyUtils.format(loyaltyTransaction.getAdjustedAmount()));
                amount = loyaltyTransaction.getAdjustedAmount();
            }

            @Override
            public void onShowConfirmFeePrompt(TransactionType.FeeChargeType feeChargeType, Currency currency, BigDecimal feeAmount, BigDecimal finalAmount) {

            }

            @Override
            public void onRequestForInput() {

            }

            @Override
            public void onShowBalance(Balance balance) {
                if (isActivityDestroyed) return;
                UIHelper.showMessageDialogWithCallback(AbstractPaymentProgressActivity.this,
                        "Your balance is " + balance.getCurrency().getCurrencySymbol() + mMoneyUtils.format(balance.getAmount()),
                        "Ok",
                        new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                super.onPositive(dialog);
                                serviceManager.get().onShowBalanceFinished();
                            }
                        });
            }
        });
    }

    private void showTransactionProgress() {
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

    private void animateCardL2R(final boolean isChip) {
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

    private void animatePinPad(){
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

    private void animateCardB2T(final boolean isChipAlone){
        UIHelper.log(""+isChipAlone);
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

    public void processRedeemableList(ArrayList<Integer> positions) {
        List<Redeemable> redeemableList = new ArrayList<>();
        if (positions.size() != 0) {
            for (int i = 0; i < positions.size(); i++) {
                int position = positions.get(i);
                Redeemable redeemable = mRedeemableList.get(position);
                redeemable.redeem(redeemable.getAmount());
                redeemableList.add(redeemable);
            }
        }
        serviceManager.get().updateLoyaltyRedeemable(redeemableList);
    }

    private void showLoyaltySelectionDialog(LoyaltyTransaction loyaltyTransaction) {
        ArrayList<UIRedeemable> redeemableArrayList = new ArrayList<UIRedeemable>();
        List<Redeemable> redeemableList = loyaltyTransaction.getRedeemableList();
        mRedeemableList = loyaltyTransaction.getRedeemableList();
        if (redeemableList != null && redeemableList.size() > 0) {
            for (int i = 0; i < redeemableList.size(); i++) {
                Redeemable redeemable = redeemableList.get(i);
                UIRedeemable uiRedeemable = new UIRedeemable(redeemable.getId(), redeemable.getName(), redeemable.getRedeemableType().getEnumId(), redeemable.getAmount(), redeemable.getUnit(), redeemable.getBalance(), false);
                redeemableArrayList.add(uiRedeemable);
            }
        }

        LoyaltyDialogFragment loyaltyFragment = new LoyaltyDialogFragment().newInstance(redeemableArrayList,
                loyaltyTransaction.getAdjustedAmount(),
                loyaltyTransaction.getDiscountAmount(),
                amount);
        loyaltyFragment.setCancelable(false);
        loyaltyFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
        loyaltyFragment.show(getSupportFragmentManager(), LOYALTY_DIALOG);
    }

    private void showLoyaltyBalance(Loyalty loyalty) {

        if (loyalty != null) {
            ArrayList<UIRedeemable> redeemableArrayList = new ArrayList<UIRedeemable>();
            List<Redeemable> redeemableList = loyalty.getRedeemableList();
            mRedeemableList = loyalty.getRedeemableList();
            if (redeemableList != null && redeemableList.size() > 0) {
                for (int i = 0; i < redeemableList.size(); i++) {
                    Redeemable redeemable = redeemableList.get(i);
                    UIRedeemable uiRedeemable = new UIRedeemable(redeemable.getId(), redeemable.getName(), redeemable.getRedeemableType().getEnumId(), redeemable.getAmount(), redeemable.getUnit(), redeemable.getBalance(), false);
                    redeemableArrayList.add(uiRedeemable);
                }
            }

            Fragment loyaltyFragment = new LoyaltyFragment().newInstance(redeemableArrayList, true);
            fragmentManager.beginTransaction().replace(R.id.container_body, loyaltyFragment).commitAllowingStateLoss();
            fragmentContainer.setVisibility(View.VISIBLE);

        }

        transactionFl.setVisibility(View.INVISIBLE);
        failBtnLl.setVisibility(View.INVISIBLE);
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

    public abstract void doPostProcessing(Transaction resultTlvResponse, SmartPesa.Verification transactionVerification, SmartPesaTransactionType transaction);

    public void displayBluetoothDevice(Collection<SpTerminal> devices) {
        TerminalDialogFragment dialog;
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(BLUETOOTH_FRAGMENT_TAG);
        if (fragment == null) {
            dialog = new TerminalDialogFragment();
            dialog.setCancelable(false);
            dialog.show(getSupportFragmentManager(), BLUETOOTH_FRAGMENT_TAG);
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

    //initialize UIComponents
    protected void initializeComponents() {
        mContext = AbstractPaymentProgressActivity.this;
        serviceManager = SmartPesaApplication.component(mContext).serviceManager();
        fragmentManager = getSupportFragmentManager();

        if (transactionType.equals(SmartPesaTransactionType.BALANCE_INQUIRY) || (transactionType.equals(SmartPesaTransactionType.LOYALTY))) {
            amountProcessTV.setVisibility(View.INVISIBLE);
        }

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
    }

    //switch the fragment in the fragment container to LastTransactionFragment fragment
    @Override
    public void showReceipt(Bundle bundle) {
        if (transactionType.equals(SmartPesaTransactionType.REVERSAL)) {
            finish();
        } else {
            mTransactionBundle = bundle;
            showLastTransaction();
        }
    }

    @Override
    public void showSignature(ParcelableTransactionResponse transactionResponse, Bundle bundle) {
        mTransactionBundle = bundle;
        FragmentManager fragmentManager = getSupportFragmentManager();
        GetSignatureDialogFragment signDialog = GetSignatureDialogFragment.newInstance(transactionResponse.getTransactionId(), transactionResponse.getCardPayment().getCardHolderName());
        signDialog.setCancelable(false);
        signDialog.show(fragmentManager, "dialog_get_signature");
    }

    protected void showLastTransaction() {
        Fragment fragment = getLastTransactionFragment();
        fragment.setArguments(mTransactionBundle);
        fragmentManager.beginTransaction().replace(R.id.container_body, fragment).commit();
        fragmentContainer.setVisibility(View.VISIBLE);
    }

    @NonNull
    protected LastTransactionFragment getLastTransactionFragment() {
        return new LastTransactionFragment();
    }

    @Override
    public void goToHistory() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(MainActivity.GO_TO_HISTORY, true);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    //signature fragment has been dismissed and show the LastTransactionFragment fragment
    @Override
    public void onDismiss(final DialogInterface dialog) {
        showLastTransaction();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isActivityDestroyed = true;
        if (serviceManager != null) {
            serviceManager.get().stopScan();
        }
    }
}
