package com.smartpesa.smartpesa.fragment.payment;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.flavour.PaymentHandler;
import com.smartpesa.smartpesa.fragment.BaseFragment;
import com.smartpesa.smartpesa.fragment.dialog.MoreDialog;
import com.smartpesa.smartpesa.helpers.UIHelper;
import com.smartpesa.smartpesa.models.SmartPesaTransactionType;
import com.smartpesa.smartpesa.util.MoneyUtils;
import com.smartpesa.smartpesa.util.SmallCalculator;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import dagger.Lazy;
import smartpesa.sdk.ServiceManager;
import smartpesa.sdk.models.merchant.VerifiedMerchantInfo;

public abstract class AbstractPaymentFragment extends BaseFragment implements View.OnClickListener {

    public static final String KEY_CONVENIENCE_FEE = "convenience_fee";

    protected static final String BUNDLE_KEY_TITLE = AbstractPaymentFragment.class.getName() + ".title";
    protected static final String BUNDLE_KEY_TRANSACTION_TYPE = AbstractPaymentFragment.class.getName() + ".transaction";
    protected static final String BUNDLE_KEY_FROM_ACCOUNT = AbstractPaymentFragment.class.getName() + ".fromAccount";
    protected static final String BUNDLE_KEY_TO_ACCOUNT = AbstractPaymentFragment.class.getName() + ".toAccount";
    public static final int MAX_DIGIT_LENGTH = 11;
    public static final String ZERO = "0";
    public static final int REQUEST_CODE = 101;

    @Bind(R.id.amount_PAYMENT_ET) EditText amountPaymentET;
    @Bind(R.id.cashBack_ET) EditText cashBackAmountET;
    @Bind(R.id.currencyLabel_PAYMENT_TV) TextView currencyLabelTV;
    @Bind(R.id.cashBackCurrencyLabelTV) TextView cashBackCurrencyLabelTV;
    @Bind(R.id.one_PAYMENT_BTN) Button oneBTN;
    @Bind(R.id.two_PAYMENT_BTN) Button twoBTN;
    @Bind(R.id.three_PAYMENT_BTN) Button threeBTN;
    @Bind(R.id.four_PAYMENT_BTN) Button fourBTN;
    @Bind(R.id.five_PAYMENT_BTN) Button fiveBTN;
    @Bind(R.id.six_PAYMENT_BTN) Button sixBTN;
    @Bind(R.id.seven_PAYMENT_BTN) Button sevenBTN;
    @Bind(R.id.eight_PAYMENT_BTN) Button eightBTN;
    @Bind(R.id.nine_PAYMENT_BTN) Button nineBTN;
    @Bind(R.id.zero_PAYMENT_BTN) Button zeroBTN;
    @Bind(R.id.plus_PAYMENT_BTN) Button plusBTN;
    @Bind(R.id.minus_PAYMENT_BTN) Button minusBTN;
    @Bind(R.id.equal_PAYMENT_BTN) Button equalBTN;
    @Bind(R.id.clear_PAYMENT_BTN) Button clearBTN;
    @Bind(R.id.backSpace_PAYMENT_BTN) ImageButton backSpaceBTN;
    @Bind(R.id.continue_PAYMENT_BTN) Button continueBTN;
    @Bind(R.id.alipayScanBtn) Button alipayScanBtn;
    @Bind(R.id.alipayPayBtn) Button alipayPayBtn;

    @Bind(R.id.amountSelectedRL) RelativeLayout amountSelectedRL;
    @Bind(R.id.cashBackSelectedRL) RelativeLayout cashBackSelectedRL;
    @Bind(R.id.amount_PAYMENT_RL) RelativeLayout amountRL;
    @Bind(R.id.alipayContinueLl) LinearLayout alipayButtonsLl;

    Context mContext;
    Lazy<ServiceManager> serviceManager;
    VerifiedMerchantInfo mVerifyMerchantInfo;
    String mStringAmount;
    private SmallCalculator mSmallCalculator;

    public String currencySymbol;
    public SmartPesaTransactionType transactionType;

    int mFromAccount, mToAccount;
    String description;

    @Nullable public PaymentHandler mPaymentHandler;
    @Nullable public MoneyUtils mMoneyUtils;

    public AbstractPaymentFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mContext = getActivity();
        if (getMerchantComponent(getActivity()) == null) {
            logoutUser();
            return;
        }
        serviceManager = SmartPesaApplication.component(getActivity()).serviceManager();
        mVerifyMerchantInfo = getMerchantComponent(getActivity()).provideMerchant();

        if (mVerifyMerchantInfo == null) {
            logoutUser();
            return;
        }

        mFromAccount = UIHelper.getAccountPositionFromEnum(mVerifyMerchantInfo.getPlatformPermissions().getDefaultFromAccountType());
        transactionType = SmartPesaTransactionType.fromEnumId(getArguments().getInt(BUNDLE_KEY_TRANSACTION_TYPE));
        mFromAccount = getArguments().getInt(BUNDLE_KEY_FROM_ACCOUNT);
        mToAccount = getArguments().getInt(BUNDLE_KEY_TO_ACCOUNT);

        mSmallCalculator = new SmallCalculator();
        resetAmount();

        mPaymentHandler = SmartPesaApplication.flavourComponent(getActivity()).providePaymentHandler();
        mMoneyUtils = SmartPesaApplication.merchantComponent(getActivity()).provideMoneyUtils();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_payment, container, false);

        initializeComponents(view);

        //get the currency from SDK
        currencySymbol = mVerifyMerchantInfo.getCurrency().getCurrencySymbol();
        currencyLabelTV.setText(currencySymbol);
        cashBackCurrencyLabelTV.setText(currencySymbol);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showFormattedAmount(mMoneyUtils.getZero());
    }

    public void processPayment() {

        String value = amountPaymentET.getText().toString();

        double cashBackAmount = getCashbackAmount();
        double amount = mMoneyUtils.parseBigDecimal(value).doubleValue();
        //check if amount is 0.00
        if (amount != 0.00) {
            //check for internet connectivity
            if (UIHelper.isOnline(mContext)) {

                Bundle paymentBundle = new Bundle();
                paymentBundle.putDouble("amount", amount);
                paymentBundle.putDouble("cashBackAmount", cashBackAmount);
                paymentBundle.putInt("transactionType", transactionType.getEnumId());
                paymentBundle.putInt("fromAccount", mFromAccount);
                paymentBundle.putInt("toAccount", mToAccount);
                onBuildPaymentDescription(paymentBundle);

                if(mPaymentHandler != null){
                    mPaymentHandler.onButtonClick(paymentBundle, getActivity());
                }

            } else {
                UIHelper.showErrorDialog(mContext, getResources().getString(R.string.app_name), getResources().getString(R.string.internet_not_connected));
            }
        } else {
            UIHelper.showToast(mContext, mContext.getResources().getString(R.string.please_enter_amount));
        }
    }

    protected void onBuildPaymentDescription(Bundle paymentBundle) {
        if (description != null) {
            paymentBundle.putString("description", description);
        } else {
            paymentBundle.putString("description", "");
        }
    }

    protected double getCashbackAmount() {
        return 0.00;
    }

    private void initializeComponents(View view) {

        ButterKnife.bind(this, view);

        amountSelectedRL.setVisibility(View.INVISIBLE);

        oneBTN.setOnClickListener(this);
        twoBTN.setOnClickListener(this);
        threeBTN.setOnClickListener(this);
        fourBTN.setOnClickListener(this);
        fiveBTN.setOnClickListener(this);
        sixBTN.setOnClickListener(this);
        sevenBTN.setOnClickListener(this);
        eightBTN.setOnClickListener(this);
        nineBTN.setOnClickListener(this);
        zeroBTN.setOnClickListener(this);
        plusBTN.setOnClickListener(this);
        minusBTN.setOnClickListener(this);
        equalBTN.setOnClickListener(this);
        clearBTN.setOnClickListener(this);
        backSpaceBTN.setOnClickListener(this);
        continueBTN.setOnClickListener(this);
    }

    //onclick listener
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.one_PAYMENT_BTN:
                appendDigit("1");
                break;
            case R.id.two_PAYMENT_BTN:
                appendDigit("2");
                break;
            case R.id.three_PAYMENT_BTN:
                appendDigit("3");
                break;
            case R.id.four_PAYMENT_BTN:
                appendDigit("4");
                break;
            case R.id.five_PAYMENT_BTN:
                appendDigit("5");
                break;
            case R.id.six_PAYMENT_BTN:
                appendDigit("6");
                break;
            case R.id.seven_PAYMENT_BTN:
                appendDigit("7");
                break;
            case R.id.eight_PAYMENT_BTN:
                appendDigit("8");
                break;
            case R.id.nine_PAYMENT_BTN:
                appendDigit("9");
                break;
            case R.id.zero_PAYMENT_BTN:
                appendDigit("0");
                break;
            case R.id.clear_PAYMENT_BTN:
                clearCalculation();
                break;
            case R.id.backSpace_PAYMENT_BTN:
                backSpace();
                break;
            case R.id.plus_PAYMENT_BTN:
                addAmount();
                break;
            case R.id.equal_PAYMENT_BTN:
                equalAmount();
                break;
            case R.id.minus_PAYMENT_BTN:
                subAmount();
                break;
            case R.id.continue_PAYMENT_BTN:
                processPayment();
                break;
            default:
                break;
        }
    }

    protected void showFormattedAmount(String amount) {
        amountPaymentET.setText(amount);
    }

    //equals operation
    protected void equalAmount() {
        String value = getCurrentDisplayValue();
        value = mMoneyUtils.format(getCalculator().equal(mMoneyUtils.parseBigDecimal(value)));
        showFormattedAmount(value);
    }

    //addition operation
    protected void addAmount() {
        String value = getCurrentDisplayValue();
        value = mMoneyUtils.format(getCalculator().add(mMoneyUtils.parseBigDecimal(value)));
        showFormattedAmount(value);
        resetAmount();
    }

    //subtract operation
    protected void subAmount() {
        String value = getCurrentDisplayValue();
        value = mMoneyUtils.format(getCalculator().sub(mMoneyUtils.parseBigDecimal(value)));
        showFormattedAmount(value);
        resetAmount();
    }

    @NonNull
    protected String getCurrentDisplayValue() {
        return amountPaymentET.getText().toString();
    }

    //clear the calculator
    protected void clearCalculation() {
        resetAmount();
        getCalculator().clear();
        showFormattedAmount(mMoneyUtils.getZero());
    }

    protected void resetAmount() {
        mStringAmount = "";
    }

    protected void backSpace() {
        if (mStringAmount.length() != 0) {
            mStringAmount = doBackspace(mStringAmount);
            displayCurrentStringAmount();
        }
    }

    @NonNull
    protected String doBackspace(String target) {
        return target.substring(0, target.length() - 1);
    }

    protected void appendDigit(String digit) {
        if (mStringAmount.length() < MAX_DIGIT_LENGTH) {
            mStringAmount = doAppend(mStringAmount, digit);
            displayCurrentStringAmount();
        }
    }

    protected String doAppend(String target, String toBe) {
        StringBuilder sb = new StringBuilder();
        if (!TextUtils.isEmpty(target)) {
            sb.append(target);
            sb.append(toBe);
        } else if (!toBe.equals(ZERO)) {
            sb.append(toBe);
        }
        return sb.toString();
    }

    protected void displayCurrentStringAmount() {
        showFormattedAmount(formatCurrent(mStringAmount));
    }

    protected String formatCurrent(String amount) {
        return mMoneyUtils.format(mMoneyUtils.parseBigDecimal(mMoneyUtils.padding(amount)));
    }

    //more fragment dialog box
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.payment_more:
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                MoreDialog moreDialog = new MoreDialog();
                moreDialog.DismissListener(closeListener);
                moreDialog.show(fragmentManager, "dialog_more");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public interface MyDialogCloseListener {
        public void handleDialogClose(DialogInterface dialog, int fromAccount, String description);
    }

    final MyDialogCloseListener closeListener = new MyDialogCloseListener() {
        @Override
        public void handleDialogClose(DialogInterface dialog, int fromAcc, String desc) {
            mFromAccount = fromAcc;
            description = desc;
        }
    };

    //set title of fragment
    @Override
    public void onResume() {
        super.onResume();
        android.support.v7.app.ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(UIHelper.getTitleFromTransactionType(getActivity(), transactionType));
        }
    }

    public SmallCalculator getCalculator() {
        return mSmallCalculator;
    }
}
