package com.smartpesa.smartpesa.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.activity.base.BaseActivity;
import com.smartpesa.smartpesa.helpers.PreferenceHelper;
import com.smartpesa.smartpesa.helpers.UIHelper;
import com.smartpesa.smartpesa.util.DateUtils;
import com.smartpesa.smartpesa.util.constants.PreferenceConstants;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.Lazy;
import smartpesa.sdk.ServiceManager;
import smartpesa.sdk.core.error.SpException;
import smartpesa.sdk.error.SpSessionException;
import smartpesa.sdk.models.merchant.VerifiedMerchantInfo;
import smartpesa.sdk.models.merchant.VerifyMerchantCallback;
import timber.log.Timber;

public class LoginActivity extends BaseActivity implements OnClickListener {

    @BindView(R.id.login_btn) Button loginBtn;
    @BindView(R.id.forget_id_btn) Button forgotBtn;
    @BindView(R.id.signUpBtn) TextView signUpBtn;
    @BindView(R.id.merchant_id_tv) EditText merchantIdEt;
    @BindView(R.id.operator_code_tv) EditText operatorCodeEt;
    @BindView(R.id.login_pin_tv) EditText loginPinEt;
    @BindView(R.id.welcomeLl) LinearLayout welcomeLl;
    @BindView(R.id.editTextll) LinearLayout editTextLl;
    @BindView(R.id.merchantNameTV) TextView merchantNameTv;
    @BindView(R.id.operatorNameTv) TextView operatorNameTv;
    @BindView(R.id.aboutIv) ImageView aboutIv;

    Context mContext;
    public Lazy<ServiceManager> serviceManager;
    SharedPreferences mPrefs;
    PreferenceHelper mPreferenceHelper;
    ProgressDialog progressDialog;
    boolean isWelcomeShown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mPrefs = getPreferences(MODE_PRIVATE);

        //get the service manager instance from the SplashActivity screen
        serviceManager = SmartPesaApplication.component(this).serviceManager();

        //initialize the UI components
        initializeComponents();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getString(R.string.app_name));
        progressDialog.setMessage(getString(R.string.loggin_in));
        progressDialog.setCancelable(false);

        //hide the sign up button
        signUpBtn.setVisibility(View.GONE);
    }

    //initialize all the UI components
    public void initializeComponents() {
        ButterKnife.bind(this);

        mContext = LoginActivity.this;
        mPreferenceHelper = SmartPesaApplication.component(this).preferenceHelper();

        String operatorName = mPreferenceHelper.getString(PreferenceConstants.KEY_OPERATOR_NAME);
        String merchantName = mPreferenceHelper.getString(PreferenceConstants.KEY_MERCHANT_NAME);

        if (!TextUtils.isEmpty(merchantName)) {
            showWelcome();
            operatorNameTv.setText(operatorName);
            merchantNameTv.setText(merchantName);
        } else {
            showEditText();
        }

        merchantIdEt.setText(mPreferenceHelper.getString(PreferenceConstants.KEY_MERCHANT_CODE));
        operatorCodeEt.setText(mPreferenceHelper.getString(PreferenceConstants.KEY_OPERATOR_CODE));

        loginBtn.setOnClickListener(this);
        forgotBtn.setOnClickListener(this);
        signUpBtn.setOnClickListener(this);
        aboutIv.setOnClickListener(this);

        //signUpBtn.setVisibility(View.VISIBLE);

        loginPinEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    verifyMerchant();
                }
                return false;
            }
        });
    }

    private void showWelcome() {
        welcomeLl.setVisibility(View.VISIBLE);
        editTextLl.setVisibility(View.GONE);
        forgotBtn.setText("Change user");
        isWelcomeShown = true;
    }

    //onClick listeners
    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            //call the verifyMerchant()
            case R.id.login_btn:
                verifyMerchant();
                break;

            //start the ForgetID activity
            case R.id.forget_id_btn:
                if (isWelcomeShown) {
                    showEditText();
                } else {
                    Intent forgotIntent = new Intent(getApplicationContext(), ForgotIdActivity.class);
                    startActivity(forgotIntent);
                    Timber.i("User goes to ForgotID");
                }
                break;

            case R.id.signUpBtn:
                startLeadActivity();
                break;

            case R.id.aboutIv:
                startActivity(new Intent(LoginActivity.this, AboutUsActivity.class));
                break;
            default:
                break;
        }
    }

    private void startLeadActivity() {
        startActivity(new Intent(LoginActivity.this, LeadActivity.class));
    }

    private void showEditText() {
        welcomeLl.setVisibility(View.GONE);
        editTextLl.setVisibility(View.VISIBLE);
        forgotBtn.setText(R.string.forgot_id);
        isWelcomeShown = false;
    }

    //verify merchant call to the SDK
    private void verifyMerchant() {

        final String merchantCode;
        final String operatorCode;
        String operatorPin;

        if (isWelcomeShown) {
            merchantCode = mPreferenceHelper.getString(PreferenceConstants.KEY_MERCHANT_CODE);
            operatorCode = mPreferenceHelper.getString(PreferenceConstants.KEY_OPERATOR_CODE);
            operatorPin = loginPinEt.getText().toString().trim();
        } else {
            //get the trimmed string from the EditText's
            merchantCode = merchantIdEt.getText().toString().trim();
            operatorCode = operatorCodeEt.getText().toString().trim();
            operatorPin = loginPinEt.getText().toString().trim();
        }

        //check if the fields are filled and validate the length of each field
        if (isValidateFields(merchantCode, operatorCode, operatorPin)) {

            disableButtons();

            //makes the verifyMerchant call in the SDK
            serviceManager.get().verifyMerchant(merchantCode, operatorCode, operatorPin, new VerifyMerchantCallback() {

                @Override
                public void onSuccess(VerifiedMerchantInfo verifiedMerchantInfo) {
                    if (isActivityDestroyed()) return;

                    enableButtons();

                    mPreferenceHelper.putString(PreferenceConstants.KEY_MERCHANT_CODE, merchantCode);
                    mPreferenceHelper.putString(PreferenceConstants.KEY_OPERATOR_CODE, operatorCode);
                    mPreferenceHelper.putString(PreferenceConstants.KEY_OPERATOR_NAME, verifiedMerchantInfo.getOperatorName());
                    mPreferenceHelper.putString(PreferenceConstants.KEY_MERCHANT_NAME, verifiedMerchantInfo.getMerchantName());

                    //save the time logged in to Shared preference for UI version check
                    Date d = new Date();
                    String tempDate = DateUtils.format(d);
                    mPreferenceHelper.putString(PreferenceConstants.KEY_LAST_LOGIN_TIME, tempDate);

                    // log the user details for critterism
                    initCrittercismMetadata(verifiedMerchantInfo.getOperatorName(),
                            verifiedMerchantInfo.getEmailAddress(),
                            operatorCode,
                            merchantCode);

                    // init merchant module after successful login
                    ((SmartPesaApplication) getApplication()).createMerchantComponent();

                    //start the MainActivity
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));

                    //finish this activity
                    finish();
                }

                @Override
                public void onError(SpException exception) {
                    if (exception instanceof SpSessionException) {
                        if (isActivityDestroyed()) return;

                        enableButtons();

                        //show the user that session expired
                        UIHelper.showToast(mContext, getString(R.string.session_expired));

                        //finish this activity and start the splash screen
                        finish();
                        startActivity(new Intent(mContext, SplashActivity.class));
                    } else {
                        if (isActivityDestroyed()) return;

                        enableButtons();

                        //show the custom error message
                        UIHelper.showErrorDialog(mContext, getResources().getString(R.string.app_name), exception.getMessage());
                    }
                }
            });

        }
    }

    private void initCrittercismMetadata(String operatorName, String emailId, String operatorId, String merchantId) {

//        //logging for crittercism
//        Crittercism.setUsername(operatorName);
//        // instantiate metadata json object
//        JSONObject metadata = new JSONObject();
//
//        // add arbitrary metadata
//        try {
//            metadata.put(CrittercismConstants.KEY_METADATA_OPERATOR_ID, operatorId);
//            metadata.put(CrittercismConstants.KEY_METADATA_MERCHANT_ID, merchantId);
//            metadata.put(CrittercismConstants.KEY_METADATA_EMAIL_ID, emailId);
//        } catch (JSONException e) {
//            e.printStackTrace();
//            Timber.e(e, "Unable to put metadata for Crittercism operatorId: %s, merchantId: %s, emailId: %s", operatorId, merchantId, emailId);
//        }
//
//        // send metadata to crittercism (asynchronously)
//        Crittercism.setMetadata(metadata);
//
//        Timber.i("User login success");
    }

    //disable buttons when login taking place
    private void disableButtons() {
        progressDialog.show();
    }

    //enable buttons when login taking place
    private void enableButtons() {
        progressDialog.dismiss();
    }

    //check whether all the fields are entered and validate their lengths
    private boolean isValidateFields(String merchantCode, String operatorCode, String operatorPin) {

        if (merchantCode.length() <= 0) {
            merchantIdEt.setError(getResources().getString(R.string.enter_merchant_id));
            merchantIdEt.requestFocus();
            return false;
        }
        if (operatorCode.length() <= 0) {
            operatorCodeEt.setError(getResources().getString(R.string.enter_operator_code));
            operatorCodeEt.requestFocus();
            return false;
        }
        if (operatorPin.length() <= 0) {
            loginPinEt.setError(getResources().getString(R.string.enter_login_pin));
            loginPinEt.requestFocus();
            return false;
        }
        return true;
    }
}
