package com.smartpesa.smartpesa.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.Result;
import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.activity.base.BaseActivity;
import com.smartpesa.smartpesa.activity.crypto.CryptoConfirmActivity;
import com.smartpesa.smartpesa.activity.payment.AliPayPaymentProgressActivity;
import com.smartpesa.smartpesa.models.SmartPesaTransactionType;
import com.smartpesa.smartpesa.util.MoneyUtils;
import com.smartpesa.smartpesa.util.constants.SPConstants;

import java.math.BigDecimal;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.Lazy;
import me.dm7.barcodescanner.core.IViewFinder;
import me.dm7.barcodescanner.core.ViewFinderView;
import me.dm7.barcodescanner.zxing.ZXingScannerView;
import smartpesa.sdk.ServiceManager;
import smartpesa.sdk.SmartPesa;

public class AliPayQRScanActivity extends BaseActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView mScannerView;
    BigDecimal amount;
    Lazy<ServiceManager> serviceManager;
    public MoneyUtils mMoneyUtils;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.imageView)
    ImageView aliPayLogoIv;
    @BindView(R.id.textDisplay)
    TextView instructionText;
    boolean crypto;
    Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ali_pay_qr);
        ButterKnife.bind(this);
        //setup toolbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ViewGroup contentFrame = (ViewGroup) findViewById(R.id.content_frame);
        mScannerView = new ZXingScannerView(this) {
            @Override
            protected IViewFinder createViewFinderView(Context context) {
                return new CustomViewFinderView(context);
            }
        };

        contentFrame.addView(mScannerView);

        bundle = getIntent().getBundleExtra(SPConstants.BUNDLE);
        amount = new BigDecimal(bundle.getDouble(SPConstants.AMOUNT, 0.00));
        crypto = bundle.getBoolean(SPConstants.IS_CRYPTO);
        serviceManager = SmartPesaApplication.component(AliPayQRScanActivity.this).serviceManager();
        mMoneyUtils = getMerchantComponent().provideMoneyUtils();

        if (crypto) {
            aliPayLogoIv.setImageResource(R.drawable.crypto_atm_logo);
            instructionText.setText(R.string.crypto_scan_instruction);
            setTitle(R.string.title_crypto_atm);
        } else {
            setTitle(R.string.title_alipay);
        }
    }

    @Override
    public void handleResult(Result rawResult) {

        if (!crypto) {
            Bundle paymentBundle = new Bundle();
            paymentBundle.putDouble(SPConstants.AMOUNT, amount.doubleValue());
            paymentBundle.putInt(SPConstants.TRANSACTION_TYPE, SmartPesaTransactionType.SALE.getEnumId());
            paymentBundle.putInt(SPConstants.FROM_ACCOUNT, SmartPesa.AccountType.DEFAULT.getEnumId());
            paymentBundle.putInt(SPConstants.TO_ACCOUNT, SmartPesa.AccountType.DEFAULT.getEnumId());
            paymentBundle.putString(SPConstants.QR_ALI_PAY_SCAN_TEXT, rawResult.getText());
            paymentBundle.putBoolean(SPConstants.IS_ALI_PAY_SCAN, true);

            Intent paymentIntent = new Intent(this, AliPayPaymentProgressActivity.class);
            paymentIntent.putExtras(paymentBundle);
            startActivity(paymentIntent);

            finish();
        } else {
            Intent confirmIntent = new Intent(this, CryptoConfirmActivity.class);
            bundle.putString(SPConstants.CRYPTO_ATM_QR_TEXT, rawResult.getText());
            confirmIntent.putExtra(SPConstants.BUNDLE, bundle);
            startActivity(confirmIntent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    private static class CustomViewFinderView extends ViewFinderView {
        public static final String TRADE_MARK_TEXT = "SmartPesa";
        public static final int TRADE_MARK_TEXT_SIZE_SP = 20;
        public final Paint PAINT = new Paint();

        public CustomViewFinderView(Context context) {
            super(context);
            init();
        }

        public CustomViewFinderView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        private void init() {
            PAINT.setColor(Color.WHITE);
            PAINT.setAntiAlias(true);
            float textPixelSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                    TRADE_MARK_TEXT_SIZE_SP, getResources().getDisplayMetrics());
            PAINT.setTextSize(textPixelSize);
            setSquareViewFinder(true);
        }

        @Override
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            drawTradeMark(canvas);
        }

        private void drawTradeMark(Canvas canvas) {
            Rect framingRect = getFramingRect();
            float tradeMarkTop;
            float tradeMarkLeft;
            if (framingRect != null) {
                tradeMarkTop = framingRect.bottom + PAINT.getTextSize() + 10;
                tradeMarkLeft = framingRect.left;
            } else {
                tradeMarkTop = 10;
                tradeMarkLeft = canvas.getHeight() - PAINT.getTextSize() - 10;
            }
            canvas.drawText(TRADE_MARK_TEXT, tradeMarkLeft, tradeMarkTop, PAINT);
        }
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
