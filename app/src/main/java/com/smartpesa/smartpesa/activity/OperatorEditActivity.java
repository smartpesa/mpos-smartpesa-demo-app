package com.smartpesa.smartpesa.activity;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.activity.base.BaseActivity;
import com.smartpesa.smartpesa.fragment.OperatorsFragment;
import com.smartpesa.smartpesa.helpers.UIHelper;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import dagger.Lazy;
import smartpesa.sdk.ServiceManager;
import smartpesa.sdk.core.error.SpException;
import smartpesa.sdk.error.SpSessionException;
import smartpesa.sdk.models.operator.Operator;
import smartpesa.sdk.models.operator.ProfilePermissions;
import smartpesa.sdk.models.operator.UpdateOperatorCallback;
import timber.log.Timber;

public class OperatorEditActivity extends BaseActivity implements View.OnClickListener {

    Operator operatorInfo;
    Lazy<ServiceManager> serviceManager;
    ProgressDialog mProgressDialog;
    Context mContext;

    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.operatorSaveBTN) Button saveBTN;
    @Bind(R.id.operatorInititalTV) TextView operatorInitialTV;
    @Bind(R.id.operatorMailET) TextView emailET;
    @Bind(R.id.operatorPhoneET) TextView phoneET;
    @Bind(R.id.operatorNameET) TextView nameET;
    @Bind(R.id.operatorCodeET) TextView codeET;
    @Bind(R.id.operatorManageCB) CheckBox manageCB;
    @Bind(R.id.operatorHistoryCB) CheckBox historyCB;
    @Bind(R.id.operatorActiveCB) CheckBox operatorActiveCB;
    @Bind(R.id.operatorPrintCB) CheckBox printCB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_edit);
        mContext = OperatorEditActivity.this;
        serviceManager = SmartPesaApplication.component(mContext).serviceManager();
        initializeComponents();

        TextWatcher inputWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String name = s.toString();
                if (!TextUtils.isEmpty(name)) {
                    String initial = name.substring(0, 1);
                    operatorInitialTV.setText(initial.toUpperCase());
                }
            }
        };

            operatorInfo = OperatorsFragment.operatorInfo;
            setupOperator();
            saveBTN.setText(getResources().getString(R.string.update));
            setTitle(R.string.title_edit_operator);
    }

    private void setupOperator() {
        String name = operatorInfo.getOperatorName();
        String email = operatorInfo.getNotificationEmail();
        String phone = operatorInfo.getNotificationPhone();
        String code = operatorInfo.getOperatorCode();
        Boolean manage = operatorInfo.getOperatorProfilePermission().canManageOperators();
        Boolean history = operatorInfo.getOperatorProfilePermission().canViewAllHistory();
        Boolean print = operatorInfo.getOperatorProfilePermission().canPrintReceipt();
        Boolean isActive = operatorInfo.isActive();

        String initial = name.substring(0, 1);
        operatorInitialTV.setText(initial);

        nameET.setText(name);
        emailET.setText(email);
        phoneET.setText(phone);
        codeET.setText(code);

        if (isActive) {
            operatorActiveCB.setChecked(true);
        }

        if (manage) {
            manageCB.setChecked(true);
        }

        if (history) {
            historyCB.setChecked(true);
        }

        if (print) {
            printCB.setChecked(true);
        }
    }

//    private void addOperator() {
//
//        mProgressDialog.setTitle(getResources().getString(R.string.adding_operator));
//        mProgressDialog.setMessage(getResources().getString(R.string.please_wait));
//        mProgressDialog.show();
//
//        String name = nameET.getText().toString();
//        String email = emailET.getText().toString();
//        String phone = phoneET.getText().toString();
//        String code = codeET.getText().toString();
//        final Boolean manage, print, history, active;
//
//        operatorInfo = new Operator();
//        operatorInfo.setOperatorName(name);
//        operatorInfo.setNotificationEmail(email);
//        operatorInfo.setNotificationPhone(phone);
//        operatorInfo.setOperatorCode(code);
//
//        ProfilePermissions operatorProfilePermission = new ProfilePermissions();
//
//        if (manageCB.isChecked()) {
//            manage = true;
//        } else {
//            manage = false;
//        }
//
//        if (operatorActiveCB.isChecked()) {
//            active = true;
//        } else {
//            active = false;
//        }
//
//        if (historyCB.isChecked()) {
//            history = true;
//        } else {
//            history = false;
//        }
//
//        if (printCB.isChecked()) {
//            print = true;
//        } else {
//            print = false;
//        }
//
//        operatorProfilePermission.setCanManageOperators(manage);
//        operatorProfilePermission.setCanPrintReceipt(print);
//        operatorProfilePermission.setCanViewAllHistory(history);
//        operatorInfo.setOperatorProfilePermission(operatorProfilePermission);
//
//        operatorInfo.setActive(active);
//    }

    //to update the operator
    private void updateOperator() {

        mProgressDialog.setTitle(getResources().getString(R.string.updating_operator));
        mProgressDialog.setMessage(getResources().getString(R.string.please_wait));
        mProgressDialog.show();

        String name = nameET.getText().toString();
        String email = emailET.getText().toString();
        String phone = phoneET.getText().toString();
        String code = codeET.getText().toString();
        final Boolean manage, print, history, active;

        operatorInfo.setOperatorName(name);
        operatorInfo.setNotificationEmail(email);
        operatorInfo.setNotificationPhone(phone);
        operatorInfo.setOperatorCode(code);

        ProfilePermissions operatorProfilePermission = new ProfilePermissions();

        if (manageCB.isChecked()) {
            manage = true;
        } else {
            manage = false;
        }

        if (operatorActiveCB.isChecked()) {
            active = true;
        } else {
            active = false;
        }

        if (historyCB.isChecked()) {
            history = true;
        } else {
            history = false;
        }

        if (printCB.isChecked()) {
            print = true;
        } else {
            print = false;
        }

        operatorProfilePermission.setCanManageOperators(manage);
        operatorProfilePermission.setCanPrintReceipt(print);
        operatorProfilePermission.setCanViewAllHistory(history);
        operatorInfo.setOperatorProfilePermission(operatorProfilePermission);

        operatorInfo.setIsActive(active);

        serviceManager.get().updateOperators(operatorInfo, new UpdateOperatorCallback() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                if (null == mContext) return;

                mProgressDialog.dismiss();
                UIHelper.showToast(mContext, getResources().getString(R.string.operator_updated));
                finish();
            }

            @Override
            public void onError(SpException exception) {
                if (exception instanceof SpSessionException) {
                    if (null == mContext) return;
                    logoutUser();
                } else {
                    mProgressDialog.dismiss();

                    //show the custom error message
                    UIHelper.showErrorDialog(mContext, getResources().getString(R.string.app_name), exception.getMessage());
                }
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.operatorSaveBTN:
                    updateOperator();
                    Timber.i("User triggering update operator");
                break;

            default:
                break;

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initializeComponents() {
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setCancelable(false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        saveBTN.setOnClickListener(this);
    }

}
