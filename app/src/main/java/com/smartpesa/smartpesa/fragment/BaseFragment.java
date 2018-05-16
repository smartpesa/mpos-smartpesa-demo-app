package com.smartpesa.smartpesa.fragment;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.activity.SplashActivity;
import com.smartpesa.smartpesa.activity.base.BaseActivity;
import com.smartpesa.smartpesa.helpers.UIHelper;
import com.smartpesa.smartpesa.persistence.MerchantComponent;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

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
        UIHelper.showToast(getActivity(), getString(R.string.sp__session_expired));
        //finish the current activity
        getActivity().finish();
        //start the splash screen
        Intent intent = new Intent(getActivity(), SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    //set title of fragment
    @Override
    public void onResume() {
        super.onResume();
        android.support.v7.app.ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(isShowHomeAsUp());
            actionBar.setDisplayShowHomeEnabled(isShowHomeAsUp());
            actionBar.setHomeButtonEnabled(isShowHomeAsUp());
        }
    }

    protected boolean isShowHomeAsUp() {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}