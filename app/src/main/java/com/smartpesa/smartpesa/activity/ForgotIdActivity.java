package com.smartpesa.smartpesa.activity;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.activity.base.BaseActivity;
import com.smartpesa.smartpesa.helpers.PreferenceHelper;
import com.smartpesa.smartpesa.helpers.UIHelper;
import com.smartpesa.smartpesa.util.constants.PreferenceConstants;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import dagger.Lazy;
import smartpesa.sdk.ServiceManager;
import smartpesa.sdk.core.error.SpException;
import smartpesa.sdk.models.operator.ForgotPinCallback;
import timber.log.Timber;

public class ForgotIdActivity extends BaseActivity implements OnClickListener {

    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.forgot_merchant_id_btn) Button forgotMerchantBtn;
    @Bind(R.id.forgot_operator_btn) Button forgotOperatorBtn;
    @Bind(R.id.forgot_pin_btn) Button forgotPinBtn;
    @Bind(R.id.merchant_id_tv) EditText merchantIdEt;
    @Bind(R.id.operator_code_tv) EditText operatorCodeEt;
    @Bind(R.id.request_new_pin_btn) Button requestPinBtn;
    @Bind(R.id.forgot_merchant_id_tv) TextView forgotMerchantTv;
    @Bind(R.id.forgot_operator_tv) TextView forgotOperatorTv;
    @Bind(R.id.forgotProgressBar) ProgressBar mProgressBar;
    @Bind(R.id.forgot_buttons_container) ViewGroup buttonsContainer;
    @Bind(R.id.forgot_pin_container) ViewGroup forgotPinContainer;
    Context mContext;
    Lazy<ServiceManager> serviceManager;
    boolean lockUI = false;
    PreferenceHelper preferenceHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_id);

        //initialize UI components
        initializeComponents();

        serviceManager = SmartPesaApplication.component(this).serviceManager();
    }

    private void requestPIN() {

        String merchantCode = merchantIdEt.getText().toString();
        String operatorCode = operatorCodeEt.getText().toString();

        if (merchantCode.isEmpty() || operatorCode.isEmpty()) {
            UIHelper.showToast(mContext, getResources().getString(R.string.enter_valid_id));
        } else {

            lockUI = true;
            mProgressBar.setVisibility(View.VISIBLE);

            serviceManager.get().forgotPin(merchantCode, operatorCode, new ForgotPinCallback() {
                @Override
                public void onSuccess(Boolean aBoolean) {
                    if (null == mContext) return;

                    lockUI = false;
                    mProgressBar.setVisibility(View.INVISIBLE);

                    UIHelper.showToast(mContext, getResources().getString(R.string.pin_success));
                    Timber.i("User PIN request success");
                    finish();

                }

                @Override
                public void onError(SpException exception) {
                    if (null == mContext) return;
                    lockUI = false;
                    mProgressBar.setVisibility(View.INVISIBLE);

                    //show the custom error message
                    UIHelper.showErrorDialog(mContext, getResources().getString(R.string.app_name), exception.getMessage());
                }
            });
        }
    }

    //initialize the UI
    private void initializeComponents() {
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(R.string.title_activity_forgot);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        forgotMerchantBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showOrHideTextView(forgotMerchantTv);
            }
        });

        forgotOperatorBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showOrHideTextView(forgotOperatorTv);
            }
        });

        forgotPinBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showOrHideTextView(forgotPinContainer);
                merchantIdEt.requestFocus();
            }
        });

        requestPinBtn.setOnClickListener(this);

        mContext = ForgotIdActivity.this;

        preferenceHelper = new PreferenceHelper(mContext);
        merchantIdEt.setText(preferenceHelper.getString(PreferenceConstants.KEY_MERCHANT_CODE));
        operatorCodeEt.setText(preferenceHelper.getString(PreferenceConstants.KEY_OPERATOR_CODE));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showOrHideTextView(View view) {
        view.setVisibility(view.getVisibility() != View.VISIBLE ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClick(View v) {

        if (!lockUI) {

            switch (v.getId()) {
                case R.id.request_new_pin_btn:
                    requestPIN();
                    Timber.i("User request for PIN");
                    break;
                default:
                    break;
            }

        } else {
            UIHelper.showToast(mContext, getResources().getString(R.string.pin_request_inprogress));
        }
    }

    @Override
    public void onBackPressed() {
        if (!lockUI) {
            super.onBackPressed();
        } else {
            UIHelper.showToast(mContext, getResources().getString(R.string.pin_request_inprogress));
        }
    }
}

