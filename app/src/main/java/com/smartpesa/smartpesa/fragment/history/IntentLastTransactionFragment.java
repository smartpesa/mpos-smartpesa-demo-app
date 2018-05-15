package com.smartpesa.smartpesa.fragment.history;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.helpers.UIHelper;

import java.util.HashMap;

import smartpesa.sdk.core.error.SpException;
import smartpesa.sdk.models.transaction.SendNotificationCallback;

public class IntentLastTransactionFragment extends LastTransactionFragment {

    @Override
    public void sendReceipt() {
        HashMap<String, Object> config = new HashMap<>();
        onBuildReceiptConfig(config);

        String phoneNum = smsET.getText().toString();
        String email = emailET.getText().toString();

        if (validateSending(phoneNum, email)) {

            mProgressDialog.setMessage(mContext.getResources().getString(R.string.sending_receipt));
            mProgressDialog.show();

            serviceManager.get().sendNotification(transactionID, finalEmail, finalPhone, config, new SendNotificationCallback() {
                @Override
                public void onSuccess(Boolean aBoolean) {
                    if (null == getActivity()) return;

                    mProgressDialog.dismiss();

                    UIHelper.showToast(getActivity(), mContext.getResources().getString(R.string.sending_success));

                    getActivity().finish();
                }

                @Override
                public void onError(SpException exception) {
                    if (null == getActivity()) return;
                    mProgressDialog.dismiss();
                    UIHelper.showToast(getActivity(), exception.getMessage());
                    getActivity().finish();
                }
            });
        }
    }
}
