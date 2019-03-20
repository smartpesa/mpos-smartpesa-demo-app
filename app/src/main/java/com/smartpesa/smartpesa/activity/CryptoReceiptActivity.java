package com.smartpesa.smartpesa.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.activity.base.BaseActivity;
import com.smartpesa.smartpesa.fragment.dialog.BluetoothDialogFragment;
import com.smartpesa.smartpesa.fragment.dialog.PrinterDialogFragment;
import com.smartpesa.smartpesa.fragment.history.PastHistoryFragment;
import com.smartpesa.smartpesa.helpers.UIHelper;
import com.smartpesa.smartpesa.models.ParcelableGoCoinPayment;
import com.smartpesa.smartpesa.models.ParcelableTransactionResponse;
import com.smartpesa.smartpesa.util.DateUtils;
import com.smartpesa.smartpesa.util.MoneyUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import dagger.Lazy;
import smartpesa.sdk.ServiceManager;
import smartpesa.sdk.SmartPesa;
import smartpesa.sdk.core.error.SpException;
import smartpesa.sdk.devices.SpPrinterDevice;
import smartpesa.sdk.error.SpPrinterException;
import smartpesa.sdk.error.SpSessionException;
import smartpesa.sdk.interfaces.PrintingCallback;
import smartpesa.sdk.models.printing.AbstractPrintingDefinition;
import smartpesa.sdk.models.receipt.GetReceiptCallback;
import smartpesa.sdk.models.transaction.SendNotificationCallback;
import smartpesa.sdk.scanner.PrinterScanningCallback;

public class CryptoReceiptActivity extends BaseActivity {

    private static final String BLUETOOTH_FRAGMENT_TAG = "bluetooth";
    @Bind(R.id.amountTv) TextView amountTv;
    @Bind(R.id.messageTv) TextView messageTv;
    @Bind(R.id.dateTv) TextView dateTv;
    @Bind(R.id.timeTv) TextView timeTv;
    @Bind(R.id.transactionReferenceLabelTv) TextView transactionReferenceLabelTv;
    @Bind(R.id.transactionReferenceTv) TextView transactionReferenceTv;
    @Bind(R.id.cryptoCurrencyNameLabelTv) TextView cryptoCurrencyNameLabelTv;
    @Bind(R.id.cryptoCurrencyNameTv) TextView cryptoCurrencyNameTv;
    @Bind(R.id.resultLabelTv) TextView resultLabelTv;
    @Bind(R.id.resultTv) TextView resultTv;
    @Bind(R.id.goCoinIdLabelTv) TextView goCoinIdLabelTv;
    @Bind(R.id.goCoinIdTv) TextView goCoinIdTv;
    @Bind(R.id.btcSpotRateLabelTv) TextView btcSpotRateLabelTv;
    @Bind(R.id.btcSpotRateTv) TextView btcSpotRateTv;
    @Bind(R.id.cryptoCurrencyIdLabelTv) TextView cryptoCurrencyIdLabelTv;
    @Bind(R.id.cryptoCurrencyIdTv) TextView cryptoCurrencyIdTv;
    @Bind(R.id.cryptoPriceLabelTv) TextView cryptoPriceLabelTv;
    @Bind(R.id.cryptoPriceTv) TextView cryptoPriceTv;
    @Bind(R.id.paymentAddressLabelTv) TextView paymentAddressLabelTv;
    @Bind(R.id.paymentAddressTv) TextView paymentAddressTv;
    @Bind(R.id.statusLabelTv) TextView statusLabelTv;
    @Bind(R.id.statusTv) TextView statusTv;
    @Bind(R.id.paymentGoCoinIdLabelTv) TextView paymentGoCoinIdLabelTv;
    @Bind(R.id.paymentGoCoinIdTv) TextView paymentGoCoinIdTv;
    @Bind(R.id.smsLabel_RECEIPT_ET) EditText smsET;
    @Bind(R.id.emailLabel_RECEIPT_ET) EditText emailET;
    @Bind(R.id.send_RECEIPT_BTN) Button sendBTN;
    @Bind(R.id.print_RECEIPT_BTN) Button printBTN;
    @Bind(R.id.smsCB) CheckBox smsCB;
    @Bind(R.id.emailCB) CheckBox emailCB;
    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.toolbar_title) TextView toolbarTitle;
    @Bind(R.id.tickImageView) ImageView tickImageView;
    @Bind(R.id.bgImageView) ImageView bgImageView;

    String finalPhone = "", finalEmail = "";
    @Nullable public MoneyUtils mMoneyUtils;
    ProgressDialog mProgressDialog;
    Lazy<ServiceManager> serviceManager;

    ParcelableTransactionResponse mTransactionResponse;
    String dialingCode;
    boolean sendSMS, sendEmail = false;
    UUID transactionID;
    List<AbstractPrintingDefinition> dataToPrint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crypto_receipt);
        ButterKnife.bind(this);

        //setup toolbar
        setSupportActionBar(mToolbar);
        setTitle("");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbarTitle.setText("CRYPTO PAYMENT");

        mMoneyUtils = SmartPesaApplication.merchantComponent(this).provideMoneyUtils();
        serviceManager = SmartPesaApplication.component(this).serviceManager();

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);

        if (getMerchantComponent() != null && getMerchantComponent().provideMerchant() != null) {
            dialingCode = getMerchantComponent().provideMerchant().getDefaultDialingCode();
        }

        if (getIntent() != null && getIntent().getExtras() != null) {
            mTransactionResponse = getIntent().getExtras().getParcelable(PastHistoryFragment.TRANSACTION_RESPONSE);
        }

        setupDetails();
        sendBTN.setEnabled(false);
        setupSendReceipt();
    }

    private void setupSendReceipt() {

        sendBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendReceipt();
            }
        });

        printBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printReceipt(transactionID);
            }
        });

        //if sms is entered check the box
        smsET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                String smsText = s.toString();
                if (!TextUtils.isEmpty(dialingCode)) {
                    if (smsText.equals(dialingCode) || TextUtils.isEmpty(smsText)) {
                        smsCB.setChecked(false);
                        toggleSendButtonOnSms();
                    } else {
                        smsCB.setChecked(true);
                        toggleSendButtonOnSms();
                    }
                }
            }
        });

        //if email is entered check the box
        emailET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String emailText = s.toString();
                if (TextUtils.isEmpty(emailText)) {
                    emailCB.setChecked(false);
                    toggleSendButtonOnEmail();
                    sendEmail = false;
                } else {
                    emailCB.setChecked(true);
                    toggleSendButtonOnEmail();
                    sendEmail = true;
                }
            }
        });

        smsCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    String smsText = smsET.getText().toString();
                    if (!TextUtils.isEmpty(dialingCode)) {
                        if (smsText.equals(dialingCode) || TextUtils.isEmpty(smsText)) {
                            UIHelper.showToast(CryptoReceiptActivity.this, "Please enter a number first");
                            sendBTN.setEnabled(false);
                            smsCB.setChecked(false);
                            sendSMS = false;
                        } else {
                            sendSMS = true;
                            sendBTN.setEnabled(true);
                        }
                    }
                } else {
                    sendBTN.setEnabled(false);
                    sendSMS = false;
                }
            }
        });

        emailCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    String emailText = emailET.getText().toString();
                    if (TextUtils.isEmpty(emailText)) {
                        UIHelper.showToast(CryptoReceiptActivity.this, "Please enter an email address first");
                        sendBTN.setEnabled(false);
                        emailCB.setChecked(false);
                        sendEmail = false;
                    } else {
                        sendBTN.setEnabled(true);
                        sendEmail = true;
                    }
                } else {
                    sendBTN.setEnabled(false);
                    sendEmail = false;
                }
            }
        });
    }

    //print receipt
    private void printReceipt(final UUID transactionId) {

        final SmartPesa.ReceiptFormat[] receiptFormats = {
                SmartPesa.ReceiptFormat.CUSTOMER,
                SmartPesa.ReceiptFormat.MERCHANT,
                SmartPesa.ReceiptFormat.BANK};
        new AlertDialog.Builder(CryptoReceiptActivity.this)
                .setTitle(getString(R.string.select_receipt_format))
                .setAdapter(
                        new ArrayAdapter<SmartPesa.ReceiptFormat>(
                                CryptoReceiptActivity.this,
                                android.R.layout.simple_list_item_1,
                                receiptFormats
                        ),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SmartPesa.ReceiptFormat receiptFormat = receiptFormats[which];
                                fetchReceiptAndPrint(transactionID, receiptFormat);
                            }
                        })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();

    }

    protected void fetchReceiptAndPrint(UUID transactionId, SmartPesa.ReceiptFormat receiptFormat) {
        HashMap<String, Object> config = new HashMap<>();
        final ProgressDialog mp = new ProgressDialog(this);
        mp.setTitle(getString(R.string.app_name));
        mp.setMessage(getString(R.string.loading_receipt));
        mp.show();
        serviceManager.get().getReceipt(transactionId, receiptFormat, config, new GetReceiptCallback() {
            @Override
            public void onSuccess(List<AbstractPrintingDefinition> abstractPrintingDefinitions) {
                if (isActivityDestroyed()) return;
                mp.dismiss();
                dataToPrint = abstractPrintingDefinitions;

                serviceManager.get().scanPrinter(new PrinterScanningCallback() {
                    @Override
                    public void onDeviceListRefresh(Collection<SpPrinterDevice> collection) {
                        if (isActivityDestroyed()) return;
                        displayPrinterDevice(collection);
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

                    }

                    @Override
                    public void onBluetoothPermissionDenied(String[] strings) {

                    }
                });
            }

            @Override
            public void onError(SpException exception) {
                if (isActivityDestroyed()) return;

                if (exception instanceof SpSessionException) {
                    mp.dismiss();
                    //show the expired message
                    UIHelper.showToast(CryptoReceiptActivity.this, getResources().getString(R.string.session_expired));
                    //finish the current activity
                    finish();

                    //start the splash screen
                    startActivity(new Intent(CryptoReceiptActivity.this, SplashActivity.class));
                } else {
                    mp.dismiss();
                    UIHelper.showErrorDialog(CryptoReceiptActivity.this, getResources().getString(R.string.app_name), exception.getMessage());
                }
            }
        });
    }

    private void displayPrinterDevice(Collection<SpPrinterDevice> devices) {
        PrinterDialogFragment dialog;
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(BLUETOOTH_FRAGMENT_TAG);
        if (fragment == null) {
            dialog = new PrinterDialogFragment();
            dialog.show(getSupportFragmentManager(), BLUETOOTH_FRAGMENT_TAG);
        } else {
            dialog = (PrinterDialogFragment) fragment;
        }
        dialog.setSelectedListener(new CryptoReceiptActivity.PrinterSelectedImpl());
        dialog.updateDevices(devices);
    }

    private class PrinterSelectedImpl implements BluetoothDialogFragment.TerminalSelectedListener<SpPrinterDevice> {
        @Override
        public void onSelected(SpPrinterDevice device) {
            performPrint(device);
        }

        @Override
        public void onCancelled() {

        }
    }

    private void performPrint(SpPrinterDevice device) {
        closeDialogFragment();
        serviceManager.get().performPrint(SmartPesa.PrintingParam.withData(dataToPrint).printerDevice(device).build(), new PrintingCallback() {
            @Override
            public void onPrinterError(SpPrinterException errorMessage) {
                if (isActivityDestroyed()) return;
                UIHelper.showErrorDialog(CryptoReceiptActivity.this, getResources().getString(R.string.app_name), errorMessage.getMessage());
            }

            @Override
            public void onPrinterSuccess() {
                if (isActivityDestroyed()) return;
                UIHelper.log("here");
                closeDialogFragment();
            }
        });
    }

    private void toggleSendButtonOnEmail() {
        String emailText = emailET.getText().toString();
        if (TextUtils.isEmpty(emailText)) {
            sendBTN.setEnabled(false);
        } else {
            sendBTN.setEnabled(true);
        }
    }

    private void toggleSendButtonOnSms() {
        String smsText = smsET.getText().toString();
        if (!TextUtils.isEmpty(dialingCode)) {
            if (smsText.equals(dialingCode) || TextUtils.isEmpty(smsText)) {
                sendBTN.setEnabled(false);
            } else {
                sendBTN.setEnabled(true);
            }
        }
    }

    //close the printer bluetooth list if already one is present
    private void closeDialogFragment() {
        Fragment dialogBluetoothList = getSupportFragmentManager().findFragmentByTag(BLUETOOTH_FRAGMENT_TAG);
        if (dialogBluetoothList != null) {
            DialogFragment dialogFragment = (DialogFragment) dialogBluetoothList;
            if (dialogFragment != null) {
                dialogFragment.dismiss();
            }
        }
    }

    //send receipt
    public void sendReceipt() {

        HashMap<String, Object> config = new HashMap<>();
        String phoneNum = smsET.getText().toString();
        String email = emailET.getText().toString();

        if (validateSending(phoneNum, email)) {

            mProgressDialog.setMessage(getResources().getString(R.string.sending_receipt));
            mProgressDialog.show();

            serviceManager.get().sendNotification(transactionID, finalEmail, finalPhone, config, new SendNotificationCallback() {
                @Override
                public void onSuccess(Boolean aBoolean) {
                    if (isActivityDestroyed()) return;

                    mProgressDialog.dismiss();

                    UIHelper.showToast(CryptoReceiptActivity.this, getResources().getString(R.string.sending_success));
                }

                @Override
                public void onError(SpException exception) {
                    if (exception instanceof SpSessionException) {
                        mProgressDialog.dismiss();
                        UIHelper.showToast(CryptoReceiptActivity.this, getResources().getString(R.string.session_expired));
                        //finish the current activity
                        finish();

                        //start the splash screen
                        startActivity(new Intent(CryptoReceiptActivity.this, SplashActivity.class));
                    } else {
                        if (isActivityDestroyed()) return;

                        UIHelper.log("Error = " + exception.getMessage());
                        mProgressDialog.dismiss();
                        UIHelper.showErrorDialog(CryptoReceiptActivity.this, getResources().getString(R.string.app_name), exception.getMessage());
                    }
                }

            });
        }
    }

    //validate the phone and email entered
    public boolean validateSending(String phone, String email) {

        if (sendSMS || sendEmail) {

            if (sendSMS) {
                if (TextUtils.isEmpty(phone) || phone.equals(dialingCode)) {
                    finalPhone = "";
                    UIHelper.showToast(CryptoReceiptActivity.this, getResources().getString(R.string.enter_valid_number));
                    return false;
                } else {
                    finalPhone = phone;
                }
            } else {
                finalPhone = "";
            }

            if (sendEmail) {
                if (TextUtils.isEmpty(email)) {
                    finalEmail = "";
                    UIHelper.showToast(CryptoReceiptActivity.this, getResources().getString(R.string.enter_valid_email));
                    return false;
                } else {
                    finalEmail = email;
                }
            } else {
                finalEmail = "";
            }
            return true;
        } else {
            UIHelper.showToast(CryptoReceiptActivity.this, getResources().getString(R.string.select_one_option));
            return false;
        }

    }

    private void setupDetails() {
        if (mTransactionResponse != null) {

            String responseCode = mTransactionResponse.getAuthorisationResponseCode();

            if (!TextUtils.isEmpty(responseCode)) {
                if (responseCode.equals("00")) {
                    bgImageView.setImageResource(R.drawable.green_circle);
                    tickImageView.setImageResource(R.drawable.ic_done_white_24dp);
                } else {
                    bgImageView.setImageResource(R.drawable.red_circle);
                    tickImageView.setImageResource(R.drawable.ic_clear_white_24dp);
                }
            }

            transactionID = mTransactionResponse.getTransactionId();

            BigDecimal amount = mTransactionResponse.getAmount();
            String currencySymbol = mTransactionResponse.getCurrencySymbol();

            if (amount != null && currencySymbol != null && mMoneyUtils != null) {
                amountTv.setText(currencySymbol + " " + mMoneyUtils.format(amount));
            }

            String message = mTransactionResponse.getResponseDescription();

            if (!TextUtils.isEmpty(message)) {
                messageTv.setText(message);
            } else {
                messageTv.setVisibility(View.GONE);
            }

            Date dateTime = mTransactionResponse.getTransactionDatetime();

            if (dateTime != null) {
                String date = DateUtils.format(dateTime, DateUtils.DATE_FORMAT_DD_MM_YYYY);
                String time = DateUtils.format(dateTime, DateUtils.TIME_FORMAT_HH_MM_SS);

                if (!TextUtils.isEmpty(date)) {
                    dateTv.setText(date);
                }

                if (!TextUtils.isEmpty(time)) {
                    timeTv.setText(time);
                }
            }

            String transactionReference = mTransactionResponse.getTransactionReference();

            if (!TextUtils.isEmpty(transactionReference)) {
                transactionReferenceTv.setText(transactionReference);
                transactionReferenceLabelTv.setVisibility(View.VISIBLE);
                transactionReferenceTv.setVisibility(View.VISIBLE);
            }

            if (mTransactionResponse.getGoCoinPayment() != null) {

                ParcelableGoCoinPayment parcelableGoCoinPayment = mTransactionResponse.getGoCoinPayment();

                String cryptoName = parcelableGoCoinPayment.getCryptoCurrencyName();

                if (!TextUtils.isEmpty(cryptoName)) {
                    cryptoCurrencyNameTv.setText(cryptoName);
                    cryptoCurrencyNameLabelTv.setVisibility(View.VISIBLE);
                    cryptoCurrencyNameTv.setVisibility(View.VISIBLE);
                }

                String result = parcelableGoCoinPayment.getResult();

                if (!TextUtils.isEmpty(result)) {
                    resultTv.setText(result);
                    resultTv.setVisibility(View.VISIBLE);
                    resultLabelTv.setVisibility(View.VISIBLE);
                }

                UUID goCoinId = parcelableGoCoinPayment.getGoCoinId();

                if (goCoinId != null) {
                    goCoinIdTv.setText(goCoinId.toString());
                    goCoinIdTv.setVisibility(View.VISIBLE);
                    goCoinIdLabelTv.setVisibility(View.VISIBLE);
                }

                BigDecimal btcSpotRate = parcelableGoCoinPayment.getBtcSpotRate();

                if (btcSpotRate != null && mMoneyUtils != null) {
                    btcSpotRateTv.setText(mMoneyUtils.format(btcSpotRate));
                    btcSpotRateTv.setVisibility(View.VISIBLE);
                    btcSpotRateLabelTv.setVisibility(View.VISIBLE);
                }

                BigDecimal cryptoPrice = parcelableGoCoinPayment.getCryptoPrice();

                if (cryptoPrice != null && mMoneyUtils != null) {
                    cryptoCurrencyNameTv.setText(mMoneyUtils.format(cryptoPrice));
                    cryptoCurrencyNameTv.setVisibility(View.VISIBLE);
                    cryptoCurrencyNameLabelTv.setVisibility(View.VISIBLE);
                }

                String paymentAddress = parcelableGoCoinPayment.getPaymentAddress();

                if (!TextUtils.isEmpty(paymentAddress)) {
                    paymentAddressTv.setText(paymentAddress);
                    paymentAddressTv.setVisibility(View.VISIBLE);
                    paymentAddressLabelTv.setVisibility(View.VISIBLE);

                }

                String status = parcelableGoCoinPayment.getStatus();

                if (!TextUtils.isEmpty(status)) {
                    statusTv.setText(status);
                    statusTv.setVisibility(View.VISIBLE);
                    statusLabelTv.setVisibility(View.VISIBLE);
                }

                UUID paymentGoCoinId = parcelableGoCoinPayment.getPaymentGoCoinId();

                if (paymentGoCoinId != null) {
                    paymentGoCoinIdTv.setText(paymentGoCoinId.toString());
                    paymentGoCoinIdTv.setVisibility(View.VISIBLE);
                    paymentGoCoinIdLabelTv.setVisibility(View.VISIBLE);
                }
            }

        }
    }
}
