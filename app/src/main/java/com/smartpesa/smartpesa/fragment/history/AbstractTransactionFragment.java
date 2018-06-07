package com.smartpesa.smartpesa.fragment.history;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.activity.SplashActivity;
import com.smartpesa.smartpesa.flavour.PaymentHandler;
import com.smartpesa.smartpesa.fragment.BaseFragment;
import com.smartpesa.smartpesa.fragment.dialog.BluetoothDialogFragment;
import com.smartpesa.smartpesa.fragment.dialog.ExtraDetailsDialogFragment;
import com.smartpesa.smartpesa.fragment.dialog.PrinterDialogFragment;
import com.smartpesa.smartpesa.helpers.UIHelper;
import com.smartpesa.smartpesa.models.ParcelableCardPayment;
import com.smartpesa.smartpesa.models.ParcelableTransactionResponse;
import com.smartpesa.smartpesa.models.SmartPesaTransactionType;
import com.smartpesa.smartpesa.util.DateUtils;
import com.smartpesa.smartpesa.util.MoneyUtils;
import com.smartpesa.smartpesa.util.constants.SPConstants;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import dagger.Lazy;
import smartpesa.sdk.ServiceManager;
import smartpesa.sdk.SmartPesa;
import smartpesa.sdk.core.error.SpException;
import smartpesa.sdk.devices.SpPrinterDevice;
import smartpesa.sdk.error.SpPrinterException;
import smartpesa.sdk.error.SpSessionException;
import smartpesa.sdk.interfaces.PrintingCallback;
import smartpesa.sdk.models.merchant.VerifiedMerchantInfo;
import smartpesa.sdk.models.printing.AbstractPrintingDefinition;
import smartpesa.sdk.models.receipt.GetReceiptCallback;
import smartpesa.sdk.models.transaction.SendNotificationCallback;
import smartpesa.sdk.scanner.PrinterScanningCallback;

public abstract class AbstractTransactionFragment extends BaseFragment implements View.OnClickListener {
    private static final String EXTRA_DETAIL_DIALOG_TAG = "extraDetailsDialog";
    TextView amountLabelTV, amountTV, cardNumberTV, cardHolderLabelTV, cardHolderTV, expiryLabelTV, expiryTV, dateTV, timeTV, statusLabelTV, statusTV;
    TextView transactionLabelTV, transactionTV, verificationLabelTV, notificationLabelTV, failureReasonTV, referenceNumberLabelTV, referenceNumberTV;
    TextView verificationTv, approvalCodeTv, retrievalNumberLabelTV, retrievalNumberTV, noLastTransactionTV;
    EditText smsET, emailET;
    Button sendBTN, printBTN, extraDetailsBtn, voidBtn, refundBtn;
    Context mContext;
    UIHelper font;
    Lazy<ServiceManager> serviceManager;
    UUID transactionID;
    ScrollView transactionScroll;
    RelativeLayout transactionButtonsRL, passOrFailRL;
    LinearLayout noTransactionLL;
    ImageView passOrFailIV, lastTransactionIV;
    LinearLayout failureReasonLL;
    CheckBox smsCB, emailCB;
    String finalPhone = "", finalEmail = "";
    boolean sendSMS, sendEmail = false;
    FrameLayout card;
    ProgressDialog mProgressDialog;
    ImageView cardTypeIV;
    BigDecimal amount;
    private ParcelableTransactionResponse transactionTLVResponse;
    private static final String BLUETOOTH_FRAGMENT_TAG = "bluetooth";
    List<AbstractPrintingDefinition> dataToPrint;

    @Nullable public MoneyUtils mMoneyUtils;
    VerifiedMerchantInfo verifyMerchantInfo;
    @Nullable public PaymentHandler mPaymentHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        serviceManager = SmartPesaApplication.component(getActivity()).serviceManager();

        if (getMerchantComponent(getActivity()) == null) {
            logoutUser();
            return;
        }

        verifyMerchantInfo = getMerchantComponent(getActivity()).provideMerchant();
        mMoneyUtils = SmartPesaApplication.merchantComponent(getActivity()).provideMoneyUtils();
        mPaymentHandler = SmartPesaApplication.flavourComponent(getActivity()).providePaymentHandler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);

        View view = inflater.inflate(R.layout.fragment_last_transaction, container, false);

        initializeComponents(view);

        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setCancelable(false);

        //adding shimmer effect on card
        card = (FrameLayout) view.findViewById(R.id.shimmer_view_container);

        //if the last transaction result is null, hide all the views
        transactionTLVResponse = getTransactionTLVResponse();
        if (transactionTLVResponse != null) {
            if (transactionTLVResponse.getResponseCode() == -1) {
                setUnknownTransaction();
            } else {
                setupValues();
            }
        } else {
            setNoTransaction();
        }
        return view;
    }

    protected abstract ParcelableTransactionResponse getTransactionTLVResponse();

    private void setUnknownTransaction() {
        hideViews();
        noLastTransactionTV.setText(getActivity().getString(R.string.unknown_transaction_prompt));
        lastTransactionIV.setImageResource(R.drawable.ic_unknown_transaction);
    }

    //hide all the views and show that there is no transaction made
    private void setNoTransaction() {
        hideViews();
        noLastTransactionTV.setText(getActivity().getString(R.string.no_last_transaction));
        lastTransactionIV.setImageResource(R.drawable.ic_no_transaction_icon);
    }

    private void hideViews() {
        transactionScroll.setVisibility(View.INVISIBLE);
        transactionButtonsRL.setVisibility(View.INVISIBLE);
        passOrFailRL.setVisibility(View.INVISIBLE);
        card.setVisibility(View.INVISIBLE);
        noTransactionLL.setVisibility(View.VISIBLE);
    }

    //send receipt
    public void sendReceipt() {

        HashMap<String, Object> config = new HashMap<>();
        onBuildReceiptConfig(config);

        String phoneNum = smsET.getText().toString();
        String email = emailET.getText().toString();

        if (validateSending(phoneNum, email)) {

            mProgressDialog.setMessage(mContext.getResources().getString(R.string.sending_receipt));
            mProgressDialog.show();

            serviceManager.get().sendNotification(transactionID, finalEmail, finalPhone, config, new SendNotificationCallback() {
                @Override
                public void onSuccess(Boolean aBoolean) {
                    if (null == getActivity()) return;

                    mProgressDialog.dismiss();

                    UIHelper.showToast(getActivity(), mContext.getResources().getString(R.string.sending_success));
                }

                @Override
                public void onError(SpException exception) {
                    if (exception instanceof SpSessionException) {
                        mProgressDialog.dismiss();
                        UIHelper.showToast(getActivity(), getActivity().getResources().getString(R.string.session_expired));
                        //finish the current activity
                        getActivity().finish();

                        //start the splash screen
                        startActivity(new Intent(getActivity(), SplashActivity.class));
                    } else {
                        if (null == getActivity()) return;

                        UIHelper.log("Error = " + exception.getMessage());
                        mProgressDialog.dismiss();
                        UIHelper.showErrorDialog(mContext, getResources().getString(R.string.app_name), exception.getMessage());
                    }
                }

            });
        }
    }

    //validate the phone and email entered
    public boolean validateSending(String phone, String email) {

        if (sendSMS || sendEmail) {

            if (sendSMS) {
                if (TextUtils.isEmpty(phone) || phone.equals(verifyMerchantInfo.getDefaultDialingCode())) {
                    finalPhone = "";
                    UIHelper.showToast(mContext, mContext.getResources().getString(R.string.enter_valid_number));
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
                    UIHelper.showToast(mContext, mContext.getResources().getString(R.string.enter_valid_email));
                    return false;
                } else {
                    finalEmail = email;
                }
            } else {
                finalEmail = "";
            }
            return true;
        } else {
            UIHelper.showToast(mContext, mContext.getResources().getString(R.string.select_one_option));
            return false;
        }

    }

    //print receipt
    private void printReceipt(final UUID transactionId) {

        SmartPesaTransactionType transactionType = transactionTLVResponse.getTransaction();

        if (transactionType != null && transactionType == SmartPesaTransactionType.BALANCE_INQUIRY) {
            fetchReceiptAndPrint(transactionId, SmartPesa.ReceiptFormat.BALANCE);
        } else {
            handleReceiptPrint();
        }

    }

    protected abstract void handleReceiptPrint();

    protected void fetchReceiptAndPrint(UUID transactionId, SmartPesa.ReceiptFormat receiptFormat) {
        HashMap<String, Object> config = new HashMap<>();
        onBuildReceiptConfig(config);
        final ProgressDialog mp = new ProgressDialog(getActivity());
        mp.setTitle(getString(R.string.app_name));
        mp.setMessage(getString(R.string.loading_receipt));
        mp.show();
        serviceManager.get().getReceipt(transactionId, receiptFormat, config, new GetReceiptCallback() {
            @Override
            public void onSuccess(List<AbstractPrintingDefinition> abstractPrintingDefinitions) {
                if (null == getActivity()) return;
                mp.dismiss();
                dataToPrint = abstractPrintingDefinitions;

                serviceManager.get().scanPrinter(new PrinterScanningCallback() {
                    @Override
                    public void onDeviceListRefresh(Collection<SpPrinterDevice> collection) {
                        if (null == mContext) return;
                        displayPrinterDevice(collection);
                    }

                    @Override
                    public void onScanStopped() {
                        if (null == mContext) return;
                    }

                    @Override
                    public void onScanTimeout() {
                        if (null == mContext) return;
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
                if (null == getActivity()) return;

                if (exception instanceof SpSessionException) {
                    mp.dismiss();
                    //show the expired message
                    UIHelper.showToast(getActivity(), getActivity().getResources().getString(R.string.session_expired));
                    //finish the current activity
                    getActivity().finish();

                    //start the splash screen
                    startActivity(new Intent(getActivity(), SplashActivity.class));
                } else {
                    mp.dismiss();
                    UIHelper.showErrorDialog(mContext, getResources().getString(R.string.app_name), exception.getMessage());
                }
            }
        });
    }

    private void displayPrinterDevice(Collection<SpPrinterDevice> devices) {
        PrinterDialogFragment dialog;
        Fragment fragment = getActivity().getSupportFragmentManager().findFragmentByTag(BLUETOOTH_FRAGMENT_TAG);
        if (fragment == null) {
            dialog = new PrinterDialogFragment();
            dialog.show(getActivity().getSupportFragmentManager(), BLUETOOTH_FRAGMENT_TAG);
        } else {
            dialog = (PrinterDialogFragment) fragment;
        }
        dialog.setSelectedListener(new PrinterSelectedImpl());
        dialog.updateDevices(devices);
    }

    protected abstract void onBuildReceiptConfig(HashMap<String, Object> config);

    //set up the values to be displayed
    private void setupValues() {

        transactionScroll.setVisibility(View.VISIBLE);
        transactionButtonsRL.setVisibility(View.VISIBLE);
        passOrFailRL.setVisibility(View.VISIBLE);

        amount = transactionTLVResponse.getAmount();

        Date dateTime = transactionTLVResponse.getTransactionDatetime();
        String time = DateUtils.format(dateTime, DateUtils.TIME_FORMAT_HH_MM_SS);
        String date = DateUtils.format(dateTime, DateUtils.DATE_FORMAT_DD_MM_YYYY);
        String transactionType = transactionTLVResponse.getTransactionDescription();
        transactionID = transactionTLVResponse.getTransactionId();
        String currencySymbol = transactionTLVResponse.getCurrencySymbol();
        UUID nullPointer = UUID.fromString("00000000-0000-0000-0000-000000000000");

        if (transactionID == null || transactionID.equals(nullPointer)) {
            extraDetailsBtn.setVisibility(View.GONE);
            sendBTN.setVisibility(View.GONE);
            printBTN.setVisibility(View.GONE);
        }

        retrievalNumberTV.setText(String.valueOf(transactionTLVResponse.getRetrievalReferenceNumber()));

        String referenceNumber = transactionTLVResponse.getTransactionReference();

        if (TextUtils.isEmpty(referenceNumber)) {
            referenceNumberTV.setVisibility(View.GONE);
        } else {
            referenceNumberTV.setText(referenceNumber);
        }

        String failureReason = transactionTLVResponse.getResponseDescription();

        String smsNumber = transactionTLVResponse.getNotificationPhone();
        String emailID = transactionTLVResponse.getNotificationEmail();

        if (TextUtils.isEmpty(smsNumber)) {
            smsNumber = verifyMerchantInfo.getDefaultDialingCode();
        }

        SmartPesa.SettlementCode settlementCode = transactionTLVResponse.getSettlementCode();
        if (settlementCode.equals(SmartPesa.SettlementCode.SETTLEMENT_PENDING)) {
            voidBtn.setEnabled(true);
        } else {
            voidBtn.setEnabled(false);
        }

        //check and hide refund
        int[] permissions = getMerchantComponent(getActivity()).provideMerchant().getMenuControl();
        if (permissions != null && permissions.length > 0) {
            for (int i = 0; i < permissions.length; i++) {
                int permission = permissions[i];
                UIHelper.log("Permission = " + permission);
                if (permission == 2) {
                    refundBtn.setVisibility(View.VISIBLE);
                }
            }
        }

        //check if the transaction is a success or failure
        if (!TextUtils.isEmpty(transactionTLVResponse.getAuthorisationResponseCode()) && transactionTLVResponse.getAuthorisationResponseCode().equals("00")) {
            passOrFailRL.setBackground(mContext.getResources().getDrawable(R.drawable.green_circle));
            passOrFailIV.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_done_white_24dp));

            //set text as approved
            failureReasonTV.setText(mContext.getResources().getString(R.string.approved));
            failureReasonTV.setTextColor(mContext.getResources().getColor(R.color.pesa_green));
        } else {
            passOrFailRL.setBackground(mContext.getResources().getDrawable(R.drawable.red_circle));
            passOrFailIV.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_clear_white_24dp));

            //show the reason for failure
            failureReasonLL.setVisibility(View.VISIBLE);
            failureReasonTV.setText(failureReason);
            failureReasonTV.setTextColor(mContext.getResources().getColor(R.color.pesa_red));
            voidBtn.setEnabled(false);
            refundBtn.setEnabled(false);
        }

        if (transactionTLVResponse.getTransaction() == SmartPesaTransactionType.BALANCE_INQUIRY) {
            amountTV.setVisibility(View.INVISIBLE);
            amountLabelTV.setVisibility(View.INVISIBLE);
        } else {
            amountTV.setText(currencySymbol + " " + mMoneyUtils.format(amount));
        }

        dateTV.setText(date);
        timeTV.setText(time);

        transactionTV.setText(transactionType);

        smsET.setText(smsNumber);
        emailET.setText(emailID);

        //set the email and sms checkbox checked if the email or sms fields are available
        if (smsNumber != null) {
            if (!TextUtils.isEmpty(smsNumber) && !smsNumber.equals(verifyMerchantInfo.getDefaultDialingCode())) {
                smsCB.setChecked(true);
                toggleSendButtonOnSms();
            }
        }

        if (emailID != null) {
            if (!TextUtils.isEmpty(emailID)) {
                emailCB.setChecked(true);
                toggleSendButtonOnSms();
            }
        }

        ParcelableCardPayment cardPayment = transactionTLVResponse.getCardPayment();

        if (cardPayment != null) {

            String cardNumber = cardPayment.getCardNumber();
            String cardHolderName = cardPayment.getCardHolderName();
            SmartPesa.Method method = SmartPesa.Method.fromEnumId(cardPayment.getMethod());
            String approvalCode = cardPayment.getApprovalCode();
            int cardIcon = cardPayment.getCardIcon();

            if (method != null) {
                String methodType = "";
                //check the type of card used
                if (method.equals(SmartPesa.Method.MAGNETIC)) {
                    methodType = "Magnetic";
                } else if (method.equals(SmartPesa.Method.ICC)) {
                    methodType = "Chip";
                } else if (method.equals(SmartPesa.Method.CONTACTLESS)) {
                    methodType = "Tap";
                }

                statusTV.setText(methodType);
            }

            cardHolderTV.setText(cardHolderName);

            String cvmDescription = transactionTLVResponse.getCardPayment().getCvmDescription();

            if (!TextUtils.isEmpty(cardNumber)) {
                cardNumberTV.setText(cardNumber);
            }

            if (!TextUtils.isEmpty(cardHolderName)) {
                cardHolderTV.setText(cardHolderName);
            }

            if (TextUtils.isEmpty(cvmDescription)) {
                verificationLabelTV.setVisibility(View.INVISIBLE);
                verificationTv.setVisibility(View.INVISIBLE);
            } else {
                verificationTv.setText(cvmDescription);
            }

            if (!TextUtils.isEmpty(approvalCode)) {
                approvalCodeTv.setText(approvalCode);
            }

            if (cardIcon > 0) {
                cardTypeIV.setImageResource(cardIcon);
            }

        }

    }

    //initialize the UI components
    private void initializeComponents(View view) {

        amountLabelTV = (TextView) view.findViewById(R.id.amount_label_tv);
        amountTV = (TextView) view.findViewById(R.id.amount_value_tv);
        cardNumberTV = (TextView) view.findViewById(R.id.cardNumber_RECEIPT_TV);
        cardHolderLabelTV = (TextView) view.findViewById(R.id.cardHolderLabel_RECEIPT_TV);
        cardHolderTV = (TextView) view.findViewById(R.id.card_holder_name);
        expiryLabelTV = (TextView) view.findViewById(R.id.expiryLabel_RECEIPT_TV);
        expiryTV = (TextView) view.findViewById(R.id.expiry_value_tv);
        dateTV = (TextView) view.findViewById(R.id.dateTv);
        timeTV = (TextView) view.findViewById(R.id.timeTv);
        statusLabelTV = (TextView) view.findViewById(R.id.statusLabel_RECEIPT_TV);
        statusTV = (TextView) view.findViewById(R.id.methodTv);
        approvalCodeTv = (TextView) view.findViewById(R.id.approvalCodeTv);
        transactionLabelTV = (TextView) view.findViewById(R.id.transactionTypeLabel_RECEIPT_TV);
        transactionTV = (TextView) view.findViewById(R.id.transactionType_RECEIPT_TV);
        verificationLabelTV = (TextView) view.findViewById(R.id.verificationLabel_RECEIPT_TV);
        notificationLabelTV = (TextView) view.findViewById(R.id.notificationLabel_RECEIPT_TV);
        failureReasonTV = (TextView) view.findViewById(R.id.failureReasonTV);
        referenceNumberLabelTV = (TextView) view.findViewById(R.id.referenceLabelTV);
        referenceNumberTV = (TextView) view.findViewById(R.id.transactionRefTv);
        retrievalNumberLabelTV = (TextView) view.findViewById(R.id.transactionRetrievalNumberLabelTV);
        retrievalNumberTV = (TextView) view.findViewById(R.id.retrievalNumberTv);
        noLastTransactionTV = (TextView) view.findViewById(R.id.no_transaction_tv);

        transactionScroll = (ScrollView) view.findViewById(R.id.transactionScroll);

        transactionButtonsRL = (RelativeLayout) view.findViewById(R.id.transactionButtonsRL);
        passOrFailRL = (RelativeLayout) view.findViewById(R.id.passOrFailRL);

        noTransactionLL = (LinearLayout) view.findViewById(R.id.no_transaction_container);
        passOrFailIV = (ImageView) view.findViewById(R.id.passOrFailIV);

        smsET = (EditText) view.findViewById(R.id.smsLabel_RECEIPT_ET);
        emailET = (EditText) view.findViewById(R.id.emailLabel_RECEIPT_ET);

        verificationTv = (TextView) view.findViewById(R.id.verificationTv);
        sendBTN = (Button) view.findViewById(R.id.send_RECEIPT_BTN);
        printBTN = (Button) view.findViewById(R.id.print_RECEIPT_BTN);
        extraDetailsBtn = (Button) view.findViewById(R.id.extraDetails);
        voidBtn = (Button) view.findViewById(R.id.voidBtn);
        refundBtn = (Button) view.findViewById(R.id.refundBtn);

        //hide refund button, show only if has permission to refund
        refundBtn.setVisibility(View.GONE);

        failureReasonLL = (LinearLayout) view.findViewById(R.id.failureReasonLL);

        cardTypeIV = (ImageView) view.findViewById(R.id.cardTypeIV);
        lastTransactionIV = (ImageView) view.findViewById(R.id.last_transaction_iv);

        smsCB = (CheckBox) view.findViewById(R.id.smsCB);
        emailCB = (CheckBox) view.findViewById(R.id.emailCB);
        smsCB.setOnClickListener(this);
        emailCB.setOnClickListener(this);

        printBTN.setOnClickListener(this);
        sendBTN.setOnClickListener(this);
        extraDetailsBtn.setOnClickListener(this);
        voidBtn.setOnClickListener(this);
        refundBtn.setOnClickListener(this);

        //setting fonts here
        font = new UIHelper(mContext);
        amountLabelTV.setTypeface(font.regularFont);
        amountTV.setTypeface(font.boldFont);
        cardNumberTV.setTypeface(font.ocrFont);
        cardHolderLabelTV.setTypeface(font.regularFont);
        cardHolderTV.setTypeface(font.ocrFont);
        expiryLabelTV.setTypeface(font.regularFont);
        expiryTV.setTypeface(font.ocrFont);
        statusLabelTV.setTypeface(font.regularFont);
        statusTV.setTypeface(font.boldFont);
        transactionLabelTV.setTypeface(font.regularFont);
        transactionTV.setTypeface(font.boldFont);
        verificationLabelTV.setTypeface(font.regularFont);
        notificationLabelTV.setTypeface(font.regularFont);
        smsET.setTypeface(font.boldFont);
        emailET.setTypeface(font.boldFont);
        sendBTN.setTypeface(font.boldFont);
        printBTN.setTypeface(font.boldFont);
        referenceNumberLabelTV.setTypeface(font.regularFont);
        referenceNumberTV.setTypeface(font.boldFont);
        failureReasonTV.setTypeface(font.boldFont);
        retrievalNumberTV.setTypeface(font.boldFont);
        retrievalNumberLabelTV.setTypeface(font.regularFont);
        dateTV.setTypeface(font.boldFont);
        timeTV.setTypeface(font.boldFont);
        approvalCodeTv.setTypeface(font.boldFont);
        verificationTv.setTypeface(font.boldFont);

        sendBTN.setEnabled(false);

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

                String diallingCode = verifyMerchantInfo.getDefaultDialingCode();
                String smsText = s.toString();
                if (smsText.equals(diallingCode) || TextUtils.isEmpty(smsText)) {
                    smsCB.setChecked(false);
                    toggleSendButtonOnSms();
                } else {
                    smsCB.setChecked(true);
                    toggleSendButtonOnSms();
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
                    String diallingCode = verifyMerchantInfo.getDefaultDialingCode();
                    String smsText = smsET.getText().toString();
                    if (smsText.equals(diallingCode) || TextUtils.isEmpty(smsText)) {
                        UIHelper.showToast(getActivity(), "Please enter a number first");
                        sendBTN.setEnabled(false);
                        smsCB.setChecked(false);
                        sendSMS = false;
                    } else {
                        sendSMS = true;
                        sendBTN.setEnabled(true);
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
                        UIHelper.showToast(getActivity(), "Please enter an email address first");
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

    private void toggleSendButtonOnEmail() {
        String emailText = emailET.getText().toString();
        if (TextUtils.isEmpty(emailText)) {
            sendBTN.setEnabled(false);
        } else {
            sendBTN.setEnabled(true);
        }
    }

    private void toggleSendButtonOnSms() {
        String diallingCode = verifyMerchantInfo.getDefaultDialingCode();
        String smsText = smsET.getText().toString();
        if (smsText.equals(diallingCode) || TextUtils.isEmpty(smsText)) {
            sendBTN.setEnabled(false);
        } else {
            sendBTN.setEnabled(true);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.print_RECEIPT_BTN:
                printReceipt(transactionID);
                break;
            case R.id.send_RECEIPT_BTN:
                sendReceipt();
                break;
            case R.id.smsCB:
                if (smsCB.isChecked()) {
                    sendSMS = true;
                } else {
                    sendSMS = false;
                }
                break;
            case R.id.emailCB:
                if (emailCB.isChecked()) {
                    sendEmail = true;
                } else {
                    sendEmail = false;
                }
                break;
            case R.id.extraDetails:
                ExtraDetailsDialogFragment extraDetailsDialogFragment = new ExtraDetailsDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putString("transactionId", transactionID.toString());
                bundle.putParcelable("transactionResult", transactionTLVResponse);
                extraDetailsDialogFragment.setArguments(bundle);
                extraDetailsDialogFragment.show(getActivity().getSupportFragmentManager(), EXTRA_DETAIL_DIALOG_TAG);
                break;
            case R.id.voidBtn:
                performTransaction(SmartPesaTransactionType.VOID);
                break;
            case R.id.refundBtn:
                performTransaction(SmartPesaTransactionType.REFUND);
                break;
            default:
                break;
        }
    }

    private void performTransaction(final SmartPesaTransactionType transactionType) {
        Bundle paymentBundle = new Bundle();
        paymentBundle.putDouble(SPConstants.AMOUNT, amount.doubleValue());
        paymentBundle.putDouble(SPConstants.CASH_BACK_AMOUNT, BigDecimal.ZERO.doubleValue());
        paymentBundle.putInt(SPConstants.TRANSACTION_TYPE, transactionType.getEnumId());
        paymentBundle.putInt(SPConstants.FROM_ACCOUNT, transactionTLVResponse.getCardPayment().getFromAccount().getEnumId());
        paymentBundle.putInt(SPConstants.TO_ACCOUNT, transactionTLVResponse.getCardPayment().getToAccount().getEnumId());

        if (transactionType.equals(SmartPesaTransactionType.VOID)) {
            paymentBundle.putString(SPConstants.TRANSACTION_ID, transactionTLVResponse.getTransactionId().toString());
        }

        if (mPaymentHandler != null) {
            mPaymentHandler.onButtonClick(paymentBundle, getActivity());
        }

        getActivity().finish();
    }

    //close the printer bluetooth list if already one is present
    private void closeDialogFragment() {
        Fragment dialogBluetoothList = getActivity().getSupportFragmentManager().findFragmentByTag(BLUETOOTH_FRAGMENT_TAG);
        if (dialogBluetoothList != null) {
            DialogFragment dialogFragment = (DialogFragment) dialogBluetoothList;
            if (dialogFragment != null) {
                dialogFragment.dismiss();
            }
        }
    }

    private class PrinterSelectedImpl implements BluetoothDialogFragment.TerminalSelectedListener<SpPrinterDevice> {
        @Override
        public void onSelected(SpPrinterDevice device) {
            performPrint(device);
        }
    }

    private void performPrint(SpPrinterDevice device) {
        closeDialogFragment();
        serviceManager.get().performPrint(SmartPesa.PrintingParam.withData(dataToPrint).printerDevice(device).build(), new PrintingCallback() {
            @Override
            public void onPrinterError(SpPrinterException errorMessage) {
                if (null == mContext) return;
                UIHelper.showErrorDialog(mContext, mContext.getResources().getString(R.string.app_name), errorMessage.getMessage());
            }

            @Override
            public void onPrinterSuccess() {
                if (null == mContext) return;
                UIHelper.log("here");
                closeDialogFragment();
            }
        });
    }
}