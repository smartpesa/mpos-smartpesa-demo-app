package com.smartpesa.smartpesa.activity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.activity.base.BaseActivity;
import com.smartpesa.smartpesa.helpers.UIHelper;
import com.smartpesa.smartpesa.util.MoneyUtils;

import net.glxn.qrgen.android.QRCode;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.widget.ImageView;

import java.math.BigDecimal;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import dagger.Lazy;
import smartpesa.sdk.ServiceManager;
import smartpesa.sdk.core.error.SpException;
import smartpesa.sdk.models.ExtraDataContainer;
import smartpesa.sdk.models.provider.GetProviderExtraDataCallback;

public class AliPayActivity extends BaseActivity {

    public static final String FUNCTION = "function";
    public static final String AMOUNT = "amount";
    public static final String DISPLAY = "display";
    public static final String ALI_PAY_BARCODE_PAYMENT = "AliPayBarcodePayment";

    BigDecimal amount;
    Lazy<ServiceManager> serviceManager;
    public MoneyUtils mMoneyUtils;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.qrImageView) ImageView qrImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ali_pay);

        ButterKnife.bind(this);
        //setup toolbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle bundle = getIntent().getBundleExtra("bundle");
        amount = new BigDecimal(bundle.getDouble("amount", 0.00));
        serviceManager = SmartPesaApplication.component(AliPayActivity.this).serviceManager();
        mMoneyUtils = getMerchantComponent().provideMoneyUtils();

        processPayment();
    }

    private void processPayment() {
        final ProgressDialog progressDialog = new ProgressDialog(AliPayActivity.this);
        progressDialog.setTitle(R.string.app_name);
        progressDialog.setMessage("Submitting payment request, please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put(AMOUNT, mMoneyUtils.format(amount));
        hashMap.put(FUNCTION, DISPLAY);
        serviceManager.get().getProviderExtraData(ALI_PAY_BARCODE_PAYMENT, hashMap, new GetProviderExtraDataCallback() {
            @Override
            public void onSuccess(ExtraDataContainer extraDataContainer) {
                String qrText = extraDataContainer.raw;
                if (!TextUtils.isEmpty(qrText)) {
                    progressDialog.dismiss();
                    qrText = qrText.replaceAll("^\"|\"$", "");
                    UIHelper.log(qrText);
                    Bitmap myBitmap = QRCode.from(qrText).bitmap();
                    if (myBitmap != null) {
                        qrImageView.setImageBitmap(myBitmap);
                    }
                }
            }

            @Override
            public void onError(SpException exception) {
                UIHelper.showMessageDialogWithCallback(AliPayActivity.this, exception.getMessage(), "Ok", new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        finish();
                    }
                });
            }
        });
    }
}
