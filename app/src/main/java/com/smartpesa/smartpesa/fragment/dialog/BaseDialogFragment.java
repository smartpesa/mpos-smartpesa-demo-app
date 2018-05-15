package com.smartpesa.smartpesa.fragment.dialog;

import com.smartpesa.smartpesa.activity.base.BaseActivity;
import com.smartpesa.smartpesa.persistence.MerchantComponent;

import android.app.Activity;
import android.support.v4.app.DialogFragment;

import timber.log.Timber;

public class BaseDialogFragment extends DialogFragment {

    public MerchantComponent getMerchantComponent(Activity activity) {
        if (activity instanceof BaseActivity) {
            return ((BaseActivity) activity).getMerchantComponent();
        } else {
            Timber.e(new RuntimeException("Unknown Activity"), activity.getClass().getName());
            return null;
        }
    }
}