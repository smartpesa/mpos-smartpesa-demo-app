package com.smartpesa.smartpesa.activity.crypto;

import android.app.ProgressDialog;
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
import com.smartpesa.smartpesa.activity.AliPayQRScanActivity;
import com.smartpesa.smartpesa.activity.SplashActivity;
import com.smartpesa.smartpesa.activity.base.BaseActivity;
import com.smartpesa.smartpesa.helpers.UIHelper;
import com.smartpesa.smartpesa.persistence.MerchantModule;
import com.smartpesa.smartpesa.util.MoneyUtils;
import com.smartpesa.smartpesa.util.constants.SPConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import dagger.Lazy;
import smartpesa.sdk.ServiceManager;
import smartpesa.sdk.core.error.SpException;
import smartpesa.sdk.error.SpSessionException;
import smartpesa.sdk.models.ExtraDataContainer;
import smartpesa.sdk.models.currency.Currency;
import smartpesa.sdk.models.provider.GetProviderExtraDataCallback;

public class CryptoInfoActivity extends BaseActivity {

    public static final String ACTION = "action";
    public static final String RATE = "rate";
    public static final String CRYPTO_SYMBOL = "crypto_currency_symbol";
    public static final String FIAT_SYMBOL = "fiat_currency_symbol";
    public static final String BIT_STAMP = "BitStamp";
    public static final String BTC = "btc";
    public static final String LTC = "ltc";
    public static final String ASK = "ask";

    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.cryptoNameTv) TextView cryptoNameTv;
    @Bind(R.id.cryptoPriceTv) TextView cryptoPriceTv;
    @Bind(R.id.amountTv) TextView amountTv;
    @Bind(R.id.marginTv) TextView marginTv;
    @Bind(R.id.totalTv) TextView totalTv;
    @Bind(R.id.cryptoValueTv) TextView cryptoValueTv;
    @Bind(R.id.continueBtn) Button continueBtn;

    public MoneyUtils mMoneyUtils;
    Lazy<ServiceManager> serviceManager;
    Bundle bundle;
    double btc;
    Currency currency;
    int cryptoType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crypto_info);
        ButterKnife.bind(this);

        //setup toolbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        bundle = getIntent().getBundleExtra(SPConstants.BUNDLE);

        if (bundle != null) {
            currency = (Currency) bundle.getSerializable(SPConstants.TRANSACTION_CURRENCY);
            cryptoType = bundle.getInt(SPConstants.CRYPTO_ATM_CRYPTO_TYPE);
        }

        serviceManager = SmartPesaApplication.component(CryptoInfoActivity.this).serviceManager();
        mMoneyUtils = getMerchantComponent().provideMoneyUtils();

        if (cryptoType == MerchantModule.MENU_ID_CRYPTO_ATM_BITCOIN) {
            setTitle(R.string.buy_bitcoin);
        } else if (cryptoType == MerchantModule.MENU_ID_CRYPTO_ATM_LITECOIN) {
            setTitle(R.string.buy_litecoin);
        }

        continueBtn.setEnabled(false);
        getCrptoPrice();


    }

    private void getCrptoPrice() {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Getting crypto price...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put(ACTION, RATE);

        if (cryptoType == MerchantModule.MENU_ID_CRYPTO_ATM_BITCOIN) {
            hashMap.put(CRYPTO_SYMBOL, BTC);
            cryptoNameTv.setText(R.string.bitcoin_price);
        } else if (cryptoType == MerchantModule.MENU_ID_CRYPTO_ATM_LITECOIN) {
            hashMap.put(CRYPTO_SYMBOL, LTC);
            cryptoNameTv.setText(R.string.litecoin_price);
        }

        hashMap.put(FIAT_SYMBOL, currency.getCurrencySymbol());

        serviceManager.get().getProviderExtraData(BIT_STAMP, hashMap, new GetProviderExtraDataCallback() {
            @Override
            public void onSuccess(ExtraDataContainer extraDataContainer) {
                if (isActivityDestroyed()) return;
                progressDialog.dismiss();
                processBTCData(extraDataContainer);
            }

            @Override
            public void onError(SpException e) {
                if (isActivityDestroyed()) return;

                progressDialog.dismiss();
                if (e instanceof SpSessionException) {
                    //show the expired message
                    UIHelper.showToast(CryptoInfoActivity.this, getResources().getString(R.string.session_expired));
                    //finish the current activity
                    finish();

                    //start the splash screen
                    startActivity(new Intent(CryptoInfoActivity.this, SplashActivity.class));
                } else {
                    UIHelper.showErrorDialog(CryptoInfoActivity.this, getResources().getString(R.string.app_name), e.getMessage());
                }
            }
        });
    }

    private void processBTCData(ExtraDataContainer extraDataContainer) {
        if (extraDataContainer != null) {

            String rawData = extraDataContainer.raw;

            if (!TextUtils.isEmpty(rawData)) {

                try {
                    JSONObject jsonObject = new JSONObject(rawData);

                    if (jsonObject != null) {

                        if (jsonObject.has(ASK)) {

                            String ask = jsonObject.getString(ASK);

                            if (!TextUtils.isEmpty(ask)) {
                                setUpValues(ask);
                            } else {
                                showBTCFailed();
                            }

                        } else {
                            showBTCFailed();
                        }

                    } else {
                        showBTCFailed();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    showBTCFailed();
                }

            } else {
                showBTCFailed();
            }

        } else {
            showBTCFailed();
        }
    }

    private void setUpValues(String ask) {

        if (bundle != null) {
            BigDecimal askAmount = mMoneyUtils.parseBigDecimal(ask);

            double amount = bundle.getDouble(SPConstants.AMOUNT, 0.00);
            double margin = bundle.getDouble(SPConstants.TIPS, 0.00);

            double totalAmount = amount - margin;

            btc =  totalAmount / askAmount.doubleValue();

            cryptoPriceTv.setText(currency.getCurrencySymbol() + " " + mMoneyUtils.format(askAmount));
            amountTv.setText(currency.getCurrencySymbol() + " " + mMoneyUtils.format(amount));
            marginTv.setText(currency.getCurrencySymbol() + " " + mMoneyUtils.format(margin));
            totalTv.setText(currency.getCurrencySymbol() + " " + mMoneyUtils.format(totalAmount));

            if (cryptoType == MerchantModule.MENU_ID_CRYPTO_ATM_BITCOIN) {
                cryptoValueTv.setText(String.format("%.8f", btc) + " BTC");
            } else if (cryptoType == MerchantModule.MENU_ID_CRYPTO_ATM_LITECOIN) {
                cryptoValueTv.setText(String.format("%.8f", btc) + " LTC");
            }

            continueBtn.setEnabled(true);

            continueBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(CryptoInfoActivity.this, AliPayQRScanActivity.class);
                    bundle.putDouble(SPConstants.CRYPTO_ATM_CRYPTO_VALUE, btc);
                    intent.putExtra(SPConstants.BUNDLE, bundle);
                    startActivity(intent);
                }
            });
        }
    }

    private void showBTCFailed() {
        UIHelper.showErrorDialog(this,
                getString(R.string.app_name),
                R.string.crypto_fail);
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
