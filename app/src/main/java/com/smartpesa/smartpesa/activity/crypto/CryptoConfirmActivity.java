package com.smartpesa.smartpesa.activity.crypto;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.activity.base.BaseActivity;
import com.smartpesa.smartpesa.activity.payment.AllPaymentsProgressActivity;
import com.smartpesa.smartpesa.persistence.MerchantModule;
import com.smartpesa.smartpesa.util.MoneyUtils;
import com.smartpesa.smartpesa.util.constants.SPConstants;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.Lazy;
import smartpesa.sdk.ServiceManager;
import smartpesa.sdk.models.currency.Currency;

public class CryptoConfirmActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.cryptoNameTv)
    TextView cryptoNameTv;
    @BindView(R.id.cryptoValueTv) TextView cryptoValueTv;
    @BindView(R.id.amountTv) TextView amountTv;
    @BindView(R.id.walletAddressTv) TextView walletAddressTv;
    @BindView(R.id.cashBtn) Button cashBtn;
    @BindView(R.id.cardBtn) Button cardBtn;

    public MoneyUtils mMoneyUtils;
    Lazy<ServiceManager> serviceManager;
    Bundle bundle;
    String finalCryptoAddress;
    int cryptoType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crypto_confirm);
        ButterKnife.bind(this);

        //setup toolbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        bundle = getIntent().getBundleExtra(SPConstants.BUNDLE);
        cryptoType = bundle.getInt(SPConstants.CRYPTO_ATM_CRYPTO_TYPE);
        serviceManager = SmartPesaApplication.component(CryptoConfirmActivity.this).serviceManager();
        mMoneyUtils = getMerchantComponent().provideMoneyUtils();

        if (cryptoType == MerchantModule.MENU_ID_CRYPTO_ATM_BITCOIN) {
            setTitle(R.string.buy_bitcoin);
        } else if (cryptoType == MerchantModule.MENU_ID_CRYPTO_ATM_LITECOIN) {
            setTitle(R.string.buy_litecoin);
        }

        setUpValues();
    }

    private void setUpValues() {

        if (bundle != null) {

            double amount = bundle.getDouble(SPConstants.AMOUNT, 0.00);
            String btc =  bundle.getString(SPConstants.CRYPTO_ATM_CRYPTO_VALUE);
            Currency currency = (Currency) bundle.getSerializable(SPConstants.TRANSACTION_CURRENCY);

            amountTv.setText(currency.getCurrencySymbol() + " " + mMoneyUtils.format(amount));

            if (cryptoType == MerchantModule.MENU_ID_CRYPTO_ATM_LITECOIN) {
                cryptoValueTv.setText(btc + " LTC");
            } else if (cryptoType == MerchantModule.MENU_ID_CRYPTO_ATM_BITCOIN) {
                cryptoValueTv.setText(btc + " BTC");
            }

            cashBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startPayment();
                }
            });

            cardBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startPayment();
                }
            });

            String qrScannedString = bundle.getString(SPConstants.CRYPTO_ATM_QR_TEXT);

            if (!TextUtils.isEmpty(qrScannedString)) {
                int index = qrScannedString.indexOf(":");

                if (index != -1) {

                    String trimmed = qrScannedString.substring(index+1, qrScannedString.length());

                    int trimmedIndex = trimmed.indexOf("?");

                    if (trimmedIndex != -1) {

                        String trimEnd = trimmed.substring(0, trimmedIndex);

                        walletAddressTv.setText(trimEnd);
                        finalCryptoAddress = trimEnd;

                    } else {
                        walletAddressTv.setText(trimmed);
                        finalCryptoAddress = trimmed;
                    }

                } else {
                    walletAddressTv.setText(qrScannedString);
                    finalCryptoAddress = qrScannedString;
                }
            }
        }
    }

    private void startPayment() {
        Intent paymentIntent = new Intent(this, AllPaymentsProgressActivity.class);
        bundle.putString(SPConstants.CRYPTO_ATM_QR_TEXT, finalCryptoAddress);
        paymentIntent.putExtras(bundle);
        startActivity(paymentIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
