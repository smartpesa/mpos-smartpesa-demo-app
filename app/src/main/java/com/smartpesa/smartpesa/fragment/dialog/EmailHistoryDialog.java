package com.smartpesa.smartpesa.fragment.dialog;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.activity.SplashActivity;
import com.smartpesa.smartpesa.helpers.UIHelper;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.Date;

import butterknife.Bind;
import smartpesa.sdk.core.error.SpException;
import smartpesa.sdk.error.SpSessionException;
import smartpesa.sdk.models.transaction.SendNotificationCallback;

public class EmailHistoryDialog extends AbstractHistoryDialog {

    @Bind(R.id.sendEmail_ET) EditText emailET;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_send_history, container);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //set the merchant email
        emailET.setText(serviceManager.get().getCachedMerchant().getEmailAddress());
    }

    //send email
    private void emailHistory(Date startDate, Date endDate) {
        //hide the keyboard
        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getDialog().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        //send the history
        String finalEmail = emailET.getText().toString();

        if (finalEmail.isEmpty()) {
            UIHelper.showToast(getActivity(), getActivity().getResources().getString(R.string.enter_email));
        } else {

            //lock the buttons
            disableButtons();

            serviceManager.get().sendHistory(finalEmail, startDate, endDate, new SendNotificationCallback() {
                @Override
                public void onSuccess(Boolean aBoolean) {
                    if (null == getActivity()) return;
                    getDialog().dismiss();
                    if (aBoolean) {
                        UIHelper.showToast(getActivity(), getActivity().getResources().getString(R.string.history_sent));
                    } else {
                        UIHelper.showToast(getActivity(), getString(R.string.notification_failed));
                    }
                }

                @Override
                public void onError(SpException exception) {
                    if (null == getActivity()) return;
                    enableButtons();
                    if (exception instanceof SpSessionException) {
                        //show the expired message
                        UIHelper.showToast(getActivity(), getActivity().getResources().getString(R.string.session_expired));
                        //finish the current activity
                        getActivity().finish();

                        //start the splash screen
                        startActivity(new Intent(getActivity(), SplashActivity.class));
                    } else {
                        UIHelper.showErrorDialog(getActivity(), getResources().getString(R.string.app_name), exception.getMessage());
                    }

                }
            });
        }
    }

    @Override
    protected void handleClickPositive(Date startDate, Date endDate) {
        emailHistory(startDate, endDate);
    }

    @Override
    protected void enableButtons() {
        super.enableButtons();
        emailET.setEnabled(true);
    }

    @Override
    protected void disableButtons() {
        super.disableButtons();
        emailET.setEnabled(false);
    }
}