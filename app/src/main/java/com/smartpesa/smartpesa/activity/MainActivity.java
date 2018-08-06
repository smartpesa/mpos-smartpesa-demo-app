package com.smartpesa.smartpesa.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.smartpesa.smartpesa.R;
import com.smartpesa.smartpesa.SmartPesaApplication;
import com.smartpesa.smartpesa.activity.base.BaseActivity;
import com.smartpesa.smartpesa.flavour.MenuHandler;
import com.smartpesa.smartpesa.fragment.MenuFragment;
import com.smartpesa.smartpesa.fragment.payment.AbstractPaymentFragment;
import com.smartpesa.smartpesa.helpers.UIHelper;
import com.smartpesa.smartpesa.persistence.MerchantComponent;
import com.smartpesa.smartpesa.persistence.MerchantModule;

import butterknife.Bind;
import butterknife.ButterKnife;
import dagger.Lazy;
import smartpesa.sdk.ServiceManager;
import smartpesa.sdk.core.error.SpException;
import smartpesa.sdk.models.merchant.VerifiedMerchantInfo;
import smartpesa.sdk.models.operator.LogoutCallback;
import timber.log.Timber;

public class MainActivity extends BaseActivity implements MenuFragment.PaymentTypeListener {

    public static final String GO_TO_HISTORY = MainActivity.class.getName() + ".goToHistory";

    private static MainActivityComponent sMainActivityComponent;

    public Lazy<ServiceManager> serviceManager;

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    Context mContext;
    Boolean doubleBackToExitPressedOnce = false;
    VerifiedMerchantInfo currentMerchant;
    MerchantComponent merchantComponent;

    @Nullable public MenuHandler mMenuHandler;

    @NonNull
    public static MainActivityComponent getComponent(MainActivity activity) {
        return sMainActivityComponent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = MainActivity.this;

        sMainActivityComponent = SmartPesaApplication.component(this).plus(new MainActivityModule(this));

        ButterKnife.bind(this);

        serviceManager = SmartPesaApplication.component(this).serviceManager();

        if (getMerchantComponent() != null) {
            currentMerchant = getMerchantComponent().provideMerchant();
        } else {
            logoutUser();
            return;
        }

        merchantComponent = getMerchantComponent();
        currentMerchant = getMerchantComponent().provideMerchant();

        mMenuHandler = SmartPesaApplication.flavourComponent(this).provideMenuHandler();

        //setup toolbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // display the first navigation drawer view on app launch
        if (savedInstanceState == null) {
            // on first time display view for first nav item
            displayView(MerchantModule.MENU_ID_HOME);
        }
    }

    //to display the fragments and change title of actionbar
    private void displayView(int identifier) {

        if (identifier != MerchantModule.MENU_ID_WECHAT) {
            if (identifier == MerchantModule.MENU_ID_LOGOUT) {
                showLogoutDialog();
            } else {
                if (mMenuHandler != null) {
                    Fragment f = mMenuHandler.fragmentForMenuWithId(identifier);
                    getComponent(this).provideNavigator().pushFragment(f);
                }
            }
        }

    }

    //show logout dialog
    private void showLogoutDialog() {
        if (null == MainActivity.this) return;

        final ProgressDialog mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Logging out, please wait..");
        UIHelper.showMessageDialogWithCallback(this, getResources().getString(R.string.logout_prompt), getResources().getString(R.string.logout_yes), getResources().getString(R.string.logout_no), new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                super.onPositive(dialog);
                if (null == MainActivity.this) return;

                Timber.i("User logs out of the app");
                mProgressDialog.show();
                //logout the merchant and show login screen
                serviceManager.get().logout(new LogoutCallback() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        if (null == MainActivity.this) return;

                        mProgressDialog.dismiss();
                        ((SmartPesaApplication) getApplication()).releaseMerchant();
                        startActivity(new Intent(mContext, SplashActivity.class));
                        finish();
                    }

                    @Override
                    public void onError(SpException exception) {
                        if (null == MainActivity.this) return;

                        mProgressDialog.dismiss();
                        ((SmartPesaApplication) getApplication()).releaseMerchant();
                        startActivity(new Intent(mContext, SplashActivity.class));
                        finish();
                    }
                });

            }

            @Override
            public void onNegative(MaterialDialog dialog) {
                super.onNegative(dialog);
            }
        });
    }

    //check if it is the PaymentFragment, if so show the double BACK to exit toast
    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            if (doubleBackToExitPressedOnce) {
                finish();
                return;
            }
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, getResources().getString(R.string.double_back), Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        } else {
            getSupportFragmentManager().popBackStack();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AbstractPaymentFragment.REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data.getBooleanExtra(GO_TO_HISTORY, false)) {
                    displayView(MerchantModule.MENU_ID_HISTORY);
                }
            }
        }
    }

    @Override
    public void processMenu(int menuId) {
        displayView(menuId);
    }
}
