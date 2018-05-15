package com.smartpesa.smartpesa.activity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.hanks.htextview.HTextView;
import com.skyfishjy.library.RippleBackground;
import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.activity.base.BaseActivity;
import com.smartpesa.smartpesa.fragment.dialog.BluetoothDialogFragment;
import com.smartpesa.smartpesa.fragment.dialog.TerminalDialogFragment;
import com.smartpesa.smartpesa.helpers.UIHelper;
import com.wang.avi.AVLoadingIndicatorView;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collection;

import butterknife.Bind;
import butterknife.ButterKnife;
import smartpesa.sdk.devices.SpTerminal;
import smartpesa.sdk.ota.OtaInjectionListener;
import smartpesa.sdk.ota.OtaManager;
import smartpesa.sdk.ota.error.SpOtaException;
import smartpesa.sdk.scanner.TerminalScanningCallback;

public class OTAProgressActivity extends BaseActivity {

    private static final int REQUEST_PERMISSION_BT_TRANSACTION = 8111;
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
    public OtaManager otaManager;
    protected boolean lockBackBTN = false;
    protected FragmentManager fragmentManager;
    protected static final String BLUETOOTH_FRAGMENT_TAG = "bluetooth";
    public boolean isRetry;
    public final boolean[] transactionFinished = {false};
    public final boolean[] bluetoothDisconnected = {false};
    public final boolean[] bluetoothTimedOut = {false};
    boolean isActivityDestroyed, chipOnly, isTimerRunning;
    Thread t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_progress);
        ButterKnife.bind(this);
        isActivityDestroyed = false;
        isTimerRunning = false;

        if (getMerchantComponent() != null) {
            startTransaction();
        }
    }

    public void startTransaction() {
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

        amountProcessTV.setVisibility(View.INVISIBLE);
        transactionTypeTV.setText("Inject OTA");

        otaManager.get().scan(new TerminalScanningCallback() {
            @Override
            public void onDeviceListRefresh(Collection<SpTerminal> collection) {
                if (isActivityDestroyed()) return;

                displayBluetoothDevice(collection);
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

            }

            @Override
            public void onEnablingBluetooth(String s) {
                progressTV.setText(s);
            }

            @Override
            public void onBluetoothPermissionDenied(String[] permissions) {
                ActivityCompat.requestPermissions(OTAProgressActivity.this,
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
        if (otaManager != null) {
            otaManager.get().stopScan();
        }
    }

    //initialise the transaction with the SDK
    public void processPayment(SpTerminal terminal) {

        otaManager.inject(terminal, new OtaInjectionListener() {
            @Override
            public void onOtaDeviceConnected(SpTerminal spTerminal) {
                progressTV.setText("Connected to " + spTerminal.getName());
            }

            @Override
            public void onStartKeyInjection(String s) {
                progressTV.setText("Starting key injection");
            }

            @Override
            public void onInjectionCompleted() {
                UIHelper.showMessageDialogWithCallback(OTAProgressActivity.this,
                        "Key injection completed successfully",
                        "Ok",
                        new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                super.onPositive(dialog);
                                finish();
                            }
                        });
                progressTV.setText("Key injection completed successfully");
                lockBackBTN = false;
            }

            @Override
            public void onDisconnected() {

            }

            @Override
            public void onError(SpOtaException e) {
                UIHelper.showMessageDialogWithCallback(OTAProgressActivity.this, e.getLocalizedMessage(),
                        "Ok",
                        new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                super.onPositive(dialog);
                                finish();
                            }
                        });
                lockBackBTN = false;
            }
        });
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

    //initialize UIComponents
    protected void initializeComponents() {
        mContext = OTAProgressActivity.this;
        otaManager = OtaManager.get();
        fragmentManager = getSupportFragmentManager();

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

    @Override
    public void onBackPressed() {
        if (!lockBackBTN) {
            super.onBackPressed();
        }
    }

    public void retry() {
        startTerminalScan();
    }
}
