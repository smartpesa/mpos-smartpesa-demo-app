package com.smartpesa.smartpesa.fragment.dialog;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.activity.SplashActivity;
import com.smartpesa.smartpesa.helpers.UIHelper;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.Lazy;
import smartpesa.sdk.ServiceManager;
import smartpesa.sdk.core.error.SpException;
import smartpesa.sdk.error.SpSessionException;
import smartpesa.sdk.models.operator.ChangePinCallback;

public class ChangePinDialog extends BaseDialogFragment {

    @BindView(R.id.oldPinEt) EditText oldPinEt;
    @BindView(R.id.newPinEt) EditText newPinEt;
    @BindView(R.id.confirmNewPinEt) EditText confirmPinEt;
    @BindView(R.id.changePinBtn) Button changePinBtn;
    @BindView(R.id.cancelBtn) Button cancelBtn;

    protected Lazy<ServiceManager> serviceManager;
    private boolean isPINChanging;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(getString(R.string.change_pin));
        View view = inflater.inflate(R.layout.dialog_change_pin, container);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        serviceManager = SmartPesaApplication.component(getActivity()).serviceManager();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        changePinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()) {
                    changePIN();
                }
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPINChanging) {
                    UIHelper.showToast(getActivity(), "Please wait PIN change in progress");
                } else {
                    getDialog().dismiss();
                }
            }
        });
    }

    private void changePIN() {
        isPINChanging = true;
        String oldPin = oldPinEt.getText().toString();
        String newPin = newPinEt.getText().toString();
        serviceManager.get().changeOperatorPin(oldPin, newPin, new ChangePinCallback() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                if (null == getActivity()) return;
                isPINChanging = false;
                if (aBoolean) {
                    UIHelper.showToast(getActivity(), "PIN changed successfully");
                    getDialog().dismiss();
                } else {
                    UIHelper.showToast(getActivity(), "Error changing PIN, try again later");
                }
            }

            @Override
            public void onError(SpException exception) {
                if (exception instanceof SpSessionException) {
                    if (null == getActivity()) return;
                    isPINChanging = false;
                    //show the expired message
                    UIHelper.showToast(getActivity(), getActivity().getResources().getString(R.string.session_expired));
                    //finish the current activity
                    getActivity().finish();
                    //start the splash screen
                    startActivity(new Intent(getActivity(), SplashActivity.class));
                } else {
                    if (null == getActivity()) return;
                    UIHelper.showToast(getActivity(), exception.getMessage());
                    isPINChanging = false;
                }
            }
        });
    }

    private boolean validate() {
        String oldPin = oldPinEt.getText().toString();
        String newPin = newPinEt.getText().toString();
        String confirmPin = confirmPinEt.getText().toString();

        if (TextUtils.isEmpty(oldPin)) {
            UIHelper.showToast(getActivity(), "Please enter your old PIN");
            return false;
        } else if (TextUtils.isEmpty(newPin)) {
            UIHelper.showToast(getActivity(), "Please enter a new PIN");
            return false;
        } else if (TextUtils.isEmpty(confirmPin)) {
            UIHelper.showToast(getActivity(), "Please re-enter the new PIN");
            return false;
        } else if (newPin.length() < 4) {
            UIHelper.showToast(getActivity(), "New PIN should be above 4 characters and below 6 characters");
            return false;
        } else if (newPin.length() > 6) {
            UIHelper.showToast(getActivity(), "New PIN should be above 4 characters and below 6 characters");
            return false;
        } else if (!newPin.equals(confirmPin)) {
            UIHelper.showToast(getActivity(), "The new PIN's do not match");
            return false;
        }

        return true;
    }

}