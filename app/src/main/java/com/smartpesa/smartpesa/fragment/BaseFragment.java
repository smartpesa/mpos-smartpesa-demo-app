package com.smartpesa.smartpesa.fragment;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.activity.SplashActivity;
import com.smartpesa.smartpesa.activity.base.BaseActivity;
import com.smartpesa.smartpesa.helpers.UIHelper;
import com.smartpesa.smartpesa.persistence.MerchantComponent;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;

import timber.log.Timber;

public class BaseFragment extends Fragment {

    public MerchantComponent getMerchantComponent(Activity activity) {
        if (activity instanceof BaseActivity) {
            return ((BaseActivity) activity).getMerchantComponent();
        } else {
            Timber.e(new RuntimeException("Unknown Activity"), activity.getClass().getName());
            return null;
        }
    }

    public void logoutUser() {
        // Push to SplashActivity
        UIHelper.showToast(getActivity(), getString(R.string.session_expired));
        //finish the current activity
        getActivity().finish();
        //start the splash screen
        Intent intent = new Intent(getActivity(), SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

}
