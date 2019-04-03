package com.smartpesa.smartpesa.activity;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.activity.base.BaseActivity;
import com.smartpesa.smartpesa.helpers.UIHelper;
import com.smartpesa.smartpesa.models.SmartPesaTransactionType;
import com.smartpesa.smartpesa.util.MoneyUtils;
import com.smartpesa.smartpesa.util.constants.SPConstants;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import smartpesa.sdk.models.currency.Currency;

public class TipsAndTaxesActivity extends BaseActivity {

    public static final String NONE = "None";
    public static final String IAC = "IAC";
    public static final String IVA = "IVA";
    @BindView(R.id.amountTv) TextView amountTv;
    @BindView(R.id.taxAmountTv) TextView taxAmountTv;
    @BindView(R.id.tipsAmountTv) TextView tipsAmountTv;
    @BindView(R.id.totalAmountTv) TextView totalAmountTv;
    @BindView(R.id.noneRb) RadioButton noneRb;
    @BindView(R.id.iacRb) RadioButton iacRb;
    @BindView(R.id.ivaRb) RadioButton ivaRb;
    @BindView(R.id.continueBtn) Button continueBtn;
    @BindView(R.id.loyaltySwitch) Switch loyaltySwitch;

    public double amount;
    public SmartPesaTransactionType transactionType;
    double tips;
    int fromAccount, toAccount;
    String description;
    List<RadioButton> taxRadioButtons;
    double taxResult = 0.00, tipsResult = 0.00, grandTotal = 0.00;
    boolean isTaxEntered;
    String taxId;
    String currencySymbol;
    Currency transactionCurrency;

    @Nullable public MoneyUtils mMoneyUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tips_and_taxes);
        ButterKnife.bind(this);

        mMoneyUtils = SmartPesaApplication.merchantComponent(this).provideMoneyUtils();

        amount = this.getIntent().getDoubleExtra(SPConstants.AMOUNT, 0.00);
        transactionType = SmartPesaTransactionType.fromEnumId(this.getIntent().getIntExtra(SPConstants.TRANSACTION_TYPE, -1));
        tips = this.getIntent().getDoubleExtra(SPConstants.CASH_BACK_AMOUNT, 0.00);
        fromAccount = this.getIntent().getIntExtra(SPConstants.FROM_ACCOUNT, 0);
        toAccount = this.getIntent().getIntExtra(SPConstants.TO_ACCOUNT, 0);
        description = this.getIntent().getStringExtra(SPConstants.DESCRIPTION);
        transactionCurrency = (Currency) this.getIntent().getSerializableExtra(SPConstants.TRANSACTION_CURRENCY);

        //set the amount
        if (transactionCurrency != null) {
            currencySymbol = transactionCurrency.getCurrencySymbol();
        } else {
            currencySymbol = getMerchantComponent().provideMerchant().getCurrency().getCurrencySymbol();
        }

        amountTv.setText(mMoneyUtils.format(amount) + " " + currencySymbol);
        tipsAmountTv.setText(mMoneyUtils.format(tips) + " " + currencySymbol);

        setUpTextViews();
        setUpRadioButtons();
        loyaltySwitch.setChecked(false);
        noneRb.setChecked(true);

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isTaxEntered) {
                    UIHelper.showToast(TipsAndTaxesActivity.this, getResources().getString(R.string.select_tax));
                } else {
                    processPayment();
                }
            }
        });

    }

    private void processPayment() {
        Intent paymentIntent = new Intent(TipsAndTaxesActivity.this, SmartPesaPaymentProgressActivity.class);
        double amount = mMoneyUtils.parseBigDecimal(grandTotal).doubleValue();
        paymentIntent.putExtra(SPConstants.AMOUNT, amount);
        paymentIntent.putExtra(SPConstants.CASH_BACK_AMOUNT, 0.00);
        paymentIntent.putExtra(SPConstants.TRANSACTION_TYPE, transactionType.getEnumId());
        paymentIntent.putExtra(SPConstants.FROM_ACCOUNT, fromAccount);
        paymentIntent.putExtra(SPConstants.TO_ACCOUNT, toAccount);
        paymentIntent.putExtra(SPConstants.TIPS, tipsResult);
        paymentIntent.putExtra(SPConstants.TAX_ID, taxId);
        paymentIntent.putExtra(SPConstants.TAX, taxResult);
        paymentIntent.putExtra(SPConstants.TRANSACTION_CURRENCY, transactionCurrency);

        if (loyaltySwitch.isChecked()) {
            paymentIntent.putExtra(SPConstants.IS_WITH_LOYALTY, true);
        } else {
            paymentIntent.putExtra(SPConstants.IS_WITH_LOYALTY, false);
        }

        startActivity(paymentIntent);
    }

    private void setUpTextViews() {
        taxAmountTv.setText(mMoneyUtils.format(0.00) + " " + currencySymbol);
        tipsResult = tips;
        setTotal();
    }

    private void setUpRadioButtons() {
        taxRadioButtons = new ArrayList<RadioButton>();
        taxRadioButtons.add(noneRb);
        taxRadioButtons.add(iacRb);
        taxRadioButtons.add(ivaRb);

        for (RadioButton button : taxRadioButtons) {
            button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    isTaxEntered = true;
                    if (isChecked) processRadioButtons(buttonView);
                }
            });
        }
    }

    //handle radio buttons
    private void processRadioButtons(CompoundButton buttonView) {
        for (RadioButton button : taxRadioButtons) {
            if (button != buttonView) button.setChecked(false);

            //check which RB is pressed
            if (buttonView.getId() == noneRb.getId()) {
                taxAmountTv.setText(mMoneyUtils.format(amount) + " " + currencySymbol);
                calculateTax(0);
                taxId = NONE;
                setTotal();
            } else if (buttonView.getId() == iacRb.getId()) {
                calculateTax(8);
                taxId = IAC;
            } else if (buttonView.getId() == ivaRb.getId()) {
                calculateTax(16);
                taxId = IVA;
            }
        }
    }

    //calculate tax and set in the TAX field
    private void calculateTax(int percentage) {
        taxResult = (amount * percentage) / 100;
        taxAmountTv.setText(mMoneyUtils.format(taxResult) + " " + currencySymbol);
        setTotal();
    }

    //set the grand total
    private void setTotal() {
        grandTotal = amount + taxResult + tipsResult;
        totalAmountTv.setText(mMoneyUtils.format(grandTotal) + " " + currencySymbol);
    }
}