package com.smartpesa.smartpesa.activity.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.activity.SplashActivity;
import com.smartpesa.smartpesa.helpers.UIHelper;
import com.smartpesa.smartpesa.persistence.MerchantComponent;

public class BaseActivity extends AppCompatActivity {

    private boolean mIsDestroyed;
    private boolean mIsPaused;

    @Nullable
    public MerchantComponent getMerchantComponent() {
        MerchantComponent merchantComponent = SmartPesaApplication.merchantComponent(this);
        if (merchantComponent == null || merchantComponent.provideMerchant() == null) {
            return null;
        }
        return merchantComponent;
    }

    public void logoutUser() {
        // Push to SplashActivity
        UIHelper.showToast(this, getString(R.string.session_expired));
        //finish the current activity
        finish();
        //start the splash screen
        Intent intent = new Intent(this, SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsDestroyed = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsPaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsPaused = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIsDestroyed = true;
    }

    protected boolean isActivityPaused() {
        return mIsPaused;
    }

    protected boolean isActivityDestroyed() {
        return mIsDestroyed;
    }
}